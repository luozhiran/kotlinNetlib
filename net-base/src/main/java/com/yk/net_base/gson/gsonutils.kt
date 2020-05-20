package com.yk.net_base.gson

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.Type

class DoubleDefaultAdapter : JsonSerializer<Double?>,
    JsonDeserializer<Double> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Double {
        try {
            if (json.asString == "" || json.asString == "null") { //定义为double类型,如果后台返回""或者null,则返回0.00
                return 0.00
            }
        } catch (ignore: Exception) {
        }
        return try {
            json.asDouble
        } catch (e: NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }

    override fun serialize(
        src: Double?,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src)
    }
}


class IntegerDefaultAdapter : JsonSerializer<Int?>, JsonDeserializer<Int> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Int {
        try {
            if (json.asString == "" || json.asString == "null") { //定义为int类型,如果后台返回""或者null,则返回0
                return 0
            }
        } catch (ignore: java.lang.Exception) {
        }
        return try {
            json.asInt
        } catch (e: java.lang.NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }

    override fun serialize(
        src: Int?,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src)
    }
}


class LongDefaultAdapter : JsonSerializer<Long?>,
    JsonDeserializer<Long> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Long {
        try {
            if (json.asString == "" || json.asString == "null") { //定义为long类型,如果后台返回""或者null,则返回0
                return 0L
            }
        } catch (ignore: java.lang.Exception) {
        }
        return try {
            json.asLong
        } catch (e: java.lang.NumberFormatException) {
            throw JsonSyntaxException(e)
        }
    }

    override fun serialize(
        src: Long?,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src)
    }
}


class StringNullAdapter : TypeAdapter<String?>() {
    @Throws(IOException::class)
    override fun read(reader: JsonReader): String? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return "" //原先是返回Null，这里改为返回空字符串
        }
        val jsonStr = reader.nextString()
        return if (jsonStr == "null") {
            ""
        } else {
            jsonStr
        }
    }

    @Throws(IOException::class)
    override fun write(
        writer: JsonWriter,
        value: String?
    ) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.value(value)
    }
}