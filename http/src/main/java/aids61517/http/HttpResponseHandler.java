package aids61517.http;

import android.util.Log;

import java.util.List;
import java.util.Map;

/**
 * Created by deming_huang on 2015/12/31.
 */
public abstract class HttpResponseHandler {
    protected int responseCode;
    protected Map<String, List<String>> headerFields;
    protected int requestCode;

    public HttpResponseHandler() {
        requestCode = 0;
    }

    public HttpResponseHandler(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setHeaderFields(Map<String, List<String>> headerFields) {
        this.headerFields = headerFields;
    }

    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    public abstract void onSuccess(String result);

    public void onFail(String errorCode, String errorMessage) {
        Log.d(this.getClass().getSimpleName(), "errorCode = " + errorCode + ", errorMessage = " + errorMessage);
    }
}
