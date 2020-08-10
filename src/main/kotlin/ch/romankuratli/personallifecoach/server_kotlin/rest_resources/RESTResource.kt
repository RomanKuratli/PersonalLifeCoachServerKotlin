package ch.romankuratli.personallifecoach.server_kotlin.rest_resources



import ch.romankuratli.personallifecoach.server_kotlin.utils.NotImplementedRoute
import spark.Route

interface RESTResource {
    val subPath: String
    val subResources: Array<RESTResource>
        get() = arrayOf()

    fun handleGet(): Route {
        return NotImplementedRoute()
    }

    fun handlePut(): Route {
        return NotImplementedRoute()
    }

    fun handlePost(): Route {
        return NotImplementedRoute()
    }

    fun handleDelete(): Route {
        return NotImplementedRoute()
    }
}