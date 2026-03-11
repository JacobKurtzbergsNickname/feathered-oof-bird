package com.paypalclone.featheredoofbird.identity.domain;

import java.util.List;

public enum Role {
    PERSONAL(List.of("write:transactions")),
    BUSINESS(List.of("write:transactions")),
    ADMIN(List.of("write:transactions", "admin:all"));

    private final List<String> scopes;

    Role(List<String> scopes) {
        this.scopes = scopes;
    }

    public List<String> scopes() {
        return scopes;
    }
}
