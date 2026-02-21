package ru.digitalpaper.server.util

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Utils {

    private val gson = GsonBuilder()
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return false
            }

            override fun shouldSkipField(f: FieldAttributes): Boolean {
                return f.name.equals("unknownFields")
            }
        })
        .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter())
        .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTypeAdapter())
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()

    class LocalDateTypeAdapter : JsonSerializer<LocalDate?>, JsonDeserializer<LocalDate?> {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        override fun serialize(
            src: LocalDate?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.format(formatter))
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): LocalDate {
            return LocalDate.parse(json.asString, formatter)
        }
    }

    class ZonedDateTypeAdapter : JsonSerializer<ZonedDateTime?>, JsonDeserializer<ZonedDateTime?> {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm z")
        override fun serialize(
            src: ZonedDateTime?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src?.format(formatter))
        }

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ZonedDateTime? {
            return ZonedDateTime.parse(json.asString, formatter)
        }
    }

    fun logToJson(obj: Any): String {
        return gson.toJson(obj)
    }
}