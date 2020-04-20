package com.mihir.familytracker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener


class SelfTracking : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap
    var lat:Double=77.0
    var lng:Double=77.0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_tracking)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        var email:String?=null
        if(intent!=null){
            lat=intent.getDoubleExtra("lat",77.0)
            lng=intent.getDoubleExtra("lng",77.0)
            email= intent.getStringExtra("user")
        }
       // Toast.makeText(this, "$lat $lng",Toast.LENGTH_SHORT).show()

        val sydney = LatLng(lat,lng)
        mMap.addMarker(MarkerOptions().position(sydney).snippet("Your Location").title(email))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,lng),12.0f))
    }


}


