package com.backend.ripple.websocket.service


import com.backend.ripple.websocket.SessionStore
import com.backend.ripple.websocket.service.ChatWebSocketHandler
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import tools.jackson.databind.ObjectMapper

@Service
class NotificationService(private val objectMapper: ObjectMapper) {

    private fun send(userId: Long, payload: Any) {
        val session = SessionStore.sessions[userId] ?: return
        if (!session.isOpen) return
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(payload)))
    }

    fun notifyFriendRequest(receiverId: Long, senderId: Long, senderUsername: String, senderName: String?) {
        send(receiverId, mapOf(
            "type" to "FRIEND_REQUEST",
            "payload" to mapOf(
                "senderId" to senderId,
                "senderUsername" to senderUsername,
                "senderName" to (senderName ?: senderUsername),
            )
        ))
    }

    fun notifyRequestAccepted(receiverId: Long, acceptorId: Long, acceptorUsername: String) {
        send(receiverId, mapOf(
            "type" to "REQUEST_ACCEPTED",
            "payload" to mapOf(
                "acceptorId" to acceptorId,
                "acceptorUsername" to acceptorUsername,
            )
        ))
    }

    fun notifyFriendRemoved(receiverId: Long, removerId: Long) {
        send(receiverId, mapOf(
            "type" to "FRIEND_REMOVED",
            "payload" to mapOf("removerId" to removerId)
        ))
    }
}