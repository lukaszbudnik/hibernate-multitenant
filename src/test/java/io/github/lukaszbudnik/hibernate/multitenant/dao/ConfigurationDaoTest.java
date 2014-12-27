package io.github.lukaszbudnik.hibernate.multitenant.dao;


import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.lukaszbudnik.hibernate.multitenant.ioc.DaoGuiceModule;
import io.github.lukaszbudnik.hibernate.multitenant.model.Configuration;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Optional;

import static java.security.KeyPairGenerator.getInstance;

public class ConfigurationDaoTest {

    private ConfigurationDao configurationDao;

    private WriteContext writeContext;
    private ReadContext readContext;

    @Before
    public void before() throws Exception {
        Injector i = Guice.createInjector(new DaoGuiceModule());
        configurationDao = i.getInstance(ConfigurationDao.class);

        TenantThreadLocal.tenantThreadLocal.set("public");
        EntityManagerFactory emf = i.getInstance(EntityManagerFactory.class);
        EntityManager em = emf.createEntityManager();

        InputStream is = TestRunDaoTest.class.getResourceAsStream("/insert.sql");
        String sql = null;
        try {
            sql = IOUtils.toString(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        em.getTransaction().begin();
        try {
            em.createNativeQuery(sql).executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
        }

        KeyPairGenerator keyPairGenerator = getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        writeContext = new WriteContext("a", keyPair.getPublic());
        readContext = new ReadContext("a", keyPair.getPrivate());
    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testSave() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setName("testSave() Configuration Name " + System.currentTimeMillis());
        configuration.setUsername("root");
        configuration.setPassword("secret");
        configurationDao.save(writeContext, configuration);

        Optional<Configuration> fetchedConfiguration = configurationDao.findByName(readContext, configuration.getName());

        Assert.assertTrue(fetchedConfiguration.isPresent());
        Assert.assertArrayEquals(configuration.getKey(), fetchedConfiguration.get().getKey());
        Assert.assertEquals(configuration.getUsername(), fetchedConfiguration.get().getUsername());
        Assert.assertEquals(configuration.getPassword(), fetchedConfiguration.get().getPassword());
    }

}
