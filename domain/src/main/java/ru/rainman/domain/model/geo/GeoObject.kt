package ru.rainman.domain.model.geo

sealed interface MetaDataContainer

data class GeoObject(
    val name: String?,
    val descriptionText: String?,
    val geometry: List<Geometry>,
    val boundingBox: BoundingBox?,
    val attributionMap: Map<String, Attribution>,
    val metadataContainer: MetaDataContainer?
)

data class BusinessObjectData(
    val oid: String,
    val name: String,
    val address: Address,
    val categories: List<Category>,
    val phones: List<Phone>,
    val workingHours: WorkingHours?,
    val links: List<SearchLink>,
    val distance: LocalizedValue,
    val chains: List<Chain>,
    val unreliable: Boolean?,
    val seoName: String?,
    val shortName: String?,
    val properties: Map<String, String>?,
    val indoorLevel: String?
) : MetaDataContainer

data class SelectionMetaData(
    val id: String,
    val layerId: String
) : MetaDataContainer

data class ToponymObjectData(
    val address: Address,
    val formerName: String?,
    val point: Point,
    val id: String?
) : MetaDataContainer

data class SearchResult(
    val point: Point,
    val shorAddress: String,
    val name: String
)

data class Attribution(
    val attributionAuthor: Author?,
    val attributionLink: String?
)

data class Author(
    val name: String,
    val uri: String?,
    val email: String?,
)

data class Image(
    val urlTemplate: String,
    val sizes: List<ImageSize>,
    val tags: List<String>
)

data class ImageSize(
    val size: String,
    val width: Int?,
    val height: Int?
)

sealed interface Geometry

data class BoundingBox(
    val southWest: Point,
    val northEast: Point
) : Geometry

data class Circle(
    val center: Point,
    val radius: Float
) : Geometry

data class MultiPolygon(
    val polygons: List<Polygon>
) : Geometry

data class Polygon(
    val outerRing: LinearRing,
    val innerRings: List<LinearRing>
) : Geometry

data class Point(
    val latitude: Double,
    val longitude: Double
) : Geometry

data class Polyline(
    val points: List<Point>
) : Geometry

data class LinearRing(
    val points: List<Point>
)


data class Phone(
    val type: PhoneType,
    val formattedNumber: String,
    val info: String?,
    val country: String?,
    val prefix: String?,
    val ext: String?,
    val number: String?
)

enum class PhoneType {
    FAX, PHONE, PHONE_FAX
}

data class WorkingHours(
    val text: String?,
    val availabilities: List<Availability>,
    val state: State?
)

data class State(
    val isOpenNow: Boolean?,
    val text: String?,
    val shortText: String?,
    val tags: List<String>
)

data class Availability(
    val days: Int,
    val timeRanges: List<TimeRange>

)

data class TimeRange(
    val isTwentyFourHours: Boolean?,
    val from: Int?,
    val to: Int?
)


data class Chain(
    val id: String,
    val name: String
)

data class LocalizedValue(
    val value: Double?,
    val text: String?
)

data class SearchLink(
    val aref: String?,
    val tag: String?
)

data class Address(
    val formattedAddress: String,
    val additionalInfo: String?,
    val postalCode: String?,
    val countryCode: String?,
    val addressComponents: AddressComponent
)


data class AddressComponent(
    val airport: String?,
    val apartment: String?,
    val area: String?,
    val country: String?,
    val district: String?,
    val entrance: String?,
    val building: String?,
    val hydro: String?,
    val level: String?,
    val locality: String?,
    val metroStation: String?,
    val other: String?,
    val province: String?,
    val railwayStation: String?,
    val region: String?,
    val route: String?,
    val station: String?,
    val street: String?,
    val unknown: String?,
    val vegetation: String?
)

data class Category(
    val name: String,
    val categoryClass: String?,
    val tags: List<String>
)