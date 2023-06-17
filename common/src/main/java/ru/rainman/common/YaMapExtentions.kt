package ru.rainman.common

import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.search.BusinessObjectMetadata
import com.yandex.mapkit.search.Phone as YaPhone
import com.yandex.mapkit.search.ToponymObjectMetadata
import ru.rainman.domain.model.geo.*
import com.yandex.mapkit.geometry.Point as YaPoint
import com.yandex.mapkit.GeoObject as YaGeoObject
import com.yandex.mapkit.Attribution as YaAttribution
import com.yandex.mapkit.search.Address as YaAddress
import com.yandex.mapkit.geometry.Circle as YaCircle
import com.yandex.mapkit.geometry.BoundingBox as YaBoundingBox
import com.yandex.mapkit.geometry.MultiPolygon as YaMultiPolygon
import com.yandex.mapkit.geometry.Polygon as YaPolygon
import com.yandex.mapkit.geometry.Polyline as YaPolyline
import com.yandex.mapkit.geometry.LinearRing as YaLinearRing
import com.yandex.mapkit.geometry.Geometry as YaGeometry
import com.yandex.mapkit.search.SearchLink as YaSearchLink
import com.yandex.mapkit.search.TimeRange as YaTimeRange
import com.yandex.mapkit.search.Availability as YaAvailability
import com.yandex.mapkit.search.State as YaState
import com.yandex.mapkit.search.WorkingHours as YaWorkingHours
import com.yandex.mapkit.search.Category as YaCategory

fun YaPoint.toModel() = Point(latitude, longitude)

fun YaGeoObject.toModel() = GeoObject(
    name = name,
    descriptionText = descriptionText,
    geometry = geometry.map(YaGeometry::toModel),
    boundingBox = boundingBox?.toModel(),
    attributionMap = attributionMap.map { entry -> entry.key to entry.value.toModel() }.toMap(),
    metadataContainer = metadataContainer.toModel()
)

fun YaAttribution.toModel() = Attribution(
    attributionAuthor = this.author?.toModel(),
    attributionLink = link?.href
)

fun YaAttribution.Author.toModel() = Author(
    name = name,
    uri = uri,
    email = email,
)

fun ToponymObjectMetadata.toModel() = ToponymObjectData(
    address = address.toModel(),
    formerName = formerName,
    point = balloonPoint.toModel(),
    id = id
)

fun com.yandex.runtime.any.Collection.toModel(): MetaDataContainer? {
    return getItem(BusinessObjectMetadata::class.java)?.toModel()
        ?: getItem(ToponymObjectMetadata::class.java)?.toModel()
        ?: getItem(GeoObjectSelectionMetadata::class.java)?.toModel()
}

fun GeoObjectSelectionMetadata.toModel() = SelectionMetaData(
    id = id,
    layerId = layerId
)

fun BusinessObjectMetadata.toModel() = BusinessObjectData(
    oid = oid,
    name = name,
    address = address.toModel(),
    categories = categories.map(YaCategory::toModel),
    phones = phones.map(YaPhone::toModel),
    workingHours = workingHours?.let(YaWorkingHours::toModel),
    links = links.map(YaSearchLink::toModel),
    distance = LocalizedValue(
        value = distance?.value,
        text = distance?.text
    ),
    chains = chains.map { chain ->
        Chain(
            id = chain.id,
            name = chain.name
        )
    },
    unreliable = unreliable,
    seoName = seoname,
    shortName = shortName,
    properties = properties?.items?.associate { prop ->
        prop.key to prop.value
    },
    indoorLevel = indoorLevel
)

fun YaAddress.toModel() = Address(
    formattedAddress = formattedAddress,
    additionalInfo = additionalInfo,
    postalCode = postalCode,
    countryCode = countryCode,
    addressComponents = AddressComponent(
        airport = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.AIRPORT)
        }?.name,
        apartment = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.APARTMENT)
        }?.name,
        area = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.AREA)
        }?.name,
        country = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.COUNTRY)
        }?.name,
        district = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.DISTRICT)
        }?.name,
        entrance = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.ENTRANCE)
        }?.name,
        building = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.HOUSE)
        }?.name,
        hydro = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.HYDRO)
        }?.name,
        level = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.LEVEL)
        }?.name,
        locality = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.LOCALITY)
        }?.name,
        metroStation = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.METRO_STATION)
        }?.name,
        other = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.OTHER)
        }?.name,
        province = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.PROVINCE)
        }?.name,
        railwayStation = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.RAILWAY_STATION)
        }?.name,
        region = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.REGION)
        }?.name,
        route = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.ROUTE)
        }?.name,
        station = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.STATION)
        }?.name,
        street = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.STREET)
        }?.name,
        unknown = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.UNKNOWN)
        }?.name,
        vegetation = components.find { component ->
            component.kinds.contains(YaAddress.Component.Kind.VEGETATION)
        }?.name,
    )
)

