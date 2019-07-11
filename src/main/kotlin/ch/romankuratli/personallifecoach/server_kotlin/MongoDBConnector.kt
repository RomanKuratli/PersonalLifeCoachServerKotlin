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
        private var client: MongoClient? = null
        private var db: MongoDatabase? = null

        fun connect() {
            client = MongoClients.create()
            if (client == null) {
                val msg = "Could not connect to MongoDB"
                LOGGER.severe(msg)
                throw IOException(msg)
            }
            LOGGER.info("Successfully connected to MongoDB")
            db = client!!.getDatabase(dbName)
            if (db == null) {
                val msg = "Could not load database $dbName"
                LOGGER.severe(msg)
                throw IOException(msg)
            }
            LOGGER.info("Successfully loaded database $dbName")
        }

        private fun collectionExists(colName: String): Boolean {
            for (existingCol in db!!.listCollectionNames()) {
                if (colName == existingCol) return true
            }
            return false
        }

        fun getCollection(colName: String): MongoCollection<Document> {
            if (db == null) {
                val msg = "connect() must be called prior to requesting collection $colName"
                LOGGER.severe(msg)
                throw IllegalStateException(msg)
            }
            if (!collectionExists(colName)) {
                val msg = "Could not find collection in database: $colName"
                LOGGER.severe("Could not find collection in database: $colName")
                throw IllegalArgumentException(msg)
            }
            return db!!.getCollection(colName)
        }
    }
}