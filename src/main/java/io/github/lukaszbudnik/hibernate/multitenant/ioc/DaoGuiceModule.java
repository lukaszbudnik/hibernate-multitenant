package io.github.lukaszbudnik.hibernate.multitenant.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.onami.lifecycle.jsr250.PostConstructModule;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class DaoGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new PostConstructModule());
    }

    @Provides
    @Singleton
    public EntityManagerFactory entityManagerFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-hibernate");

        return emf;
    }

}
