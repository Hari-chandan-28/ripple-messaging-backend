package com.backend.ripple.websocket

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
object SessionStore {
    val sessions = ConcurrentHashMap<Long, WebSocketSession>()
}