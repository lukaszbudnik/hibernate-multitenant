package io.github.lukaszbudnik.hibernate.multitenant.dao;

import io.github.lukaszbudnik.hibernate.multitenant.encryption.AsymmetricEncryptionUtils;
import io.github.lukaszbudnik.hibernate.multitenant.model.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.security.SecureRandom;
import java.util.Optional;

@Singleton
public class ConfigurationDao {

    @Inject
    EntityManagerFactory emf;

    private SecureRandom secureRandom = new SecureRandom();

    public Configuration save(WriteContext ctx, Configuration configuration) throws Exception {
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);

        TenantThreadLocal.tenantThreadLocal.set(ctx.getTenant());
        TenantThreadLocal.symmetricKeyThreadLocal.set(key);
        TenantThreadLocal.publicKeyThreadLocal.set(ctx.getPublicKey());

        EntityManager em = emf.createEntityManager();

        byte[] encryptedKey = AsymmetricEncryptionUtils.encrypt(key, ctx.getPublicKey());
        configuration.setKey(encryptedKey);

        em.getTransaction().begin();
        Configuration newConfiguration = em.merge(configuration);
        em.getTransaction().commit();

        return newConfiguration;
    }

    public Optional<Configuration> findByName(ReadContext ctx, String name) {
        TenantThreadLocal.tenantThreadLocal.set(ctx.getTenant());
        TenantThreadLocal.privateKeyThreadLocal.set(ctx.getPrivateKey());

        EntityManager em = emf.createEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Configuration> q = cb.createQuery(Configuration.class);
        Root<Configuration> c = q.from(Configuration.class);
        ParameterExpression<String> p = cb.parameter(String.class);
        q.select(c).where(cb.equal(c.get("name"), p));

        TypedQuery<Configuration> query = em.createQuery(q);
        query.setParameter(p, name);

        Configuration configuration = query.getSingleResult();

        return Optional.ofNullable(configuration);
    }

    public Optional<Configuration> findById(ReadContext ctx, int id) {
        TenantThreadLocal.tenantThreadLocal.set(ctx.getTenant());
        TenantThreadLocal.privateKeyThreadLocal.set(ctx.getPrivateKey());

        EntityManager em = emf.createEntityManager();

        Configuration configuration = em.find(Configuration.class, id);

        return Optional.ofNullable(configuration);
    }
}
