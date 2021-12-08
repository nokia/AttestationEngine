package com.example.mobileattester.ui.components

import android.view.View
import android.widget.Button
import android.widget.Toast
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class ElementInfoWindow(mapView: MapView?) :
    MarkerInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, mapView) {

    init {
        val btn: Button =
            mView.findViewById<View>(org.osmdroid.bonuspack.R.id.bubble_moreinfo) as Button
        btn.setOnClickListener { view ->
            Toast.makeText(view.context, "Button clicked", Toast.LENGTH_LONG).show()
        }
    }

    override fun onOpen(item: Any?) {
        super.onOpen(item)
        mView.findViewById<View>(org.osmdroid.bonuspack.R.id.bubble_moreinfo).visibility =
            View.VISIBLE
    }
}