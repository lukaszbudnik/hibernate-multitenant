package io.github.lukaszbudnik.hibernate.multitenant.dao;

import io.github.lukaszbudnik.hibernate.multitenant.model.Status;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.Optional;

@Singleton
public class StatusDao {

    @Inject
    EntityManagerFactory emf;

    public Optional<Status> findById(int id) {
        TenantThreadLocal.tenantThreadLocal.set("ref");

        EntityManager em = emf.createEntityManager();

        Status status = em.find(Status.class, id);

        return Optional.ofNullable(status);
    }

    public Optional<Status> findByName(Status.Name statusName) {
        TenantThreadLocal.tenantThreadLocal.set("ref");

        EntityManager em = emf.createEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Status> q = cb.createQuery(Status.class);
        Root<Status> c = q.from(Status.class);
        ParameterExpression<String> p = cb.parameter(String.class);
        q.select(c).where(cb.equal(c.get("name"), p));

        TypedQuery<Status> query = em.createQuery(q);
        query.setParameter(p, statusName.name());

        try {
            Status status = query.getSingleResult();
            return Optional.of(status);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
