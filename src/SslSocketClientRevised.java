/**
 * SslSocketClientRevised.java
 * Copyright (c) 2005 by Dr. Herong Yang
 */
import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;
public class SslSocketClientRevised {
   public static void main(String[] args) {
      if (args.length<3) {
         System.out.println("Usage:");
         System.out.println(
            "   java SslReverseEchoerRevised ksName ksPass ctPass");
         return;
      }
      String ksName = args[0];
      char[] ksPass = args[1].toCharArray();
      char[] ctPass = args[2].toCharArray();
      System.setProperty("javax.net.ssl.trustStore", args[0]);
      System.setProperty("javax.net.ssl.trustStorePassword", 
         args[1]);
      BufferedReader in = new BufferedReader(
         new InputStreamReader(System.in));
      PrintStream out = System.out;
      try {
         KeyStore ks = KeyStore.getInstance("JKS");
         ks.load(new FileInputStream(ksName), ksPass);
         KeyManagerFactory kmf = 
         KeyManagerFactory.getInstance("SunX509");
         kmf.init(ks, ctPass);
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(kmf.getKeyManagers(), null, null);
         SSLSocketFactory f = sc.getSocketFactory();
         SSLSocket c =
           (SSLSocket) f.createSocket("localhost", 8888);
         printSocketInfo(c);
         c.startHandshake();
         BufferedWriter w = new BufferedWriter(
            new OutputStreamWriter(c.getOutputStream()));
         BufferedReader r = new BufferedReader(
            new InputStreamReader(c.getInputStream()));
         String m = null;
         while ((m=r.readLine())!= null) {
            out.println(m);
            m = in.readLine();
            w.write(m,0,m.length());
            w.newLine();
            w.flush();
         }
         w.close();
         r.close();
         c.close();
      } catch (Exception e) {
         System.err.println(e.toString());
      }
   }
   private static void printSocketInfo(SSLSocket s) {
      System.out.println("Socket class: "+s.getClass());
      System.out.println("   Remote address = "
         +s.getInetAddress().toString());
      System.out.println("   Remote port = "+s.getPort());
      System.out.println("   Local socket address = "
         +s.getLocalSocketAddress().toString());
      System.out.println("   Local address = "
         +s.getLocalAddress().toString());
      System.out.println("   Local port = "+s.getLocalPort());
      System.out.println("   Need client authentication = "
         +s.getNeedClientAuth());
      SSLSession ss = s.getSession();
      try {
         System.out.println("Session class: "+ss.getClass());
         System.out.println("   Cipher suite = "
            +ss.getCipherSuite());
         System.out.println("   Protocol = "+ss.getProtocol());
         System.out.println("   PeerPrincipal = "
            +ss.getPeerPrincipal().getName());
         System.out.println("   LocalPrincipal = "
            +ss.getLocalPrincipal().getName());
         System.out.println("   PeerPrincipal = "
            +ss.getPeerPrincipal().getName());
      } catch (Exception e) {
         System.err.println(e.toString());
      }
   }
}