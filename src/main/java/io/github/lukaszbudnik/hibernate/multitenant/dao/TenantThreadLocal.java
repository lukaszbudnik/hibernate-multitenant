package io.github.lukaszbudnik.hibernate.multitenant.dao;


import java.security.PrivateKey;
import java.security.PublicKey;

public class TenantThreadLocal {
    public static final ThreadLocal<String> tenantThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<PublicKey> publicKeyThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<PrivateKey> privateKeyThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<byte[]> symmetricKeyThreadLocal = new ThreadLocal<>();
}
