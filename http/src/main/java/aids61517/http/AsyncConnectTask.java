package aids61517.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by deming_huang on 2015/12/31.
 */
public class AsyncConnectTask extends AsyncTask<String, String, Boolean> {
    private final String TAG = AsyncConnectTask.class.getSimpleName();
    private HttpRequest mRequest;
    private boolean isConnecting = false;
    private HttpURLConnection mConnection;
    private DataOutputStream postBodyParamOutputStream;
    private StringBuilder resultStringBuilder = new StringBuilder();
    private BufferedReader readResultInputStream;
    private HttpResponseHandler mResponseHandler;

    public AsyncConnectTask(HttpRequest request, HttpResponseHandler responseHandler) {
        super();
        mRequest = request;
        this.mResponseHandler = responseHandler;
    }

    public boolean isConnect() {
        return isConnecting;
    }

    public void setHeaders() {
        for (Map.Entry<String, String> header : mRequest.getHeaders().entrySet()) {
            mConnection.setRequestProperty(header.getKey(), header.getValue());
        }
    }

    public void setBodyParams() throws Exception {
        String paramString = "";
        try {
            for (Map.Entry<String, String> param : mRequest.getBodyParams().entrySet()) {
                if (!paramString.isEmpty()) {
                    paramString += "&";
                }
                if (param.getValue() == null) {
                    paramString += param.getKey() + "=";
                } else {
                    paramString += param.getKey() + "=" + URLEncoder.encode(param.getValue(), mRequest.getCharsetName());
                }
            }

            if (!paramString.isEmpty()) {
                postBodyParamOutputStream = new DataOutputStream(mConnection.getOutputStream());
                postBodyParamOutputStream.writeBytes(paramString);
                postBodyParamOutputStream.flush();
            }
        } catch (IOException exception) {
            Log.d(TAG, "setBodyParams IOException:" + exception.toString());
            throw exception;
        } catch (NullPointerException exception) {
            Log.d(TAG, "setBodyParams NullPointerException:" + exception.toString());
            throw exception;
        }
    }

    public void readResult() throws Exception {
        try {
            readResultInputStream = new BufferedReader(new InputStreamReader(mConnection.getInputStream(), mRequest.getCharsetName()));
            mResponseHandler.setHeaderFields(mConnection.getHeaderFields());
            String read;
            while ((read = readResultInputStream.readLine()) != null) {
                resultStringBuilder.append(read);
            }
        } catch (IOException exception) {
            Log.d(TAG, "readResult error:" + exception.toString());
            throw exception;
        }
    }

    public void closeStreamAndReader() {
        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }
            if (postBodyParamOutputStream != null) {
                postBodyParamOutputStream.close();
            }
            if (readResultInputStream != null) {
                readResultInputStream.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "closeStreamAndReader error:" + e.toString());
        }
    }

    private void setSSLSocket() {
        try {
            String e = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(e);
            SSLContext ctx = SSLContext.getInstance("TLS");
            //ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            ctx.init(null, new TrustManager[]{new DefaultTrustManager()}, null);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) mConnection;
            httpsURLConnection.setSSLSocketFactory(ctx.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            Log.d(TAG, "setSSLSocket error");
        }
    }

    private boolean isHttpsConnect() {
        return mRequest.getUrl().toString().startsWith("https://");
    }

    private boolean isNeedSetBodyParams() {
        return mRequest.getRequestMethodType().equals(HttpRequest.REQUEST_TYPE_POST)
                || mRequest.getRequestMethodType().equals(HttpRequest.REQUEST_TYPE_DELETE)
                || mRequest.getRequestMethodType().equals(HttpRequest.REQUEST_TYPE_PUT);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean isSuccess = false;
        try {
            isConnecting = true;
            mConnection = (HttpURLConnection) mRequest.getUrl().openConnection();
            if (isHttpsConnect()) {
                setSSLSocket();
            }
            if (isNeedSetBodyParams()) {
                mConnection.setDoOutput(true);
            }
            setHeaders();
            mConnection.setConnectTimeout(mRequest.getTimeout());
            mConnection.setReadTimeout(mRequest.getTimeout());
            mConnection.setRequestMethod(mRequest.getRequestMethodType());
            mConnection.connect();

            if (isNeedSetBodyParams()) {
                setBodyParams();
            }

            int responseCode = mConnection.getResponseCode();
            if (mResponseHandler != null) {
                mResponseHandler.setResponseCode(responseCode);
                readResult();
            }
            mConnection.disconnect();

            isSuccess = true;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
            resultStringBuilder.append(e.toString());
        }
        closeStreamAndReader();
        return isSuccess;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        isConnecting = false;
        if (mResponseHandler == null) {
            return;
        }
        if (isSuccess) {
            mResponseHandler.onSuccess(resultStringBuilder.toString());
        } else {
            mResponseHandler.onFail(String.valueOf(mResponseHandler.getResponseCode()), resultStringBuilder.toString());
        }
    }


    private class DefaultTrustManager implements X509TrustManager {
        private DefaultTrustManager() {
        }

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
