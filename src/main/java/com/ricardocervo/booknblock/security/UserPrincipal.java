package com.ricardocervo.booknblock.security;
import com.ricardocervo.booknblock.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal {
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(User user){
        this.username = user.getEmail();
        this.password = user.getPassword();

        this.authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority((role.getName()))).collect(Collectors.toList());
    }

    public static UserPrincipal create(User user){
        return new UserPrincipal(user);
    }
}