fun YaPhone.toModel() = Phone(
    type = PhoneType.valueOf(type.name),
    formattedNumber = formattedNumber,
    info = info,
    country = country,
    prefix = prefix,
    ext = ext,
    number = number
)

fun YaCategory.toModel() = Category(
    name = name,
    categoryClass = categoryClass,
    tags = tags
)

fun YaWorkingHours.toModel() = WorkingHours(
    text = text,
    availabilities = availabilities.map(YaAvailability::toModel),
    state = state?.let(YaState::toModel)
)

fun YaState.toModel() = State(
    isOpenNow = isOpenNow,
    text = text,
    shortText = shortText,
    tags = tags
)

fun YaTimeRange.toModel() = TimeRange(
    isTwentyFourHours = isTwentyFourHours,
    from = from,
    to = to
)

fun YaAvailability.toModel() = Availability(
    days = days,
    timeRanges = timeRanges.map(YaTimeRange::toModel)
)

fun YaSearchLink.toModel() = SearchLink(
    aref = aref,
    tag = tag
)

fun YaGeometry.toModel(): Geometry {
    return boundingBox?.toModel()
        ?: circle?.toModel()
        ?: multiPolygon?.toModel()
        ?: point?.toModel()
        ?: polygon?.toModel()
        ?: polyline?.toModel()!!
}

fun Geometry.toGeometry(): YaGeometry {
    return when (this) {
        is BoundingBox -> YaGeometry.fromBoundingBox(toBoundingBox())
        is Circle -> YaGeometry.fromCircle(toCircle())
        is MultiPolygon -> YaGeometry.fromMultiPolygon(toMultiPolygon())
        is Point -> YaGeometry.fromPoint(toPoint())
        is Polygon -> YaGeometry.fromPolygon(toPolygon())
        is Polyline -> YaGeometry.fromPolyline(toPolyline())
    }
}



fun Point.toPoint(): YaPoint {
    return YaPoint(
        latitude,
        longitude
    )
}

fun YaPolyline.toModel() = Polyline(
    points = points.map(YaPoint::toModel)
)

fun Polyline.toPolyline() = YaPolyline(
    points.map(Point::toPoint)
)

fun YaPolygon.toModel() = Polygon(
    outerRing = outerRing.toModel(),
    innerRings = innerRings.map(YaLinearRing::toModel)
)

fun YaLinearRing.toModel() = LinearRing(
    points = points.map(YaPoint::toModel)
)

fun LinearRing.toLinearRings() = YaLinearRing(
    points.map(Point::toPoint)
)

fun Polygon.toPolygon() = YaPolygon(
    outerRing.toLinearRings(),
    innerRings.map(LinearRing::toLinearRings)
)

fun YaMultiPolygon.toModel() = MultiPolygon(
    polygons = polygons.map(YaPolygon::toModel)
)

fun MultiPolygon.toMultiPolygon() = YaMultiPolygon(
    polygons.map(Polygon::toPolygon)
)

fun YaBoundingBox.toModel() = BoundingBox(
    southWest = southWest.toModel(),
    northEast = northEast.toModel()
)

fun BoundingBox.toBoundingBox(): YaBoundingBox {
    return YaBoundingBox(
        southWest.toPoint(),
        northEast.toPoint()
    )
}

fun YaCircle.toModel() = Circle(
    center = center.toModel(),
    radius = radius
)

fun Circle.toCircle() = YaCircle(
    center.toPoint(),
    radius
)

fun List<Geometry>.findPointOrNull(): Point {
    return find { it is Point } as Point
}

fun GeoObject.toSearchResult() = SearchResult(
    point = geometry.findPointOrNull(),
    name = name ?: "",
    shorAddress = when (metadataContainer) {
        is BusinessObjectData -> (metadataContainer as BusinessObjectData).address.formattedAddress
        is ToponymObjectData -> (metadataContainer as ToponymObjectData).address.formattedAddress
        else -> ""
    }
)


