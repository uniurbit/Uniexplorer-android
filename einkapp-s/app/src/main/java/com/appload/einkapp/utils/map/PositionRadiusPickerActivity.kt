package com.appload.einkapp.utils.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.appload.einkapp.R
import com.appload.einkapp.databinding.ActivityPositionRadiusPickerBinding
import com.appload.einkapp.utils.PermissionHelper
import com.appload.einkapp.utils.Utils.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.slider.Slider
import com.google.maps.android.SphericalUtil
import java.math.BigDecimal
import java.text.DecimalFormat


class PositionRadiusPickerActivity : AppCompatActivity(), OnMapReadyCallback {
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var lastLocation: LatLng = LatLng(45.4642, 9.1900)
    private lateinit var location: LatLng
    private lateinit var geocoder: Geocoder
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPositionRadiusPickerBinding
    private var sliderVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPositionRadiusPickerBinding.inflate(layoutInflater)
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
                showSlider()
            }
        }

        binding.cancel.setOnClickListener {
            finish()
        }

        binding.myPosition.setOnClickListener {
            showLocation()
        }

        binding.slider.addOnChangeListener { slider, value, fromUser ->
            val km = (value / 1000)
            val formatter = DecimalFormat("0.#")
            binding.selectedRadius.text =
                getString(R.string.km_placeholder, formatter.format(km))
            updateRadius()
        }

        binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                fitRadius()
            }

        })

        binding.cancelRadiusButton.setOnClickListener {
            hideSlider()
        }

        binding.confirmRadiusButton.setOnClickListener {
            val intent = Intent().apply {
                putExtra(MapConstants.EXTRA_COORDINATES, location)
                putExtra(MapConstants.EXTRA_RADIUS, binding.slider.value.toInt())
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }


    private fun updateRadius() {
        if (circle == null) {
            circle = mMap.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(binding.slider.value.toDouble())
                    .strokeWidth(1.dp.toFloat())
                    .strokeColor(getColor(R.color.blue))
                    .fillColor(getColor(R.color.light_blue))
            )
        } else if (circle != null && circle!!.center != location) {
            circle?.center = location
        }
        marker?.isVisible = true
        circle?.isVisible = true
        circle?.radius = binding.slider.value.toDouble()
    }


    private fun showSlider() {
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (address?.isNotEmpty() == true) {
            setMarker()
            updateRadius()
            binding.slider.value = intent.getIntExtra(RADIUS, 5).toFloat() * 1000
            fitRadius()
            binding.selectedAddress.text = address.first().getAddressLine(0)
            binding.positionPicker.visibility = View.GONE
            binding.selectedPositionContainer.animate()
                .translationY((binding.selectedPositionContainer.height + 24.dp.toFloat()))
                .setDuration(150)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    binding.selectedPositionContainer.visibility = View.GONE
                    binding.selectRadiusContainer.animate()
                        .translationY(0f)
                        .setDuration(150)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                    sliderVisible = true
                }
                .start()
            binding.selectRadiusContainer.visibility = View.INVISIBLE
            binding.selectRadiusContainer.translationY =
                (binding.selectRadiusContainer.height + 24.dp.toFloat())
            binding.selectRadiusContainer.visibility = View.VISIBLE
        }
    }

    private fun setMarker() {
        marker = mMap.addMarker(
            MarkerOptions()
                .position(location)
        )
    }

    private fun fitRadius() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(calculateBounds(), 0))
    }

    private fun calculateBounds(): LatLngBounds {
        val radius = (binding.slider.value.toDouble()) * 1.6
        val northEast: LatLng = SphericalUtil.computeOffset(
            SphericalUtil.computeOffset(location, radius, 90.0),
            radius,
            0.0
        )
        val southWest: LatLng = SphericalUtil.computeOffset(
            SphericalUtil.computeOffset(location, radius, 270.0),
            radius,
            180.0
        )

        return LatLngBounds(southWest, northEast)
    }

    private fun hideSlider() {
        marker?.isVisible = false
        circle?.isVisible = false
        binding.positionPicker.visibility = View.VISIBLE
        binding.selectRadiusContainer.animate()
            .translationY((binding.selectRadiusContainer.height + 24.dp.toFloat()))
            .setDuration(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.selectRadiusContainer.visibility = View.GONE
                binding.selectedPositionContainer.animate()
                    .translationY(0f)
                    .setDuration(150)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                sliderVisible = false
            }
            .start()
        binding.selectedPositionContainer.visibility = View.INVISIBLE
        binding.selectedPositionContainer.translationY =
            (binding.selectedPositionContainer.height + 24.dp.toFloat())
        binding.selectedPositionContainer.visibility = View.VISIBLE
    }

    private fun showLocation() {
        mMap.animateCamera(
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
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.setOnCameraIdleListener {
            if (!sliderVisible)
                updateSelectedAddress()
        }

        mMap.setOnCameraMoveStartedListener {
            cameraIsMoving()
        }

        if (intent.hasExtra(COORDINATES)) {
            lastLocation = intent.extras?.get(COORDINATES) as LatLng
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

    }

    private fun cameraIsMoving() {
        if (!sliderVisible)
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
        private const val RADIUS = "RADIUS"
        fun newIntent(
            context: Context,
            coordinates: LatLng?,
            radius: BigDecimal = BigDecimal(5)
        ): Intent {
            return Intent(context, PositionRadiusPickerActivity::class.java).apply {
                if (coordinates != null)
                    putExtra(COORDINATES, coordinates)
                putExtra(RADIUS, radius)
            }
        }
    }

}