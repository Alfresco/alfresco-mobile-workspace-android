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

/**
 * Mark as FacetContext class
 */
@Parcelize
data class FacetContext(
    val facetResponse: FacetResponse?
) : Parcelable {
    companion object {
        /**
         * returns the FaceContext type of data
         */
        fun with(result: ResultSetContext?): FacetContext {
            return FacetContext(facetResponse = FacetResponse.with(result))
        }
    }
}

/**
 * Mark as FacetResponse class
 */
@Parcelize
data class FacetResponse(
    val consistency: Consistency,
    var facetQueries: List<FacetQueries>? = null,
    var facetFields: List<FacetFields>? = null,
    var facetIntervals: List<FacetIntervals>? = null
) : Parcelable {
    companion object {
        /**
         * returns the FacetResponse type of data
         */
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

/**
 * Mark as Consistency class
 */
@Parcelize
data class Consistency(var lastTxId: Int? = null) : Parcelable {
    companion object {
        /**
         * returns the Consistency type of data
         */
        fun with(result: ResponseConsistency?): Consistency {
            return Consistency(result?.lastTxId)
        }
    }
}

/**
 * Mark as FacetFields class
 */
@Parcelize
data class FacetFields(
    val label: String? = null,
    var buckets: List<Buckets>? = null
) : Parcelable {
    companion object {
        /**
         * returns the FacetFields type of data
         */
        fun with(result: ResultBuckets): FacetFields {
            return FacetFields(result.label,
                result.buckets?.map { Buckets.with(it) } ?: emptyList()
            )
        }
    }
}

/**
 * Mark as FacetQueries class
 */
@Parcelize
data class FacetQueries(
    var label: String? = null,
    var filterQuery: String? = null,
    var count: Int? = null
) : Parcelable {
    companion object {
        /**
         * returns the FacetQueries type of data
         */
        fun wth(result: ResultSetContextFacetQueries): FacetQueries {
            return FacetQueries(result.label, result.filterQuery, result.count)
        }
    }
}

/**
 * Mark as FacetIntervals class
 */
@Parcelize
data class FacetIntervals(
    var label: String? = null,
    var type: String? = null,
    var buckets: List<Buckets>? = null
) : Parcelable {
    companion object {
        /**
         * returns the FacetIntervals type of data
         */
        fun with(result: GenericFacetResponse): FacetIntervals {
            return FacetIntervals(result.label, result.type, result.buckets?.map { Buckets.with(it) } ?: emptyList())
        }
    }
}

/**
 * Mark as Buckets class
 */
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
        /**
         * returns the Buckets type of data using ResultBucketsBuckets
         */
        fun with(result: ResultBucketsBuckets): Buckets {
            return Buckets(result.label, result.filterQuery, result.count, result.display)
        }
        /**
         * returns the Buckets type of data using GenericBucket
         */
        fun with(result: GenericBucket): Buckets {
            return Buckets(result.label, result.filterQuery, metrics = result.metrics?.map { Metric.with(it) } ?: emptyList(),
                bucketInfo = result.bucketInfo?.let { BucketInfo.with(it) })
        }
    }
}

/**
 * Mark as BucketInfo class
 */
@Parcelize
data class BucketInfo(
    var start: String? = null,
    var startInclusive: String? = null,
    var end: String? = null,
    var endInclusive: String? = null
) : Parcelable {
    companion object {
        /**
         * returns the BucketInfo type of data
         */
        fun with(result: GenericBucketBucketInfo): BucketInfo {
            return BucketInfo(result.start, result.startInclusive, result.end, result.endInclusive)
        }
    }
}

/**
 * Mark as Metric class
 */
@Parcelize
data class Metric(
    var type: String? = null,
    var value: Value? = null
) : Parcelable {
    companion object {
        /**
         * returns the Metric type of data
         */
        fun with(result: GenericMetric): Metric {
            return Metric(result.type, result.value?.let { Value.with(it) })
        }
    }
}

/**
 * Mark as Value class
 */
@Parcelize
data class Value(
    val count: String? = null
) : Parcelable {
    companion object {
        /**
         * returns the Value type of data
         */
        fun with(result: GenericValue): Value {
            return Value(result.count)
        }
    }
}
