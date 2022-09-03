package com.example.carpoolapp

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Space
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.carpoolapp.ui.theme.CarpoolAppTheme
import com.example.carpoolapp.ui.theme.FireOpal
import com.example.carpoolapp.ui.theme.FireOpalDark
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import java.util.*

fun Modifier.drawColoredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    val transparentColor = android.graphics.Color.toArgb(color.copy(alpha = 0.0f).value.toLong())
    val shadowColor = android.graphics.Color.toArgb(color.copy(alpha = alpha).value.toLong())
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

@Composable
fun MyCarpoolsScreen(carpools: MutableList<Carpool>, context: Context) {
    var showCarpoolDetails by remember { mutableStateOf(false) }
    if (showCarpoolDetails) {
        CarpoolDetails(context = context, buttonState = 2)
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Timber.i(carpools.toString())
        items(carpools.size) {
            carpools.forEach { carpool ->
                CarpoolLayout(carpool = carpool, onClick = { showCarpoolDetails = true })
            }
        }
    }
}

@Composable
fun CarpoolLayout(carpool: Carpool, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(8.dp)
            .shadow(10.dp)
            .clip(RoundedCornerShape(10))
            .clickable { onClick() }
    ) {
        Box(
            Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = carpool.title,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary
                )
                Row {
                    IconButton(onClick = { /*TODO*/ }, modifier = Modifier
                        .clickable { }
                        .size(26.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_chat_24),
                            contentDescription = null,
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { /*TODO*/ }, modifier = Modifier
                        .clickable { }
                        .size(26.dp)) {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    hint: String,
    value: String,
    onValueChanged: (String) -> Unit,
    leadingIcon: ImageVector? = null,
    maxLines: Int = 1
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = value,
        onValueChange = onValueChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .drawColoredShadow(color = Color.DarkGray, alpha = 0.5f, borderRadius = 96.dp)
            .clip(CircleShape),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    Modifier.padding(start = 8.dp)
                )
            }
        },
        placeholder = { Text(hint) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),
        maxLines = maxLines
    )
}

