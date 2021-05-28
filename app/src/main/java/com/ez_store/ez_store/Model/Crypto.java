package com.ez_store.ez_store.Model;

public abstract class Crypto {
    private static final int key = 5;

    public static String decrypt(byte[] data) {
        try {
            byte[] enc = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                if (i % 2 == 0) {
                    enc[i] = (byte) (data[i] - key);
                } else {
                    enc[i] = (byte) (data[i] + key);
                }
            }
            return new String(enc);
        } catch (Exception ignored) {

        }
        return null;
    }

    public static String encrypt(byte[] data) {
        try {
            byte[] enc = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                if (i % 2 == 0) {
                    enc[i] = (byte) (data[i] + key);
                } else {
                    enc[i] = (byte) (data[i] - key);
                }
            }
            return new String(enc);
        } catch (Exception ignored) {

        }
        return null;
    }
}