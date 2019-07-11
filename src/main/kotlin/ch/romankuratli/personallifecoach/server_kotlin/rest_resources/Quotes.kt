package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import ch.romankuratli.personallifecoach.server_kotlin.MongoDBConnector
import org.bson.Document
import spark.Route

class Quotes: RESTResource {
    companion object {
        val QUOTES_COLL = MongoDBConnector.getCollection("quotes")
    }
    override val subPath: String get() = "/quotes"
    override val subResources: Array<RESTResource>
        get() = arrayOf()

    override fun handleGet(): Route {
        return Route{_, _ ->
            QUOTES_COLL.find().toList().joinToString(prefix = "[", postfix = "]") {
                """
                    {
                        "_id": "${it.get("_id").toString()}",
                        "lang": "${it.getString("lang")}",
                        "quote": "${it.getString("quote")}",
                        "author": "${it.getString("author")}",
                        "source": "${it.getString("source")}"
                    }
                """.trimIndent()
            }
        }
    }
}