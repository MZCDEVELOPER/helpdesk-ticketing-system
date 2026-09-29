package com.helpdesk.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtils() {
        // Prevent creation of PasswordUtils objects
    }

    /**
     * Creates a secure salted hash of a password.
     */
    public static String hashPassword(
            String password) {

        byte[] salt =
                new byte[SALT_LENGTH];

        SecureRandom random =
                new SecureRandom();

        random.nextBytes(salt);

        byte[] hash =
                generateHash(
                        password.toCharArray(),
                        salt
                );

        String encodedSalt =
                Base64.getEncoder()
                        .encodeToString(salt);

        String encodedHash =
                Base64.getEncoder()
                        .encodeToString(hash);

        return encodedSalt
                + ":"
                + encodedHash;
    }

    /**
     * Checks a password against a stored hash.
     */
    public static boolean verifyPassword(
            String password,
            String storedPassword) {

        if (password == null
                || storedPassword == null) {

            return false;
        }

        String[] parts =
                storedPassword.split(":");

        if (parts.length != 2) {
            return false;
        }

        try {

            byte[] salt =
                    Base64.getDecoder()
                            .decode(parts[0]);

            byte[] storedHash =
                    Base64.getDecoder()
                            .decode(parts[1]);

            byte[] attemptedHash =
                    generateHash(
                            password.toCharArray(),
                            salt
                    );

            if (storedHash.length
                    != attemptedHash.length) {

                return false;
            }

            int difference = 0;

            for (int i = 0;
                 i < storedHash.length;
                 i++) {

                difference |=
                        storedHash[i]
                        ^ attemptedHash[i];
            }

            return difference == 0;

        } catch (IllegalArgumentException e) {

            return false;
        }
    }

    /**
     * Generates a PBKDF2 password hash.
     */
    private static byte[] generateHash(
            char[] password,
            byte[] salt) {

        PBEKeySpec specification =
                new PBEKeySpec(
                        password,
                        salt,
                        ITERATIONS,
                        KEY_LENGTH
                );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return factory
                    .generateSecret(specification)
                    .getEncoded();

        } catch (NoSuchAlgorithmException
                 | InvalidKeySpecException e) {

            throw new IllegalStateException(
                    "Password hashing failed.",
                    e
            );

        } finally {

            specification.clearPassword();
        }
    }
}
