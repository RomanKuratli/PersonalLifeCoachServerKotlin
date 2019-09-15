package ch.romankuratli.personallifecoach.server_kotlin.utils

import ch.romankuratli.personallifecoach.server_kotlin.PLC_ServerKotlin
import ch.romankuratli.personallifecoach.server_kotlin.rest_resources.toMyDateString
import spark.Request
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.logging.Logger
import javax.servlet.MultipartConfigElement

object DiaryPictureManager {
    private val DIARY_PICTURES_DIR = Paths.get(File(".").canonicalPath, "resources", "diary", "pictures")
    private val LOGGER = Logger.getLogger(DiaryPictureManager::class.java.name)
    private val ALLOWED_EXT = setOf("jpg", "jpeg", "png", "gif")

    private fun getExtension(fileName: String): String {
        return fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toLowerCase()
    }

    private fun isFileAllowed(fileName: String): Boolean {
        return fileName.contains(".") && ALLOWED_EXT.contains(getExtension(fileName))
    }

    private fun doesFileExist(fileName: String, files: List<String>): Boolean {
        for (f in files) {
            if (f.contains(fileName)) return true
        }
        return false
    }

    private fun composeFileName(entryDate: String, ext: String, index: Int): String {
        return "${entryDate}_$index.$ext"
    }

    private fun getFileNames(): List<String> {
        val fileNames = ArrayList<String>()
        for (f in DIARY_PICTURES_DIR.toFile().listFiles()!!) {
            fileNames.add(f.getName())
        }
        return fileNames
    }

    private fun findFile(file: String, files: List<String>): Boolean {
        for (existingFile in files) {
            if (existingFile.contains(file)) return true
        }
        return false
    }

    fun init() {
        LOGGER.info("diary picture location: $DIARY_PICTURES_DIR");
    }

    fun addPicture(req: Request): Boolean {
        // TO allow for multipart file uploads
        req.attribute("org.eclipse.jetty.multipartConfig", MultipartConfigElement(""))
        try {
            // "file" is the key of the form data with the file itself being the value
            val filePart = req.raw().getPart("picture")
            // The name of the file user uploaded
            val fileNameIn = filePart.submittedFileName
            if (!isFileAllowed(fileNameIn)) return false

            var index = 0
            val files = getFileNames()
            val ext = getExtension(fileNameIn)
            // get date for picture
            val entryDate = Utils.getBodyJsonDoc(req, arrayOf("entryDate")).getString("entryDate")
            var fileName = composeFileName(entryDate, ext, index)
            while (findFile(fileName, files)) {
                index++
                fileName = composeFileName(entryDate, ext, index)
            }

            val stream = filePart.inputStream
            // Write stream to file under storage folder
            Files.copy(stream, Paths.get("storage").resolve(fileName), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: Exception) {
            LOGGER.severe("Error uploading file to server: " + e.message)
            e.printStackTrace()
        }

        return true
    }

    private fun isFromSameDate(dateStr: String, fileName: String): Boolean {
        if (!isFileAllowed(fileName)) {
            LOGGER.severe("file $fileName is not allowed!")
            return false
        }
        val (year, month, day) = dateStr.split("_").map { it.toInt() }
        val (fYear, fMonth, fDay) = fileName.split(".")[0].split("_").map { it.toInt() }
        return year === fYear && month === fMonth && day === fDay
    }

    fun getPicUrlsForEntry(d: Date): List<String> = getFileNames().filter { isFromSameDate(d.toMyDateString(), it)}
}