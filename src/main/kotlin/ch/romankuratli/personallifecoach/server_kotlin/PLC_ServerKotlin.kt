package ch.romankuratli.personallifecoach.server_kotlin

import ch.romankuratli.personallifecoach.server_kotlin.rest_resources.RESTResource
import ch.romankuratli.personallifecoach.server_kotlin.rest_resources.RootResource
import ch.romankuratli.personallifecoach.server_kotlin.utils.DiaryPictureManager
import ch.romankuratli.personallifecoach.server_kotlin.utils.NotImplementedRoute
import java.util.logging.Logger
import spark.*
import java.io.IOException
import java.util.ArrayList

private const val URL_ROOT = "/rest"
private const val PORT = 4567

fun main(args: Array<String>) {
    PLC_ServerKotlin.startServer(URL_ROOT, PORT)
}


class PLC_ServerKotlin {
    companion object {
        private val LOGGER = Logger.getLogger(PLC_ServerKotlin::class.java.name)

        private fun recursiveSetupResource(resource: RESTResource, parentPath: String, availableRoutes: MutableList<String> ) {
            val path = parentPath + resource.subPath

            var route = resource.handleGet()
            if (route !is NotImplementedRoute) {
                availableRoutes.add("GET: $path")
                Spark.get(path, route)
            }

            route = resource.handlePost()
            if (route !is NotImplementedRoute) {
                availableRoutes.add("POST: $path")
                Spark.post(path, route)
            }

            route = resource.handleDelete()
            if (route !is NotImplementedRoute) {
                availableRoutes.add("DELETE: $path")
                Spark.delete(path, route)
            }

            for (subResource in resource.subResources) {
                recursiveSetupResource(subResource, path, availableRoutes)
            }
        }

        private fun setupRootResource() {
            val availableRoutes = ArrayList<String>()
            val root = RootResource()
            recursiveSetupResource(root, "", availableRoutes)
            root.availableRoutes = availableRoutes
        }

        fun startServer(urlRoot: String, port: Int) {
            try {// specify port
                Spark.port(port)

                // connect to MongoDB
                MongoDBConnector.connect()

                // specify static folder on server
                Spark.staticFiles.location("/resources")
                DiaryPictureManager.init()

                // CORS
                Spark.options("/*") { req, res ->
                    val accessControlRequestHeaders = req.headers("Access-Control-Request-Headers")
                    if (accessControlRequestHeaders != null) {
                        res.header("Access-Control-Allow-Headers", accessControlRequestHeaders)
                    }

                    val accessControlRequestMethod = req.headers("Access-Control-Request-Method")
                    if (accessControlRequestMethod != null) {
                        res.header("Access-Control-Allow-Methods", accessControlRequestMethod)
                    }
                }

                Spark.before("/rest/*") { // not for /rest itself which returns html
                        _, res ->
                    res.header("Access-Control-Allow-Origin", /*"http://localhost:4200"*/ "*")
                    res.type("application/json")
                }

                Spark.exception(Exception::class.java) { exception, _, _ ->
                    LOGGER.severe("Exception handling request:$exception")
                    exception.printStackTrace()
                }

                // setup Resources AFTER connecting to MongoDB
                setupRootResource()


                LOGGER.info("Running on http://localhost:$port")
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                LOGGER.severe("Error connecting to MongoDB: $ioe")
            }
        }
    }
}