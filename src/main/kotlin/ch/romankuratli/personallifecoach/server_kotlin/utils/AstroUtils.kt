package ch.romankuratli.personallifecoach.server_kotlin.utils

import org.bson.Document

import java.util.*
import org.jsoup.Jsoup
import javax.print.Doc
import kotlin.math.abs
import kotlin.math.round

val BASE_URL_ASTRO = "https://www.astro.com/cgi/swetest.cgi?"
val PARAM_ASTRO = "&n=1&s=1&p=p&e=-eswe&f=PLBRS&arg=-head+"
val SAVE_PLANETS = setOf("sun", "moon", "mercury", "venus", "mars", "jupiter", "saturn", "uranus", "neptune", "pluto")
val ZODIAC_SIGNS = listOf("Widder", "Stier", "Zwillinge", "Krebs", "Löwe", "Jungfrau", "Waage", "Skorpion", "Schütze", "Steinbock", "Wassermann", "Fische")
val ZODIAC_SIGN_GRAD: List<Pair<String, Int>> = ZODIAC_SIGNS.mapIndexed({index, s ->  Pair(s, index * 30)})
val MAIN_ASPECTS = listOf(
    Triple("Konjunktion", 0, 8),
    Triple("Sextil", 60, 4),
    Triple("Quadrat", 90, 6),
    Triple("Trigon", 120, 7),
    Triple("Opposition", 180, 10)
)


object AstroUtils {
    fun getEphemerisForDate(dt: Date): Document {
        val result = Document()
        val url = "${BASE_URL_ASTRO}b=${dt.date}.${dt.month + 1}.${dt.year + 1900}${PARAM_ASTRO}-t${padZero(dt.hours)}.${padZero(dt.minutes)}00"
        //print("JSoup URL: $url")
        val jSoupDoc: org.jsoup.nodes.Document = Jsoup.connect(url).get()
        val content = jSoupDoc.getElementsByTag("pre").first().getElementsByTag("font").first().text()
        //print("content:\n$content")
        val lines = content.splitToSequence("\n").map { it.trim() }.filter { it.isNotEmpty() }
        for (line in lines) {
            //print("line: $line")
            val planet = line.substring(0, 16).trim().toLowerCase()
            val pos = line.substring(16, line.indexOf('.', 16)).trim()
            if (SAVE_PLANETS.contains(planet)) {
                result[planet] = parsePosition(pos)
            }
        }
        return result
    }
    /*
    def get_aspects(eph):
    found_aspects = []
    for planet, position in eph.items():
        for asp_name, asp_grad, asp_grad_orb, asp_harmony in MAIN_ASPECTS:
            asp_mts, asp_mts_orb = asp_grad * 60, asp_grad_orb * 60
            asp_range_mts_min = asp_mts - asp_mts_orb
            asp_range_mts_max = asp_mts + asp_mts_orb
            for planet2, position2 in eph.items():
                if any((planet == planet2, aspect_already_collected(found_aspects, planet, planet2, asp_name))):
                    continue
                distance_mts = get_distance_mts(position["total_min"], position2["total_min"])
                if asp_range_mts_min <= distance_mts <= asp_range_mts_max:
                    asp_diff = abs(distance_mts - asp_mts)
                    accuracy = round(100 - (asp_diff / asp_mts_orb * 100), 2)
                    found_aspects.append({
                        "planet": planet,
                        "aspect": asp_name,
                        "planet2": planet2,
                        "accuracy": accuracy
                    })
    return found_aspects
     */

