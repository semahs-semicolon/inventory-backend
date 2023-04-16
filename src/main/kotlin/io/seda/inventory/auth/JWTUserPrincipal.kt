package io.seda.inventory.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

interface UserPrincipal {
    var id: Long
}

class FullUserDetails(override var id: Long,
                      var nickname: String,
                      username: String,
                      password: String,
                      authorities: List<String>): User(username, password, authorities.map { SimpleGrantedAuthority(it) }), UserPrincipal;
class JWTUserDetails(override var id: Long, authorities: List<String>): UserDetails, UserPrincipal {
    var authorities = authorities.map { SimpleGrantedAuthority(it) }.toMutableSet()

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return authorities;
    }

    override fun getPassword(): String {
        throw UnsupportedOperationException("Password not available to jwt validated user")
    }

    override fun getUsername(): String {
        throw UnsupportedOperationException("Username not available to jwt validated user")
    }

    override fun isAccountNonExpired(): Boolean {
        return true;
    }

    override fun isAccountNonLocked(): Boolean {
        return true;
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true;
    }

    override fun isEnabled(): Boolean {
        return true;
    }

}