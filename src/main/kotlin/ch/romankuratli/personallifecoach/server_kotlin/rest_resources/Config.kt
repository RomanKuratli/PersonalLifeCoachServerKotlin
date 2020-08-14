package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import ch.romankuratli.personallifecoach.server_kotlin.MongoDBConnector
import ch.romankuratli.personallifecoach.server_kotlin.utils.Utils
import com.mongodb.operation.FindAndUpdateOperation
import org.bson.BSON
import org.bson.Document
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.*

import org.bson.conversions.Bson
import spark.Route
import java.text.SimpleDateFormat
import java.util.*

private val CONFIG_COLL = MongoDBConnector.getCollection("config")
private val SDF = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

fun getCurrentLocation(): String = CONFIG_COLL.find().first().getString("currentLocation")

fun getBirthday(): String = SDF.format(CONFIG_COLL.find().first().getDate("birthday"))

// START extend document
fun Document.toLocationJson(): String {
    return """
        {
            "city": "${getString("city")}"
        }
    """.trimIndent()
}

fun Document.toConfigJson(): String {
    return """
        {
            "city": "${getString("city")}"
        }
    """.trimIndent()
}

// END extend document

class Config: RESTResource {
    override val subPath: String get() = "/config"
    override val subResources: Array<RESTResource> get() = arrayOf(AvailableLocations(),CurrentLocation(), Birthday())
    override fun handleGet(): Route {
        return Route {_, _ ->

        }
    }
}

class AvailableLocations: RESTResource {
    override val subPath: String get() = "/available_locations"
    override fun handleGet(): Route {
        return Route {_, _ ->
            CONFIG_COLL.find().first().getList("availableLocations", Document::class.java).joinToString(prefix = "[", postfix = "]") { it.toLocationJson()}
        }
    }
}

class CurrentLocation: RESTResource {
    override val subPath: String get() = "/current_location"
    override fun handleGet(): Route {
        return Route {_, _ ->
            """
        {
            "city": "${getCurrentLocation()}"
        }
    """.trimIndent()
        }
    }

   override fun handlePost(): Route {
        return Route {req, res ->
            var msg: String? = null
            val newCity = Utils.getBodyJsonDoc(req, arrayOf("city")).getString("city")
            for (locationDoc in CONFIG_COLL.find().first().getList("availableLocations", Document::class.java)) {
                println("newCity: $newCity, locationDoc.getString('city'); ${locationDoc.getString("city")}")
                println("= ${newCity == locationDoc.getString("city")}")
                if (newCity == locationDoc.getString("city")) {
                    val filterDoc = eq("currentLocation", getCurrentLocation())
                    val updateDoc = set("currentLocation", newCity)
                    // "_id" is not a valid BSON field name
                    val vain1 = CONFIG_COLL.findOneAndUpdate(filterDoc, updateDoc)
                    val vain2 = res.status(201)
                    msg = """
                        {"msg": "Successfully updated location!"}
                    """.trimIndent()
                }
            }
            if (msg == null) {
                msg = """
                    {"msg" : "Location $newCity is not available"}
                """.trimIndent()
            }
            msg
        }
    }
}

class Birthday: RESTResource {
    override val subPath: String get() = "/birthday"
    override fun handleGet(): Route {
        return Route {_, _ ->
            """
        {
            "birthday": "${getBirthday()}"
        }
    """.trimIndent()
        }
    }

    override fun handlePost(): Route {
        return Route {req, res ->
            val newBirthdayStr = Utils.getBodyJsonDoc(req, arrayOf("birthday")).getString("birthday")
            val newBirthday = SDF.parse(newBirthdayStr)
            val filterDoc = eq("currentLocation", getCurrentLocation())
            val updateDoc = set("birthday", newBirthday)
            val vain1 = CONFIG_COLL.findOneAndUpdate(filterDoc, updateDoc)
            val vain2 = res.status(201)
            """
                {"msg": "Successfully updated location!"}
            """.trimIndent()
        }
    }
}