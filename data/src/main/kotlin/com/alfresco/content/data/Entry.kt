package com.alfresco.content.data

import android.os.Parcel
import android.os.Parcelable
import com.alfresco.content.models.DeletedNode
import com.alfresco.content.models.Favorite
import com.alfresco.content.models.FavoriteTargetNode
import com.alfresco.content.models.Node
import com.alfresco.content.models.NodeChildAssociation
import com.alfresco.content.models.PathInfo
import com.alfresco.content.models.ResultNode
import com.alfresco.content.models.SharedLink
import com.alfresco.content.models.Site
import com.alfresco.content.models.SiteRole
import java.time.Instant
import java.time.ZonedDateTime
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler

@Parcelize
@TypeParceler<ZonedDateTime, DateParceler>
data class Entry(
    val id: String,
    val type: Type,
    val title: String,
    val subtitle: String?,
    val mimeType: String?,
    val modified: ZonedDateTime? = null,
    val isPartial: Boolean = false,
    val isFavorite: Boolean = false,
    val canDelete: Boolean = false,
    val isTrashed: Boolean = false,
    val otherId: String? = null
) : Parcelable {

    val stableId: String
        get() = id + if (isFavorite) 1 else 0

    enum class Type {
        File,
        Folder,
        Site,
        FileLink,
        FolderLink,
        Group,
        Unknown;

        companion object {
            fun from(value: String): Type {
                when (value) {
                    "cm:content" -> return File
                    "cm:folder" -> return Folder
                    "st:sites" -> return Folder // Special folder for admins
                    "st:site" -> return Site
                    "app:filelink" -> return FileLink
                    "app:folderlink" -> return FolderLink
                }
                return Unknown
            }
        }
    }

    enum class SortOrder {
        ByModifiedDate,
        Default
    }

    companion object {
        fun with(node: Node): Entry {
            return Entry(
                node.id,
                Type.from(node.nodeType),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType,
                node.modifiedAt,
                node.isFavorite == null || node.allowableOperations == null,
                node.isFavorite ?: false,
                node.allowableOperations?.contains("delete") ?: false
            )
        }

        fun with(result: ResultNode): Entry {
            return Entry(
                result.id,
                Type.from(result.nodeType),
                result.name,
                result.path?.formattedString(),
                result.content?.mimeType,
                result.modifiedAt,
                result.isFavorite == null || result.allowableOperations == null,
                result.isFavorite ?: false,
                result.allowableOperations?.contains("delete") ?: false
            )
        }

        fun with(node: NodeChildAssociation): Entry {
            return Entry(
                node.id,
                Type.from(node.nodeType),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType,
                node.modifiedAt,
                node.isFavorite == null || node.allowableOperations == null,
                node.isFavorite ?: false,
                node.allowableOperations?.contains("delete") ?: false,
                otherId = node.properties?.get("cm:destination") as String?
            )
        }

        fun with(favorite: Favorite): Entry {
            val map = favorite.target
            if (map.file != null) {
                val file: FavoriteTargetNode = map.file!!
                return Entry(
                    file.id,
                    Type.File,
                    file.name,
                    file.path?.formattedString(),
                    file.content?.mimeType,
                    file.modifiedAt,
                    file.allowableOperations == null,
                    true,
                    file.allowableOperations?.contains("delete") ?: false
                )
            }
            if (map.folder != null) {
                val folder: FavoriteTargetNode = map.folder!!
                return Entry(
                    folder.id,
                    Type.Folder,
                    folder.name,
                    folder.path?.formattedString(),
                    null,
                    folder.modifiedAt,
                    folder.allowableOperations == null,
                    true,
                    folder.allowableOperations?.contains("delete") ?: false
                )
            }
            if (map.site != null) {
                val site = map.site!!
                return with(site).copy(isPartial = false, isFavorite = true)
            }
            throw IllegalStateException()
        }

        fun with(site: Site): Entry {
            return Entry(
                site.guid,
                Type.Site,
                site.title,
                null,
                null,
                isPartial = true,
                canDelete = site.role == Site.RoleEnum.SITEMANAGER,
                otherId = site.id
            )
        }

        fun with(role: SiteRole): Entry {
            return Entry(
                role.site.guid,
                Type.Site,
                role.site.title,
                null,
                null,
                isPartial = true,
                canDelete = role.role == SiteRole.RoleEnum.SITEMANAGER,
                otherId = role.site.id
            )
        }

        fun with(link: SharedLink): Entry {
            return Entry(
                link.nodeId ?: "",
                Type.File,
                link.name ?: "",
                null,
                link.content?.mimeType,
                link.modifiedAt,
                link.isFavorite == null || link.allowableOperations == null,
                link.isFavorite ?: false,
                link.allowableOperations?.contains("delete") ?: false
            )
        }

        fun with(node: DeletedNode): Entry {
            return Entry(
                node.id,
                Type.from(node.nodeType),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType,
                node.modifiedAt,
                isPartial = false,
                node.isFavorite ?: false,
                canDelete = false,
                isTrashed = true
            )
        }

        private fun PathInfo.formattedString(): String? {
            return elements?.map { it.name }
                ?.reduce { out, el -> "$out \u203A $el" }
        }
    }
}

object DateParceler : Parceler<ZonedDateTime> {
    private val zone = ZonedDateTime.now().zone

    override fun create(parcel: Parcel): ZonedDateTime {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(parcel.readLong()), zone)
    }

    override fun ZonedDateTime.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this.toInstant().epochSecond)
    }
}
