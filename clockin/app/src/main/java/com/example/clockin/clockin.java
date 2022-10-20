package com.example.clockin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.SimpleFormatter;


public class clockin extends Fragment {
    String TAG = "clockin";
    String request_msg = "";

    ImageButton imageButton;
    Button test;
    TextView isClockin;

    SharedPreferences sharedPreferences;
    SharedPreferences accounts;
    SharedPreferences.Editor editor, acc_editor;

    String url_getToken = "https://stu.eurasia.edu/yqsb/login/in";
    String url_clockin = "https://stu.eurasia.edu/yqsb/jkdj/save";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clockin, container, false);
        sharedPreferences = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        accounts = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        acc_editor = accounts.edit();

//        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,0.9f,1.0f,0.9f);
        ScaleAnimation pressed = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        pressed.setDuration(100);
        pressed.setFillAfter(true);
        pressed.setFillAfter(true);
        ScaleAnimation unpressed = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        unpressed.setDuration(50);

        isClockin = view.findViewById(R.id.isClockin);
        Log.d(TAG, "onCreateView: START TO EXAM!!!!!!!!!!!");
        examClockin();

        imageButton = view.findViewById(R.id.send);
        imageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    imageButton.startAnimation(pressed);
//                    Toast.makeText(arg0.getContext(), "down", Toast.LENGTH_SHORT).show();

                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    imageButton.startAnimation(unpressed);
//                    Toast.makeText(arg0.getContext(), "up", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getActivity(), "click", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        boolean is_success = send(url_getToken, url_clockin);
                        if (is_success) {
                            changeClockin(1);
                        }
                    }
                }).start();


            }
        });


//        test = view.findViewById(R.id.test);
//        test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                shareTojson();
//            }
//        });



        return view;
    }

    public void examClockin(){
        String date = accounts.getString("dayclockTime","");
        Log.d(TAG, "examClockin: "+date);
        if (!date.equals("")){
            Log.d(TAG, "examClockin: "+Tools.isOverTime(date));
            if (Tools.isOverTime(date)){
                changeClockin(0);
            } else {
                changeClockin(1);
            }
        } else {
            changeClockin(2);
        }

    }

    public void changeClockin(int is){
        switch (is) {
            case 1:
                isClockin.setText("今日已打卡");
                isClockin.setBackgroundColor(Color.parseColor("#FF03DAC5"));
                break;
            case 0:
                isClockin.setText("今日未打卡");
                isClockin.setBackgroundColor(Color.parseColor("#2d3033"));
                break;
            case 2:
                isClockin.setText("无记录");
                isClockin.setBackgroundColor(Color.parseColor("#2d3033"));
                break;
        }
    }



    public boolean send(String url_getToken, String url_clockin){
        try {
            Long timestamp = System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = simpleDateFormat.format(new Date(timestamp));


            String uid = sharedPreferences.getString("xh", "");
            String pwd = accounts.getString("pwd", "");
            if (uid.equals("") || pwd.equals("")){
                Toast.makeText(getActivity(), "无学号或密码无法登录", Toast.LENGTH_SHORT).show();
                return false;
            }


            String token = "";
            if (accounts.getString("token","").equals("")){
                url_getToken = String.format(url_getToken+"?zh=%s&&mm=%s",uid,pwd);
                token = getToken(url_getToken);
                if (token.equals("")){
                    Toast.makeText(getActivity(), "Token错误", Toast.LENGTH_SHORT).show();
                    return false;
                }
                acc_editor.putString("token", token);
                acc_editor.putString("tokenTime", date);
                Toast.makeText(getActivity(), "Token已更新", Toast.LENGTH_SHORT).show();
            } else {
                if (Tools.isOverTime(accounts.getString("tokenTime",""))){
                    url_getToken = String.format(url_getToken+"?zh=%s&&mm=%s",uid,pwd);
                    token = getToken(url_getToken);
                    if (token.equals("")){
                        Toast.makeText(getActivity(), "Token错误", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    acc_editor.putString("token", token);
                    acc_editor.putString("tokenTime", date);
                    Toast.makeText(getActivity(), "Token已更新", Toast.LENGTH_SHORT).show();
                } else {
                    token = accounts.getString("token","");
                }
            }

            
            if (token.equals("")){
                return false;
            } else {
                int res = clockin(url_clockin, token);

                switch (res){
                    case 1:
                        Toast.makeText(getActivity(), "打卡完成", Toast.LENGTH_SHORT).show();
                        acc_editor.putBoolean("dayclock", true);
                        acc_editor.putString("dayclockTime", date);
                        acc_editor.apply();
                        break;
                    case 2:
                        Toast.makeText(getActivity(), "今日已登记!", Toast.LENGTH_SHORT).show();
                        acc_editor.putBoolean("dayclock", true);
                        acc_editor.putString("dayclockTime", date);
                        acc_editor.apply();
                        break;
                    case 0:
                        Toast.makeText(getActivity(), request_msg, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public String getToken(String url_getToken){
        try {
            String res = "";
            URL url = new URL(url_getToken);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200){
                InputStream in = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String tmp;
                while (true){
                    tmp = bufferedReader.readLine();
                    if (tmp == null){
                        break;
                    }
                    res+=tmp;
                }
                String token;
                JSONObject jsonObject = new JSONObject(res);
                System.out.println(jsonObject.get("success"));
                System.out.println(jsonObject.get("success").getClass());

                if ((Boolean) jsonObject.get("success")){
                    token = jsonObject.getString("token");
                    return token;
                } else {
                    Toast.makeText(getActivity(), jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    return "";
                }

//                Map<String, String> map = MapandJson.jsonTomap(res);

//                if (map.get("success").equals("true")){
//                    Toast.makeText(getActivity(),"获取token成功"+map.get("token"), Toast.LENGTH_SHORT).show();
//
//                    return map.get("token");
//                } else {
//                    Toast.makeText(getActivity(), res, Toast.LENGTH_SHORT).show();
//                    return "";
//                }
            } else {
                Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT).show();
                return "";
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "未知错误", Toast.LENGTH_SHORT).show();
            return "";
        }

    }



    public int clockin(String url_clockin, String token) throws Exception {
        String data = shareTojson();
        JSONObject jsonObject = new JSONObject(data);
        String json = jsonObject.toString();

        JSONObject res = new JSONObject(_clockin(url_clockin, json, token));
        System.out.println(res);
        if (res.getBoolean("success")){
            return 1;
        } else {
            if (res.getString("msg").equals("今日已登记!")){
                return 2;
            }
            request_msg = res.getString("msg");
            return 0;
        }



    }

    public String _clockin(String url_clockin, String json, String token) throws Exception {
        URL url = new URL(url_clockin);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.addRequestProperty("Authorization", token);
        httpURLConnection.addRequestProperty("token", token);
        httpURLConnection.addRequestProperty("Content-Type", "application/json;");

        httpURLConnection.connect();

        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        dataOutputStream.writeChars(json);
        dataOutputStream.flush();
        dataOutputStream.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader( httpURLConnection.getInputStream(), "utf-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String tmp;
        while (true) {
            tmp = bufferedReader.readLine();
            if (tmp == null){
                break;
            }
            stringBuilder.append(tmp);
        }
        bufferedReader.close();
        httpURLConnection.disconnect();
        return stringBuilder.toString();
    }



    public String shareTojson(){
        Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();

        JSONObject jsonObject = new JSONObject(map);

        String json = jsonObject.toString();
        Log.d(TAG, "shareTojson: "+json);
        return json;
    }



}