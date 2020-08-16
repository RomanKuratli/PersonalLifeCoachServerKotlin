package ch.romankuratli.personallifecoach.server_kotlin.rest_resources

import spark.Route
import java.lang.StringBuilder


class RootResource : RESTResource {

    var availableRoutes: List<String> = arrayListOf()
    override val subPath get() = "/rest"
    override val subResources get() = arrayOf(Quotes(), Diary(), Config(), Activities(), Astro())

    private fun getAvailableRoutesHTML(): String {
        val sb = StringBuilder()
        sb.append("""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>PersonalLifeCoach: REST Server</title>
</head>
<body>
    <h1>PersonalLifeCoach: Rest Server is up and running!</h1>
    <p>These are the available paths:</p>
    <ul>
        """)
        sb.append(availableRoutes.joinToString(separator = " ") { "<li>$it</li>" })
        sb.append("""
    </ul>
</body>
</html>""")
        return sb.toString()
    }

    override fun handleGet(): Route {
        return Route{_, _ -> getAvailableRoutesHTML()}
    }
}