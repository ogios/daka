package com.example.clockin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

//    ImageButton imageButton;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment fragment1;
    Fragment fragment2;
    Fragment tmpFrag;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    public void showFragment(Fragment fragment){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment frag:fragments){
            fragmentTransaction.hide(frag);
        }
        fragmentTransaction.show(fragment);
//        fragmentTransaction.replace(R.id.FragContent, fragment);
        fragmentTransaction.commit();
    }

    public void addFragment(Fragment fragment, String tag){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (tmpFrag == null){
            fragmentTransaction.add(R.id.FragContent, fragment, tag).commit();
            tmpFrag = fragment;
        } else {
            fragmentTransaction.hide(tmpFrag).add(R.id.FragContent, fragment, tag).commit();
            tmpFrag = fragment;
        }


    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("setting", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerlayout);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.navi);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.clockin:
                        showFragment(fragment1);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.setting:
                        showFragment(fragment2);
                        drawerLayout.closeDrawers();
                }
                return false;
            }
        });

        fragment1 = new clockin();
        fragment2 = new setting();
        addFragment(fragment2, "setting");
        addFragment(fragment1, "clockin");

        setOption();

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setOption(){
        try {
            Boolean isSet = sharedPreferences.getBoolean("isSet", false);
            if (!isSet){
                init_setting();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    }

}