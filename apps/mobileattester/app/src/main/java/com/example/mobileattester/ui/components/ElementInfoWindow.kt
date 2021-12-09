package com.example.mobileattester.ui.components

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.mobileattester.R
import com.example.mobileattester.data.model.Element
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class ElementInfoWindow(
    mapView: MapView?,
    element: Element,
    clickHandler: ElementInfoWindowClickHandler,
) :
    MarkerInfoWindow(R.layout.layout_element_info_window, mapView) {

    init {
        val btn: Button =
            mView.findViewById<View>(R.id.bubble_moreinfo) as Button
        btn.setOnClickListener {
            clickHandler.onElementButtonClicked(element)
        }

        mView.findViewById<TextView>(R.id.bubble_title).text = element.name
    }

    override fun onOpen(item: Any?) {
        super.onOpen(item)
        mView.findViewById<View>(R.id.bubble_moreinfo).visibility =
            View.VISIBLE
    }

    override fun onClose() {
        super.onClose()
        mView.findViewById<View>(R.id.bubble_moreinfo).visibility =
            View.GONE
    }
}

interface ElementInfoWindowClickHandler {
    fun onElementButtonClicked(element: Element)
}