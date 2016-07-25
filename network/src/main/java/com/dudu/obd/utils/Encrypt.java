package com.dudu.obd.utils;

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt {

	// 密钥
	public static final String cKey = "dudusmart";

	// 加密向量 用来增加加密强度
	public static final String vi = "1233211234567741";

	public static String AESEncrypt(String sSrc) throws Exception {
		return AESEncrypt(sSrc, cKey);
	}
	
	/**
	 * AES加密，使用内部密钥
	 * @param sSrc
	 * @param sKey
	 * @return
	 * @throws Exception
	 */
	public static String AESEncrypt(String sSrc, String sKey) throws Exception {
		if (sKey == null) {
			System.out.print("Key为空null");
			return null;
		}
		// 判断Key是否为16位
		if (sKey.length() != 16) {
			System.out.print("Key长度不是16位");
			return null;
		}
		byte[] raw = sKey.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
		IvParameterSpec iv = new IvParameterSpec(vi.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes());
		return byte2hex(encrypted).toLowerCase();
	}
	
	public static String AESEncrypt(byte[]bs) throws Exception {
		if (cKey == null) {
			System.out.print("Key为空null");
			return null;
		}
		// 判断Key是否为16位
		if (cKey.length() != 16) {
			System.out.print("Key长度不是16位");
			return null;
		}
		byte[] raw = cKey.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
		IvParameterSpec iv = new IvParameterSpec(vi.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(bs);
		return byte2hex(encrypted).toLowerCase();
	}
	
	/*可选 密钥*/
	public static String AESEncrypt(byte[]bs,String sKey) throws Exception{
		if(sKey == null){
			System.out.print("Key为空null");
			return null;
		}
		// 判断Key是否为16位
		if(sKey.length() != 16){
			System.out.print("Key长度不是16位");
			return null;
		}
		byte[] raw = sKey.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// "算法/模式/补码方式"
		IvParameterSpec iv = new IvParameterSpec(vi.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(bs);
		return byte2hex(encrypted).toLowerCase();
	}
	
	/**
	 * AES解密，使用内部密钥
	 * @param sSrc
	 * @return
	 * @throws Exception 
	 */
	public static String AESDecrypt(String sSrc) throws Exception{
		return AESDecrypt(sSrc,cKey);
	}
	
	// 解密
	public static String AESDecrypt(String sSrc, String sKey) throws Exception {
		// 判断Key是否正确
		if (sKey == null) {
			System.out.print("Key为空null");
			return null;
		}
		// 判断Key是否为16位
		if (sKey.length() != 16) {
			System.out.print("Key长度不是16位");
			return null;
		}
		byte[] raw = sKey.getBytes("ASCII");
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec iv = new IvParameterSpec(vi.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		byte[] encrypted1 = hex2byte(sSrc);
		byte[] original = cipher.doFinal(encrypted1);
		String originalString = new String(original, "UTF-8");
		return originalString;
	}
	
	public static byte[] AESDecryptByte(String sSrc) throws Exception {
		// 判断Key是否正确
		if (cKey == null) {
			System.out.print("Key为空null");
			return null;
		}
		// 判断Key是否为16位
		if (cKey.length() != 16) {
			System.out.print("Key长度不是16位");
			return null;
		}
		byte[] raw = cKey.getBytes("ASCII");
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec iv = new IvParameterSpec(vi.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		byte[] encrypted1 = hex2byte(sSrc);
		return cipher.doFinal(encrypted1);
	}
	
	public static byte[] AESDecryptByte(String sSrc,String sKey) throws Exception{
		// 判断Key是否正确
		if (sKey == null) {
			System.out.print("Key为空null");
			return null;
		}
		// 判断Key是否为16位
		if (sKey.length() != 16) {
			System.out.print("Key长度不是16位");
			return null;
		}
		byte[] raw = sKey.getBytes("ASCII");
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec iv = new IvParameterSpec(vi.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		byte[] encrypted1 = hex2byte(sSrc);
		return cipher.doFinal(encrypted1);
	}
	

	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	public synchronized static byte[] hex2byte(String strhex) {
		if (strhex == null) {
			return null;
		}
		int l = strhex.length();
		if (l % 2 == 1) {
			return null;
		}
		byte[] b = new byte[l / 2];
		for (int i = 0; i != l / 2; i++) {
			b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2),
					16);
		}
		return b;
	}

	/**
	 * md5加密
	 * 
	 * @param s
	 * @return
	 */
	public final static String MD5Encode(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * SHA1加密
	 * 
	 * @param sourceString
	 * @return
	 */
	public static String SHA1Encode(String s) {
		String resultString = null;
		try {
			resultString = new String(s);
			MessageDigest md = MessageDigest.getInstance("SHA1");
			resultString = byte2hexString(md.digest(resultString.getBytes()));
		} catch (Exception ex) {
		}
		return resultString;
	}

	private static final String byte2hexString(byte[] bytes) {
		StringBuffer buf = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			if (((int) bytes[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) bytes[i] & 0xff, 16));
		}
		return buf.toString().toUpperCase();
	}


	public static void main(String[] args) throws Exception {
//		String s = Base64Encode("你好");
//		System.out.println(s);
//		System.out.println(BASE64Decoder(s));
		
//		System.out.println(MD5Encode("yiyixiang@youkang")); // web service密码
//		System.out.println(MD5Encode("yiyixiang@youkang@aes")); // web service内容加密key的md5值
//		
//		System.out.println(AESEncrypt("this is a test", "57a2e4a0aa8e025e", "AES/ECB/PKCS5Padding"));
//		System.out.println(AESDecrypt("MA1xHbZQIieI+cBjpr4VPWgk8rBIhu7RsQREAhLMSmn8dYXy3WeDSUaNLplFcRFm2puBJYkaSbrAK0s6USl9cajLjUlhyTt1BjmIvZCFZgqLVluLRN3OJWp0J+5lHDxIFQX949hZKqDxkdckcILlZs2FvWDSkWhhBUmdL0+z/V7BFd+gJ4wFYex6uytJCnzZJDSN3t7UX5xwDEl2nNxbPmoJ12xbut8ltsDMmKY0l/a4YkdBDh/jZ13mOyQD0a9YQAF5EFlMo5J3TjVp8+6DPJiSQ1+bFsInBn56bvf5fZedxuyvt2UG6ZeCfl4r8C+n2thljBYTh2YQ50vksxuib1hJPMChEqEBAMJCCqnRfTC2h41ZpIYKY9q/bGhBaqmBbe1owSBM3PwI2qBjvP+6vdgsJUkk9/iERIdo840IjOI=", "57a2e4a0aa8e025e", "AES/ECB/PKCS5Padding"));
		
//		System.out.println(Base64Encode("a7fe6fc3-22be-4cbd-8021-23deb2a30ccc"));
//		System.out.println(BASE64Decoder("YTdmZTZmYzMtMjJiZS00Y2JkLTgwMjEtMjNkZWIyYTMwY2Nj"));
		
//		System.out.println(MD5Encode("82996141"));
//		AESEncrypt("thyujkmnhy","1234567890123456");
		
		System.out.println(MD5Encode("dudu20150806"));
//		System.out.println(AESDecrypt("05ef8ff9f4439f20b5afeb8e66812eb884b92cdfe659bc51325ec4bb53df32f06db3dc3b046e875a5589ffc93fa503bc8f94701914c10f4ef8a4625dceee3a0de1a9ef0442682111a2ddf159ef75696ce87bb540170660e173799681227bb33b696ce51514a081b59441cf0eb7a3d5a0fc852f7f29b9b91127ddc3a3a9515ae1f93609c697cab6b4a2393783442501a25cff60ddad2a3f34bd13ac47a5ddb66130b86316027b49d02c66ef81d2fa2162413cf7b421f21cd2f44b1253a3fd36a36c7ca482f5fdff677114197ac7047ce7e0c5be9f5c8936a752ce057621a98c3b3adee1239a7e78878cb2c49342f32a6e4b78af148baecd3f522d0fa49b1853aa1e121bd621fc8134a6eab44d56584ec04474cac304eae91fb8f32a513bf17c8a806f369aed7e0af60547d4ce967368335c2a3c9614f664b7efcb6ada1e6d0c5643a18ec7467217db4a4990e2d06a8321e99c745166f4ef2f14463b71fea181e4e081a2fad3320616facaa704428c8375","1233211234567741"));

	}
}
