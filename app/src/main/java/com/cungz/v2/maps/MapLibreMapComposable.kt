package com.cungz.v2.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.maplibre.android.compose.MapLibreMap
import org.maplibre.android.compose.MapOptions
import org.maplibre.android.compose.camera.CameraPositionState
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraPosition

@Composable
fun MapLibreMapComposable() {
    MapLibreMap(
        modifier = Modifier,
        cameraPosition = CameraPositionState(
            position = CameraPosition.fromLatLngZoom(LatLng(-6.2, 106.8), 12.0)
        ),
        mapOptions = MapOptions()
    )
}
