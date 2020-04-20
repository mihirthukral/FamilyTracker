package com.mihir.familytracker

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mihir.familytracker.model.Tracking
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_map_tracking.view.*
import java.text.DecimalFormat


class MapTracking : AppCompatActivity(), OnMapReadyCallback {
    private var latitude: Double = 0.0
    private var longitude:Double=0.0
    private lateinit var mMap: GoogleMap
    var tempMap:GoogleMap?=null
    var f=0
    lateinit var locations:DatabaseReference
    var lat=0.0
    var lng=0.0
    var email="abc@gmail.com"
    lateinit var clear:Button
    lateinit var fram_map:FrameLayout
    lateinit var btn_draw_State:Button
    var Is_MAP_Moveable=false
    var arrayList= arrayListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_tracking)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val sharedPreferences=getSharedPreferences("FamilyTracker",Context.MODE_PRIVATE)
       val ans=sharedPreferences.getString("nameOfGroup","yoo")
        locations=FirebaseDatabase.getInstance().getReference(ans!!)
        clear=findViewById(R.id.clear)
        if(intent!=null){
            email=intent.getStringExtra("email")
            lat=intent.getDoubleExtra("lat",0.0)
            lng=intent.getDoubleExtra("lng",0.0)
            // Toast.makeText(this,email+" "+lat+" "+lng,Toast.LENGTH_LONG).show()
        }
        clear.setOnClickListener {
            mMap.clear()
            if(tempMap!=null) {
                mMap = tempMap!!
            }
            loadLocationForThisUser(email)
            val intent= Intent(this,MapTracking::class.java)
            intent.putExtra("email",email)
            intent.putExtra("lat",lat)
            intent.putExtra("lng",lng)
            startActivity(intent)
            finish()
        }
        if(intent!=null){
            email=intent.getStringExtra("email")
            lat=intent.getDoubleExtra("lat",0.0)
            lng=intent.getDoubleExtra("lng",0.0)
           // Toast.makeText(this,email+" "+lat+" "+lng,Toast.LENGTH_LONG).show()
        }
            loadLocationForThisUser(email)

        btn_draw_State=findViewById(R.id.btn_draw_State)
        fram_map=findViewById(R.id.fram_map)
        btn_draw_State.setOnClickListener {
            Is_MAP_Moveable=!Is_MAP_Moveable
            if(btn_draw_State.text.equals("Free Draw")) {
                fram_map.visibility = View.VISIBLE
                if(f==0)
                tempMap=mMap
                f++
                btn_draw_State.text="Stop Free Draw"
            }
            else{
                fram_map.visibility=View.GONE
                btn_draw_State.text="Free Draw"
            }


        }
    fram_map.setOnTouchListener(View.OnTouchListener { v, event ->
        val x = event.x
        val y = event.y

        val x_co = Math.round(x)
        val y_co = Math.round(y)

        val projection = mMap.projection
        val x_y_points = Point(x_co, y_co)

        val latLng = mMap.projection.fromScreenLocation(x_y_points)
        latitude = latLng.latitude

        longitude = latLng.longitude

        val eventaction = event.action
        when (eventaction) {
            MotionEvent.ACTION_DOWN -> {
                // finger touches the screen
                arrayList.add(LatLng(latitude, longitude))
                // finger moves on the screen
                arrayList.add(LatLng(latitude, longitude))
                // finger leaves the screen
                Draw_Map()
            }
            MotionEvent.ACTION_MOVE -> {
                arrayList.add(LatLng(latitude, longitude))
                Draw_Map()
            }
            MotionEvent.ACTION_UP -> Draw_Map()

        }
        return@OnTouchListener Is_MAP_Moveable

    })
    }

     fun Draw_Map() {
         val poly=mMap.addPolygon(PolygonOptions().addAll(arrayList).strokeColor(Color.BLUE).fillColor(Color.CYAN).strokeWidth(7.0f))
    }

    private fun loadLocationForThisUser(email: String) {
       // val user_location:Query=locations.orderByChild("email").equalTo(email)
       // Toast.makeText(this,email,Toast.LENGTH_LONG).show()

        locations.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (i in p0.children) {
                    var tracking: Tracking? = null
                    Log.d("yoo", i.toString())
                    try {
                        val tracking: Tracking? = i.getValue(Tracking::class.java)

                    //  if (!tracking?.email.equals(email)) {
                    if (tracking != null){
                    val friendLocation =
                        LatLng(tracking!!.lat!!.toDouble(), tracking.lng!!.toDouble())

                    val currentUser = Location("")
                    currentUser.latitude = lat
                    currentUser.longitude = lng

                    val friend = Location("")
                    friend.latitude = tracking.lat!!.toDouble()
                    friend.longitude = tracking.lng!!.toDouble()

                    val mark = mMap.addMarker(
                        MarkerOptions()
                            .position(friendLocation)
                            .title(tracking.email)
                            .snippet(
                                "Distance " + DecimalFormat("#.#").format(
                                    distance(
                                        currentUser,
                                        friend
                                    )
                                ) + "km"
                            )
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_BLUE
                                )
                            )

                    )




                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lat, lng),
                            12.0f
                        )
                    )

                    //  }
                }
                    } catch (e: DatabaseException) {

                    }
            }
            }

        })
    }

    private fun distance(currentUser: Location, friend: Location):Double {
        val theta=currentUser.longitude-friend.longitude
        var dist=Math.sin(deg2rad(currentUser.latitude)) * Math.sin(deg2rad(friend.latitude))*Math.cos(deg2rad(currentUser.latitude))*Math.cos(deg2rad(friend.latitude))*Math.cos(deg2rad(theta))
        dist=Math.acos(dist)
        dist=rad2deg(dist)
        dist=dist*60*1.1515

        return dist

    }

    private fun rad2deg(dist: Double): Double {
        return (dist*180 / Math.PI)
    }

    private fun deg2rad(latitude: Double): Double {
        return  (latitude * Math.PI /180.0)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

}
