package io.github.lukaszbudnik.hibernate.multitenant.dao;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Stoppable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider, Stoppable {


    @Override
    public Connection getAnyConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:mymemdb", "SA", null);

        return conn;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        final Connection connection = getAnyConnection();
        try {
            connection.createStatement().execute("SET SEARCH_PATH=" + tenantIdentifier);
        } catch (SQLException e) {
            throw new HibernateException("Could not alter JDBC connection to specified schema [" + tenantIdentifier + "]", e);
        }
        return connection;
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) {
        try {
            this.releaseAnyConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return ConnectionProvider.class.equals(unwrapType) || MultiTenantConnectionProvider.class.equals(unwrapType) || MultiTenantConnectionProviderImpl.class.isAssignableFrom(unwrapType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (isUnwrappableAs(unwrapType)) {
            return (T) this;
        } else {
            throw new UnknownUnwrapTypeException(unwrapType);
        }
    }

    public void stop() {
        //
    }
}