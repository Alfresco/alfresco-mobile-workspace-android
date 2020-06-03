package com.alfresco.content.data

import com.alfresco.content.models.Node
import com.alfresco.content.models.NodeChildAssociation
import com.alfresco.content.models.ResultNode

fun with(resultNode: ResultNode): Node {
    return Node(
        resultNode.id,
        resultNode.name,
        resultNode.nodeType,
        resultNode.isFolder,
        resultNode.isFile,
        resultNode.modifiedAt,
        resultNode.modifiedByUser,
        resultNode.createdAt,
        resultNode.createdByUser,
        resultNode.isLocked,
        resultNode.parentId,
        resultNode.isLink,
        resultNode.isFavorite,
        resultNode.content,
        resultNode.aspectNames,
        resultNode.properties,
        resultNode.allowableOperations,
        resultNode.path,
        resultNode.permissions
    )
}

fun with(childAssociation: NodeChildAssociation): Node {
    return Node(
        childAssociation.id,
        childAssociation.name,
        childAssociation.nodeType,
        childAssociation.isFolder,
        childAssociation.isFile,
        childAssociation.modifiedAt,
        childAssociation.modifiedByUser,
        childAssociation.createdAt,
        childAssociation.createdByUser,
        childAssociation.isLocked,
        childAssociation.parentId,
        childAssociation.isLink,
        childAssociation.isFavorite,
        childAssociation.content,
        childAssociation.aspectNames,
        childAssociation.properties,
        childAssociation.allowableOperations,
        childAssociation.path,
        childAssociation.permissions
    )
}
