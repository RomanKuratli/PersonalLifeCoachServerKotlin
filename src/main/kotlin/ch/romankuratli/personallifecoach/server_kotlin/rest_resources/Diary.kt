package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import ch.romankuratli.personallifecoach.server_kotlin.MongoDBConnector
import ch.romankuratli.personallifecoach.server_kotlin.utils.DiaryPictureManager
import ch.romankuratli.personallifecoach.server_kotlin.utils.Utils
import org.bson.Document
import spark.Route
import java.util.*
import kotlin.collections.ArrayList

private val DIARY_COLL = MongoDBConnector.getCollection("diary")

fun Date.toMyDateString(): String = "${year + 1900}_${month + 1}_$date"

fun String.myDateStringToDate(): Date {
    val (year, month, date) = split("_").map { toInt() }
    val c = Calendar.getInstance()
    c.set(year, month, date)
    return c.time
}

fun Document.toDiaryJson(): String  {
    val d: Date = getDate("entry_date")

    return """
{
    "_id": "${get("_id").toString()}",
    "entryDate": "${d.toMyDateString()}",
    "entries": ${
    get("entries", ArrayList<String>()).joinToString(prefix = "[", postfix = "],")
    { "\"${it.replace('"', "'"[0])}\"" }
    }
    "pictureUrls": ${DiaryPictureManager.getPicUrlsForEntry(d).joinToString(prefix = "[", postfix = "]") {"\"$it\""}}
}
"""
}

class Diary: RESTResource {
    override val subPath get() = "/diary"
    override val subResources: Array<RESTResource> get() = arrayOf(GoodDay())

    override fun handleGet(): Route {
        return Route {_, _ ->
            DIARY_COLL.find().joinToString(prefix = "[", postfix = "]") { it.toDiaryJson()}
        }
    }

    override fun handlePost(): Route {
        return Route {req, _ ->
            val doc = Utils.getBodyJsonDoc(req, arrayOf("entry_date", "entries"))
            doc["entry_date"] = doc.getString("entry_date").myDateStringToDate()
            DIARY_COLL.insertOne(doc)
            doc.toDiaryJson()
        }
    }
}

private class GoodDay : RESTResource {
    override val subPath get() = "/goodDay"

    override fun handleGet(): Route {
        return Route { _, _ ->
            val goodDays = DIARY_COLL.find().toList().filter {
                it.getList("entries", String::class.java).size > 3
            }
            if (goodDays.isEmpty()) "[]"
            else goodDays[Random().nextInt(goodDays.size - 1)].toDiaryJson()
        }
    }
}