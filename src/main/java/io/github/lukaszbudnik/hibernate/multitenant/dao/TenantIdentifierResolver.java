package io.github.lukaszbudnik.hibernate.multitenant.dao;


import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantThreadLocal.tenantThreadLocal.get();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
