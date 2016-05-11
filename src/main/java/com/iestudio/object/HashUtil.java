package com.iestudio.object;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashUtil {
	public static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String getHash(String fileName, String hashType) throws Exception {
		InputStream fis;
		fis = new FileInputStream(fileName);
		try {
			return getHash(fis, hashType);
		} finally {
			if (!ObjUtil.isEmpty(fis)) {
				fis.close();
				fis = null;
			}
		}
	}

	private static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static String getHash(InputStream iStream, String hashType) throws Exception {
		byte[] buffer = new byte[1024];
		MessageDigest md5 = MessageDigest.getInstance(hashType);
		int numRead = 0;
		while ((numRead = iStream.read(buffer)) > 0) {
			md5.update(buffer, 0, numRead);
		}
		return toHexString(md5.digest());
	}

	public static String getHash(byte[] bytes, String hashType) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance(hashType);
		md5.update(bytes, 0, 0);
		return toHexString(md5.digest());
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println(getHash("c:/bar.emf","MD5").toUpperCase());
	}
}
