package me.sv.route.viewmodel

import android.app.Application
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import me.sv.route.R
import me.sv.route.utils.AVERAGE_WALKING_SPEED_METERS_IN_S
import me.sv.route.utils.copy


class MapsActivityViewModel(application: Application) : AndroidViewModel(application) {

    private var oldAPoint: LatLng? = null
    private var oldBPoint: LatLng? = null

    var aPoint: MutableLiveData<LatLng?> = MutableLiveData()
    var bPoint: MutableLiveData<LatLng?> = MutableLiveData()

    private val apiKey = getApplication<Application>().resources.getString(R.string.google_maps_key)
    private var geoApiContext: GeoApiContext? = null

    private val routeLiveData: MutableLiveData<List<LatLng>> = MutableLiveData()

    val distanceLiveData = MutableLiveData<Long>()
    val timeLiveData = MutableLiveData<Long>()
    val allPointsSelected: MutableLiveData<Boolean> = MutableLiveData()
    var moveToStartPosition = true
    val bottomSheetState : MutableLiveData<Int> = MutableLiveData()

    init {
        geoApiContext = GeoApiContext.Builder()
            .apiKey(apiKey)
            .build()

        allPointsSelected.value = false
        bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN
    }

    fun getRouteLiveData(): LiveData<List<LatLng>> = routeLiveData

    /**
     * Dispatch Create Route button click in UI
     */
    fun onCreateRouteClick() {
        if (needToLoadNewRoute()) {
            loadNewRoute()
        }
    }

    /**
     * Check if required to load new direction
     * @return false if new points equals to old and directions were already loaded
     */
    private fun needToLoadNewRoute(): Boolean {
        return aPoint.value?.equals(oldAPoint) == false
                || bPoint.value?.equals(oldBPoint) == false
    }

    /**
     * Load directions for two point using Google Directions API
     */
    private fun loadNewRoute() {
        GlobalScope.launch(Default) {
            // make request in IO thread to prevent lock of Main thread
            val directionsResult = async {
                DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.WALKING)
                    .origin(aPoint.value)
                    .destination(bPoint.value)
                    .await()
            }.await()

            GlobalScope.launch(Main) {
                // update UI in main thread
                resetOldPoint()

                directionsResult.routes?.let {
                    var newDistance = 0L
                    if (it.isNotEmpty()) {
                        for (leg in it[0].legs) {
                            newDistance += leg.distance.inMeters
                        }
                        distanceLiveData.value = newDistance
                        timeLiveData.value = getWalkingTimeForDistance(newDistance)
                        routeLiveData.value = it[0].overviewPolyline.decodePath()
                    }
                }
            }
        }
    }


    private fun resetOldPoint() {
        oldAPoint = aPoint.value?.copy()
        oldBPoint = bPoint.value?.copy()
    }

    /**
     * Calculate the approximate time to walk this distance
     * @param distance distance in meters
     *
     */
    private fun getWalkingTimeForDistance(distance: Long): Long {
        return (distance / AVERAGE_WALKING_SPEED_METERS_IN_S).toLong()

    }

    /**
     * Dispatch click on Map and assign A and B point in corresponding order
     */
    fun onPointClick(point: LatLng) {

        when {
            (aPoint.value == null) -> {
                aPoint.value = point
                allPointsSelected.value = false
            }
            (aPoint.value != null && bPoint.value == null) -> {
                bPoint.value = point
                allPointsSelected.value = true
            }
            (aPoint.value != null && bPoint.value != null) -> {
                aPoint.value = point
                bPoint.value = null
                allPointsSelected.value = false
            }

        }

        resetCounters()
    }

    /**
     * Resets Distance and Time counters in UI
     * when started new route creation
     */
    private fun resetCounters() {
        timeLiveData.value = 0
        distanceLiveData.value = 0
    }

}