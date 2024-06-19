package com.souvik.climacast.model.forecast

import com.google.gson.annotations.SerializedName
import com.souvik.climacast.model.weather.Clouds
import com.souvik.climacast.model.weather.Main
import com.souvik.climacast.model.weather.Sys
import com.souvik.climacast.model.weather.Weather
import com.souvik.climacast.model.weather.Wind

data class CustomList(
    @SerializedName("dt") var dt: Int? = null,
    @SerializedName("main") var main: Main? = Main(),
    @SerializedName("weather") var weather: ArrayList<Weather>? = arrayListOf(),
    @SerializedName("clouds") var clouds: Clouds? = Clouds(),
    @SerializedName("wind") var wind: Wind? = Wind(),
    @SerializedName("visibility") var visibility: Int? = null,
    @SerializedName("pop") var pop: Double? = null,
    @SerializedName("sys") var sys: Sys? = Sys(),
    @SerializedName("dt_text") var dt_text: String? = null,

    )
