package dev.vstd.shoppingcart.common.utils

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.BufferedReader

class VietnamAdministrationProvider(private val context: Context) {
    private val gson = GsonBuilder().create()
    private val cities: List<City>

    init {
        val jsonArray = gson.fromJson(
            context.assets.open("cities.json").bufferedReader().use(BufferedReader::readText),
            JsonObject::class.java
        )
        cities = jsonArray.entrySet().map {
            City(
                it.key.toInt(),
                it.value.asJsonObject["fullName"].asString
            )
        }
    }

    fun getCities(): List<String> {
        return cities.map { it.displayName }
    }

    fun getDistricts(cityName: String): List<String>? {
        val city = cities.find { it.displayName == cityName } ?: return null
        return getDistricts(city.id).map { it.displayName }
    }

    private fun getDistricts(cityId: Int): List<District> {
        val jsonArray = gson.fromJson(
            context.assets.open(String.format("districts/%02d.json", cityId)).bufferedReader()
                .use(BufferedReader::readText),
            JsonObject::class.java
        )

        return jsonArray.entrySet().map {
            District(
                it.key.toInt(),
                it.value.asJsonObject["fullName"].asString
            )
        }
    }

    data class City(
        val id: Int,
        val displayName: String,
    )

    data class District(
        val id: Int,
        val displayName: String
    )
}