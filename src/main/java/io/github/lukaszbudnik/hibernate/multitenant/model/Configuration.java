package io.github.lukaszbudnik.hibernate.multitenant.model;

import io.github.lukaszbudnik.hibernate.multitenant.dao.TenantThreadLocal;
import io.github.lukaszbudnik.hibernate.multitenant.encryption.AsymmetricEncryptionUtils;
import io.github.lukaszbudnik.hibernate.multitenant.encryption.SymmetricEncryptionUtils;
import lombok.Data;

import javax.persistence.*;
import java.security.PrivateKey;


@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "unique_name", columnNames = "name")})
@Data
@Cacheable
public class Configuration {

    @Id
    @SequenceGenerator(name = "configuration_seq", sequenceName = "configuration_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_seq")
    private int id;

    @Basic(optional = false)
    private byte[] key;

    private String name;

    @Column(columnDefinition = "binary(1000)")
    private byte[] username;

    @Column(columnDefinition = "binary(1000)")
    private byte[] password;

    public void setUsername(String username) {
        this.username = username.getBytes();
    }

    public String getUsername() {
        return new String(username);
    }

    public void setPassword(String password) {
        this.password = password.getBytes();
    }

    public String getPassword() {
        return new String(password);
    }

    @PrePersist
    @PreUpdate
    public void encrypt() {
        byte[] symmetricKey = TenantThreadLocal.symmetricKeyThreadLocal.get();
        byte[] iv = "1234567890123456".getBytes();
        try {
            byte[] encryptedUsername = SymmetricEncryptionUtils.encrypt(username, symmetricKey, iv);
            this.username = encryptedUsername;

            byte[] encryptedPassword = SymmetricEncryptionUtils.encrypt(password, symmetricKey, iv);
            this.password = encryptedPassword;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostLoad
    public void decrypt() {
        PrivateKey privateKey = TenantThreadLocal.privateKeyThreadLocal.get();

        try {
            byte[] decryptedKey = AsymmetricEncryptionUtils.decrypt(key, privateKey);

            byte[] iv = "1234567890123456".getBytes();
            byte[] decryptedUsername = SymmetricEncryptionUtils.decrypt(username, decryptedKey, iv);
            username = decryptedUsername;

            byte[] decryptedPassword = SymmetricEncryptionUtils.decrypt(password, decryptedKey, iv);
            password = decryptedPassword;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}