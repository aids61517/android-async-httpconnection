package aids61517.http;

import android.util.Log;

import java.util.List;
import java.util.Map;

/**
 * Created by deming_huang on 2015/12/31.
 */
public abstract class HttpResponseHandler {
    protected final static String FIELD_COOKIE = "Set-Cookie";
    private int mResponseCode;
    private Map<String, List<String>> mHeaderFields;
    private int mRequestCode;
    private List<String> mCookieList;

    public HttpResponseHandler() {
        mRequestCode = 0;
    }

    public HttpResponseHandler(int requestCode) {
        mRequestCode = requestCode;
    }

    public void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public int getResponseCode() {
        return mResponseCode;
    }

    public void setResponseCode(int responseCode) {
        mResponseCode = responseCode;
    }

    public void setHeaderFields(Map<String, List<String>> headerFields) {
        mHeaderFields = headerFields;
    }

    public Map<String, List<String>> getHeaderFields() {
        return mHeaderFields;
    }

    public List<String> getCookieList() {
        if (mCookieList == null) {
            mCookieList = mHeaderFields.get(FIELD_COOKIE);
        }

        return mCookieList;
    }

    public abstract void onSuccess(String result);

    public void onFail(String errorCode, String errorMessage) {
        Log.d(this.getClass().getSimpleName(), "errorCode = " + errorCode + ", errorMessage = " + errorMessage);
    }
}
