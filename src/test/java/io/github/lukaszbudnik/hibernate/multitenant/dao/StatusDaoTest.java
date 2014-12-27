package io.github.lukaszbudnik.hibernate.multitenant.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.lukaszbudnik.hibernate.multitenant.ioc.DaoGuiceModule;
import io.github.lukaszbudnik.hibernate.multitenant.model.Status;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Optional;

public class StatusDaoTest {

    private static EntityManagerFactory emf;
    private static TestRunDao testRunDao;
    private static ConfigurationDao configurationDao;
    private static StatusDao statusDao;

    private static KeyPair keyPair;
    private static WriteContext writeContextA;
    private static ReadContext readContextA;

    @BeforeClass
    public static void before() throws Exception {
        Injector i = Guice.createInjector(new DaoGuiceModule());
        testRunDao = i.getInstance(TestRunDao.class);
        configurationDao = i.getInstance(ConfigurationDao.class);
        statusDao = i.getInstance(StatusDao.class);
        emf = i.getInstance(EntityManagerFactory.class);

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

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPair = keyPairGenerator.generateKeyPair();

        writeContextA = new WriteContext("a", keyPair.getPublic());
        readContextA = new ReadContext("a", keyPair.getPrivate());
    }

    @After
    public void after() throws Exception {

    }

    @Test
    public void testStatusFindByName() throws Exception {
        Optional<Status> created = statusDao.findByName(Status.Name.Created);
        Optional<Status> queued = statusDao.findByName(Status.Name.Queued);
        Optional<Status> executed = statusDao.findByName(Status.Name.Executed);
        Assert.assertTrue(created.isPresent());
        Assert.assertTrue(queued.isPresent());
        Assert.assertTrue(executed.isPresent());
    }

    @Test
    public void testStatusFindByNameCache() throws Exception {
        Optional<Status> created1 = statusDao.findByName(Status.Name.Created);

        Optional<Status> createdFromCache1 = statusDao.findById(created1.get().getId());
        Optional<Status> createdFromCache2 = statusDao.findById(created1.get().getId());

        EntityManager em = emf.createEntityManager();

        Session session = (Session) em.getDelegate();
        Statistics statistics = session.getSessionFactory().getStatistics();

        Assert.assertEquals(1, statistics.getSecondLevelCachePutCount());
        Assert.assertEquals(2, statistics.getSecondLevelCacheHitCount());
    }

}
