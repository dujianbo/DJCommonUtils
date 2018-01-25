package org.dj.djcommonutils.utils;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * @author qinzy
 * @classname AESPlus
 * @description TODO(可逆加密算法)
 * @date 2015年9月14日 下午1:47:07
 */
public class AESPlus {

    public static String encrypt(String strKey, String strIn) throws Exception {

        Base64 base64 = new Base64();
        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(strIn.getBytes());
        return Hex.encodeHexString(base64.encode(encrypted));
    }

    public static String decrypt(String strKey, String strIn) throws Exception {
        Base64 base64 = new Base64();
        SecretKeySpec skeySpec = getKey(strKey);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] encrypted1 = base64.decode(Hex.decodeHex(strIn.toCharArray()));
        byte[] original = cipher.doFinal(encrypted1);
        return new String(original);
    }

    private static SecretKeySpec getKey(String strKey) throws Exception {
        byte[] arrBTmp = strKey.getBytes();
        byte[] arrB = new byte[16]; // 创建一个空的16位字节数组（默认值为0）

        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }
        return new SecretKeySpec(arrB, "AES");
    }
}
