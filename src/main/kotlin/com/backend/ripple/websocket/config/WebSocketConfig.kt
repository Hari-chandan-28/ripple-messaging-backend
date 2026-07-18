package com.backend.ripple.websocket.config

import com.backend.ripple.websocket.service.ChatWebSocketHandler
import com.backend.ripple.websocket.service.JwtHandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig (private val webSocketHandler: ChatWebSocketHandler,private val jwtHandshakeInterceptor: JwtHandshakeInterceptor): WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry : WebSocketHandlerRegistry){
        registry.addHandler(webSocketHandler, "/ws/chat")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOrigins("*")
    }
}