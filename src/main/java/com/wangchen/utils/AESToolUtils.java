package com.wangchen.utils;


import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class AESToolUtils {
	/** 密钥算法 */
	private static final String KEY_ALGORITHM = "key";
	private static final int KEY_SIZE = 128;

	/** 加密/解密算法/工作模式/填充方法 */
	// public static final String CIPHER_ALGORITHM = "AES/ECB/NoPadding";

	/**
	 * 获取密钥
	 * 
	 * @return
	 * @throws Exception
	 */
	private static Key getKey() throws Exception {
		// 实例
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		// AES 
		kg.init(KEY_SIZE);
		// 生成密钥
		SecretKey secretKey = kg.generateKey();
		return secretKey;
	}

	/**
	 * 转化密钥
	 * 
	 * @param key
	 *            密钥
	 * @return Key 密钥
	 * @throws Exception
	 */
	public static Key codeToKey(String key) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(key);
		SecretKey secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
		return secretKey;
	}

	/**
	 * 解密
	 * 
	 * @param data
	 *            待解密数捄1�7
	 * @param key
	 *            密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	private static String decrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);
		/**
		 * 实例匄1�7 使用PKCS7Padding填充方式，按如下方式实现
		 * Cipher.getInstance(CIPHER_ALGORITHM,"BC");
		 */
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		// 初始化，设置解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		// 执行操作
		return new String(cipher.doFinal(data), "UTF-8");
	}

	/**
	 * 解密
	 * 
	 * @param data
	 *            待解密数捄1�7
	 * @param key
	 *            密钥
	 * @return byte[] 解密数据
	 * @throws Exception
	 */
	public static String decrypt(String data, String key) throws Exception {
		return decrypt(Base64.decodeBase64(data), Base64.decodeBase64(key));
	}

	/**
	 * 加密
	 * 
	 * @param data
	 *            待加密数捄1�7
	 * @param key
	 *            密钥
	 * @return bytes[] 加密数据
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = new SecretKeySpec(key, KEY_ALGORITHM);
		/**
		 * 实例匄1�7 使用PKCS7Padding填充方式，按如下方式实现
		 * Cipher.getInstance(CIPHER_ALGORITHM,"BC");
		 */
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		// 初始化，设置为加密模弄1�7
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}
	/**
	 * aes加密
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String data, String key) throws Exception {
		byte[] dataBytes = data.getBytes("UTF-8");
		byte[] keyBytes = Base64.decodeBase64(key);
		return Base64.encodeBase64String(encrypt(dataBytes, keyBytes));
	}

	/**
	 * 初始化密钄1�7
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getKeyStr() throws Exception {
		return Base64.encodeBase64String(getKey().getEncoded());
	}

	public static void main(String[] args){
		//生成密鑰
		//String keystr = getKeyStr();
		String key = "U7gLYObp+8/rK6iWpwgmOg==";
		System.out.println("key:"+key);
		Map<String, String> cipherData = new HashMap<String,String>();
		
		Map<String, String> map = new HashMap<String,String>();
		map.put("guid", "79ca3dad7bb345d1aecb7382e3a0c993");
		map.put("activityId", "yfdj123456789");
		map.put("jnlNo", "123456789123456789");
		map.put("timeStamp", System.currentTimeMillis()+"");
		map.put("type", "1");
		map.put("reward", "5");
		String mw = null;
		try {
			mw = AESToolUtils.encrypt(JSON.toJSONString(map), key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String a = "lFS/7Lgvg5tnkRdnaM76Z5Rd5gsThQLCnRiLU+VZEosys0nY3TFpXyFlfqjQLmtwgigZHzdEExo135XZBOtmEXt/tw0QqPHoV2kRV2QAqk12yGgBtC3S2OPsqVOMy4LjruhY6OvvS+6Nd8F+fGc/r5J/c4qgTaHSXFO5b3EilgMO3RyBvXUKVYzJih00voU6Qmk7nhAAVWc19w6K/NLBYg==";
		System.out.println("密文:" + mw);
		String jm = null;
		try {
			jm = AESToolUtils.decrypt(mw,key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("明文:" + jm);
		
		cipherData.put("activityId", "yfdj123456789");
		cipherData.put("cipherData", mw);
		System.out.println("请求入参:"+JSON.toJSONString(cipherData));
	}
}
