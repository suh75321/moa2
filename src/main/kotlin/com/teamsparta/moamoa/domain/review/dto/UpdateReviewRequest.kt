package com.teamsparta.moamoa.domain.review.dto

import com.teamsparta.moamoa.domain.review.model.Review

class UpdateReviewRequest(
    val title: String,
    val content: String,
    val name: String,
    val imageUrl: String,
) {
    fun toUpdateReview(review: Review) {
        review.title = this.title
        review.content = this.content
        review.name = this.name
        review.imageUrl = this.imageUrl
    }
}
