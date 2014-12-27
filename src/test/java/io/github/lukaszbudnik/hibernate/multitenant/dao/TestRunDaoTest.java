package io.github.lukaszbudnik.hibernate.multitenant.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.lukaszbudnik.hibernate.multitenant.ioc.DaoGuiceModule;
import io.github.lukaszbudnik.hibernate.multitenant.model.*;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestRunDaoTest {

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
    public void testSave() throws Exception {
        TestRun tra = new TestRun();
        tra.setName("testSave() Test Name A");
        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertTrue(tra.getId() > 0);
    }

    @Test
    public void testSaveWithTestCases() throws Exception {
        Optional<Status> created = statusDao.findByName(Status.Name.Created);

        TestRun tra = new TestRun();
        tra.setName("testSaveWithTestCases() Test Name A");

        TestCase tc1 = new TestCase();
        tc1.setTestRun(tra);
        tc1.setStatus(created.get());
        tc1.setName("tc1 name");

        TestCase tc2 = new TestCase();
        tc2.setTestRun(tra);
        tc2.setStatus(created.get());
        tc2.setName("tc2 name");

        List<TestCase> testCases = Arrays.asList(tc1, tc2);
        tra.setTestCases(testCases);

        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertTrue(tra.getId() > 0);
        Assert.assertTrue(tra.getTestCases().stream().allMatch(tc -> tc.getId() > 0));
    }

    @Test
    public void testSaveWithConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setName("testSaveWithConfiguration() Configuration Name 1");
        configuration.setUsername("root");
        configuration.setPassword("secret");
        Configuration savedConfiguration = configurationDao.save(writeContextA, configuration);

        Optional<Configuration> fetchedConfiguration = configurationDao.findById(readContextA, savedConfiguration.getId());

        TestRun tra = new TestRun();
        tra.setName("testSaveWithConfiguration() Test Name A");
        tra.setConfiguration(fetchedConfiguration.get());
        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertTrue(tra.getId() > 0);
        Assert.assertTrue(tra.getConfiguration().getId() > 0);
    }

    @Test
    public void testUpdateWithTestCasesAndConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setName("testUpdateWithTestCasesAndConfiguration() Configuration Name 1");
        configuration.setUsername("root");
        configuration.setPassword("secret");
        Configuration savedConfiguration = configurationDao.save(writeContextA, configuration);

        Optional<Configuration> fetchedConfiguration = configurationDao.findById(readContextA, savedConfiguration.getId());
        Optional<Status> created = statusDao.findByName(Status.Name.Created);

        TestRun tra = new TestRun();
        tra.setName("testUpdateWithTestCasesAndConfiguration() Test Name A");
        // save just test run
        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertTrue(tra.getId() > 0);

        // set configuration and save test run
        tra.setConfiguration(fetchedConfiguration.get());
        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertTrue(tra.getConfiguration().getId() > 0);

        // set test cases and save test run
        TestCase tc1 = new TestCase();
        tc1.setTestRun(tra);
        tc1.setStatus(created.get());
        tc1.setName("tc1 name");

        TestCase tc2 = new TestCase();
        tc2.setTestRun(tra);
        tc2.setStatus(created.get());
        tc2.setName("tc2 name");

        List<TestCase> testCases = Arrays.asList(tc1, tc2);
        tra.setTestCases(testCases);
        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertTrue(tra.getTestCases().stream().allMatch(tc -> tc.getId() > 0));

        // finally change name of the test run
        tra.setName("testUpdateWithTestCasesAndConfiguration() Test Name A #2");
        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertEquals("testUpdateWithTestCasesAndConfiguration() Test Name A #2", tra.getName());
    }

    @Test
    public void testFindAll() throws Exception {
        TestRun trb = new TestRun();
        trb.setName("testFindAll() Test Name B");
        trb = testRunDao.save(new Context("b"), trb);
        Assert.assertTrue(trb.getId() > 0);

        TestRun tra = new TestRun();
        tra.setName("testFindAll() Test Name A1");
        tra = testRunDao.save(new Context("a"), tra);
        Assert.assertTrue(tra.getId() > 0);


        TestRun tra2 = new TestRun();
        tra2.setName("testFindAll() Test Name A2");
        tra2 = testRunDao.save(new Context("a"), tra2);
        Assert.assertTrue(tra2.getId() > 0);

        List<TestRun> allA = testRunDao.findAll(new Context("a"));
        Assert.assertEquals(2, allA.size());

        List<TestRun> allB = testRunDao.findAll(new Context("b"));
        Assert.assertEquals(1, allB.size());

        List<TestRun> none = testRunDao.findAll(new Context("c"));
        Assert.assertEquals(0, none.size());
    }

}
