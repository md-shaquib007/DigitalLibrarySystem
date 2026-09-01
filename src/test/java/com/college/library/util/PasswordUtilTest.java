package com.college.library.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashAndVerifyPassword() {
        String plain = "testPassword123";
        String hashed = PasswordUtil.hash(plain);

        assertNotNull(hashed);
        assertNotEquals(plain, hashed);
        assertTrue(PasswordUtil.verify(plain, hashed));
        assertFalse(PasswordUtil.verify("wrongPassword", hashed));
    }

    @Test
    void differentHashesForSamePassword() {
        String plain = "samePassword";
        String hash1 = PasswordUtil.hash(plain);
        String hash2 = PasswordUtil.hash(plain);

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.verify(plain, hash1));
        assertTrue(PasswordUtil.verify(plain, hash2));
    }
}
