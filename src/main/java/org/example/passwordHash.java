package org.example;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public class passwordHash {
    private static final int ITERATIONS = 100000;
    private static final int PASSWORD_LENGTH = 256;

    public static String hashPassword(String password) {
        byte[] salt = generateSalt();

        try {
            // Generate the derived key using PBKDF2 with SHA-256
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, PASSWORD_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] derivedKey = skf.generateSecret(spec).getEncoded();

            // Concatenate the salt and derived key as hexadecimal strings
            return bytesToHex(salt) + bytesToHex(derivedKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean verifyPassword(String inputPassword, String storedHashedPassword) {
        byte[] salt = hexToBytes(storedHashedPassword.substring(0, 32));
        String inputHashed = hashPasswordWithSalt(inputPassword, salt);
        return storedHashedPassword.equals(inputHashed);
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static String hashPasswordWithSalt(String password, byte[] salt) {
        try {
            // Generate the derived key using PBKDF2 with SHA-256 and the provided salt
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, PASSWORD_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] derivedKey = skf.generateSecret(spec).getEncoded();

            // Concatenate the salt and derived key as hexadecimal strings
            return bytesToHex(salt) + bytesToHex(derivedKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
