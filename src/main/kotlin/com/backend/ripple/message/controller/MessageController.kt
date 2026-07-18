package com.backend.ripple.message.controller

import com.backend.ripple.dto.message.MessageResponse
import com.backend.ripple.message.service.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.collections.List

@RestController
@RequestMapping("/api/message")
class MessageController (private val messageService: MessageService) {
    @PostMapping("/create/conversation/{receiverId}")
    fun createConversation(@PathVariable receiverId: Long): ResponseEntity< Long>
    {
        val conversation = messageService.createConversation(receiverId)
        return ResponseEntity.ok(conversation)
    }
    @DeleteMapping("/delete/{messageId}")
    fun deleteMessage(@PathVariable messageId: Long,@RequestParam deleteType: String): ResponseEntity<Void>{
        messageService.deleteMessage(messageId,deleteType)
        return ResponseEntity.noContent().build()
    }
    @PatchMapping("/edit/{messageId}")
    fun editMessage(@PathVariable messageId: Long,@RequestBody content:String): ResponseEntity<Void>
    {
        messageService.editMessage(messageId,content)
        return ResponseEntity.noContent().build()
    }
    @GetMapping("/get/conversation/{conversationId}")
    fun getConversation(@PathVariable conversationId: Long): ResponseEntity<List<MessageResponse>>{
        val conversation = messageService.getMessages(conversationId)
        return ResponseEntity.ok(conversation)
    }
    @GetMapping("/get/conversation")
    fun getAllConversation(): ResponseEntity<List<Long>>{
        val allConversation = messageService.getChats()
        return ResponseEntity.ok(allConversation)
    }
}