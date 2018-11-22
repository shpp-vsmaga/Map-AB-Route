package me.sv.route.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
    }

    /**
     * Map ready callback
     * we can work wih map from this moment
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener {

            /*reset map when new route creation started*/
            if (viewModel?.allPointsSelected?.get() == true) {
                mMap.clear()
            }

            viewModel?.onPointClick(LatLng(it.latitude, it.longitude)) //pass new point to viewmodel
            mMap.addMarker(MarkerOptions().position(it))

        }


        viewModel?.getRouteLiveData()?.observe(this, Observer<List<LatLng>> {
            drawRoute(it)
        })
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
