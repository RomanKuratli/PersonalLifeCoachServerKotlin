package ch.romankuratli.personallifecoach.server_kotlin

import java.util.logging.Logger
import spark.*
import java.io.IOException

private const val URL_ROOT = "/rest"
private const val PORT = 4567

fun main(args: Array<String>) {
    PLC_ServerKotlin.startServer(URL_ROOT, PORT)
}


class PLC_ServerKotlin {
    companion object {
        private val LOGGER = Logger.getLogger(PLC_ServerKotlin::class.java.name)

        fun startServer(urlRoot: String, port: Int) {
            try {// specify port
                Spark.port(port)

                // connect to MongoDB
                MongoDBConnector.connect()

                // specify static folder on server
                Spark.staticFiles.location("/resources")

                LOGGER.info("Running on http://localhost:$port")
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                LOGGER.severe("Error connecting to MongoDB: $ioe")
            }
        }
    }
}