package com.example.fp_v2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.GoogleMap
import com.example.fp_v2.databinding.ActivityLocationPickerBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.type.LatLng


class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding

    private companion object{
        private const val TAG="LOCATION_PICKER_TAG"

        private const val DEFAULT_ZOOM=15

    }

    private var mMap: GoogleMap? =null

    private var mPlacesClient:PlacesClient?=null
    private var mFusedLocationProviderClient:FusedLocationProviderClient?=null


    private var mLastKnownLocation:Location?=null
    private var selectedLatitude:Double?=null
    private var selectedLongtitude:Double?=null

    private var selectedAddress=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.doneLl.visibility= View.GONE

        val mapFragment=supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(this,getString(R.string.my_google_map_api_key))

        mPlacesClient=Places.createClient(this)
        mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        val autocompleteSupportMapFragment=supportFragmentManager.findFragmentById(R.id.autoComplete_fragment) as AutocompleteSupportFragment

        val placeList= arrayOf(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG)

        autocompleteSupportMapFragment.setPlaceFields(listOf(*placeList))

        autocompleteSupportMapFragment.setOnPlaceSelectedListener(object:PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                Log.d(TAG,"onPlaceSelected")

                val id =place.id
                val name=place.name
                val latLng=place.latLng
                selectedLatitude=latLng?.latitude
                selectedLongtitude=latLng?.longitude
                selectedAddress=place.address ?:""

                Log.d(TAG,"onPlaceSelected: id: $id")
                Log.d(TAG,"onPlaceSelected: name: $name")
                Log.d(TAG,"onPlaceSelected: latLng: $latLng")
                Log.d(TAG,"onPlaceSelected: selectedLatitude: $selectedLatitude")
                Log.d(TAG,"onPlaceSelected: selectedLongtitude: $selectedLongtitude")
                Log.d(TAG,"onPlaceSelected: selectedAddress: $selectedAddress")

                addMarker(latLng,name,selectedAddress)
            }

            override fun onError(status: Status) {

            }
        })

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarGpsBtn.setOnClickListener {
            if (isGpsEnabled()){
                requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }else{
                Utils.toast(this,"Lokasyon Bulunamadı")
            }
        }

        binding.doneBtn.setOnClickListener {
            val intent= Intent()
            intent.putExtra("latitude",selectedLatitude)
            intent.putExtra("longtitude",selectedLongtitude)
            intent.putExtra("adress",selectedAddress)
            setResult(Activity.RESULT_OK,intent)

            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG,"onMapReady: ")
        mMap=googleMap

        requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

        mMap!!.setOnMapClickListener { latLng->

            selectedLatitude=latLng.latitude
            selectedLongtitude=latLng.longitude

            Log.d(TAG,"onMapReady: selectedLatitude: $selectedLatitude ")
            Log.d(TAG,"onMapReady: selectedLongtitude: $selectedLongtitude ")

            adressFromLatlng(latLng)

        }
    }

    @SuppressLint("MissingPermission")
    private val requestLocationPermission:ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            Log.d(TAG,"requestLocationPermission: isGranted: $isGranted")

            if (isGranted){
                mMap!!.isMyLocationEnabled=true
                pickCurrentPlace()
            }else{
                Utils.toast(this,"İzin Reddedildi")
            }
        }

    private fun adressFromLatlng(latLng: com.google.android.gms.maps.model.LatLng){

        Log.d(TAG,"adressFromLatlng")

        val geocoder=Geocoder(this)

        try {
            val addresslist=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1)
            val address=addresslist!![0]

            val addressLine=address.getAddressLine(0)
            val subLocality=address.subLocality

            selectedAddress="$addressLine"

            addMarker(latLng,"$subLocality","$addressLine")
        }catch (e:Exception){
            Log.e(TAG,"adressFromLatlng: ",e)
        }

    }

    private fun pickCurrentPlace(){
        Log.d(TAG,"pickCurrentPlace")

        if (mMap==null){
            return
        }
        dedectAndShowDeviceLocationMap()
    }

    @SuppressLint("MissingPermission")
    private fun dedectAndShowDeviceLocationMap(){

        try {
            val locationResult=mFusedLocationProviderClient!!.lastLocation

            locationResult.addOnSuccessListener {location->

                if (location!=null){
                    mLastKnownLocation=location
                    selectedLatitude=location.latitude
                    selectedLongtitude=location.longitude

                    Log.d(TAG,"dedectAndShowDeviceLocationMap:selectedLatitude: $selectedLatitude")
                    Log.d(TAG,"dedectAndShowDeviceLocationMap:selectedLongtitude: $selectedLongtitude")

                    val latLng= com.google.android.gms.maps.model.LatLng(
                        selectedLatitude!!,
                        selectedLongtitude!!
                    )
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM.toFloat()))

                    adressFromLatlng(latLng)
                }

            }.addOnFailureListener {e->

            }

        }catch (e:java.lang.Exception){
            Log.e(TAG,"dedectAndShowDeviceLocationMap: ",e)
        }

    }

    private fun isGpsEnabled():Boolean{

        val lm =getSystemService(Context.LOCATION_SERVICE) as LocationManager

        var gpsEnabled=false
        var networkEnabled=false

        try {
            gpsEnabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }catch (e:java.lang.Exception){
            Log.e(TAG,"isGpsEnabled: ",e)
        }
        try {
            networkEnabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }catch (e:java.lang.Exception){
            Log.e(TAG,"isGpsEnabled: ",e)
        }

        return !(!gpsEnabled && !networkEnabled)
    }

    private fun addMarker(latLng: com.google.android.gms.maps.model.LatLng,title: String,address:String){
        Log.d(TAG,"addMarker latitude: ${latLng.latitude}")
        Log.d(TAG,"addMarker longitude: ${latLng.longitude}")
        Log.d(TAG,"addMarker title: $title")
        Log.d(TAG,"addMarker title: $address")

        mMap!!.clear()

        try {

            val markerOptions=MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("$title")
            markerOptions.snippet("$address")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            mMap!!.addMarker(markerOptions)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))

            binding.doneLl.visibility=View.VISIBLE
            binding.selectedPlaceTv.text=address

        }catch (e:java.lang.Exception){
            Log.e(TAG,"addMarker: ",e)
        }

    }
}