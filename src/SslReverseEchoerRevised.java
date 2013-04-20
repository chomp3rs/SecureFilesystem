import java.io.*;
import java.security.*;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class SslReverseEchoerRevised {
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage:");
			System.out.println("   java SslReverseEchoerRevised ksName ksPass ctPass");
			return;
		}
		String ksName = args[0];
		char[] ksPass = args[1].toCharArray();
		char[] ctPass = args[2].toCharArray();
		System.setProperty("javax.net.ssl.trustStore", args[0]);
		System.setProperty("javax.net.ssl.trustStorePassword", args[1]);
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(ksName), ksPass);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ctPass);
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(8888);
			s.setNeedClientAuth(true);
			printServerSocketInfo(s);
			SSLSocket c;
			//**********************CHANGE ME PUBLIC KEY*********************
			byte[] pubKey = "rfvfrfvrfvrfvrfv".getBytes();
			//*****************************************************

			while (true) {
				c = (SSLSocket) s.accept();
				System.out.println("Accepted a connection"); // debug line

				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
				BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
				String com = r.readLine();
				System.out.println("Command: " + com);//debug line
				String command[];
				command = com.split(" ");
				System.out.println("User: " + command[0]);
				// start of the parsing for commands
				if (command[1].equals("get")) {
					System.out.println("Went into get");
					File file = new File(command[2] + ".txt");
					File filemeta = new File(command[2] + "-meta.txt");
					if (file.exists() && filemeta.exists()) {
						try {
							String encTextMeta = "", plainTextMeta, plainText, encText;
							String metaData[];
							BufferedReader br = new BufferedReader(new FileReader(command[2] + "-meta.txt"));
							String inputLine;
							while ((inputLine = br.readLine()) != null) {
								encTextMeta += inputLine + "\n";
							}
							br.close();
							metaData = encTextMeta.split("\n");
							plainTextMeta = decrypt(metaData[0], pubKey);
							metaData[0] = plainTextMeta;
							System.out.println(Arrays.toString(metaData));//debug line
							for (int i = 1; i < metaData.length; ++i) {
								if (command[0].equals(metaData[i])) {
									BufferedReader br2 = new BufferedReader(new FileReader(command[2] + ".txt"));
									encText = br2.readLine();
									br2.close();
									plainText = decrypt(encText, metaData[0].getBytes());
									w.write(plainText, 0, plainText.length());
									w.newLine();
									w.flush();
								}
							}
						} catch (Exception e) {

						}
					}
				} else if (command[1].equals("put")) {
					System.out.println("Went into put");
					try {
						String encText;
						String encMeta;
						String fileText = command[3].replaceAll("_", " ");
						
						byte[] hash = hasher(fileText);
						String hashString = new String(hash);
						// encrypt the file and the meta file
						encText = encrypt(fileText, hash);
						encMeta = encrypt(hashString, pubKey);
						encMeta = encMeta  + "\n" + command[0];
						File file = new File(command[2] + ".txt");
						File filemeta = new File(command[2] + "-meta.txt");
						if (!file.exists()) {
							file.createNewFile();
						}
						if (!filemeta.exists()) {
							filemeta.createNewFile();
						}
						FileWriter fw = new FileWriter(file.getAbsoluteFile());
						FileWriter fw2 = new FileWriter(filemeta.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						BufferedWriter bw2 = new BufferedWriter(fw2);
						bw.write(encText);
						bw2.write(encMeta);
						bw.close();
						bw2.close();
						
						w.write("Successfully put " + command[2], 0, ("Successfully put " + command[2]).length());
						w.newLine();
						w.flush();
					} catch (Exception e) {

					}
				} else if (command[1].equals("delegate")) {
					System.out.println("Went into delegate");
					File filemeta = new File(command[2] + "-meta.txt");
					if (filemeta.exists()) {
						try {
							String encTextMeta = "";
							String metaData[];
							BufferedReader br = new BufferedReader(new FileReader(command[2] + "-meta.txt"));
							String inputLine;
							while ((inputLine = br.readLine()) != null) {
								encTextMeta += inputLine + "\n";
							}
							br.close();
							metaData = encTextMeta.split("\n");
							System.out.println("meta: " + Arrays.toString(metaData));//debug line
							for (int i = 1; i < metaData.length; ++i) {
								if (command[0].equals(metaData[i])) {
									encTextMeta = encTextMeta + "\n" + command[3];
									FileWriter fileWritter = new FileWriter(filemeta.getName());
									BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
									bufferWritter.write(encTextMeta);
									bufferWritter.close();
									
									w.write("Successfully added " + command[3], 0, ("Successfully added " + command[3]).length());
									w.newLine();
									w.flush();
								}
							}
						} catch (Exception e) {

						}
					}
				} else {
					w.write("You made a type!", 0, ("You made a type!").length());
					w.newLine();
					w.flush();
				}
			}

		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	public static String encrypt(String data, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		String strCipherText = new String();

		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher aesCipher = Cipher.getInstance("AES");

		aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);

		byte[] byteDataToEncrypt = data.getBytes();
		byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt);
		strCipherText = new BASE64Encoder().encode(byteCipherText);

		return strCipherText;
	}

	public static String decrypt(String cipherText, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		String strDecryptedText = new String();
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] byteDecryptedText = aesCipher.doFinal(new BASE64Decoder().decodeBuffer(cipherText));
		strDecryptedText = new String(byteDecryptedText);
		return strDecryptedText;
	}

	public static byte[] hasher(String fi) {
		byte[] hash = { -1 };
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(fi.getBytes("UTF-8"));

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash;
	}

	private static void printSocketInfo(SSLSocket s) {
		System.out.println("Socket class: " + s.getClass());
		System.out.println("   Remote address = " + s.getInetAddress().toString());
		System.out.println("   Remote port = " + s.getPort());
		System.out.println("   Local socket address = " + s.getLocalSocketAddress().toString());
		System.out.println("   Local address = " + s.getLocalAddress().toString());
		System.out.println("   Local port = " + s.getLocalPort());
		System.out.println("   Need client authentication = " + s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		try {
			System.out.println("Session class: " + ss.getClass());
			System.out.println("   Cipher suite = " + ss.getCipherSuite());
			System.out.println("   Protocol = " + ss.getProtocol());
			System.out.println("   PeerPrincipal = " + ss.getPeerPrincipal().getName());
			System.out.println("   LocalPrincipal = " + ss.getLocalPrincipal().getName());
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	private static void printServerSocketInfo(SSLServerSocket s) {
		System.out.println("Server socket class: " + s.getClass());
		System.out.println("   Socker address = " + s.getInetAddress().toString());
		System.out.println("   Socker port = " + s.getLocalPort());
		System.out.println("   Need client authentication = " + s.getNeedClientAuth());
		System.out.println("   Want client authentication = " + s.getWantClientAuth());
		System.out.println("   Use client mode = " + s.getUseClientMode());
	}
}

/*
 * private static PublicKey getPemPublicKey(String filename, String algorithm)
 * throws Exception { File f = new File(filename); FileInputStream fis = new
 * FileInputStream(f); DataInputStream dis = new DataInputStream(fis); byte[]
 * keyBytes = new byte[(int) f.length()]; dis.readFully(keyBytes); dis.close();
 * 
 * String temp = new String(keyBytes); String publicKeyPEM =
 * temp.replace("-----BEGIN PUBLIC KEY-----\n", ""); publicKeyPEM =
 * publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
 * 
 * 
 * Base64 b64 = new Base64(); byte [] decoded = b64.decode(publicKeyPEM);
 * 
 * X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded); KeyFactory kf =
 * KeyFactory.getInstance(algorithm); return kf.generatePublic(spec); }
 */

/*
 * // Get public key String alias = "server"; PublicKey publicKey =
 * getPemPublicKey("server.pem","JKS"); System.out.println("Got public key");//
 * debug line System.out.println("Got into the if statment");//debug line
 * //publicKey = ks.getCertificate(alias).getPublicKey();
 * System.out.println("blah");//debug line byte[] pubKey =
 * publicKey.getEncoded(); System.out.println("Public Key: " +
 * pubKey.toString());//debug line TCPRun t = new TCPRun((SSLSocket) s.accept(),
 * pubKey); System.out.println("created new TCPRun");//debug line t.start();
 * t.join();
 * 
 * printsocketInfo(c); BufferedWriter w = new BufferedWriter(new
 * OutputStreamWriter(c.getOutputStream())); BufferedReader r = new
 * BufferedReader(new InputStreamReader(c.getInputStream())); String m =
 * "Welcome to SSL Reverse Echo Server." + " Please type in some words.";
 * w.write(m, 0, m.length()); w.newLine(); w.flush(); while ((m = r.readLine())
 * != null) { System.out.println(m);
 * 
 * if (m.equals(".")) break; char[] a = m.toCharArray(); int n = a.length; for
 * (int i=0; i<n/2; i++) { char t = a[i]; a[i] = a[n-1-i]; a[n-i-1] = t; }
 * w.write(a,0,n); w.newLine(); w.flush();
 * 
 * } w.close(); r.close(); c.close(); s.close();
 */