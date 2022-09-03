package com.example.carpoolapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.carpoolapp.ui.theme.CarpoolAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.search.OfflineSearchEngineSettings
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import timber.log.Timber

data class Carpool(
    val title: String,
    val destination: String
)

class HomePageActivity : ComponentActivity() {
    private val currentUser = Firebase.auth.currentUser
    private val db = Firebase.firestore
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this, MapInitOptions(this))
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        Timber.plant(Timber.DebugTree())
        FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                goToLoginPage()
            }
        }
        if (currentUser == null) {
            goToLoginPage()
            return
        }
        val userDocRef = db.collection("users").document(currentUser.uid)
        setContent {
            val carpools = remember { mutableStateListOf<Carpool>() }
            userDocRef.get()
                .addOnSuccessListener { document ->
                    Timber.i(document["carpools"].toString())
                    for (carpoolRef in (document["carpools"] as ArrayList<DocumentReference>)) {
                        carpoolRef.get().addOnSuccessListener {
                            carpools.add(
                                Carpool(
                                    title = it["title"].toString(),
                                    destination = it["location"].toString()
                                ),
                            )
                            Timber.i(it.toString())
                        }
                    }
                }

            CarpoolAppTheme(darkTheme = false) {
                Timber.i("Displaying home page")
                AppScreen(carpools = carpools, searchMapView = mapView)
            }
        }
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun goToLoginPage() {
        startActivity(Intent(this, LogInActivity::class.java))
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
}

@Composable
fun AppScreen(carpools: MutableList<Carpool>, searchMapView: MapView) {
    val titles = mapOf<Int, String?>(
        0 to "My Carpools",
        1 to "Find a Carpool",
        2 to "Create a Carpool",
        3 to "My Profile"
    )
    var selectedTab by remember { mutableStateOf(0) }
    var createCarpoolTitle by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        content = {
            var context = LocalContext.current
            if (selectedTab == 0) {
                MyCarpoolsScreen(carpools = carpools, context)
            } else if (selectedTab == 2) {
                CreateCarpoolScreen(
                    title = createCarpoolTitle,
                    onTitleChanged = { createCarpoolTitle = it }, {selectedTab = it} ,context
                )
            } else if (selectedTab == 1) {
                SearchCarpoolsScreen(context = context)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titles[selectedTab]!!,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                backgroundColor = MaterialTheme.colors.primaryVariant,
                modifier = Modifier.drawColoredShadow(
                    color = Color.DarkGray,
                    alpha = 0.5f
                )
            )
        },
        bottomBar = {
            TabRow(
                selectedTabIndex = selectedTab,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Home, contentDescription = null,
                        Modifier.padding(vertical = 12.dp)
                    )
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Search, contentDescription = null,
                        Modifier.padding(vertical = 12.dp)
                    )
                }
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Add, contentDescription = null,
                        Modifier.padding(vertical = 12.dp)
                    )
                }
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Person, contentDescription = null,
                        Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    )
}