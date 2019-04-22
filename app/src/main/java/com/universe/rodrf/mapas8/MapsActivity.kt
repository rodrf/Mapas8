package com.universe.rodrf.mapas8

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.kotlinpermissions.KotlinPermissions
import com.universe.rodrf.mapas8.Adapters.JacarandaAdapter
import com.universe.rodrf.mapas8.entitys.JacarandasDataSet
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.util.jar.Manifest
import kotlin.math.roundToInt

class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var jacarandaAdapter: JacarandaAdapter

    private val currentLocation = LatLng(19.395163, -99.152475)

    private var circleMarker : Circle?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.title=""
        }

        jacarandaAdapter = JacarandaAdapter()

        jacarandaAdapter.setOnItemClickListener { _, jacarandaItem ->
            focusCameraOnPosition(jacarandaItem.location, true, 17f)
            showCardDistance(currentLocation, jacarandaItem.location)

            //drawCircleMark(jacarandaItem) TODO: Ponerlo despues


        }

        rcListJacarandas?.layoutManager = LinearLayoutManager(
            this@MapsActivity,
            RecyclerView.HORIZONTAL,
            false
        )
        rcListJacarandas?.adapter = jacarandaAdapter
    }
    private fun showCardDistance(initLocation: LatLng, destinationLocation:LatLng){
        val firstLocation = Location("")
        val secondLocation = Location("")

        firstLocation.latitude = initLocation.latitude
        firstLocation.longitude = initLocation.longitude

        secondLocation.latitude = destinationLocation.latitude
        secondLocation.longitude = destinationLocation.longitude

        val destanceFromLocation = firstLocation.distanceTo(secondLocation)

        val distanceArray = FloatArray(3)
        Location.distanceBetween(
            initLocation.latitude,
            initLocation.longitude,
            destinationLocation.latitude,
            destinationLocation.longitude,
            distanceArray
        )
        val distanceFromArray = distanceArray[0]
       val cardMessage = if (destanceFromLocation>=1000){
            val distanceKm= destanceFromLocation/1000
            "La distancia entre tu ubicación y la jacaranda es de ${distanceKm} Km"

        }else{
            "La distancia entre tu ubicación y la jacaranda es de ${destanceFromLocation} m"
        }
        tvCardDistance?.text=cardMessage
        cardDistance?.visibility=View.VISIBLE


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_nromal ->{
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.action_terrain ->{
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
            R.id.action_hibrid ->{
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.action_none ->{
                mMap.mapType = GoogleMap.MAP_TYPE_NONE
            }
            R.id.action_search ->{
                initGooglePlaces()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3009) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                    Log.e("PlaceResult", "${place?.toString()}")
                }
                Activity.RESULT_CANCELED -> {
                    Log.e("PlaceCanceled", "No Place selected")
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val statusPlace = data?.let { Autocomplete.getStatusFromIntent(it) }
                    Log.e("AutocompleateError", "${statusPlace?.statusMessage}")
                }
            }
        }
    }

    private fun initGooglePlaces() {
        val fleids = listOf(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG

        )
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY,
            fleids
        ).build(this@MapsActivity)
        startActivityForResult(intent, 3009)

    }

    private fun drawCircleMark(jacarandaItem: LatLng) {
        val circleOptions = CircleOptions()
            .center(jacarandaItem)
            .radius(30.0)
            .strokeWidth(6f)
            .strokeColor(
                getColorIntWithAlpha("#382ab7", 1f)
            )
            .fillColor(getColorIntWithAlpha("#382ab7", 0.5f))

        circleMarker?.remove()
        circleMarker = mMap.addCircle(circleOptions)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener(this@MapsActivity)

        checkForLocationPermission()
        mMap.isMyLocationEnabled = true
        loadMapStyle()
        focusCameraOnPosition(currentLocation)

        //Para los ajustes del mapa
        updateUISettingsMap()

        populateMapJacarandas()
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        Log.d("markerClicked", "${marker?.id}")
        Log.d("markerClicked", "${marker?.title}")
        Log.d("markerClicked", "${marker?.snippet}")
        Log.d("markerClicked", "${marker?.position?.toString()}")

        val tagMarker = marker?.tag as? JacarandaItem

        val positionMark = tagMarker?.id ?: 0
        Log.d("positionScroll", "$positionMark")

        marker?.position?.let {position ->
            drawCircleMark(position)
            showCardDistance(currentLocation, marker?.position)
        }

        rcListJacarandas?.scrollToPosition(positionMark)
        jacarandaAdapter.notifyDataSetChanged()


        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this@MapsActivity)
            .inflate(R.menu.menu_maps, menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun populateMapJacarandas() {
        val json = loadJsonFromAssets("jacarandas.json")
        //Log.v("JsonJacarandas", "$json")
        try {

            val gson = Gson()
            val jarandasJson = gson.fromJson(json, JacarandasDataSet::class.java)
            //Log.i("jsonJacarandas", "$jarandasJson")
            val jacarandasList = jarandasJson?.jacaradas?.mapIndexed { index, featuresItem ->
                val jacarandaslatlong = LatLng(
                    featuresItem?.geometry?.Y ?: 0.0,
                    featuresItem?.geometry?.X ?: 0.0
                )
                JacarandaItem(index, jacarandaslatlong)
            }

            jacarandasList?.let {
                jacarandaAdapter.dataSourceList = it
            }


            jacarandasList?.mapIndexed { index, jacarandaItem ->
                val snippetInfo =
                    "Jacaranda position: ${jacarandaItem.location.latitude}, ${jacarandaItem.location.longitude}"
                val markerOptions = MarkerOptions()
                    .position(jacarandaItem.location)
                    .snippet(snippetInfo)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icn_marker_jacaranda))
                    .title("Marker ${jacarandaItem.id}")
                val marker = mMap.addMarker(markerOptions)

            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("GSON", "GSON cannot handle exception")
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkForLocationPermission() {
        KotlinPermissions.with(this@MapsActivity)
            .permissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .onAccepted { mMap.isMyLocationEnabled = true }
            .onDenied {
                Toast.makeText(this@MapsActivity, "Localización no disponible", Toast.LENGTH_SHORT).show()
            }


    }

    private fun updateUISettingsMap() {
        val uiSettingsMap = mMap.uiSettings
        uiSettingsMap?.apply {
            isMapToolbarEnabled = true
            isZoomControlsEnabled = true
            isCompassEnabled = false
            isZoomGesturesEnabled = true
            isIndoorLevelPickerEnabled = true
            isMyLocationButtonEnabled = true
        }
    }

    private fun focusCameraOnPosition(currentLocation: LatLng, animated: Boolean = false, zoom: Float = 16f) {
        val cameraPosition = CameraPosition.builder()
            .target(currentLocation)
            .zoom(zoom)
            .bearing(180f)
            .tilt(90f)
            .build()

        if (animated) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } else {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

    }

    private fun focusCameraOnPositionAnimated(currentLocation: LatLng) {
        val cameraPosition = CameraPosition.builder()
            .target(currentLocation)
            .zoom(16f)
            .bearing(180f)
            .tilt(90f)
            .build()

    }

    private fun loadMapStyle() {
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.google_map_style))
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
    }

    fun loadJsonFromAssets(path: String): String? {
        return try {
            val inputStream = assets.open(path)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            String(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    }
    fun getColorIntWithAlpha(hexColor: String, alpha: Float): Int =
        Color.parseColor(hexColor.alphaColor(alpha))

    fun String.alphaColor(percentage: Float = 0f): String = if (percentage < 0 || percentage > 1) {
        fail("The percentage need to be a float number between 0.0 and 1.0")
    } else {
        val alphInt = ((percentage * 255) + 0.000001).roundToInt()
        val alphaHex = String.format("%02X", alphInt)
        this.replace("#", "#$alphaHex")
    }

    fun fail(message: String): Nothing {
        throw IllegalArgumentException(message)
    }
}
