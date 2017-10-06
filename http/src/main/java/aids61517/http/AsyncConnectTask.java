package aids61517.http;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by deming_huang on 2015/12/31.
 */
public class AsyncConnectTask extends AsyncTask<String, String, Boolean> {
    private final String TAG = AsyncConnectTask.class.getSimpleName();
    private HttpRequest mRequest;
    private boolean mIsConnecting = false;
    private HttpURLConnection mConnection;
    private DataOutputStream mPostBodyParamOutputStream;
    private StringBuilder mResultStringBuilder = new StringBuilder();
    private BufferedReader mReadResultInputStream;
    private HttpResponseHandler mResponseHandler;

    public AsyncConnectTask(HttpRequest request, HttpResponseHandler responseHandler) {
        super();
        mRequest = request;
        this.mResponseHandler = responseHandler;
    }

    public boolean isConnect() {
        return mIsConnecting;
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
                mPostBodyParamOutputStream = new DataOutputStream(mConnection.getOutputStream());
                mPostBodyParamOutputStream.writeBytes(paramString);
                mPostBodyParamOutputStream.flush();
            }
        } catch (IOException exception) {
            Log.d(TAG, "setBodyParams IOException:" + exception.toString());
            throw exception;
        } catch (NullPointerException exception) {
            Log.d(TAG, "setBodyParams NullPointerException:" + exception.toString());
            throw exception;
        }
    }

    private void readResult(boolean successful) throws Exception {
        try {
            if (successful) {
                mReadResultInputStream = new BufferedReader(new InputStreamReader(mConnection.getInputStream(), mRequest.getCharsetName()));
            } else {
                mReadResultInputStream = new BufferedReader(new InputStreamReader(mConnection.getErrorStream(), mRequest.getCharsetName()));
            }
            mResponseHandler.setHeaderFields(mConnection.getHeaderFields());
            String read;
            while ((read = mReadResultInputStream.readLine()) != null) {
                mResultStringBuilder.append(read);
            }
        } catch (IOException exception) {
            throw exception;
        }
    }

    private void closeStreamAndReader() {
        try {
            if (mConnection != null) {
                mConnection.disconnect();
            }
            if (mPostBodyParamOutputStream != null) {
                mPostBodyParamOutputStream.close();
                mPostBodyParamOutputStream = null;
            }
            if (mReadResultInputStream != null) {
                mReadResultInputStream.close();
                mReadResultInputStream = null;
            }
        } catch (IOException e) {
            Log.d(TAG, "closeStreamAndReader error:" + e.toString());
        }
    }

    private void setSSLSocket() {
        try {
            String e = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(e);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) mConnection;
            httpsURLConnection.setSSLSocketFactory(ctx.getSocketFactory());
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
            mIsConnecting = true;
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
                readResult(isRequestSuccess(responseCode));
            }
            mConnection.disconnect();
            isSuccess = true;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
            mResultStringBuilder.append(e.toString());
        }
        closeStreamAndReader();
        return isSuccess;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        mIsConnecting = false;
        if (mResponseHandler == null) {
            return;
        }
        if (isSuccess) {
            mResponseHandler.onSuccess(mResultStringBuilder.toString());
        } else {
            mResponseHandler.onFail(String.valueOf(mResponseHandler.getResponseCode()), mResultStringBuilder.toString());
        }

        mResponseHandler = null;
        mRequest = null;
    }

    private boolean isRequestSuccess(int httpStatusCode) {
        return httpStatusCode >= 200 && httpStatusCode < 300;
    }
}
