package com.test.fuckgdufenet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends Activity {
    private static String TAG="xingtong";
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    TextView ret_text;
    SharedPreferences sharedPreferences;

    public static String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            // Wi-Fi is not enabled, return null or handle the case accordingly
            return null;
        }

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        byte[] bytes = new byte[4];
        int k=4;
        for (int i = 0; i < 4; ++i) {
            bytes[--k] = (byte)(ipAddress >> ((3 - i) * 8));
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((b & 0xFF)).append(".");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("account", MODE_PRIVATE);
        String wlan_ip = getWifiIpAddress(this);
        if (wlan_ip==null){
            Toast.makeText(this,"请打开wifi连接GDUFE后重试",Toast.LENGTH_SHORT).show();
            return;
        }
        EditText account_edit = findViewById(R.id.account_input);
        EditText pwd_edit = findViewById(R.id.password_input);
        ret_text = findViewById(R.id.json_ret);
        Button send_button = findViewById(R.id.send_request);

        String stored_account = sharedPreferences.getString("my_account", "");
        String stored_pwd = sharedPreferences.getString("my_pwd", "");

        account_edit.setText(stored_account);
        pwd_edit.setText(stored_pwd);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = account_edit.getText().toString();
                String password = pwd_edit.getText().toString();
                if (account.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"请输入账号或密码",Toast.LENGTH_SHORT).show();

                }else {
                    String base_url = "http://100.64.13.17:801/eportal/portal/login?callback=dr1003&login_method=1&user_account=%s&user_password=%s&wlan_user_ip=%s&wlan_user_ipv6=&wlan_user_mac=000000000000&wlan_ac_ip=100.64.13.18&wlan_ac_name=&jsVersion=4.1.3&terminal_type=1&lang=zh-cn&v=%s&lang=zh";
                    Random random = new Random();
                    int no_cache = random.nextInt(8000)+1000;
                    String rurl = String.format(base_url,"%2C0%2C"+account,password,wlan_ip,no_cache+"");
                    Log.d(TAG,rurl);
                    getAsynHttp(rurl);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("my_account", account);
                    editor.putString("my_pwd", password);
                    editor.apply();
                }

            }
        });

    }


    private void getAsynHttp(String rurl) {
        mOkHttpClient=new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(rurl);

        Request request = requestBuilder.build();
        Call mcall= mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(TAG, "network---" + str);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ret_text.setText(str);
                    }
                });
            }
        });
    }
}