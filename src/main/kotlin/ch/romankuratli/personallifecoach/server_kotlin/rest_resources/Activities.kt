package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import ch.romankuratli.personallifecoach.server_kotlin.MongoDBConnector
import ch.romankuratli.personallifecoach.server_kotlin.utils.Utils
import com.mongodb.client.model.Filters
import org.bson.Document
import org.bson.types.ObjectId
import spark.Route
import java.util.*
import kotlin.collections.ArrayList

private val ACTIVITY_COLL = MongoDBConnector.getCollection("activities")

fun Document.toActivityJson(): String = """
{
    "_id": "${get("_id").toString()}",
    "activity": "${getString("activity")}",
    "mental_energy": "${getLong("mental_energy")}",
    "physical_energy": "${getLong("physical_energy")}",
    "time_required": "${getString("time_required")}",
    "weather_relevant": "${getBoolean("weather_relevant")}"
}
"""

class Activities : RESTResource {
    override val subPath: String get() = "/activities"
    override val subResources: Array<RESTResource> get() = arrayOf(ActivityById(), Recommended())
    override fun handleGet(): Route {
        return Route{_, _ ->
            ACTIVITY_COLL.find().toList().joinToString(prefix = "[", postfix = "]") {it.toActivityJson()}
        }
    }

    override fun handlePost(): Route {
        return Route{req, _ ->
            val doc = Utils.getBodyJsonDoc(req, arrayOf("activity", "mental_energy", "physical_energy", "time_required", "weather_relevant"))
            ACTIVITY_COLL.insertOne(doc)
            doc.toActivityJson()
        }
    }
}

class ActivityById : RESTResource {
    override val subPath: String get() = "/:id"
    override fun handleDelete(): Route {
        return Route{req, _ ->
            ACTIVITY_COLL.deleteOne(Filters.eq<ObjectId>("_id", ObjectId(req.params("id"))))
            """{"msg": "Activity succesfully deleted !"}"""
        }
    }
}

class Recommended : RESTResource {
    override val subPath: String get() = "/recommended"
    override fun handleGet(): Route {
        return Route{req, _ ->
            var result: MutableList<RecommendedActivity> = mutableListOf()
            val userMentalEnergy = Integer.parseInt(req.queryParams("mentalEnergy"))
            val userPhysicalEnergy = Integer.parseInt(req.queryParams("physicalEnergy"))
            val userAvailableMinutes = timeStringToMinutes(req.queryParams("availableTime"))
            val goodWeather = req.queryParams("goodWeather") == "true"
            for (activityDoc in ACTIVITY_COLL.find()) {
                val requiredMentalEnergy = activityDoc.getLong("mental_energy").toInt()
                val requiredPhysicalEnergy = activityDoc.getLong("physical_energy").toInt()
                val requiredMinutes = timeStringToMinutes(activityDoc.getString("time_required"))
                val weatherRelevant = activityDoc.getBoolean("weather_relevant")
                if (userMentalEnergy >= requiredMentalEnergy &&
                    userPhysicalEnergy >= requiredPhysicalEnergy &&
                    userAvailableMinutes >= requiredMinutes &&
                    goodWeather == weatherRelevant) {
                    val score = getScore(userMentalEnergy, userPhysicalEnergy, userAvailableMinutes, goodWeather,
                        requiredMentalEnergy, requiredPhysicalEnergy, requiredMinutes, weatherRelevant)
                    result.add(RecommendedActivity(activityDoc.getString("activity"), score))
                }
            }
            result.joinToString(prefix = "[", postfix = "]") {it.toString()}
        }
    }
}

fun getScore(userMentalEnergy: Int, userPhysicalEnergy: Int, userAvailableMinutes: Int, goodWeather: Boolean,
             requiredMentalEnergy: Int, requiredPhysicalEnergy: Int, requiredMinutes: Int, weatherRelevant: Boolean): Double {
    var score: Double = requiredMinutes / userAvailableMinutes.toDouble() * 100
    var factors: Int = 1
    if (requiredMentalEnergy > 1) {
        factors += 1
        if (requiredMentalEnergy == userMentalEnergy) score += 100 else score += 50
    }
    if (requiredPhysicalEnergy > 1) {
        factors += 1
        if (requiredPhysicalEnergy == userPhysicalEnergy) score += 100 else score += 50
    }
    if (goodWeather && weatherRelevant) {
        factors += 2
        score += 200
    }
    return score / factors
}

class RecommendedActivity(val activityName: String, val score: Double) {
    override fun toString(): String = """
        {"activity": "$activityName", "score": "$score"}
    """.trimIndent()
}

fun timeStringToMinutes(timeString: String): Int {
    val timeSplitted = timeString.split(":")
    val hours = Integer.parseInt(timeSplitted[0].trim())
    val mins = Integer.parseInt(timeSplitted[1].trim())
    return (hours * 60) + mins
}