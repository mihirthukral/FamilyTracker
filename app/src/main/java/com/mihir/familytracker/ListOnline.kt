package com.mihir.familytracker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mihir.familytracker.model.Tracking
import com.mihir.familytracker.model.User
import java.lang.NullPointerException
import java.lang.StringBuilder

class ListOnline : AppCompatActivity(),GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener,
                LocationListener{
    var lat=0.0
    var lng=0.0
    var email:String?=null
    var nameOfGroup:String?=null
    lateinit var onlineRef:DatabaseReference
    lateinit var checkIfExist:DatabaseReference
    lateinit var currentUserRef:DatabaseReference
    lateinit var sharedPreferences: SharedPreferences
    lateinit var counterRef:DatabaseReference
    lateinit var frame:FrameLayout
    lateinit var locations:DatabaseReference

    lateinit var adapter: FirebaseRecyclerAdapter<User,ListOnlineViewHolder>
    lateinit var groups:DatabaseReference
    lateinit var listOnline:RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager

    lateinit var toolbar:androidx.appcompat.widget.Toolbar

    var MY_PERMISSION_REQUEST_CODE=7171
    var PLAY_SERVICES_RES_REQUEST=7172
    var UPDATE_INTERVAL=5000L
    var FASTEST_INTERVAL=3000L
    var DISTANCE=10F
    lateinit var mLocationRequest:LocationRequest
    lateinit var mGoogleApiClient:GoogleApiClient
     var mLastLocation:Location?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_online)
        frame=findViewById(R.id.frame)
        listOnline=findViewById(R.id.listOnline)
        listOnline.setHasFixedSize(true)
        layoutManager=LinearLayoutManager(this)
        listOnline.layoutManager=layoutManager
        sharedPreferences=getSharedPreferences("FamilyTracker",Context.MODE_PRIVATE)
        toolbar=findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title="Family tracker"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sharedPreferences.edit().putString("nameOfGroup",null).apply()
        groups=FirebaseDatabase.getInstance().getReference("groups")
        locations=FirebaseDatabase.getInstance().getReference("Locations")
        onlineRef=FirebaseDatabase.getInstance().getReference().child(".info/connected")
        checkIfExist=FirebaseDatabase.getInstance().getReference("Locations")
        counterRef=FirebaseDatabase.getInstance().getReference("lastOnline")
        currentUserRef=FirebaseDatabase.getInstance().getReference("lastOnline")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
            ){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),MY_PERMISSION_REQUEST_CODE)
    }
        else{
        if(checkPlayServices()){
            buildGoogleApiClient()
            createLocationRequest()
          //  displayLocation()
        }
    }
  //  setUpSystem()
     //   updateList()

        if(intent!=null) {
            email = intent.getStringExtra("user")
            lat = intent.getDoubleExtra("lat", 0.0)
            lng = intent.getDoubleExtra("lng", 0.0)
        }
        listOnline.visibility=View.GONE
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame,SelfTrackingFragment())
                .commit()


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            MY_PERMISSION_REQUEST_CODE->if(grantResults.size>0 && grantResults.get(0)==PackageManager.PERMISSION_GRANTED){
                if(checkPlayServices()){
                    buildGoogleApiClient()
                    createLocationRequest()
                    displayLocation()

                }
            }
        }
    }

    private fun displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if(mLastLocation!=null){
           // Toast.makeText(this,"In not null",Toast.LENGTH_LONG).show()
            locations.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(Tracking(FirebaseAuth.getInstance().currentUser?.email,FirebaseAuth.getInstance().currentUser?.uid,mLastLocation?.latitude.toString(),mLastLocation?.longitude.toString()))
        }
        else{
            //Toast.makeText(this,"Couldn't get the location",Toast.LENGTH_LONG).show()
         //   Toast.makeText(this,"In null",Toast.LENGTH_LONG).show()
            Log.d("TEST","Couldn't load location")
        }
    }

    private fun createLocationRequest() {
        mLocationRequest= LocationRequest()
        mLocationRequest.interval=UPDATE_INTERVAL
        mLocationRequest.fastestInterval=FASTEST_INTERVAL
        mLocationRequest.smallestDisplacement=DISTANCE
        mLocationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient=GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient.connect()
    }

    private fun checkPlayServices(): Boolean {
        var resultCode=GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if(resultCode!=ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RES_REQUEST).show()
            }
            else{
                Toast.makeText(this,"This device is not supportted",Toast.LENGTH_SHORT).show()
                finish()
            }
            return false
        }
        return true
    }

     fun updateList() {
        frame.visibility=View.GONE
        listOnline.visibility=View.VISIBLE

        val options=FirebaseRecyclerOptions.Builder<User>()
            .setQuery(locations, User::class.java)
            .setLifecycleOwner(this)
            .build()
         adapter=object : FirebaseRecyclerAdapter<User,ListOnlineViewHolder>(options){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ListOnlineViewHolder {
                return ListOnlineViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_layout,parent,false))
            }

            override fun onBindViewHolder(p0: ListOnlineViewHolder, p1: Int, p2: User) {
                p0.txtEmail.text=p2.email
                p0.llContent.setOnClickListener {
                   // Toast.makeText(this@ListOnline,"YOO BOYS",Toast.LENGTH_SHORT).show()

                        if(!p2.email.equals(FirebaseAuth.getInstance().currentUser!!.email)){
                         //   Toast.makeText(this@ListOnline,"BOYS",Toast.LENGTH_SHORT).show()
                            val map=Intent(this@ListOnline,MapTracking::class.java)
                            map.putExtra("email",p2.email)
                            map.putExtra("lat",mLastLocation?.latitude)
                            map.putExtra("lng",mLastLocation?.longitude)
                            startActivity(map)
                        }

                }
            }

        }
        listOnline.adapter=adapter
        adapter.notifyDataSetChanged()

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_invite->{
                if(nameOfGroup==null){
                    Toast.makeText(this,"First join a group",Toast.LENGTH_SHORT).show()
                }
                else{
                    val pm=packageManager
                    try{
                        val intent=Intent(Intent.ACTION_SEND)
                        intent.setType("text/plain")
                        val text="Download Family Tracker app\n And Join my group using this group name : "+nameOfGroup
                        val info=pm.getPackageInfo("com.whatsapp",PackageManager.GET_META_DATA)
                        intent.setPackage("com.whatsapp")
                        intent.putExtra(Intent.EXTRA_TEXT,text)
                        startActivity(Intent.createChooser(intent,"Share with"))
                    }
                    catch(e:PackageManager.NameNotFoundException){
                        Toast.makeText(this,"WhatsApp not installed",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.action_join-> {

                val alert=AlertDialog.Builder(this)
                alert.setTitle("Join an existing group")
                alert.setMessage("Enter group Name")
                val input= EditText(this)
                alert.setView(input)
                alert.setPositiveButton("OK") { dialog, which ->
                    val nameOfGroup=input.text.toString()
                    //Toast.makeText(this,nameOfGroup,Toast.LENGTH_SHORT).show()
                    this.nameOfGroup=nameOfGroup

                    sharedPreferences.edit().putString("nameOfGroup",nameOfGroup)

                 //  groups.child(nameOfGroup)
                   // Toast.makeText(this,groups.toString(),Toast.LENGTH_SHORT).show()
                    groups.child(nameOfGroup).addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.value==null){
                                Toast.makeText(this@ListOnline,"Group Name doesn't exist",Toast.LENGTH_SHORT).show()
                                dialog.cancel()
                            }
                            else{
                                supportActionBar?.title=nameOfGroup
                                sharedPreferences.edit().putString("nameOfGroup",nameOfGroup).apply()
                                locations=FirebaseDatabase.getInstance().getReference(nameOfGroup)
                                counterRef=FirebaseDatabase.getInstance().getReference(nameOfGroup)
//                                onlineRef=FirebaseDatabase.getInstance().getReference(nameOfGroup).child(".info/connected")
                                if(checkPlayServices()){
                                    buildGoogleApiClient()
                                    createLocationRequest()
                                    displayLocation()

                                }
                               // setUpSystem()
                                updateList()
                               /* counterRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .setValue(FirebaseAuth.getInstance().currentUser!!.email?.let {
                                        User(
                                            it,
                                            "Online"
                                        )
                                    })*/
                            }
                        }

                    })

                }
                alert.setNegativeButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }
                alert.show()
}
            R.id.action_logout->{
                supportActionBar?.title="Family Tracker"

                AlertDialog.Builder(this)
                    .setTitle("You want to Logout?")
                    .setMessage("Click OK to Logout")
                    .setPositiveButton("OK"){ dialogInterface: DialogInterface, i: Int ->
                            sharedPreferences.edit().clear()
                        val intent=Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Cancel"){ dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.cancel()
                    }
                   .create()
                   .show()

            }
            R.id.action_left-> {

                val alert=AlertDialog.Builder(this)
                alert.setTitle("Are you sure")
                alert.setMessage("Exit Group")
                alert.setPositiveButton("OK"){ dialogInterface: DialogInterface, i: Int ->
                    supportActionBar?.title="Family Tracker"
                    try {
                        val remove = FirebaseDatabase.getInstance().getReference(nameOfGroup!!)
                        remove.child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue()
                        locations = FirebaseDatabase.getInstance().getReference("Locations")
                        locations.child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue()
                        locations = FirebaseDatabase.getInstance().getReference("Dustbin")
                        currentUserRef.removeValue()

                        if (intent != null) {
                            email = intent.getStringExtra("user")
                            lat = intent.getDoubleExtra("lat", 0.0)
                            lng = intent.getDoubleExtra("lng", 0.0)
                        }
                        sharedPreferences.edit().putString("nameOfGroup",null).apply()
                        listOnline.visibility = View.GONE
                        frame.visibility = View.VISIBLE
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.frame, SelfTrackingFragment())
                            .commit()
                    }
                    catch (e:NullPointerException){
                        Toast.makeText(this,"First Join a Group",Toast.LENGTH_SHORT).show()
                    }
                }
                alert.setNegativeButton("Cancel"){ dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.cancel()
                }
                alert.show()

                }
            R.id.action_create->{

                val alert=AlertDialog.Builder(this)
                alert.setTitle("Create a group")
                alert.setMessage("Enter Name to create a group")
                val input= EditText(this)
                alert.setView(input)
                alert.setPositiveButton("OK") { dialog, which ->
                    val nameOfGroup=input.text.toString()
                    var flag=0
                    //Toast.makeText(this,nameOfGroup,Toast.LENGTH_SHORT).show()
                    groups.child(nameOfGroup).addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.value==null){
                                supportActionBar?.title=nameOfGroup
                                flag=1
                                groups.child(nameOfGroup).setValue(nameOfGroup)
                                val sharedPreferences=getSharedPreferences("FamilyTracker", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("nameOfGroup",nameOfGroup)
                                locations=FirebaseDatabase.getInstance().getReference(nameOfGroup)
                               // onlineRef=FirebaseDatabase.getInstance().getReference(nameOfGroup)
                                if(checkPlayServices()){
                                    buildGoogleApiClient()
                                    createLocationRequest()
                                    displayLocation()

                                }
                                //setUpSystem()
                                updateList()
                                /*counterRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .setValue(FirebaseAuth.getInstance().currentUser!!.email?.let {
                                        User(
                                            it,
                                            "Online"
                                        )
                                    })*/
                            }
                            else{
                                Toast.makeText(this@ListOnline,"Group Name Already Exist",Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
                    if(flag==1)
                        this.nameOfGroup=nameOfGroup
                }
                alert.setNegativeButton("Cancel") { dialog, which ->
                    dialog.cancel()
                }
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConnected(p0: Bundle?) {
        displayLocation()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        ){
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this)
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(p0: Location) {
        mLastLocation=p0
        displayLocation()
    }

   fun email():String?{
        return email
    }
    fun lat():Double{
        return lat
    }
    fun lng():Double{
        return lng
    }

    override fun onBackPressed() {

    }
}
