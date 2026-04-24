package com.server.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.Principal;

@Data
@AllArgsConstructor
public class UserPrincipal implements Principal {
    private Long userId;
    private String username;
    private String role;

    @Override
    public String getName() {
        return username;
    }
}
