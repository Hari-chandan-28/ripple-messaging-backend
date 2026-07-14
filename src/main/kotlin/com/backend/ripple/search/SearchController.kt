package com.backend.ripple.search

import com.backend.ripple.dto.friendship.UserSummaryResponse
import com.backend.ripple.model.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search")
class SearchController(private val searchService:SearchService) {
    @GetMapping("/profile")
    fun search(@RequestParam username: String): List<UserSummaryResponse>
    {
        return searchService.search(username)
    }
}