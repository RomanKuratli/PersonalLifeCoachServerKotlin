package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import ch.romankuratli.personallifecoach.server_kotlin.MongoDBConnector
import ch.romankuratli.personallifecoach.server_kotlin.utils.Utils
import com.mongodb.operation.FindAndUpdateOperation
import org.bson.BSON
import org.bson.Document
import org.bson.conversions.Bson
import spark.Route

private val CONFIG_COLL = MongoDBConnector.getCollection("config")

// START extend document
fun Document.toLocationJson(): String {
    return """
        {
            "city": "${getString("city")}"
        }
    """.trimIndent()
}

// END extend document

class Config: RESTResource {
    override val subPath: String get() = "/config"
    override val subResources: Array<RESTResource> get() = arrayOf(AvailableLocations(),CurrentLocation())
}

class AvailableLocations: RESTResource {
    override val subPath: String get() = "/available_locations"
    override fun handleGet(): Route {
        return Route {_, _ ->
            CONFIG_COLL.find().first().get("current_location", Document::class.java).toLocationJson()
        }
    }
}

class CurrentLocation: RESTResource {
    override val subPath: String get() = "/current_location"
    override fun handleGet(): Route {
        return Route {_, _ ->
            CONFIG_COLL.find().first().getList("available_locations", Document::class.java).joinToString(prefix = "[", postfix = "]") { it.toLocationJson()}
        }
    }

   /* override fun handlePut(): Route {
        return Route {req, res ->
            val city = Utils.getBodyJsonDoc(req, arrayOf("city")).getString("city")
            CONFIG_COLL.findOneAndUpdate(new Bson(),Bson())
            res.status(201)
        }
    } */
}