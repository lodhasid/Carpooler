package com.example.carpoolapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.carpoolapp.ui.theme.CarpoolAppTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.android.core.location.*
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import timber.log.Timber

class MapSearchActivity : ComponentActivity() {
    private lateinit var searchMapView: MapView
    private lateinit var searchResultsView: SearchResultsView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchEngine: SearchEngine
    private var location: Point? = null

    @SuppressLint("MissingPermission")
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        searchEngine =
            MapboxSearchSdk.createSearchEngine(SearchEngineSettings(getString(R.string.mapbox_access_token)))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        searchMapView = MapView(this)
        searchResultsView = SearchResultsView(this)
        searchResultsView.initialize(
            SearchResultsView.Configuration(
                CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
                searchEngineSettings = searchEngine.settings,
                offlineSearchEngineSettings = OfflineSearchEngineSettings(getString(R.string.mapbox_access_token))
            )
        )
        val annotationApi = searchMapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager().apply {
            addClickListener(OnPointAnnotationClickListener {
                val cameraPosition = CameraOptions.Builder()
                    .zoom(12.0)
                    .center(it.point)
                    .build()
                searchMapView.getMapboxMap().flyTo(cameraPosition)
                return@OnPointAnnotationClickListener true
            })
        }
        searchResultsView.addOnSuggestionClickListener(SearchResultsView.OnSuggestionClickListener { searchSuggestion ->
            searchEngine.select(searchSuggestion, object : SearchSelectionCallback {
                override fun onCategoryResult(
                    suggestion: SearchSuggestion,
                    results: List<SearchResult>,
                    responseInfo: ResponseInfo
                ) {
                    Timber.i("onCategoryResult")
                }

                override fun onError(e: Exception) {
                    Timber.e(e)
                }

                override fun onResult(
                    suggestion: SearchSuggestion,
                    result: SearchResult,
                    responseInfo: ResponseInfo
                ) {
                    val pointAnnotationOptions = PointAnnotationOptions()
                        .withPoint(result.coordinate!!)
                        .withIconImage(
                            AppCompatResources.getDrawable(getContext(), R.drawable.ic_untitled)
                                ?.toBitmap()!!
                        )
                    pointAnnotationManager.deleteAll()
                    pointAnnotationManager.create(pointAnnotationOptions)
                    val cameraPosition = CameraOptions.Builder()
                        .zoom(16.0)
                        .center(result.coordinate)
                        .build()
                    searchMapView.getMapboxMap()
                        .flyTo(cameraPosition, MapAnimationOptions.Builder().duration(2000).build())
                }

                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                    Timber.i("onSuggestions")
                }

            })
            Timber.i(searchSuggestion.address!!.place)
            return@OnSuggestionClickListener true
        })
        setContent {
            var query by remember { mutableStateOf("") }
            CarpoolAppTheme {
                Column(Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { searchMapView },
                        Modifier
                            .fillMaxHeight(0.5f)
                    )
                    AndroidView(
                        factory = { searchResultsView },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
                CustomTextField(
                    hint = "Search for a destination",
                    value = query,
                    onValueChanged = { newValue ->
                        query = newValue
                        location?.let {
                            searchResultsView.search(
                                query,
                                SearchOptions.Builder().origin(it).proximity(it).build()
                            )
                        }
                        searchResultsView.search(query)
                    },
                    leadingIcon = Icons.Filled.Search,
                    maxLines = 1
                )
            }
        }
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener {
                location = Point.fromLngLat(it.longitude, it.latitude)
                Timber.e(location.toString())
                searchMapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(it!!.longitude, it.latitude))
                        .zoom(12.0)
                        .build()
                )
            }
    }

    private companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getContext(): Context {
        return this
    }
}