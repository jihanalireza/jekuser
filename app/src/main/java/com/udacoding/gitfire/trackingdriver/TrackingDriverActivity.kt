package com.udacoding.gitfire.trackingdriver

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.udacoding.gitfire.R
import com.udacoding.gitfire.network.myFirebaseDatabase
import com.udacoding.gitfire.trackingdriver.model.Driver
import com.udacoding.gitfire.utama.HomeActivity
import com.udacoding.gitfire.utama.home.model.Booking
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity

class TrackingDriverActivity : AppCompatActivity(), OnMapReadyCallback {

    var mMap :GoogleMap?= null
    var booking: Booking? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_driver)
        booking = intent.getSerializableExtra("booking") as Booking?

        homeAwal.text = booking?.lokasiAwal
        homeTujuan.text = booking?.lokasiTujuan
        homeprice.text = booking?.harga
        homeWaktudistance.text = booking?.jarak

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        homebuttonnext.text = "Home"

        homebuttonnext.onClick {
            startActivity<HomeActivity>()
        }


    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0
        trackDriver()
    }

    private fun trackDriver() {
        myFirebaseDatabase.driverRef().orderByChild("uid").equalTo(booking?.driver).addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (issue in p0.children){
                    val driver = issue.getValue(Driver::class.java)
                    showMarkerDriver(driver)
                }
            }

        })
    }

    private fun showMarkerDriver(driver: Driver?) {

        val positionDriver = LatLng(driver?.lat ?: 0.0,driver?.lon ?: 0.0)
        mMap?.addMarker(MarkerOptions().position(positionDriver).title("Driver Anda"))

        val mapBounds = LatLngBounds.builder()

        mapBounds.include(positionDriver)

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.12).toInt()

        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds.build(), width, height, padding))

    }
}
