package com.backend.ripple.websocket.service

import com.backend.ripple.auth.utils.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor


@Component
class JwtHandshakeInterceptor(private val jwtUtil: JwtUtil) : HandshakeInterceptor {
    override fun beforeHandshake(request: ServerHttpRequest, response: ServerHttpResponse,
                                 wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>): Boolean {
        val token = request.uri.query
            ?.split("&")
            ?.find { it.startsWith("token=") }
            ?.removePrefix("token=")
        if (token == null || !jwtUtil.isTokenValid(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }
        attributes["userId"] = jwtUtil.extractUserId(token)
        return true
    }
    override fun afterHandshake(request: ServerHttpRequest, response: ServerHttpResponse,
                                wsHandler: WebSocketHandler, exception: Exception?) {

    }
}