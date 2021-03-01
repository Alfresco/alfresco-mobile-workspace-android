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
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter
import java.lang.Exception
import java.time.Instant
import java.time.ZonedDateTime
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<ZonedDateTime, DateParceler>
@Entity
data class Entry(
    val id: String,
    val parentId: String? = null,
    @Convert(converter = BoxEntryTypeConverter::class, dbType = String::class)
    val type: Type,
    val name: String,
    val path: String?,
    val mimeType: String?,
    @Convert(converter = BoxDateConverter::class, dbType = Long::class)
    val modified: ZonedDateTime? = null,
    val isPartial: Boolean = false,
    val isFavorite: Boolean = false,
    val canDelete: Boolean = false,
    val isTrashed: Boolean = false,
    val otherId: String? = null,
    @Id var boxId: Long = 0,
    val isOffline: Boolean = false,
    @Convert(converter = BoxOfflineStatusConverter::class, dbType = String::class)
    val offlineStatus: OfflineStatus = OfflineStatus.UNDEFINED,
) : Parcelable {

    val isSynced: Boolean
        get() = offlineStatus == OfflineStatus.SYNCED

    val isLocal: Boolean
        get() = boxId != 0L

    val isFile: Boolean
        get() = type == Type.FILE

    val isFolder: Boolean
        get() = type == Type.FOLDER

    val isLink: Boolean
        get() = type == Type.FILE_LINK || type == Type.FOLDER_LINK

    val hasOfflineStatus: Boolean
        get() = offlineStatus != OfflineStatus.UNDEFINED

    // TODO: move to repository level
    fun withOfflineStatus(): Entry {
        val offline = OfflineRepository().fetchOfflineEntry(this)
        return if (offline != null) {
            copy(
                boxId = offline.boxId,
                isOffline = offline.isOffline)
        } else {
            this
        }
    }

    fun metadataEquals(other: Entry): Boolean =
        id == other.id &&
            parentId == other.parentId &&
            type == other.type &&
            name == other.name &&
            path == other.path &&
            mimeType == other.mimeType &&
            modified?.toEpochSecond() == other.modified?.toEpochSecond() &&
            isFavorite == other.isFavorite &&
            canDelete == other.canDelete &&
            otherId == other.otherId

    fun copyWithMetadata(other: Entry) =
        copy(
            parentId = other.parentId,
            type = other.type,
            name = other.name,
            path = other.path,
            mimeType = other.mimeType,
            modified = other.modified,
            isFavorite = other.isFavorite,
            canDelete = other.canDelete,
            otherId = other.otherId
        )

    enum class Type {
        FILE,
        FOLDER,
        SITE,
        FILE_LINK,
        FOLDER_LINK,
        GROUP,
        UNKNOWN;

        companion object {
            fun from(value: String, isFile: Boolean = false, isFolder: Boolean = false): Type {
                when (value) {
                    "cm:content" -> return FILE
                    "cm:folder" -> return FOLDER
                    "st:sites" -> return FOLDER // Special folder for admins
                    "st:site" -> return SITE
                    "app:filelink" -> return FILE_LINK
                    "app:folderlink" -> return FOLDER_LINK
                }
                if (isFile) return FILE
                if (isFolder) return FOLDER
                return UNKNOWN
            }
        }
    }

    companion object {
        fun with(node: Node): Entry {
            return Entry(
                node.id,
                node.parentId,
                Type.from(node.nodeType, node.isFile, node.isFolder),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType,
                node.modifiedAt,
                node.isFavorite == null || node.allowableOperations == null,
                node.isFavorite ?: false,
                node.allowableOperations?.contains("delete") ?: false
            ).withOfflineStatus()
        }

        fun with(result: ResultNode): Entry {
            return Entry(
                result.id,
                result.parentId,
                Type.from(result.nodeType, result.isFile, result.isFolder),
                result.name,
                result.path?.formattedString(),
                result.content?.mimeType,
                result.modifiedAt,
                result.isFavorite == null || result.allowableOperations == null,
                result.isFavorite ?: false,
                result.allowableOperations?.contains("delete") ?: false
            ).withOfflineStatus()
        }

        fun with(node: NodeChildAssociation): Entry {
            return Entry(
                node.id,
                node.parentId,
                Type.from(node.nodeType, node.isFile, node.isFolder),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType,
                node.modifiedAt,
                node.isFavorite == null || node.allowableOperations == null,
                node.isFavorite ?: false,
                node.allowableOperations?.contains("delete") ?: false,
                otherId = node.properties?.get("cm:destination") as String?
            ).withOfflineStatus()
        }

        fun with(favorite: Favorite): Entry {
            val map = favorite.target
            if (map.file != null) {
                val file: FavoriteTargetNode = map.file!!
                return Entry(
                    file.id,
                    file.parentId,
                    Type.FILE,
                    file.name,
                    file.path?.formattedString(),
                    file.content?.mimeType,
                    file.modifiedAt,
                    file.allowableOperations == null,
                    true,
                    file.allowableOperations?.contains("delete") ?: false
                ).withOfflineStatus()
            }
            if (map.folder != null) {
                val folder: FavoriteTargetNode = map.folder!!
                return Entry(
                    folder.id,
                    folder.parentId,
                    Type.FOLDER,
                    folder.name,
                    folder.path?.formattedString(),
                    null,
                    folder.modifiedAt,
                    folder.allowableOperations == null,
                    true,
                    folder.allowableOperations?.contains("delete") ?: false
                ).withOfflineStatus()
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
                null,
                Type.SITE,
                site.title,
                null,
                null,
                isPartial = true,
                canDelete = site.role == Site.RoleEnum.SITEMANAGER,
                otherId = site.id
            ).withOfflineStatus()
        }

        fun with(role: SiteRole): Entry {
            return Entry(
                role.site.guid,
                null,
                Type.SITE,
                role.site.title,
                null,
                null,
                isPartial = true,
                canDelete = role.role == SiteRole.RoleEnum.SITEMANAGER,
                otherId = role.site.id
            ).withOfflineStatus()
        }

        fun with(link: SharedLink): Entry {
            return Entry(
                link.nodeId ?: "",
                null,
                Type.FILE,
                link.name ?: "",
                link.path?.formattedString(),
                link.content?.mimeType,
                link.modifiedAt,
                link.isFavorite == null || link.allowableOperations == null,
                link.isFavorite ?: false,
                link.allowableOperations?.contains("delete") ?: false
            ).withOfflineStatus()
        }

        fun with(node: DeletedNode): Entry {
            return Entry(
                node.id,
                node.parentId,
                Type.from(node.nodeType, node.isFile, node.isFolder),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType,
                node.modifiedAt,
                isPartial = false,
                node.isFavorite ?: false,
                canDelete = false,
                isTrashed = true
            ).withOfflineStatus()
        }

        private fun PathInfo.formattedString(): String? {
            return elements?.map { it.name }
                ?.reduce { out, el -> "$out \u203A $el" }
        }
    }
}

