package io.github.lukaszbudnik.hibernate.multitenant.dao;


import io.github.lukaszbudnik.hibernate.multitenant.model.Context;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.security.PublicKey;

@Data
@EqualsAndHashCode(callSuper = true)
public class WriteContext extends Context {
    private PublicKey publicKey;

    public WriteContext(String tenant, PublicKey publicKey) {
        super(tenant);
        this.publicKey = publicKey;
    }
}
