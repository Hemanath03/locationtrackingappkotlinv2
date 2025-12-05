package com.example.locationtrackingappv2.data.repository

import android.content.Context
import com.example.locationtrackingappv2.domain.models.TollMatch
import com.example.locationtrackingappv2.domain.models.TollRoadSegment
import com.example.locationtrackingappv2.domain.repository.ITollRepository
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ITollRepositoryImpl @Inject constructor() : ITollRepository {

    private val tollSegments = mutableListOf<TollRoadSegment>()

    /* -------------------------------------------------------------------------- */
    /*   LOAD TOLL GEOJSON                                                        */
    /* -------------------------------------------------------------------------- */
    override suspend fun loadTolls(context: Context) = withContext(Dispatchers.IO) {

        tollSegments.clear()

        val jsonText = context.assets.open("nsw_toll_roads.geojson")
            .bufferedReader().use { it.readText() }

        val geoJson = JSONObject(jsonText)
        val features = geoJson.getJSONArray("features")

        var idCounter = 0

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            val type = geometry.getString("type")

            when (type) {
                "LineString" -> {
                    val coords = geometry.getJSONArray("coordinates")
                    tollSegments.add(
                        TollRoadSegment(
                            id = idCounter++,
                            points = decodeLineString(coords)
                        )
                    )
                }
                "MultiLineString" -> {
                    val multi = geometry.getJSONArray("coordinates")
                    for (j in 0 until multi.length()) {
                        tollSegments.add(
                            TollRoadSegment(
                                id = idCounter++,
                                points = decodeLineString(multi.getJSONArray(j))
                            )
                        )
                    }
                }
                else -> continue
            }
        }
    }

    private fun decodeLineString(arr: JSONArray): List<LatLng> {
        val list = mutableListOf<LatLng>()
        for (i in 0 until arr.length()) {
            val pair = arr.getJSONArray(i)
            val lng = pair.getDouble(0)
            val lat = pair.getDouble(1)
            list.add(LatLng(lat, lng))
        }
        return list
    }

    /* -------------------------------------------------------------------------- */
    /*   REAL-TIME TOLL CHECKING                                                  */
    /* -------------------------------------------------------------------------- */
    override fun isPointOnToll(lat: Double, lng: Double): Boolean {
        val userPoint = LatLng(lat, lng)

        return tollSegments.any { segment ->
            PolyUtil.isLocationOnPath(
                userPoint,
                segment.points,
                false,
                50.0 // meters threshold
            )
        }
    }

    /* -------------------------------------------------------------------------- */
    /*   TOLLS ALONG A ROUTE (Planned trip)                                       */
    /* -------------------------------------------------------------------------- */
    override fun getTollsIntersecting(routePoints: List<LatLng>): List<TollMatch> {
        val matches = mutableListOf<TollMatch>()

        for (segment in tollSegments) {
            val intersects = PolyUtil.isLocationOnPath(
                // Check entire route path vs toll segment
                routePoints,
                segment.points,
                false,
                50.0
            )

            if (intersects) {
                matches.add(
                    TollMatch(
                        segmentId = segment.id,
                        segmentPoints = segment.points
                    )
                )
            }
        }

        return matches.distinctBy { it.segmentId } // avoid duplicates
    }
}