@Composable
fun SearchCarpoolsScreen(
    context: Context
) {
    val searchEngine =
        MapboxSearchSdk.createSearchEngine(SearchEngineSettings(context.getString(R.string.mapbox_access_token)))
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val searchMapView = MapView(context)
    val searchResultsView = SearchResultsView(context)
    var location: Point? = null
    var showResults by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var showCarpoolDetails by remember { mutableStateOf(false) }
    if  (showCarpoolDetails) {
        CarpoolDetails(context = context, buttonState = 0)
        return
    }
    searchResultsView.initialize(
        SearchResultsView.Configuration(
            CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
            searchEngineSettings = searchEngine.settings,
            offlineSearchEngineSettings = OfflineSearchEngineSettings(context.getString(R.string.mapbox_access_token))
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
                showResults = true
                val pointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(result.coordinate!!)
                    .withIconImage(
                        AppCompatResources.getDrawable(context, R.drawable.ic_untitled)
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
    CarpoolAppTheme {
        Column(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { searchMapView },
                Modifier
                    .fillMaxHeight(0.5f)
            )
            if (!showResults) {
                AndroidView(
                    factory = { searchResultsView },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            } else {
                CarpoolLayout(carpool = Carpool("Carpool to Basketball Practice", destination = "Evergreen Middle School")) {
                    showCarpoolDetails = true
                }
            }
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
                showResults = false
            },
            leadingIcon = Icons.Filled.Search,
            maxLines = 1
        )
    }
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
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


@Composable
fun CreateCarpoolScreen(
    title: String,
    onTitleChanged: (String) -> Unit,
    onScreenChanged: (Int) -> Unit,
    context: Context
) {
    val searchEngine =
        MapboxSearchSdk.createSearchEngine(SearchEngineSettings(context.getString(R.string.mapbox_access_token)))
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val searchMapView = MapView(context)
    val searchResultsView = SearchResultsView(context)
    var location: Point? = null
    var query by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(true) }
    var repeatingCarpool by remember { mutableStateOf(true) }
    searchResultsView.initialize(
        SearchResultsView.Configuration(
            CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
            searchEngineSettings = searchEngine.settings,
            offlineSearchEngineSettings = OfflineSearchEngineSettings(context.getString(R.string.mapbox_access_token))
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
                        AppCompatResources.getDrawable(context, R.drawable.ic_untitled)
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
                showSearchResults = false
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
    Column(Modifier.fillMaxSize()) {

        CustomTextField(
            hint = "Title of Carpool",
            value = title,
            onValueChanged = onTitleChanged,
            leadingIcon = Icons.Default.Edit,
            maxLines = 1
        )
        Box {
            AndroidView(
                factory = { searchMapView },
                Modifier
                    .fillMaxHeight(0.5f)
            )
            CustomTextField(
                hint = "Choose a destination",
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
                    showSearchResults = true
                },
                leadingIcon = Icons.Filled.Search,
                maxLines = 1
            )
        }
        if (showSearchResults) {
            AndroidView(
                factory = { searchResultsView },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        } else {
            var sun by remember { mutableStateOf(false) }
            var mon by remember { mutableStateOf(false) }
            var tue by remember { mutableStateOf(false) }
            var wed by remember { mutableStateOf(false) }
            var thu by remember { mutableStateOf(false) }
            var fri by remember { mutableStateOf(false) }
            var sat by remember { mutableStateOf(false) }
            val textButtonModifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .padding(8.dp)
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]

            val time = remember { mutableStateOf("") }
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hour: Int, minute: Int ->
                    time.value = ""
                    if (minute < 10) {
                        time.value += "0$minute"
                    } else {
                        time.value += "$minute"
                    }
                    if (hour < 12) {
                        time.value = "$hour:" + time.value + " am"
                    } else if (hour == 12) {
                        time.value = "$hour:" + time.value + " pm"
                    } else if (hour == 0) {
                        time.value = "12:" + time.value + " am"
                    } else {
                        time.value = "${hour - 12}:" + time.value + " pm"
                    }
                }, hour, minute, false
            )
            Column(Modifier.padding(16.dp)) {
                Row {
                    Text(text = "Does this carpool repeat?", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Checkbox(
                        checked = repeatingCarpool,
                        onCheckedChange = { repeatingCarpool = it })
                }
                if (repeatingCarpool) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Text(
                            text = "Sun",
                            modifier = textButtonModifier.clickable { sun = !sun },
                            color = if (sun) FireOpal else Color.Black
                        )
                        Text(
                            text = "Mon",
                            modifier = textButtonModifier.clickable { mon = !mon },
                            color = if (mon) FireOpal else Color.Black
                        )
                        Text(
                            text = "Tue",
                            modifier = textButtonModifier.clickable { tue = !tue },
                            color = if (tue) FireOpal else Color.Black
                        )
                        Text(
                            text = "Wed",
                            modifier = textButtonModifier.clickable { wed = !wed },
                            color = if (wed) FireOpal else Color.Black
                        )
                        Text(
                            text = "Thu",
                            modifier = textButtonModifier.clickable { thu = !thu },
                            color = if (thu) FireOpal else Color.Black
                        )
                        Text(
                            text = "Fri",
                            modifier = textButtonModifier.clickable { fri = !fri },
                            color = if (fri) FireOpal else Color.Black
                        )
                        Text(
                            text = "Sat",
                            modifier = textButtonModifier.clickable { sat = !sat },
                            color = if (sat) FireOpal else Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.size(24.dp))
                    Text(text = "Selected Time: ${time.value}")
                    Spacer(modifier = Modifier.size(16.dp))
                    Button(onClick = {
                        timePickerDialog.show()
                    }) {
                        Text(text = "Choose time")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    //TODO
                    onScreenChanged(0)
                }, Modifier.fillMaxWidth()) {
                    Text(text = "Publish Carpool")
                }
            }
        }
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {

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

@Composable
fun CarpoolDetails(context: Context, buttonState: Int) {
    val mapView = MapView(context)
    val annotationApi = mapView.annotations
    val point = Point.fromLngLat(-122.06090545654, 47.66756439209)
    var joinButtonState by remember { mutableStateOf(buttonState) }
    val pointAnnotationManager = annotationApi.createPointAnnotationManager().apply {
        addClickListener(OnPointAnnotationClickListener {
            val cameraPosition = CameraOptions.Builder()
                .zoom(13.0)
                .center(it.point)
                .build()
            mapView.getMapboxMap().flyTo(cameraPosition)
            return@OnPointAnnotationClickListener true
        })
    }
    pointAnnotationManager.create(
        PointAnnotationOptions().withPoint(point).withIconImage(
            AppCompatResources.getDrawable(context, R.drawable.ic_untitled)
                ?.toBitmap()!!
        )
    )
    val cameraPosition = CameraOptions.Builder()
        .zoom(15.0)
        .center(point)
        .build()
    mapView.getMapboxMap().setCamera(cameraPosition)
    Column(
        Modifier.fillMaxSize())
    {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Carpool to Basketball Practice",
            Modifier.fillMaxWidth(),
            fontSize = 5.em,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxHeight(0.5f)) {
            AndroidView(factory = { mapView }, Modifier.fillMaxHeight())
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.White),) {
                Text(text = "Destination: Evergreen Middle School", Modifier.align(
                    Alignment.Center))
            }
        }
        Column(Modifier.padding(16.dp)) {
            Text(text = "Time: 10:00 am")
            Spacer(Modifier.height(12.dp))
            Text(text = "Repeats on: Mon, Tue, Thu, Fri")
            Spacer(Modifier.height(12.dp))
            if (joinButtonState == 0) {
                Button(onClick = { joinButtonState = 1 }, Modifier.fillMaxWidth()) {
                    Text(text = "Request to join")
                }
            } else if (joinButtonState == 1) {
                Button(
                    onClick = { joinButtonState = 0 },
                    Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = FireOpalDark, contentColor = Color.White)
                ) {
                    Text(text = "Cancel join request")
                }
            } else if (joinButtonState == 2) {
                Button(
                    onClick = { joinButtonState = 0 },
                    Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = FireOpalDark, contentColor = Color.White)
                ) {
                    Text(text = "Leave Carpool")
                }
            }
        }
    }
}