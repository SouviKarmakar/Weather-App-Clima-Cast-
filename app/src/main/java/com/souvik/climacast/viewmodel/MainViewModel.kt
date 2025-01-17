package com.souvik.climacast.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souvik.climacast.model.MyLatLng
import com.souvik.climacast.model.forecast.ForecastResult
import com.souvik.climacast.model.weather.WeatherResult
import com.souvik.climacast.network.RetrofitClient
import kotlinx.coroutines.launch


enum class STATE {
    LOADING,
    SUCCESS,
    FAILED
}

class MainViewModel : ViewModel() {
    //Control State of View Model
    var state by mutableStateOf(STATE.LOADING)

    //Hold value from API for Weather info
    var weatherResponse: WeatherResult by mutableStateOf(WeatherResult())

    //Hold value from API for Forecast info
    var forecastResponse: ForecastResult by mutableStateOf(ForecastResult())
    var errorMessage: String by mutableStateOf("")

    fun getWeatherByLocation(latLng: MyLatLng){
        viewModelScope.launch {
            state = STATE.LOADING
            val apiService = RetrofitClient.getInstance()
            try {
                val apiResponse = apiService.getWeather(latLng.lat, latLng.lng)
                weatherResponse = apiResponse //Update state
                state = STATE.SUCCESS
            }catch (e:Exception){
                errorMessage = e.message!!.toString()
                state = STATE.FAILED
            }

        }
    }

    fun getForecastByLocation(latLng: MyLatLng){
        viewModelScope.launch {
            state = STATE.LOADING
            val apiService = RetrofitClient.getInstance()
            try {
                val apiResponse = apiService.getForecast(latLng.lat, latLng.lng)
                forecastResponse = apiResponse //Update state
                state = STATE.SUCCESS
            }catch (e:Exception){
                errorMessage = e.message!!.toString()
                state = STATE.FAILED
            }

        }
    }
}