    fun getAspects(eph: Document): List<Document> {
        val result = mutableListOf<Document>()
        for ((planet: String, position) in eph.entries) {
            val pos = position as Document
            for((aspName, aspGrad, orbGrad) in MAIN_ASPECTS) {
                val aspMts = aspGrad * 60
                val orbMts = orbGrad * 60
                val aspRangeMtsMin = aspMts - orbMts
                val aspRangeMtsMax = aspMts + orbMts
                for ((planet2, position2) in eph.entries) {
                    val pos2 = position2 as Document
                    if (planet == planet2 || aspectAlreadyCollected(result, planet, planet2, aspName)) {
                        continue // check if aspect has already been collected
                    }
                    val distanceMts = getDistanceMts(pos.getInteger("minsTotal"), pos2.getInteger("minsTotal"))
                    if (distanceMts in aspRangeMtsMin .. aspRangeMtsMax) {
                        val aspDiff = abs(distanceMts - aspMts)
                        val accuracy = round(100 - (aspDiff.toDouble() / orbMts * 100))
                        val asp = Document()
                        asp["planet"] = planet
                        asp["aspect"] = aspName
                        asp["planet2"] = planet2
                        asp["accuracy"] = accuracy
                        result += asp
                    }
                }
            }
        }
        return result
    }

    fun getTransits(ephNow: Document, ephRadix: Document): List<Document> {
        val result = mutableListOf<Document>()
        for ((planetTransit: String, positionTransit) in ephNow.entries) {
            val posTransit = positionTransit as Document
            for((aspName, aspGrad, orbGrad) in MAIN_ASPECTS) {
                val aspMts = aspGrad * 60
                val orbMts = orbGrad * 60
                val aspRangeMtsMin = aspMts - orbMts
                val aspRangeMtsMax = aspMts + orbMts
                for ((planetRadix, positionRadix) in ephRadix.entries) {
                    val posRadix = positionRadix as Document
                    if (planetTransit == planetRadix || aspectAlreadyCollected(result, planetTransit, planetRadix, aspName)) {
                        continue // check if aspect has already been collected
                    }
                    val distanceMts = getDistanceMts(posTransit.getInteger("minsTotal"), posRadix.getInteger("minsTotal"))
                    if (distanceMts in aspRangeMtsMin .. aspRangeMtsMax) {
                        val aspDiff = abs(distanceMts - aspMts)
                        val accuracy = round(100 - (aspDiff.toDouble() / orbMts * 100))
                        val asp = Document()
                        asp["planet"] = planetTransit
                        asp["aspect"] = aspName
                        asp["planet2"] = planetRadix
                        asp["accuracy"] = accuracy
                        result += asp
                    }
                }
            }
        }
        return result
    }
}

fun getDistanceMts(posMts1: Int, posMts2:Int): Int {
    var distance = Math.abs(posMts1 - posMts2)
    // distance cannot be more than 180° => take the shorter way which is the other part of the 360°
    if (distance > (180 * 60)) {
        distance = (360 * 60) - distance
    }
    return distance
}

fun aspectAlreadyCollected(result: List<Document>, planet: String, planet2: String, aspName: String): Boolean {
    for (foundAspect in result) {
        val planets = setOf(foundAspect.getString("planet"), foundAspect.getString("planet2"))
        if (aspName == foundAspect.getString("aspect") && planets.contains(planet) && planets.contains(planet2)) {
            return true
        }
    }
    return false
}

fun padZero(num: Int): String {
    val str = num.toString()
    if (str.length == 1) return "0$str" else return str
}

fun parsePosition(pos: String): Document {
    val result = Document()
    val (gradTotalStr, rest1) = pos.split("°")
    var gradTotal = gradTotalStr.toInt()
    val (minsStr, rest2) = rest1.split("'")
    var mins = minsStr.strip().toInt()
    val sec = rest2.strip().toInt()
    if (sec >= 30) {
        mins += 1
        if (mins == 60) {
            gradTotal += 1
            mins = 0
        }
    }
    val minsTotal = (gradTotal * 60) + mins
    for ((sign, signStartGrad) in ZODIAC_SIGN_GRAD) {
        val grad = gradTotal - signStartGrad
        if (30 > grad && grad > -1) {
            result["grad"] = grad
            result["mins"] = mins
            result["sign"] = sign
            result["minsTotal"] = minsTotal
        }
    }
    return result
}

