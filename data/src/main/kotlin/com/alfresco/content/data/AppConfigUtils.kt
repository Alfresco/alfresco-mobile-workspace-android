package com.alfresco.content.data

import android.content.Context
import android.os.Environment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.lang.StringBuilder

const val APP_CONFIG_JSON = "app.config.json"

fun isTimeToFetchConfig(timeStamp: Long): Boolean {
    val hours24 = (1 * 24 * 60 * 60 * 1000)
    val ago24 = System.currentTimeMillis() - hours24
    println("isTimeToFetchConfig $ago24 :: $timeStamp")
    return timeStamp < ago24
}

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

inline fun <reified T> getModelFromStringJSON(jsonFileString: String): T {
    val gson = Gson()
    val type = object : TypeToken<T>() {}.type

    return gson.fromJson(jsonFileString, type)
}

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

fun getAppConfigParentDirectory(context: Context) = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

fun isAppConfigExistOnLocal(context: Context): Boolean {

    val fileDirectory = getAppConfigParentDirectory(context)
    if (fileDirectory != null && !fileDirectory.exists()) {
        return false
    }
    val file = File(fileDirectory, APP_CONFIG_JSON)

    return file.exists()
}

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

inline fun <reified T> getJSONFromModel(model: T): String = Gson().toJson(model)
