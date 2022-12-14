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
        idTovname.put("xm", "??????");
        idTovname.put("lxdh", "????????????");
        idTovname.put("rctw", "??????");
        idTovname.put("fxdj", "????????????");
        idTovname.put("xxdz", "????????????");
        idTovname.put("jtzz", "????????????");
        idTovname.put("sfzs", "???????????????");
        idTovname.put("mqsxdz", "??????????????????");

        this.idTohint = new HashMap<String, String>();
        idTohint.put("xm", "??????");
        idTohint.put("lxdh", "????????????");
        idTohint.put("rctw", "??????");
        idTohint.put("fxdj", "???/???/???????????????");
        idTohint.put("xxdz", "?????????????????????");
        idTohint.put("jtzz", "?????????");
        idTohint.put("sfzs", "??????-0; ???-1");
        idTohint.put("mqsxdz", "?????????????????????");
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

                Log.d(TAG, "onClick: ????????????"+uid.getText().toString());
                editor.putString("xh",uid.getText().toString());
                acc_editor.putString("uid",uid.getText().toString());
                acc_editor.putString("pwd", pwd.getText().toString());

                List<setting_item> setting_items = setting_adapter.getItems();
                for (setting_item setting_item:setting_items){
                    editor.putString(setting_item.getId(),setting_item.getValue());
                }
                editor.apply();
                acc_editor.apply();
                Log.d(TAG, "onClick: ????????????"+sharedPreferences.getString("xh",""));
                Log.d(TAG, "onClick: ????????????"+ accounts.getString("pwd",""));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (send(url_getToken)){
                            Toast.makeText(getActivity(), "Token?????????", Toast.LENGTH_SHORT).show();
                            showClockin();
                        } else {
                            Toast.makeText(getActivity(), "Token????????????", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "??????????????????????????????", Toast.LENGTH_SHORT).show();
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
//                    Toast.makeText(getActivity(),"??????token??????"+map.get("token"), Toast.LENGTH_SHORT).show();
//
//                    return map.get("token");
//                } else {
//                    Toast.makeText(getActivity(), res, Toast.LENGTH_SHORT).show();
//                    return "";
//                }
            } else {
                Toast.makeText(getActivity(), "????????????", Toast.LENGTH_SHORT).show();
                return "";
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "????????????", Toast.LENGTH_SHORT).show();
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


//        setting_adapter.add(new setting_item("xm", "??????", ""));
//        setting_adapter.add(new setting_item("lxdh", "????????????", ""));
//        setting_adapter.add(new setting_item("rctw", "??????", "37"));
//        setting_adapter.add(new setting_item("fxdj", "????????????", "???????????????", "???/???/???????????????"));
//        setting_adapter.add(new setting_item("xxdz", "????????????", "", "?????????????????????"));
//        setting_adapter.add(new setting_item("jtzz", "????????????", "??????????????????","?????????"));
//        setting_adapter.add(new setting_item("sfzs", "???????????????", "1", "??????-0; ???-1"));
//        setting_adapter.add(new setting_item("mqsxdz", "??????????????????", "??????????????????", "?????????????????????"));
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void init(setting_Adapter setting_adapter){

//        setting_adapter.add(new setting_item("xm", "??????", sharedPreferences.getString("xm","")));
//        setting_adapter.add(new setting_item("lxdh", "????????????", sharedPreferences.getString("lxdh","")));
//        setting_adapter.add(new setting_item("rctw", "??????", sharedPreferences.getString("rctw","")));
//        setting_adapter.add(new setting_item("fxdj", "????????????", sharedPreferences.getString("fxdj",""), "???/???/???????????????"));
//        setting_adapter.add(new setting_item("xxdz", "????????????", sharedPreferences.getString("xxdz",""), "?????????????????????"));
//        setting_adapter.add(new setting_item("jtzz", "????????????", sharedPreferences.getString("jtzz",""),"?????????"));
//        setting_adapter.add(new setting_item("sfzs", "???????????????", sharedPreferences.getString("sfzs",""), "??????-0; ???-1"));
//        setting_adapter.add(new setting_item("mqsxdz", "??????????????????", sharedPreferences.getString("mqsxdz",""), "?????????????????????"));

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