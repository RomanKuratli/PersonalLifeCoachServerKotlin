package ch.romankuratli.personallifecoach.server_kotlin

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

import java.io.IOException
import java.util.logging.Logger

class MongoDBConnector {

    companion object {

        private val LOGGER = Logger.getLogger(MongoDBConnector::class.java!!.name)
        private const val dbName = "PersonalLifeCoach"

        fun connect() {
            val client: MongoClient? = MongoClients.create()
            if (client == null) {
                val msg = "Could not connect to MongoDB"
                LOGGER.severe(msg)
                throw IOException(msg)
            }
            LOGGER.info("Successfully connected to MongoDB")
            val db: MongoDatabase? = client.getDatabase(dbName)
            if (db == null) {
                val msg = "Could not load database $dbName"
                LOGGER.severe(msg)
                throw IOException(msg)
            }
            LOGGER.info("Successfully loaded database $dbName")
        }
    }
}