enum class OfflineStatus {
    PENDING,
    SYNCING,
    SYNCED,
    ERROR,
    UNDEFINED;

    @OptIn(ExperimentalStdlibApi::class)
    fun value() = name.lowercase()
}

@ExperimentalStdlibApi
class BoxOfflineStatusConverter : PropertyConverter<OfflineStatus, String> {
    override fun convertToEntityProperty(databaseValue: String?) =
        try { OfflineStatus.valueOf(databaseValue?.uppercase() ?: "") } catch (_: Exception) { OfflineStatus.UNDEFINED }

    override fun convertToDatabaseValue(entityProperty: OfflineStatus?) =
        entityProperty?.value()
}

@ExperimentalStdlibApi
class BoxEntryTypeConverter : PropertyConverter<Entry.Type, String> {
    override fun convertToEntityProperty(databaseValue: String?) =
        try { Entry.Type.valueOf(databaseValue?.uppercase() ?: "") } catch (_: Exception) { Entry.Type.UNKNOWN }

    override fun convertToDatabaseValue(entityProperty: Entry.Type?) =
        entityProperty?.name?.lowercase()
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

class BoxDateConverter : PropertyConverter<ZonedDateTime, Long> {
    private val zone = ZonedDateTime.now().zone

    override fun convertToEntityProperty(databaseValue: Long?): ZonedDateTime? =
        if (databaseValue != null) {
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(databaseValue), zone)
        } else {
            null
        }

    override fun convertToDatabaseValue(entityProperty: ZonedDateTime?) =
        entityProperty?.toInstant()?.epochSecond
}
