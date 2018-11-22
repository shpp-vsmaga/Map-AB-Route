package me.sv.route.view.binding

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*
import me.sv.route.R
import me.sv.route.utils.METERS_IN_KM
import java.util.concurrent.TimeUnit

@BindingAdapter("app:distance")
fun setDistance(textView: TextView, distance: Long){
    if (distance < METERS_IN_KM){
        textView.text = textView.context.getString(R.string.distance_m, distance)
    } else {
        textView.text = textView.context.getString(R.string.distance_km, distance.toFloat() / METERS_IN_KM)
    }
}

@BindingAdapter("app:setTime")
fun setTime(textView: TextView, timeSeconds: Long){
    val hours = TimeUnit.SECONDS.toHours(timeSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(timeSeconds - TimeUnit.HOURS.toSeconds(hours))
    if (hours > 0){
        textView.text = textView.context.getString(R.string.time_h_m, hours, minutes)
    } else {
        textView.text = textView.context.getString(R.string.time_m, minutes)
    }
}

@BindingAdapter("app:setPoint")
fun setPoint(textView: TextView, point : LatLng?){

   if (point != null){
       textView.text = String.format("%.4f, %.4f", point.lng, point.lat)
   } else {
       textView.text = textView.context.getString(R.string.tap_on_map_to_select_point)
   }
}

@BindingAdapter("bottomSheetState")
fun setState(v: View, bottomSheetBehaviorState: Int) {
    val viewBottomSheetBehavior = BottomSheetBehavior.from(v)
    viewBottomSheetBehavior.state = bottomSheetBehaviorState
}

@BindingAdapter("app:setArrow")
fun setArrow(imageView: AppCompatImageView, state : Int){
    when (state) {
        BottomSheetBehavior.STATE_EXPANDED -> {
            imageView.setImageResource(R.drawable.ic_arrow_drop_down)
        }
        else -> imageView.setImageResource(R.drawable.ic_arrow_drop_up)
    }
}