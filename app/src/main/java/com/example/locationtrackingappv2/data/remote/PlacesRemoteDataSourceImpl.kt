//package com.example.locationtrackingappv2.data.remote
//
//class PlacesRepositoryImpl @Inject constructor(
//    @ApplicationContext private val ctx: Context
//) : PlacesRepository {
//
//    private val placesClient by lazy {
//        Places.initialize(ctx, BuildConfig.MAPS_API_KEY)
//        Places.createClient(ctx)
//    }
//
//    override suspend fun getPredictions(query: String, country: String): List<PlacePrediction> =
//        suspendCancellableCoroutine { cont ->
//            val req = FindAutocompletePredictionsRequest.builder()
//                .setQuery(query)
//                .setCountries(country)
//                .build()
//
//            placesClient.findAutocompletePredictions(req)
//                .addOnSuccessListener { resp ->
//                    val list = resp.autocompletePredictions.map {
//                        PlacePrediction(it.placeId, it.getFullText(null).toString())
//                    }
//                    cont.resume(list) {}
//                }
//                .addOnFailureListener { e ->
//                    cont.resume(emptyList()) {}
//                }
//        }
//
//    override suspend fun fetchPlaceLatLng(placeId: String): PlaceLatLng? =
//        suspendCancellableCoroutine { cont ->
//            val fields = listOf(Place.Field.LAT_LNG)
//            val request = FetchPlaceRequest.newInstance(placeId, fields)
//            placesClient.fetchPlace(request)
//                .addOnSuccessListener { r ->
//                    r.place.latLng?.let {
//                        cont.resume(PlaceLatLng(it.latitude, it.longitude)) {}
//                    } ?: cont.resume(null) {}
//                }
//                .addOnFailureListener {
//                    cont.resume(null) {}
//                }
//        }
//}
