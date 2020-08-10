package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import ch.romankuratli.personallifecoach.server_kotlin.MongoDBConnector
import ch.romankuratli.personallifecoach.server_kotlin.utils.Utils
import org.bson.Document
import org.bson.types.ObjectId
import spark.Route
import kotlin.random.Random
import com.mongodb.client.model.Filters.eq

val QUOTES_COLL = MongoDBConnector.getCollection("quotes")

fun Document.toQuoteJson(): String = """
{
    "_id": "${get("_id").toString()}",
    "lang": "${getString("lang")}",
    "quote": "${getString("quote")}",
    "author": "${getString("author")}",
    "source": "${getString("source")}"
}
"""

class Quotes: RESTResource {
    override val subPath get() = "/quotes"
    override val subResources: Array<RESTResource>
        get() = arrayOf(RandomQuote(), QuoteById())

    override fun handleGet(): Route {
        return Route{_, _ ->
            QUOTES_COLL.find().toList().joinToString(prefix = "[", postfix = "]") {it.toQuoteJson()}
        }
    }

    override fun handlePost(): Route {
        return Route{req, _ ->
            val doc = Utils.getBodyJsonDoc(req, arrayOf("author", "quote", "source", "lang"))
            // add the index value 'quote hash'
            doc["quote_hash"] = doc["quote"].hashCode()
            QUOTES_COLL.insertOne(doc)
            doc.toQuoteJson()
        }
    }
}

class RandomQuote: RESTResource {
    override val subPath get() = "/random"

    override fun handleGet(): Route {
        return Route{_,_ ->
            val quotes: List<Document> = QUOTES_COLL.find().toList()
            quotes[Random.nextInt(quotes.size - 1)].toQuoteJson()
        }
    }
}

class QuoteById: RESTResource {
    override val subPath: String get() = "/:id"

    override fun handleDelete(): Route {
        return Route{req, _ ->
            QUOTES_COLL.deleteOne(eq<ObjectId>("_id", ObjectId(req.params("id"))))
            """{"msg": "Quote succesfully deleted !"}"""
        }
    }
}