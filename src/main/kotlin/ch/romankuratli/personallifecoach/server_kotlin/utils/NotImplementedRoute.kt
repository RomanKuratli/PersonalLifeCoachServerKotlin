package ch.romankuratli.personallifecoach.server_kotlin.utils

import spark.Request
import spark.Response
import spark.Route

class NotImplementedRoute : Route {
    @Throws(Exception::class)
    override fun handle(request: Request, response: Response): Any {
        throw PathNotImplementedException()
    }
}