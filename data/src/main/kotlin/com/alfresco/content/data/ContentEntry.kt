
package com.alfresco.content.data

import android.os.Parcelable
import com.alfresco.process.models.ContentDataEntry
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

/**
 * Marked as ContentEntry class
 */

@Parcelize
data class ContentEntry(
    val id: Int = 0,
    val name: String = "",
    val created: ZonedDateTime? = null,
    val userDetails: UserGroupDetails? = null,
    val isRelatedContent: Boolean? = false,
    val isContentAvailable: Boolean? = false,
    val hasLink: Boolean? = false,
    val mimeType: String? = "",
    val simpleType: String? = "",
    val previewStatus: String? = "",
    val thumbnailStatus: String? = "",
) : Parcelable {

    companion object {
        /**
         * return the ContentEntry obj after converting the data from ContentDataEntry obj
         */

        fun with(data: ContentDataEntry): ContentEntry {
            return ContentEntry(
                id = data.id ?: 0,
                name = data.name ?: "",
                created = data.created,
                userDetails = data.createdBy?.let { UserGroupDetails.with(it) } ?: UserGroupDetails(),
                isRelatedContent = data.relatedContent,
                isContentAvailable = data.contentAvailable,
                mimeType = data.mimeType,
                simpleType = data.simpleType,
                previewStatus = data.previewStatus,
                thumbnailStatus = data.thumbnailStatus,
            )
        }
    }
}
