package com.teamsparta.moamoa.domain.socialUser.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class SocialUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_user_id")
    var id: Long? = null,
    val email: String,
    @Enumerated(EnumType.STRING)
    val provider: OAuth2Provider,
    val providerId: String,
    val nickname: String,
) {
    companion object {
        fun ofKakao(
            email: String,
            providerId: String,
            nickname: String,
        ): SocialUser {
            return SocialUser(
                email = email,
                provider = OAuth2Provider.KAKAO,
                providerId = providerId,
                nickname = nickname,
            )
        }
    }
}

enum class OAuth2Provider {
    KAKAO,
}
