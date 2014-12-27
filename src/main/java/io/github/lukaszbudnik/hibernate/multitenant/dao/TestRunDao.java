package io.github.lukaszbudnik.hibernate.multitenant.dao;

import com.google.inject.Inject;
import io.github.lukaszbudnik.hibernate.multitenant.model.Context;
import io.github.lukaszbudnik.hibernate.multitenant.model.TestRun;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;


@Singleton
public class TestRunDao {

    @Inject
    EntityManagerFactory emf;

    public TestRun save(Context ctx, TestRun testRun) {
        TenantThreadLocal.tenantThreadLocal.set(ctx.getTenant());

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        TestRun merged = em.merge(testRun);
        em.getTransaction().commit();
        return merged;
    }

    public List<TestRun> findAll(Context ctx) {
        TenantThreadLocal.tenantThreadLocal.set(ctx.getTenant());

        EntityManager em = emf.createEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TestRun> q = cb.createQuery(TestRun.class);
        Root<TestRun> c = q.from(TestRun.class);
        q.select(c);

        TypedQuery<TestRun> query = em.createQuery(q);
        List<TestRun> results = query.getResultList();

        return results;
    }

    public Optional<TestRun> findById(int id) {
        return Optional.empty();
    }

    public Optional<TestRun> findByName(String name) {
        return Optional.empty();
    }

    public void delete(TestRun testRun) {

    }

}
