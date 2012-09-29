package com.yyl.myrmex.tlsupdater;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.content.Context;

public class MyTLSClient extends DefaultHttpClient {

	final Context context;

	public MyTLSClient(Context context) {
		this.context = context;
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry registry = new SchemeRegistry();
		// Register for port 443 our SSLSocketFactory with our keystore
		// to the ConnectionManager
		registry.register(new Scheme("https", newSslSocketFactory(), 443));
//		registry.register(new Scheme("http", newSslSocketFactory(), 2727));
		SingleClientConnManager cm = new SingleClientConnManager(getParams(),
				registry);
		return cm;
	}

	private SSLSocketFactory newSslSocketFactory() {
		try {
			// Get an instance of the Bouncy Castle KeyStore format
			KeyStore trusted = KeyStore.getInstance("BKS");
			// Get the raw resource, which contains the keystore with
			// your trusted certificates (root and any intermediate certs)
			InputStream in = context.getResources().openRawResource(
					R.raw.mykeystore);
			try {
				// Initialize the keystore with the provided trusted
				// certificates
				// Also provide the password of the keystore
				trusted.load(in, "tlstest".toCharArray());
			} finally {
				in.close();
			}
			// Pass the keystore to the SSLSocketFactory. The factory is
			// responsible
			// for the verification of the server certificate.
			SSLSocketFactory sf = new SSLSocketFactory(trusted);
			// Hostname verification from certificate
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return sf;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
