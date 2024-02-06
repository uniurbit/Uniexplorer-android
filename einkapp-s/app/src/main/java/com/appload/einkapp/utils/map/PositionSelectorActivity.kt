package com.appload.einkapp.utils.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.appload.einkapp.R
import com.appload.einkapp.databinding.ActivityPositionSelectBinding
import com.appload.einkapp.utils.PermissionHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class PositionSelectorActivity : AppCompatActivity(), OnMapReadyCallback {

    private var lastLocation: LatLng = MapConstants.milano
    private lateinit var location: LatLng
    private lateinit var geocoder: Geocoder
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPositionSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPositionSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setListeners()
        geocoder = Geocoder(this)
    }

    private fun setListeners() {
        binding.confirmButton.setOnClickListener {
            if (this::location.isInitialized) {
                val intent = Intent().apply {
                    putExtra(MapConstants.EXTRA_COORDINATES, location)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        binding.cancel.setOnClickListener {
            finish()
        }

        binding.myPosition.setOnClickListener {
            showLocation()
        }
    }

    private fun showLocation() {
        mMap.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                    LatLng(lastLocation.latitude, lastLocation.longitude),
                    16f,
                    0f,
                    0f
                )
            )
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnCameraIdleListener {
            updateSelectedAddress()
        }

        mMap.setOnCameraMoveStartedListener {
            moveCamera()
        }

        if (intent.hasExtra(COORDINATES)) {
            val intentLocation = intent.extras?.get(COORDINATES)
            if (intentLocation != null)
                lastLocation = intentLocation as LatLng
            else
                selectMyPosition()
        } else {
            selectMyPosition()
        }

        showLocation()
    }

    @SuppressLint("MissingPermission")
    private fun selectMyPosition() {
        if (PermissionHelper.hasPositionPermissions(this)) {
            binding.myPosition.show()
            val locationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null)
                lastLocation = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false
        }
        showLocation()
    }

    private fun moveCamera() {
        binding.positionPicker.animate()
            .translationY(-16f)
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .start()
    }

    private fun updateSelectedAddress() {
        val lat = mMap.cameraPosition.target.latitude
        val lon = mMap.cameraPosition.target.longitude
        location = LatLng(lat, lon)
        val address = geocoder.getFromLocation(lat, lon, 1)
        if (address?.isNotEmpty() == true) {
            binding.coordinates.text = address[0].getAddressLine(0)
            Log.e("SELECTED ADDRESS", address[0].getAddressLine(0))
            binding.positionPicker.animate()
                .translationY(0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200)
                .start()

            if (binding.confirmButton.visibility == View.GONE) {
                binding.confirmButton.visibility = View.VISIBLE
                binding.confirmButton.scaleX = 0f
                binding.confirmButton.scaleY = 0f
                binding.confirmButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(250)
                    .start()
            }
        }
    }

    companion object {
        private const val COORDINATES = "COORDINATES"
        fun newIntent(context: Context, coordinates: LatLng?, radius: Int = 35): Intent {
            return Intent(context, PositionSelectorActivity::class.java).apply {
                putExtra(COORDINATES, coordinates)
            }
        }
    }

}