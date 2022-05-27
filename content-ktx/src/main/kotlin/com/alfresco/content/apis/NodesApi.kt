package com.alfresco.content.apis

/**
 * Convenience fun for retrieving current logged in user's home directory.
 */
suspend fun NodesApi.getMyNode(
    include: List<String>? = null,
    fields: List<String>? = null
) = getNode(MY_NODE_ID, include, null, fields)

/**
 * Convenience fun for retrieving current logged in user's files.
 */
suspend fun NodesApi.getMyFiles(
    skipCount: Int? = null,
    maxItems: Int? = null,
    orderBy: List<String>? = null,
    where: String? = null,
    include: List<String>? = null,
    relativePath: String? = null,
    includeSource: Boolean? = null,
    fields: List<String>? = null
) = listNodeChildren(
    MY_NODE_ID,
    skipCount,
    maxItems,
    orderBy,
    where,
    include,
    relativePath,
    includeSource,
    fields
)

private const val MY_NODE_ID = "-my-"
