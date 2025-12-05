package com.example.locationtrackingappv2.data.remote

import android.content.Context
import com.example.locationtrackingappv2.BuildConfig
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GoogleMapsRemoteDataSource @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) {

    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Base URLs
    private val PLACES_BASE = "https://places.googleapis.com/v1/"
    private val ROUTES_BASE = "https://routes.googleapis.com/"

    /* -------------------------------------------------------------------------- */
    /*   AUTOCOMPLETE (POST: places:autocomplete)                                */
    /* -------------------------------------------------------------------------- */
    suspend fun fetchAutocomplete(body: Map<String, Any>): String {
        val url = PLACES_BASE + "places:autocomplete"

        val headers = Headers.Builder()
            .add("X-Goog-Api-Key", BuildConfig.MAPS_API_KEY)
            .add("X-Goog-FieldMask", "*") // exactly like your Flutter repo
            .build()

        val requestBody = gson.toJson(body).toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .headers(headers)
            .post(requestBody)
            .build()

        return client.await(request)
    }

    /* -------------------------------------------------------------------------- */
    /*   PLACE DETAILS (GET: places/{placeId}?fields=*)                           */
    /* -------------------------------------------------------------------------- */
    suspend fun fetchPlaceDetails(placeId: String, sessionToken: String): String {
        val url = "${PLACES_BASE}places/$placeId?fields=*&sessionToken=$sessionToken"

        val headers = Headers.Builder()
            .add("X-Goog-Api-Key", BuildConfig.MAPS_API_KEY)
            .add("X-Goog-FieldMask", "*")
            .build()

        val request = Request.Builder()
            .url(url)
            .headers(headers)
            .get()
            .build()

        return client.await(request)
    }

    /* -------------------------------------------------------------------------- */
    /*   ROUTES API v2 (POST: directions/v2:computeRoutes)                        */
    /* -------------------------------------------------------------------------- */
    suspend fun computeRoutes(body: Map<String, Any>): String {
        val url = ROUTES_BASE + "directions/v2:computeRoutes"

        val headers = Headers.Builder()
            .add("X-Goog-Api-Key", BuildConfig.MAPS_API_KEY)
            .add(
                "X-Goog-FieldMask",
                "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"
            )
            .build()

        val requestBody = gson.toJson(body).toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .headers(headers)
            .post(requestBody)
            .build()

        return client.await(request)
    }
}

/* -------------------------------------------------------------------------- */
/*   OkHttp await() EXTENSION (Used above for suspend-friendly calls)        */
/* -------------------------------------------------------------------------- */
suspend fun OkHttpClient.await(request: Request): String =
    suspendCancellableCoroutine { cont ->
        val call = newCall(request)

        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isCancelled) return
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    cont.resumeWithException(IOException("HTTP ${response.code}: $body"))
                } else {
                    cont.resume(body)
                }
            }
        })
    }
