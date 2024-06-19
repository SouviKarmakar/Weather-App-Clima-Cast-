package com.souvik.climacast.network

import com.souvik.climacast.model.weather.WeatherResult
import com.souvik.climacast.constant.Const.Companion.openWeatherMapApiKey
import com.souvik.climacast.model.forecast.ForecastResult
import retrofit2.http.GET
import retrofit2.http.Query

interface IApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double = 0.0,
        @Query("lon") lng:Double = 0.0,
        @Query("units") units:String = "metric",
        @Query("appid") appId:String = openWeatherMapApiKey
    ): WeatherResult

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double = 0.0,
        @Query("lon") lng:Double = 0.0,
        @Query("units") units:String = "metric",
        @Query("appid") appId:String = openWeatherMapApiKey
    ): ForecastResult
}