package com.alfresco.content.data

import android.content.Context
import android.os.Environment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.concurrent.TimeUnit

const val APP_CONFIG_JSON = "app.config.json"
const val TASK_FILTERS_JSON = "task.filters.json"
const val INTERVAL_HOURS = 24L

/**
 * @property previousFetchTime
 * This method check if the given timeStamp has passed the 24 hours or not
 */
fun isTimeToFetchConfig(previousFetchTime: Long): Boolean {
    val hoursInMilliseconds = TimeUnit.HOURS.toMillis(INTERVAL_HOURS)
    val agoIntervalHours = System.currentTimeMillis() - hoursInMilliseconds
    return previousFetchTime < agoIntervalHours
}

/**
 * @property context
 * @property fileName
 * get the Json file from the asset folder
 */
fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.use { it.read(buffer) }

        jsonString = String(buffer)
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

/**
 * @property jsonFileString
 * Get generic model from given json string
 */
inline fun <reified T> getModelFromStringJSON(jsonFileString: String): T {
    val gson = Gson()
    val type = object : TypeToken<T>() {}.type

    return gson.fromJson(jsonFileString, type)
}

/**
 * @property context
 * @property jsonFileString
 * Save AppConfigJSON to the internal storage
 */
fun saveJSONToInternalDirectory(context: Context, jsonFileString: String) {
    val fileDirectory = getAppConfigParentDirectory(context)
    if (fileDirectory != null && !fileDirectory.exists()) {
        fileDirectory.mkdirs()
    }
    val file = File(fileDirectory, APP_CONFIG_JSON)
    val fileWriter = FileWriter(file)
    val bufferedWriter = BufferedWriter(fileWriter)
    bufferedWriter.write(jsonFileString)
    bufferedWriter.close()
}

/**
 * @property context
 * Get App Config parent directory
 */
fun getAppConfigParentDirectory(context: Context) = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

/**
 * @property context
 * returns true if AppConfig exists on the internal storage otherwise false
 */
fun isAppConfigExistOnLocal(context: Context): Boolean {
    val fileDirectory = getAppConfigParentDirectory(context)
    if (fileDirectory != null && !fileDirectory.exists()) {
        return false
    }
    val file = File(fileDirectory, APP_CONFIG_JSON)

    return file.exists()
}

/**
 * @property context
 * Retrieve JSON in string form from the internal storage
 */
fun retrieveJSONFromInternalDirectory(context: Context): String {
    val fileDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val file = File(fileDirectory, APP_CONFIG_JSON)
    val fileReader = FileReader(file)
    val bufferedReader = BufferedReader(fileReader)
    val stringBuilder = StringBuilder()
    var line: String? = bufferedReader.readLine()
    while (line != null) {
        stringBuilder.append(line).append("\n")
        line = bufferedReader.readLine()
    }
    bufferedReader.close()
    return stringBuilder.toString()
}

/**
 * @property model
 * Convert given model to any JSON format
 */
inline fun <reified T> getJSONFromModel(model: T): String = Gson().toJson(model)

val gson: Gson = GsonBuilder()
    .registerTypeAdapter(
        ZonedDateTime::class.java,
        object : TypeAdapter<ZonedDateTime?>() {
            override fun write(out: JsonWriter, value: ZonedDateTime?) {
                out.value(value.toString())
            }

            override fun read(inType: JsonReader): ZonedDateTime? {
                return ZonedDateTime.parse(inType.nextString(), formatter)
            }
        },
    )
    .enableComplexMapKeySerialization()
    .create()

private val formatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .optionalStart().appendOffset("+HHMM", "Z").optionalEnd()
    .toFormatter()
