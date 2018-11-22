package me.sv.route.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.model.LatLng
import me.sv.route.R
import me.sv.route.databinding.ActivityMapsBinding
import me.sv.route.viewmodel.MapsActivityViewModel
import com.google.android.gms.maps.model.LatLng as LatLngFromServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import kotlinx.android.synthetic.main.activity_maps.*


/**
 * Main and single activity for app
 * No fragments required, because app have only one screen without any navigation
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var viewModel: MapsActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMapsBinding>(this, R.layout.activity_maps)

        viewModel = ViewModelProviders.of(this).get(MapsActivityViewModel::class.java)
        binding.viewmodel = viewModel
        binding.setLifecycleOwner(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initBottomSheet()
    }

    private fun initBottomSheet() {
        val behavior = BottomSheetBehavior.from(ns_bottom_scroll)
        behavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                viewModel?.bottomSheetState?.set(newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    /**
     * Map ready callback
     * we can work wih map from this moment
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        moveToStartPosition()
        mMap.setOnMapClickListener {

            /*reset map when new route creation started*/
            if (viewModel?.allPointsSelected?.get() == true) {
                mMap.clear()
            }

            viewModel?.onPointClick(LatLng(it.latitude, it.longitude)) //pass new point to viewmodel
        }

        /* observe route and markers from viewmodel and update it*/
        viewModel?.getRouteLiveData()?.observe(this, Observer<List<LatLng>> {
            drawRoute(it)
        })

        viewModel?.aPoint?.observe(this, Observer { it ->
            it?.let {
                mMap.addMarker(MarkerOptions().position(LatLngFromServices(it.lat, it.lng)))
            }
        })

        viewModel?.bPoint?.observe(this, Observer { it ->
            it?.let {
                mMap.addMarker(MarkerOptions().position(LatLngFromServices(it.lat, it.lng)))
            }
        })
    }

    private fun moveToStartPosition() {
        if (viewModel?.moveToStartPosition == true) {
            val kyiv = LatLngFromServices(50.45466, 30.5238)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(kyiv, 12f)
            mMap.animateCamera(cameraUpdate)
            viewModel?.moveToStartPosition = false
        }
    }

    /**
     * Draw route on map for corresponding points
     */
    private fun drawRoute(route: List<LatLng>) {
        val line = PolylineOptions()

        for (point in route) {
            line.add(LatLngFromServices(point.lat, point.lng))
        }

        line.width(18f).color(resources.getColor(R.color.color_route_line))
        mMap.addPolyline(line)
    }
}
