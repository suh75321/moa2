package com.teamsparta.moamoa.domain.socialUser.repository

import com.teamsparta.moamoa.domain.socialUser.model.OAuth2Provider
import com.teamsparta.moamoa.domain.socialUser.model.SocialUser
import org.springframework.data.repository.CrudRepository
import java.util.*

interface SocialUserRepository : CrudRepository<SocialUser, Long> {
    fun existsByProviderAndProviderId(
        provider: OAuth2Provider,
        id: String,
    ): Boolean

    fun findByProviderAndProviderId(
        provider: OAuth2Provider,
        id: String,
    ): SocialUser

    fun findByProviderId(providerId: String): Optional<SocialUser>
}
