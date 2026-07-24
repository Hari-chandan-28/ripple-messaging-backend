package com.backend.ripple.websocket.service


import com.backend.ripple.websocket.SessionStore
import com.backend.ripple.websocket.service.ChatWebSocketHandler
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import tools.jackson.databind.ObjectMapper

@Service
class NotificationService(
    private val objectMapper: ObjectMapper
) {
    fun notifyFriendRequest(receiverId: Long, senderId: Long, senderUsername: String) {
        val session = SessionStore.sessions[receiverId] ?: return
        if (!session.isOpen) return
        val packet = objectMapper.writeValueAsString(mapOf(
            "type" to "FRIEND_REQUEST",
            "payload" to mapOf(
                "senderId" to senderId,
                "senderUsername" to senderUsername,
            )
        ))
        session.sendMessage(TextMessage(packet))
    }

    fun notifyRequestAccepted(receiverId: Long, acceptorId: Long, acceptorUsername: String) {
        val session = SessionStore.sessions[receiverId] ?: return
        if (!session.isOpen) return
        val packet = objectMapper.writeValueAsString(mapOf(
            "type" to "REQUEST_ACCEPTED",
            "payload" to mapOf(
                "acceptorId" to acceptorId,
                "acceptorUsername" to acceptorUsername,
            )
        ))
        session.sendMessage(TextMessage(packet))
    }
}