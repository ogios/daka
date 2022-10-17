package com.example.clockin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class setting extends Fragment {
    String TAG = "setting";
    Map<String, String> idTovname;
    Map<String, String> idTohint;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    SharedPreferences accounts;
    SharedPreferences.Editor acc_editor;

    List<setting_item> setting_items = new ArrayList<setting_item>();

    RecyclerView recyclerView;
    Button save;
    Button reset;
    EditText uid;
    EditText pwd;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    String url_getToken = "https://stu.eurasia.edu/yqsb/login/in";


    public void showClockin(){
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment frag:fragments){
            if (frag.getTag().equals("clockin")){
                fragmentTransaction.show(frag);
            } else {
                fragmentTransaction.hide(frag);
            }
        }
//        fragmentTransaction.replace(R.id.FragContent, fragment);
        fragmentTransaction.commit();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.idTovname = new HashMap<String, String>();
        idTovname.put("xm", "姓名");
        idTovname.put("lxdh", "联系电话");
        idTovname.put("rctw", "体温");
        idTovname.put("fxdj", "风险等级");
        idTovname.put("xxdz", "详细地址");
        idTovname.put("jtzz", "家庭住址");
        idTovname.put("sfzs", "是否在陕西");
        idTovname.put("mqsxdz", "目前陕西地址");

        this.idTohint = new HashMap<String, String>();
        idTohint.put("xm", "姓名");
        idTohint.put("lxdh", "联系电话");
        idTohint.put("rctw", "体温");
        idTohint.put("fxdj", "低/中/高风险地区");
        idTohint.put("xxdz", "具体到区或街道");
        idTohint.put("jtzz", "仅省市");
        idTohint.put("sfzs", "不在-0; 在-1");
        idTohint.put("mqsxdz", "具体到区或街道");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        sharedPreferences = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        accounts = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
        acc_editor = accounts.edit();

        uid = view.findViewById(R.id.uid);
        pwd = view.findViewById(R.id.pwd);

        uid.setText(sharedPreferences.getString("xh",""));
        pwd.setText(accounts.getString("pwd",""));


        recyclerView = view.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        setting_Adapter setting_adapter = new setting_Adapter(setting_items, getActivity());
        recyclerView.setAdapter(setting_adapter);
        init(setting_adapter);

        save = view.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: 开始保存"+uid.getText().toString());
                editor.putString("xh",uid.getText().toString());
                acc_editor.putString("uid",uid.getText().toString());
                acc_editor.putString("pwd", pwd.getText().toString());

                List<setting_item> setting_items = setting_adapter.getItems();
                for (setting_item setting_item:setting_items){
                    editor.putString(setting_item.getId(),setting_item.getValue());
                }
                editor.apply();
                acc_editor.apply();
                Log.d(TAG, "onClick: 保存完成"+sharedPreferences.getString("xh",""));
                Log.d(TAG, "onClick: 保存完成"+ accounts.getString("pwd",""));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (send(url_getToken)){
                            Toast.makeText(getActivity(), "Token已更新", Toast.LENGTH_SHORT).show();
                            showClockin();
                        } else {
                            Toast.makeText(getActivity(), "Token更新失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();



            }
        });

        reset = view.findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                reset(setting_adapter);
            }
        });



        return view;
    }

    public boolean send(String url_getToken) {
        try {
            Long timestamp = System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = simpleDateFormat.format(new Date(timestamp));


            String uid = sharedPreferences.getString("xh", "");
            String pwd = accounts.getString("pwd", "");
            if (uid.equals("") || pwd.equals("")) {
                Toast.makeText(getActivity(), "无学号或密码无法登录", Toast.LENGTH_SHORT).show();
                return false;
            }


            String token = "";

            url_getToken = String.format(url_getToken + "?zh=%s&&mm=%s", uid, pwd);
            token = getToken(url_getToken);
            if (token.equals("")) {
                return false;
            } else {
                acc_editor.putString("token", token);
                acc_editor.putString("tokenTime", date);
                acc_editor.apply();
                return true;
            }

        } catch (Exception e) {
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void reset(setting_Adapter setting_adapter){
        editor.clear();
        editor.apply();
        init_setting();
        acc_editor.clear();
        acc_editor.apply();
        setting_adapter.clear();
        init(setting_adapter);
        uid.setText(sharedPreferences.getString("xh",""));
        pwd.setText(accounts.getString("pwd",""));


//        setting_adapter.add(new setting_item("xm", "姓名", ""));
//        setting_adapter.add(new setting_item("lxdh", "联系电话", ""));
//        setting_adapter.add(new setting_item("rctw", "体温", "37"));
//        setting_adapter.add(new setting_item("fxdj", "风险等级", "低风险地区", "低/中/高风险地区"));
//        setting_adapter.add(new setting_item("xxdz", "详细地址", "", "具体到区或街道"));
//        setting_adapter.add(new setting_item("jtzz", "家庭住址", "陕西省西安市","仅省市"));
//        setting_adapter.add(new setting_item("sfzs", "是否在陕西", "1", "不在-0; 在-1"));
//        setting_adapter.add(new setting_item("mqsxdz", "目前陕西地址", "西安欧亚学院", "具体到区或街道"));
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void init(setting_Adapter setting_adapter){

//        setting_adapter.add(new setting_item("xm", "姓名", sharedPreferences.getString("xm","")));
//        setting_adapter.add(new setting_item("lxdh", "联系电话", sharedPreferences.getString("lxdh","")));
//        setting_adapter.add(new setting_item("rctw", "体温", sharedPreferences.getString("rctw","")));
//        setting_adapter.add(new setting_item("fxdj", "风险等级", sharedPreferences.getString("fxdj",""), "低/中/高风险地区"));
//        setting_adapter.add(new setting_item("xxdz", "详细地址", sharedPreferences.getString("xxdz",""), "具体到区或街道"));
//        setting_adapter.add(new setting_item("jtzz", "家庭住址", sharedPreferences.getString("jtzz",""),"仅省市"));
//        setting_adapter.add(new setting_item("sfzs", "是否在陕西", sharedPreferences.getString("sfzs",""), "不在-0; 在-1"));
//        setting_adapter.add(new setting_item("mqsxdz", "目前陕西地址", sharedPreferences.getString("mqsxdz",""), "具体到区或街道"));

        idTovname.forEach((key,val)->{
            setting_adapter.add(new setting_item(key, val, sharedPreferences.getString(key, ""), idTohint.get(key)));
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void init_setting(){
        try {
            InputStream in = getResources().openRawResource(R.raw.defaultsetting);
            int length = in.available();
            byte[] tmp = new byte[length];
            in.read(tmp);
//                System.out.println(new String(tmp));
            Map<String, String> map = Tools.jsonTomap(new String(tmp));
            mapToshared(map);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void mapToshared(Map<String, String> map){
        map.forEach((key,val)->{
            editor.putString(key,val);
        });
        editor.apply();
    }


    public String shareTojson(){
        Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();

        JSONObject jsonObject = new JSONObject(map);

        String json = jsonObject.toString();
        Log.d(TAG, "shareTojson: "+json);
        return json;
    }
}