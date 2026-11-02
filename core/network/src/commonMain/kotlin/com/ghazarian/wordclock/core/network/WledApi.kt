package com.ghazarian.wordclock.core.network

import com.ghazarian.wordclock.core.model.DeviceAddress
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.WordClockDeviceInfo
import com.ghazarian.wordclock.core.model.WordClockState
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class WledApi(
    private val httpClient: HttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getInfo(address: DeviceAddress): WordClockDeviceInfo {
        val responseText = getText(address, "/json/info")
        val root = try {
            json.parseToJsonElement(responseText) as? JsonObject
                ?: throw invalidDeviceInfo()
        } catch (exception: WledApiException) {
            throw exception
        } catch (exception: Exception) {
            throw invalidDeviceInfo(exception)
        }
        val leds = root["leds"] as? JsonObject ?: throw invalidDeviceInfo()
        val name = (root["name"] as? JsonPrimitive)
            ?.contentOrNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw invalidDeviceInfo()
        val physicalLedCount = (leds["count"] as? JsonPrimitive)
            ?.intOrNull
            ?.takeIf { it > 0 }
            ?: throw invalidDeviceInfo()
        val segmentCount = (leds["maxseg"] as? JsonPrimitive)
            ?.intOrNull
            ?.takeIf { it > 0 }
            ?: throw invalidDeviceInfo()

        return WordClockDeviceInfo(
            name = name,
            macAddress = (root["mac"] as? JsonPrimitive)
                ?.contentOrNull
                ?.trim()
                ?.takeIf { it.isNotEmpty() },
            physicalLedCount = physicalLedCount,
            segmentCount = segmentCount,
            firmwareVersion = (root["ver"] as? JsonPrimitive)
                ?.contentOrNull
                ?.trim()
                ?.takeIf { it.isNotEmpty() },
        )
    }

    suspend fun getState(address: DeviceAddress): WordClockState =
        parseState(
            json.parseToJsonElement(
                getText(address, "/json/state"),
            )
        )

    suspend fun setPower(
        address: DeviceAddress,
        isOn: Boolean
    ): WordClockState = updateState(address) {
        put("on", JsonPrimitive(isOn))
    }

    suspend fun setBrightness(
        address: DeviceAddress,
        brightness: Int,
    ): WordClockState {
        require(brightness in 0..255)
        return updateState(address) {
            put("bri", JsonPrimitive(brightness))
        }
    }

    suspend fun setColor(
        address: DeviceAddress,
        color: LedColor,
    ): WordClockState = updateState(address) {
        put(
            "seg",
            buildJsonArray {
                add(
                    buildJsonObject {
                        put(
                            "col",
                            buildJsonArray {
                                add(
                                    buildJsonArray {
                                        add(JsonPrimitive(color.red))
                                        add(JsonPrimitive(color.green))
                                        add(JsonPrimitive(color.blue))
                                    },
                                )
                            },
                        )
                    },
                )
            },
        )
    }

    private suspend fun updateState(
        address: DeviceAddress,
        payload: JsonObjectBuilder.() -> Unit,
    ): WordClockState {
        val body = json.encodeToString(
            JsonObject.serializer(),
            buildJsonObject(payload),
        )
        val response = httpClient.post(address.baseUrl + "/json/state") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(body)
        }
        return parseState(readSuccessfulJson(response))
    }

    private suspend fun getText(
        address: DeviceAddress,
        path: String,
    ): String {
        val response = httpClient.get(address.baseUrl + path)
        return readSuccessfulText(response)
    }

    private suspend fun readSuccessfulText(response: HttpResponse): String {
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw WledApiException(
                "Word Clock returned HTTP " + response.status.value + ".",
            )
        }
        return body
    }

    private suspend fun readSuccessfulJson(response: HttpResponse): JsonElement =
        json.parseToJsonElement(readSuccessfulText(response))

    private fun parseState(element: JsonElement): WordClockState {
        val root = element.jsonObject
        val segment = root["seg"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
        val rgb = segment
            ?.get("col")
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonArray

        return WordClockState(
            isOn = root["on"]?.jsonPrimitive?.booleanOrNull ?: true,
            brightness = root["bri"]?.jsonPrimitive?.intOrNull ?: 80,
            color = LedColor(
                red = rgb?.getOrNull(0)?.jsonPrimitive?.intOrNull ?: 255,
                green = rgb?.getOrNull(1)?.jsonPrimitive?.intOrNull ?: 255,
                blue = rgb?.getOrNull(2)?.jsonPrimitive?.intOrNull ?: 255,
            ),
            effectId = segment?.get("fx")?.jsonPrimitive?.intOrNull,
            physicalLedCount = segment?.get("len")?.jsonPrimitive?.intOrNull,
            segmentCount = root["seg"]?.jsonArray?.size ?: 1,
        )
    }

    private fun invalidDeviceInfo(cause: Throwable? = null) =
        WledApiException(
            message = "The endpoint did not return valid Word Clock device information.",
            isInvalidResponse = true,
            cause = cause,
        )
}

class WledApiException(
    message: String,
    val isInvalidResponse: Boolean = false,
    cause: Throwable? = null,
) : Exception(message, cause)
