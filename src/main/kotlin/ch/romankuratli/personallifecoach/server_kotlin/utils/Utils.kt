package ch.romankuratli.personallifecoach.server_kotlin.utils

import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import spark.Request

import java.util.logging.Logger

object Utils {
    private val LOGGER = Logger.getLogger(Utils::class.java.name)

    private fun _mapFromDocument(jsonObj: JSONObject): MutableMap<String, Any> {
        val ret = HashMap<String, Any>()
        for (key in arrayOf("author", "lang", "quote", "source")) {
            ret[key] = jsonObj[key]!!
        }
        return ret
    }

    @Throws(ParseException::class)
    fun getBodyJsonDoc(req: Request): Document {
        val jsonParser = JSONParser()
        val jsonObj: JSONObject = jsonParser.parse(req.body()) as JSONObject
        return Document(_mapFromDocument(jsonObj))
    }
}// hide constructor, utility class