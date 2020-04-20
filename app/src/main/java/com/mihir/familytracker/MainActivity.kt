package com.mihir.familytracker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class MainActivity : AppCompatActivity() {
    var lat=0.0
    var lng=0.0
    lateinit var btnLogin:Button
    var LOGIN_PERMISSION=1000
    lateinit var currentLocation:Location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
      val REQUEST_CODE=101
    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        btnLogin=findViewById(R.id.btnLogin)
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_CODE)
            return
        }
        var task=fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener(OnSuccessListener {
            if(it!=null){
                currentLocation=it
                lat=currentLocation.latitude
                lng=currentLocation.longitude
            }
        })
        btnLogin.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(Collections.singletonList(AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(true).build())).build(),LOGIN_PERMISSION

            )
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==LOGIN_PERMISSION){
            startNewActivity(resultCode,data)
        }
    }

    private fun startNewActivity(resultCode: Int, data: Intent?) {
        if(resultCode== Activity.RESULT_OK) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ){
            val user = FirebaseAuth.getInstance().currentUser!!.email
            //  Toast.makeText(this,user.toString(),Toast.LENGTH_LONG).show()

            var intent = Intent(this, ListOnline::class.java)
                intent.putExtra("lat",lat)
                intent.putExtra("lng",lng)
            intent.putExtra("user", user)
            startActivity(intent)
            finish()
        }
            else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
                }
                else{
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
                }
            }
        }
        else{
            Toast.makeText(this,"Login Failed !!!",Toast.LENGTH_SHORT).show()
        }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1->if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()
                    val user = FirebaseAuth.getInstance().currentUser!!.email
                    var intent = Intent(this, ListOnline::class.java)
                    intent.putExtra("lat",lat)
                    intent.putExtra("lng",lng)
                    intent.putExtra("user", user)
                    startActivity(intent)
                    finish()
                }
                else{
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
                        finishAffinity()
                    }
                    else
                        finish()
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater=menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return true
    }


}

