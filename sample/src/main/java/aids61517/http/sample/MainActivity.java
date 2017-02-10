package aids61517.http.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import aids61517.http.HttpRequest;
import aids61517.http.HttpResponseHandler;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String URL = "https://www.google.com.tw/";
    private Button mSendRequestButton;
    private Button mSendRequestButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendRequestButton = (Button) findViewById(R.id.send_button);
        mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpRequest httpRequest = new HttpRequest(URL, new GetResponseHandler());
                httpRequest.addHeader("header key", "header value");
                httpRequest.sendGetRequest();

                //send POST request
//                httpRequest.addBodyParam("post key","post value");
//                httpRequest.sendPostRequest();
            }
        });

        mSendRequestButton2 = (Button) findViewById(R.id.send2_button);
        mSendRequestButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpRequest httpRequest = new HttpRequest(URL);
                httpRequest.addHeader("header key", "header value");
                httpRequest.sendGetRequest();

                //send POST request
//                httpRequest.addBodyParam("post key","post value");
//                httpRequest.sendPostRequest();
            }
        });

    }

    private class GetResponseHandler extends HttpResponseHandler {

        @Override
        public void onSuccess(String result) {
            Log.d(TAG, "result = " + result);
        }
    }
}
