package com.vhall.live.http;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class VHAPI {

    static OkHttpClient mOkHttpClient;
    private static final String url = "http://e.vhall.com/api/vhallapi/v2/webinar/list";

    /**
     * 公共请求方法
     */
    public static void post(HashMap<String, String> hashMap, Callback callback) {
        RequestBody body = getRequestBody(hashMap);
        post(url, body, callback);
    }

    private static RequestBody getRequestBody(HashMap<String, String> hashMap) {
        String param = "";
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : hashMap.keySet()) {
            param = param + key + ":" + hashMap.get(key) + "\n";
            builder.add(key, hashMap.get(key));
        }
        return builder.build();
    }

    protected static void post(String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);
    }

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                    .hostnameVerifier(new TrustAllHostnameVerifier())
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.SECONDS)
                    .build();
        }
        return mOkHttpClient;
    }

    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {

        }
        return ssfFactory;
    }
}
