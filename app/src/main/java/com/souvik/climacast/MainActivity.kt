package com.souvik.climacast

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.souvik.climacast.constant.Const.Companion.colorBg1
import com.souvik.climacast.constant.Const.Companion.colorBg2
import com.souvik.climacast.constant.Const.Companion.permissions
import com.souvik.climacast.model.MyLatLng
import com.souvik.climacast.model.forecast.ForecastResult
import com.souvik.climacast.model.weather.WeatherResult
import com.souvik.climacast.ui.theme.ClimaCastTheme
import com.souvik.climacast.view.ForecastSection
import com.souvik.climacast.view.WeatherSection
import com.souvik.climacast.viewmodel.MainViewModel
import com.souvik.climacast.viewmodel.STATE
import kotlinx.coroutines.coroutineScope

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback //The LocationCallback interface is part of the Android Jetpack Location library
    private var locationRequired: Boolean = false           /* and is used to receive location updates from the FusedLocationProviderClient class.
                                                             A LocationCallback implementation must override the onLocationResult() method,
                                                             which is called when the device's location is updated.
                                                             This method receives a LocationResult object, which contains a list of Location objects
                                                             representing the device's location.*/


    override fun onResume() {
        super.onResume()
        if (locationRequired) startLocationUpdate();
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
    }

    @SuppressLint("MissingPermission")
    /* this function starts the location updates in the app
    by creating a LocationRequest object with the desired parameters and
    then requesting location updates using the FusedLocationProviderClient object. */
    private fun startLocationUpdate() {
        locationCallback.let {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 100
            )
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(100)
                .build()

            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLocationClient()
        intiViewModel()

        setContent {

            // This will keep the value of our current location
            var currentLocation by remember {
                mutableStateOf(MyLatLng(0.0, 0.0))
            }

            //Implement location callback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    //We are using the for loop to get all the locations or just use the last location
                    for (location in p0.locations) {
                        currentLocation = MyLatLng(
                            location.latitude,
                            location.longitude
                        )
                    }
                    //Fetch API when location change
                    fetchWeatherInformation(mainViewModel, currentLocation)

                }
            }

            ClimaCastTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationScreen(this@MainActivity, currentLocation)
                }
            }
        }
    }

    private fun fetchWeatherInformation(mainViewModel: MainViewModel, currentLocation: MyLatLng) {
        mainViewModel.state = STATE.LOADING
        mainViewModel.getWeatherByLocation(currentLocation)
        mainViewModel.getForecastByLocation(currentLocation)
        mainViewModel.state = STATE.SUCCESS

    }

    private fun intiViewModel() {
        mainViewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java]
    }

    @Composable
    private fun LocationScreen(context: Context, currentLocation: MyLatLng) {

        //Request runtime Permission
        val launcherMultiplePermissions =
            rememberLauncherForActivityResult(       //This launcher will be used to request multiple permissions from the user.
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissionMap ->                    /*This is a lambda function that will be called when the user grants or denies
                                                     the requested permissions. The permissionMap parameter is a map containing
                                                     the granted status of each requested permission.*/

                val areGranted = permissionMap.values.reduce { accepted, next ->    // This line uses the reduce function to check if all permissions are granted.
                    accepted && next}                                               /* It iterates through the values of the permissionMap and checks
                                                                                       if each permission is granted (true). If all permissions are granted,
                                                                                       areGranted will be true; otherwise, it will be false.*/


                //Check all permission is accepted
                if (areGranted) {
                    locationRequired = true;
                    startLocationUpdate();
                    Toast.makeText(context, "Permission Granted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
                }
            }

        val systemUiController = rememberSystemUiController()   /*SystemUiController is a class provided by Jetpack Compose
                                                                that allows you to control and customize the system UI elements,
                                                                such as the status bar and navigation bar. This can be useful
                                                                for creating a consistent look and feel for your app, especially
                                                                when dealing with full-screen experiences or immersive content.*/

        DisposableEffect(key1 = true, effect = {
            systemUiController.isSystemBarsVisible = false //This hides the status bar
            onDispose {
                systemUiController.isSystemBarsVisible = true //This shows the status bar
            }
        })

        LaunchedEffect(key1 = currentLocation, block = {
            coroutineScope {
                if (permissions.all {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }) {
                    //If all permissions are accepted
                    startLocationUpdate()
                } else {
                    launcherMultiplePermissions.launch(permissions)
                }

            }
        })

        LaunchedEffect(key1 = true, block = {
            fetchWeatherInformation(mainViewModel, currentLocation)
        })

        val gradient = Brush.linearGradient(
            colors = listOf(Color(colorBg1), Color(colorBg2)),
            start = Offset(1000f, -1000f),
            end = Offset(1000f, 1000f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.BottomCenter
        ) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val marginTop = screenHeight * 0.1f //margin top by 10%
            val marginTopPx = with(LocalDensity.current) { marginTop.toPx() }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)

                        //Define the layout of the child
                        layout(
                            placeable.width,
                            placeable.height + marginTopPx.toInt()
                        ) {
                            placeable.placeRelative(0, marginTopPx.toInt())
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mainViewModel.state == STATE.LOADING) {
                    LoadingSection()
                } else if (mainViewModel.state == STATE.FAILED) {
                    ErrorSection(mainViewModel.errorMessage)
                } else {
                    WeatherSection(mainViewModel.weatherResponse)
                    ForecastSection(mainViewModel.forecastResponse)
                }
            }
            /*
            FloatingActionButton(
                onClick = { //Fetch API when location change
                    fetchWeatherInformation(mainViewModel, currentLocation) },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh" )
            }*/
        }
    }


    @Composable
    fun ErrorSection(errorMessage: String) {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = errorMessage, color = Color.White)
        }
    }

    @Composable
    fun LoadingSection() {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }


    /* The function initLocationClient() initializes a Fused Location
     Provider Client, which is a system service provided by Google Play Services
     to handle location-related tasks. The Fused Location Provider Client intelligently
     manages the use of multiple location providers, such as GPS and Wi-Fi, to give you
     the best location information.*/
    private fun initLocationClient() {
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)
    }

}


