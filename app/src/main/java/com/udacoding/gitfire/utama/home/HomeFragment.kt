package com.udacoding.gitfire.utama.home


import android.graphics.Camera
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.udacoding.gitfire.R
import com.udacoding.gitfire.utils.GPSTracker
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import android.support.v4.app.ShareCompat.IntentBuilder
import android.content.Intent
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import org.jetbrains.anko.support.v4.toast
import android.R.attr.data
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nandohusni.baggit.network.NetworkModule
import com.udacoding.gitfire.network.myFirebaseDatabase
import com.udacoding.gitfire.utama.home.model.Booking
import com.udacoding.gitfire.utama.home.model.directions.ResultDirections
import com.udacoding.gitfire.utils.ChangeFormat
import com.udacoding.gitfire.utils.Constan
import com.udacoding.gitfire.utils.DirectionMapsV2
import com.udacoding.gitfire.waiting.WaitingDriverActivity
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.yesButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment(), OnMapReadyCallback {

    var latAwal: Double? = null
    var lonAwal: Double? = null

    var latAkhir: Double? = null
    var lonAkhir: Double? = null

    var distanceText: String? = null
    var mMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync(this)
        homeAwal.onClick {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(activity)
                startActivityForResult(intent, 1)
            } catch (e: GooglePlayServicesRepairableException) {
                // TODO: Handle the error.
            } catch (e: GooglePlayServicesNotAvailableException) {
                // TODO: Handle the error.
            }

        }

        homeTujuan.onClick {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(activity)
                startActivityForResult(intent, 2)
            } catch (e: GooglePlayServicesRepairableException) {
                // TODO: Handle the error.
            } catch (e: GooglePlayServicesNotAvailableException) {
                // TODO: Handle the error.
            }
        }

        homebuttonnext.onClick {
            if (homeAwal.text.isNotEmpty() && homeTujuan.text.isNotEmpty()){
                bookingDriver()
            }else{
                toast("silahkan pilih dari mana dan kemana?")
            }
        }

    }

    private fun bookingDriver() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val currentTime = Calendar.getInstance().time
        val tanggalNow = currentTime.toString()
        val booking = Booking()
        booking.tanggal = tanggalNow
        booking.lokasiAwal = homeAwal.text.toString()
        booking.lokasiTujuan = homeTujuan.text.toString()
        booking.latAwal = latAwal
        booking.lonAwal = lonAwal
        booking.latTujuan = latAkhir
        booking.lonTujuan = lonAkhir
        booking.harga = homeprice.text.toString()
        booking.status = 1
        booking.driver = ""
        booking.uid = uid
        booking.jarak = distanceText

        val key = myFirebaseDatabase.firebaseDatabase().reference.push().key

        val query = myFirebaseDatabase.bookingRef().orderByChild("uid").equalTo(uid)
        query.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var bookingonprocess = 0
               for (data in p0.children){
                    val item = data.getValue(Booking::class.java)
                   if (item?.status == 1){
                       bookingonprocess += 1
                   }

               }

                if (bookingonprocess == 0){
                    myFirebaseDatabase.bookingRef().child(key ?: "").setValue(booking)

                    startActivity<WaitingDriverActivity>(Constan.Key to key.toString())
                }else{
                    toast("Tunggu sedang menunggu driver take...")
                }
            }
        });

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//
//        mapView.onDestroy()
//    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), 12
            )
        } else {
            showgps()
        }
    }

    private fun showgps() {

        val gps = context?.let { GPSTracker(it) }
        if (gps?.canGetLocation ?: true) {
            latAwal = gps?.latitude
            lonAwal = gps?.longitude

            val namaHomeAwal = showName(latAwal, lonAwal)

//            set hasil conversi ke text home awal
            homeAwal.text = namaHomeAwal

            showMarker(latAwal, lonAwal, namaHomeAwal)
        }
    }

    private fun showName(lat: Double?, lon: Double?): String? {
        val geo = Geocoder(context, Locale.getDefault())

        val nameLocation = geo.getFromLocation(lat ?: 0.0, lon ?: 0.0, 1)

        val resultName = nameLocation[0].getAddressLine(0)
        val countryName = nameLocation[0].countryName
        val cityName = nameLocation[0].locale

//        yang di ambil nama jalan saja
        return resultName
    }

    private fun showMarker(lat: Double?, lon: Double?, namaLokasi: String?) {
        var latlang = LatLng(lat ?: 0.0, lon ?: 0.0)
//        Create Marker
        mMap?.addMarker(MarkerOptions().position(latlang).title(namaLokasi))
//        setting camera and zoom
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlang, 16f))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 12) {
            showgps()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(activity, data);

                val alamatLokasi = place.address.toString()
                val namaLokasi = place.name.toString()
                latAwal = place.latLng.latitude
                lonAwal = place.latLng.longitude


                homeAwal.text = alamatLokasi

                mMap?.clear()

                if(homeTujuan.text.length > 0 ){
                    val nameAkhir = showName(latAkhir,lonAkhir)
                    showMarker(latAkhir,lonAkhir,nameAkhir)
                }

                showMarker(latAwal, lonAwal, alamatLokasi)

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(activity, data);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(activity, data);

                val alamatLokasi = place.address.toString()
                val namaLokasi = place.name.toString()
                latAkhir = place.latLng.latitude
                lonAkhir = place.latLng.longitude


                homeTujuan.text = alamatLokasi
                if(homeAwal.text.length > 0){
                    mMap?.clear()

                    val nameAwal = showName(latAwal,lonAwal)
                    showMarker(latAwal,lonAwal,nameAwal)
                }

                showMarker(latAkhir, lonAkhir, alamatLokasi)
                route()
                setBound()

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(activity, data);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private fun setBound() {
//        mMap?.setPadding(200,0,200,0)
        val coor1 = LatLng(latAwal?:0.0,lonAwal?:0.0)
        val coor2 = LatLng(latAkhir?:0.0,lonAkhir?:0.0)

        val bound = LatLngBounds.builder()

        bound.include(coor1)
        bound.include(coor2)

        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bound.build(),150))
    }

    fun route(){
        val lokasiAwal = "$latAwal,$lonAwal"
        val lokasiAkhir = "$latAkhir,$lonAkhir"

        NetworkModule.getService().getRoute(lokasiAwal,lokasiAkhir, activity?.getString(R.string.google_maps_key) ?: "")
            .enqueue(object : Callback<ResultDirections>{
                override fun onFailure(call: Call<ResultDirections>, t: Throwable) {

                }

                override fun onResponse(call: Call<ResultDirections>, response: Response<ResultDirections>) {
                    val route = response.body()?.routes
                    val route0 = route?.get(0)

                    val overview = route0?.overviewPolyline

                    val points = overview?.points

                    val legs = route0?.legs?.get(0)
                    val durationText = legs?.duration?.text
                    distanceText =  legs?.distance?.text

                    val distanceValue = legs?.distance?.value?.toDouble() ?: 0.0
                    val distanceKm = distanceValue / 1000

                    val harga = ChangeFormat.toRupiahFormat2((distanceKm * 2000).toString())

                    val estimasi = "$durationText ($distanceText)"

                    homeprice.text = "Rp $harga"
                    homeWaktudistance.text = estimasi

                    mMap?.let { points?.let { it1 -> DirectionMapsV2.gambarRoute(it, it1) } }
                }

            })
    }
}
