package com.souvik.climacast.constant

class Const {
    companion object {
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        const val openWeatherMapApiKey = "1f8b84f13a3812cd8cec4ac5e7a58c3b"
        const val colorBg1 = 0xff08203e
        const val colorBg2 = 0xff557c93
        const val colorCard = 0xff829fb0

        const val LOADING = "Loading...."
        const val NA = "N/A"

    }
}