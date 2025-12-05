package com.example.locationtrackingappv2.data

import android.content.Context
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import com.google.android.gms.maps.model.LatLng

@Singleton
class TollRepository @Inject constructor() {

    private val tollPolylines = mutableListOf<List<Pair<Double, Double>>>()

    suspend fun loadTollData(context: Context) = withContext(Dispatchers.IO) {
        //val json = context.assets.open("toll_roads.json").bufferedReader().readText()
        //val layer = GeoJsonLayer(null, JSONObject(json))

        tollPolylines.clear()

        // Extract coordinates
//        for (feature in layer.features) {
//            val geometry = feature.geometry
//            if (geometry.geometryType == "LineString") {
//                val line = geometry.coordinates.map { coord ->
//                    Pair(coord.longitude, coord.latitude)
//                }
//                tollPolylines.add(line)
//            }
//        }
    }

    fun getTollLines(): List<List<Pair<Double, Double>>> = tollPolylines

    fun isNearTollRoad(lat: Double, lng: Double, thresholdMeters: Double = 50.0): Boolean {
        val current = LatLng(lat, lng)

        for (road in tollPolylines) {
            val path = road.map { LatLng(it.second, it.first) }

//            val distance = SphericalUtil.computeDistanceToLine(
//                current,
//                path.first(),
//                path.last()
//            )
//
//            if (distance <= thresholdMeters) return true
        }
        return false
    }
}
