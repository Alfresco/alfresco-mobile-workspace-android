package com.alfresco.content.session

data class SessionNotFoundException(val sessionMessage: String) : Exception(sessionMessage)
