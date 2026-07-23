package com.backend.ripple.profile.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@RestController
@RequestMapping("/api/profile")
class ProfileUploadController {

    private val uploadDir: String by lazy {
        val dir = Paths.get(System.getProperty("user.dir"), "uploads")
        if (!Files.exists(dir)) Files.createDirectories(dir)
        dir.toAbsolutePath().toString()
    }

    @PostMapping("/upload-pic")
    fun uploadPic(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        val ext = file.originalFilename?.substringAfterLast(".", "jpg") ?: "jpg"
        val filename = "${UUID.randomUUID()}.$ext"
        val path = Paths.get(uploadDir, filename)
        file.transferTo(path.toFile())
        return ResponseEntity.ok(mapOf("url" to "/uploads/$filename"))
    }
}