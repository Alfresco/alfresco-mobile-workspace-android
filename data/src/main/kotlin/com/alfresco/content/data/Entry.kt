package com.alfresco.content.data

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
import java.time.ZonedDateTime

data class Entry(
    val id: String,
    val type: Type,
    val title: String,
    val subtitle: String?,
    val mimeType: String?,
    val modified: ZonedDateTime? = null
) {
    enum class Type {
        File,
        Folder,
        Site,
        Link,
        Group,
        Unknown;

        companion object {
            fun from(value: String): Type {
                when (value) {
                    "cm:content" -> return File
                    "cm:folder" -> return Folder
                    "st:sites" -> return Folder // Special folder for admins
                    "st:site" -> return Site
                    "cm:link" -> return Link
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
                node.content?.mimeType
            )
        }

        fun with(result: ResultNode): Entry {
            return Entry(
                result.id,
                Type.from(result.nodeType),
                result.name,
                result.path?.formattedString(),
                result.content?.mimeType,
                result.modifiedAt
            )
        }

        fun with(node: NodeChildAssociation): Entry {
            return Entry(
                node.id,
                Type.from(node.nodeType),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType
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
                    file.content?.mimeType
                )
            }
            if (map.folder != null) {
                val folder: FavoriteTargetNode = map.folder!!
                return Entry(
                    folder.id,
                    Type.Folder,
                    folder.name,
                    folder.path?.formattedString(),
                    null
                )
            }
            if (map.site != null) {
                val site = map.site!!
                return with(site)
            }
            throw IllegalStateException()
        }

        fun with(site: Site): Entry {
            return Entry(
                site.guid,
                Type.Site,
                site.title,
                null,
                null
            )
        }

        fun with(role: SiteRole): Entry {
            return Entry(
                role.site.guid,
                Type.Site,
                role.site.title,
                null,
                null
            )
        }

        fun with(link: SharedLink): Entry {
            return Entry(
                link.nodeId ?: "",
                Type.File,
                link.name ?: "",
                null,
                link.content?.mimeType
            )
        }

        fun with(node: DeletedNode): Entry {
            return Entry(
                node.id,
                Type.from(node.nodeType),
                node.name,
                node.path?.formattedString(),
                node.content?.mimeType
            )
        }

        private fun PathInfo.formattedString(): String? {
            return elements?.map { it.name }
                ?.reduce { out, el -> "$out \u203A $el" }
        }
    }
}
