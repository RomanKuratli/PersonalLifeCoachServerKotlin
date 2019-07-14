package ch.romankuratli.personallifecoach.server_kotlin.utils

import org.apache.commons.lang.ObjectUtils
import org.bson.Document
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import spark.Request
import java.lang.NullPointerException

import java.util.logging.Logger

object Utils {
    private val LOGGER = Logger.getLogger(Utils::class.java.name)

    private fun _mapFromDocument(jsonObj: JSONObject, fields: Array<String>): MutableMap<String, Any> {
        val ret = HashMap<String, Any>()
        for (key in fields) {
            if (jsonObj[key] == null) throw NullPointerException("Cannot fiend field $key in $jsonObj")
            ret[key] = jsonObj[key]!!
        }
        return ret
    }

    //@Throws(ParseException::class)
    fun getBodyJsonDoc(req: Request, fields: Array<String>): Document {
        val jsonParser = JSONParser()
        val jsonObj: JSONObject = jsonParser.parse(req.body()) as JSONObject
        return Document(_mapFromDocument(jsonObj, fields))
    }
}// hide constructor, utility class