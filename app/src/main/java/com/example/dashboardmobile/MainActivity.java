package com.example.dashboardmobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    
    private static final String URL_STR = "http://34.64.238.233:3520";
    private static final String TOKEN_URL_STR = "http://34.64.238.233:3520/alert/token";
    
    private static final String CHANNEL_ID = "123";
    
    private WebView webView;
    private WebSettings webSettings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webView = findViewById(R.id.web_view);
        webSettings = webView.getSettings();
        
        webView.setWebViewClient(new WebViewCustomClient());
        webView.setWebChromeClient(new WebChromeClient());
        
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);
        
        webView.loadUrl(URL_STR);
        
        createNotificationChannel();
        
        Button button = findViewById(R.id.button);
        
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("알림 제목")
                                .setContentText("알림 내용!!")
                                .setDefaults(Notification.DEFAULT_VIBRATE)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true);
                
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                
                notificationManager.notify(0, builder.build());
                
                Toast.makeText(MainActivity.this, "버튼 클릭 리스너", Toast.LENGTH_SHORT).show();
            }
        });
    
        
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
           @Override
           public void onComplete(final @NonNull Task<InstanceIdResult> task) {
               if (!task.isSuccessful()) {
                   Log.w("firebase", "getInstanceId failed", task.getException());
                   return;
               }
    
               final String token = task.getResult().getToken();
    
               AsyncTask.execute(new Runnable() {
                   @Override
                   public void run() {
                       try {
                           OkHttpClient client = new OkHttpClient();
                           RequestBody requestBody = new FormBody.Builder().add("token", token).build();
                           Request request = new Request.Builder()
                                   .url(TOKEN_URL_STR)
                                   .post(requestBody)
                                   .build();
                           Response response = client.newCall(request).execute();
                           String result = response.body().string();
    
                           Log.d("okhttptest", result);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               });
           }
       });
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private static class WebViewCustomClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}