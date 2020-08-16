package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import ch.romankuratli.personallifecoach.server_kotlin.MongoDBConnector
import ch.romankuratli.personallifecoach.server_kotlin.utils.AstroUtils
import org.bson.Document
import spark.Route
import java.util.*

private val CONFIG_COLL = MongoDBConnector.getCollection("config")

class Astro : RESTResource {
    override val subPath: String get() = "/astro"
    override val subResources: Array<RESTResource> get()
    = arrayOf(NatalEphemeris(), NatalAspects(), CurrentEphemeris(), CurrentAspects(), CurrentTransits())
}

class NatalEphemeris : RESTResource {
    override val subPath: String get() = "/natal_ephemeris"
    override fun handleGet(): Route {
        return Route {_, _ ->
            CONFIG_COLL.find().first().get("ephemeris", Document::class.java).toJson()
        }
    }
}

class NatalAspects : RESTResource {
    override val subPath: String get() = "/natal_aspects"
    override fun handleGet(): Route {
        return Route {_, _ ->
            CONFIG_COLL.find().first().getList("aspects", Document::class.java)
                .joinToString(prefix = "[", postfix = "]") { it.toJson() }
        }
    }
}

class CurrentEphemeris : RESTResource {
    override val subPath: String get() = "/current_ephemeris"
    override fun handleGet(): Route {
        return Route {_, _ ->
            AstroUtils.getEphemerisForDate(Date()).toJson()
        }
    }
}

class CurrentAspects : RESTResource {
    override val subPath: String get() = "/current_aspects"
    override fun handleGet(): Route {
        return Route {_, _ ->
            AstroUtils.getAspects(AstroUtils.getEphemerisForDate(Date()))
                .joinToString(prefix = "[", postfix = "]") { it.toJson() }
        }
    }
}

class CurrentTransits : RESTResource {
    override val subPath: String get() = "/current_transits"
    override fun handleGet(): Route {
        return Route {_, _ ->
            val birthday = CONFIG_COLL.find().first().getDate("birthday")
            AstroUtils.getTransits(AstroUtils.getEphemerisForDate(Date()), AstroUtils.getEphemerisForDate(birthday))
                .joinToString(prefix = "[", postfix = "]") { it.toJson() }
        }
    }
}