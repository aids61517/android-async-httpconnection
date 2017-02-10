package aids61517.http;

import android.util.Log;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by deming_huang on 2015/12/30.
 */
public class HttpRequest {
    public static final int PARALLEL = 0;
    public static final int SERIES = 1;
    public static final String REQUEST_TYPE_GET = "GET";
    public static final String REQUEST_TYPE_POST = "POST";
    public static final String REQUEST_TYPE_DELETE = "DELETE";
    public static final String REQUEST_TYPE_PUT = "PUT";
    private String TAG = HttpRequest.class.getSimpleName();
    private static String DEFAULT_CHARSET = "UTF-8";
    private final int DEFAULT_TIMEOUT = 10000;
    private HashMap<String, String> mHeaders;
    private HashMap<String, String> mBodyParams;
    private HashMap<String, String> mUrlParams;
    private String mUrl;
    private String mRequestMethodType;
    private AsyncConnectTask mConnect;
    private String mCharsetName;
    private HttpResponseHandler mResponseHandler;
    private int mTimeout = DEFAULT_TIMEOUT;
    private static int mConnectType = 0;

    public HttpRequest(String url, String charsetName) {
        this(url, charsetName, null);
    }

    public HttpRequest(String url, String charsetName, HttpResponseHandler httpResponseHandler) {
        this.mUrl = url;
        this.mHeaders = new HashMap<>();
        this.mBodyParams = new HashMap<>();
        this.mUrlParams = new HashMap<>();
        this.mCharsetName = charsetName;
        mResponseHandler = httpResponseHandler;
    }

    public HttpRequest(String url) {
        this(url, DEFAULT_CHARSET, null);
    }

    public HttpRequest(String url, HttpResponseHandler httpResponseHandler) {
        this(url, DEFAULT_CHARSET, httpResponseHandler);
    }

    public void addUrlParam(String key, String value) {
        mUrlParams.put(key, value);
    }

    public void addUrlParam(String key, int value) {
        addUrlParam(key, String.valueOf(value));
    }

    public void setResponseHandler(HttpResponseHandler httpResponseHandler) {
        mResponseHandler = httpResponseHandler;
    }

    public HttpResponseHandler getResponseHandler() {
        return mResponseHandler;
    }

    public void addHeader(String name, String value) {
        mHeaders.put(name, value);
    }


    public static void setDefaultCharset(String charsetName) {
        DEFAULT_CHARSET = charsetName;
    }

    public static String getDefaultCharset() {
        return DEFAULT_CHARSET;
    }

    public String getCharsetName() {
        return mCharsetName;
    }

    public void setCharsetName(String charsetName) {
        this.mCharsetName = charsetName;
    }

    public String getRequestMethodType() {
        return mRequestMethodType;
    }

    public String getHeader(String headerName) {
        return mHeaders.get(headerName);
    }

    public HashMap<String, String> getHeaders() {
        return mHeaders;
    }

    public HashMap<String, String> getBodyParams() {
        return mBodyParams;
    }

    public void addBodyParam(String key, String value) {
        mBodyParams.put(key, value);
    }

    public void addBodyParam(String key, int value) {
        mBodyParams.put(key, String.valueOf(value));
    }

    public void addBodyParam(String key, double value) {
        mBodyParams.put(key, String.valueOf(value));
    }

    public boolean sendGetRequest() {
        mRequestMethodType = REQUEST_TYPE_GET;
        return sendRequest();
    }

    public boolean sendPostRequest() {
        mRequestMethodType = REQUEST_TYPE_POST;
        return sendRequest();
    }

    public boolean sendDeleteRequest() {
        mRequestMethodType = REQUEST_TYPE_DELETE;
        return sendRequest();
    }

    public boolean sendPutRequest() {
        mRequestMethodType = REQUEST_TYPE_PUT;
        return sendRequest();
    }

    private boolean sendRequest() {
        if ((mConnect == null || mConnect.isConnect())) {
            mConnect = new AsyncConnectTask(this, mResponseHandler);
            if (mConnectType == PARALLEL) {
                mConnect.executeOnExecutor(AsyncConnectTask.THREAD_POOL_EXECUTOR);
            } else {
                mConnect.execute();
            }
            return true;
        }
        return false;
    }

    public URL getUrl() {
        String urlPath = "";
        try {
            if (!mUrl.startsWith("http://") && !mUrl.startsWith("https://")) {
                mUrl = "http://" + mUrl;
            }

            if (mUrl.contains("?")) {
                String urlParam = mUrl.substring(mUrl.indexOf("?") + 1);
                String[] params = urlParam.split("&");
                for (String param : params) {
                    String key = param.substring(0, param.indexOf("="));
                    String value = param.substring(param.indexOf("=") + 1);
                    mUrlParams.put(key, value);
                }
                mUrl = mUrl.substring(0, mUrl.indexOf("?"));
            }

            for (Map.Entry<String, String> param : mUrlParams.entrySet()) {
                if (urlPath.isEmpty()) {
                    urlPath += "?";
                } else {
                    urlPath += "&";
                }
                if (param.getValue() == null) {
                    urlPath += param.getKey() + "=";
                } else {
                    urlPath += param.getKey() + "=" + URLEncoder.encode(param.getValue(), getCharsetName());
                }
            }

            return new URL(mUrl + urlPath);
        } catch (Exception e) {
            Log.d(TAG, "getUrl error:" + e.toString());
            return null;
        }
    }

    public boolean cancel() {
        return mConnect.cancel(false);
    }

    public boolean isCancel() {
        return mConnect.isCancelled();
    }

    public void setTimeout(int mTimeout) {
        this.mTimeout = mTimeout;
    }

    public int getTimeout() {
        return mTimeout;
    }

    public static void setConnectType(int type) {
        mConnectType = type;
    }
}
