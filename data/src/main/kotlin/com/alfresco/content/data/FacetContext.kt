package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.content.models.GenericBucket
import com.alfresco.content.models.GenericBucketBucketInfo
import com.alfresco.content.models.GenericFacetResponse
import com.alfresco.content.models.GenericMetric
import com.alfresco.content.models.GenericValue
import com.alfresco.content.models.ResponseConsistency
import com.alfresco.content.models.ResultBuckets
import com.alfresco.content.models.ResultBucketsBuckets
import com.alfresco.content.models.ResultSetContext
import com.alfresco.content.models.ResultSetContextFacetQueries
import kotlinx.parcelize.Parcelize

@Parcelize
data class FacetContext(
    val facetResponse: FacetResponse?
) : Parcelable {

    companion object {

        fun with(result: ResultSetContext?): FacetContext {
            return FacetContext(facetResponse = FacetResponse.with(result))
        }
    }
}

@Parcelize
data class FacetResponse(
    val consistency: Consistency,
    var facetQueries: List<FacetQueries>? = null,
    var facetFields: List<FacetFields>? = null,
    var facetIntervals: List<FacetIntervals>? = null
) : Parcelable {
    companion object {
        fun with(result: ResultSetContext?): FacetResponse {
            return FacetResponse(
                consistency = Consistency.with(result?.consistency),
                facetQueries = result?.facetQueries?.map { FacetQueries.wth(it) } ?: emptyList(),
                facetFields = result?.facetsFields?.map { FacetFields.with(it) } ?: emptyList(),
                facetIntervals = result?.facets?.map { FacetIntervals.with(it) } ?: emptyList()
            )
        }
    }
}

@Parcelize
data class Consistency(var lastTxId: Int? = null) : Parcelable {
    companion object {
        fun with(result: ResponseConsistency?): Consistency {
            return Consistency(result?.lastTxId)
        }
    }
}

@Parcelize
data class FacetFields(
    val label: String? = null,
    var buckets: List<Buckets>? = null
) : Parcelable {
    companion object {
        fun with(result: ResultBuckets): FacetFields {
            return FacetFields(result.label,
                result.buckets?.map { Buckets.with(it) } ?: emptyList()
            )
        }
    }
}

@Parcelize
data class FacetQueries(
    var label: String? = null,
    var filterQuery: String? = null,
    var count: Int? = null
) : Parcelable {
    companion object {
        fun wth(result: ResultSetContextFacetQueries): FacetQueries {
            return FacetQueries(result.label, result.filterQuery, result.count)
        }
    }
}

@Parcelize
data class FacetIntervals(
    var label: String? = null,
    var type: String? = null,
    var buckets: List<Buckets>? = null
) : Parcelable {
    companion object {
        fun with(result: GenericFacetResponse): FacetIntervals {
            return FacetIntervals(result.label, result.type, result.buckets?.map { Buckets.with(it) } ?: emptyList())
        }
    }
}

@Parcelize
data class Buckets(
    var label: String? = null,
    var filterQuery: String? = null,
    var count: Int? = null,
    var display: String? = null,
    var metrics: List<Metric>? = null,
    var bucketInfo: BucketInfo? = null
) : Parcelable {
    companion object {
        fun with(result: ResultBucketsBuckets): Buckets {
            return Buckets(result.label, result.filterQuery, result.count, result.display)
        }

        fun with(result: GenericBucket): Buckets {
            return Buckets(result.label, result.filterQuery, metrics = result.metrics?.map { Metric.with(it) } ?: emptyList(),
                bucketInfo = result.bucketInfo?.let { BucketInfo.with(it) })
        }
    }
}

@Parcelize
data class BucketInfo(
    var start: String? = null,
    var startInclusive: String? = null,
    var end: String? = null,
    var endInclusive: String? = null
) : Parcelable {
    companion object {
        fun with(result: GenericBucketBucketInfo): BucketInfo {
            return BucketInfo(result.start, result.startInclusive, result.end, result.endInclusive)
        }
    }
}

@Parcelize
data class Metric(
    var type: String? = null,
    var value: Value? = null
) : Parcelable {
    companion object {
        fun with(result: GenericMetric): Metric {
            return Metric(result.type, result.value?.let { Value.with(it) })
        }
    }
}

@Parcelize
data class Value(
    val count: String? = null
) : Parcelable {
    companion object {
        fun with(result: GenericValue): Value {
            return Value(result.count)
        }
    }
}
