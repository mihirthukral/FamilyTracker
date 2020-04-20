package com.mihir.familytracker


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * A simple [Fragment] subclass.
 */
class SelfTrackingFragment : Fragment(),OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    var lat:Double=77.0
    var lng:Double=77.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_self_tracking, container, false)
        val mapFragment=childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        var email:String?=null
        val activity=activity as ListOnline
        email=activity.email
        lat=activity.lat
        lng=activity.lng
        val sydney = LatLng(lat,lng)
        mMap.addMarker(MarkerOptions().position(sydney).snippet("Your Location").title(email))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,lng),12.0f))
    }


}
