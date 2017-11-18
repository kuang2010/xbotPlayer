/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iscas.xlab.xbotplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cn.iscas.xlab.xbotplayer.mvp.cemera.CameraFragment;
import cn.iscas.xlab.xbotplayer.mvp.robot_state.RobotStateFragment;
import cn.iscas.xlab.xbotplayer.mvp.rvizmap.MapFragment;

/**
 * Created by lisongting on 2017/10/9.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String KEY_NAV_ITEM = "current_navigation_item";

    private BottomNavigationView bottomNavigationView;

    private MapFragment mapFragment;
    private CameraFragment cameraFragment;
    private RobotStateFragment robotStateFragment;
    private long lastExitTime;
    private FragmentManager fragmentManager;
    private ActionBar actionBar;
    private int selectedNavItem = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate()");
        setContentView(R.layout.activity_main);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        initListeners();
        actionBar = getSupportActionBar();
        initConfiguration();

        if (savedInstanceState == null) {
            log("savedInstanceState is null");
            fragmentManager = getSupportFragmentManager();
            mapFragment = new MapFragment();
            cameraFragment = new CameraFragment();
            robotStateFragment = new RobotStateFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, robotStateFragment, robotStateFragment.getClass().getSimpleName())
                    .add(R.id.container, mapFragment, mapFragment.getClass().getSimpleName())
                    .add(R.id.container, cameraFragment, cameraFragment.getClass().getSimpleName())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.robot_state);
        } else {
            log("restore savedInstanceState ");
            fragmentManager = getSupportFragmentManager();
            robotStateFragment = (RobotStateFragment) fragmentManager.getFragment(savedInstanceState, robotStateFragment.getClass().getSimpleName());
            mapFragment = (MapFragment) fragmentManager.getFragment(savedInstanceState, mapFragment.getClass().getSimpleName());
            cameraFragment = (CameraFragment) fragmentManager.getFragment(savedInstanceState, cameraFragment.getClass().getSimpleName());
            selectedNavItem = savedInstanceState.getInt(KEY_NAV_ITEM);
            switch (selectedNavItem) {
                case 0:
                    bottomNavigationView.setSelectedItemId(R.id.robot_state);
                    break;
                case 1:
                    bottomNavigationView.setSelectedItemId(R.id.controller);
                    break;
                case 2:
                    bottomNavigationView.setSelectedItemId(R.id.camera);
                    break;
                default:
                    break;
            }
        }


    }

    private void initConfiguration() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Config.ROS_SERVER_IP = sp.getString(getResources().getString(R.string.pref_key_ros_server_ip), "192.168.0.135");
        Config.speed = sp.getInt(getResources().getString(R.string.pref_key_speed),30) / 100.0;
        log("初始设置：" + Config.ROS_SERVER_IP + " ," + Config.speed);
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart()");
    }

    @Override
    protected void onResume() {
        log("onResume()");
        super.onResume();
    }



    private void initListeners() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                log("onNavigationItemSelected():"+item.getItemId());
                switch (item.getItemId()) {
                    case R.id.robot_state:
                        actionBar.setTitle("Xbot状态");
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(cameraFragment)
                                .hide(mapFragment)
                                .show(robotStateFragment)
                                .commit();
                        selectedNavItem = 0;
                        break;
                    case R.id.controller:
                        actionBar.setTitle("控制界面");
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(cameraFragment)
                                .hide(robotStateFragment)
                                .show(mapFragment)
                                .commit();
                        selectedNavItem = 1;
                        break;
                    case R.id.camera:
                        actionBar.setTitle("摄像头");
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(mapFragment)
                                .hide(robotStateFragment)
                                .show(cameraFragment)
                                .commit();
                        selectedNavItem = 2;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        if (robotStateFragment.isAdded()) {
            fragmentManager.putFragment(outState, robotStateFragment.getClass().getSimpleName(), robotStateFragment);
        }
        if (mapFragment.isAdded()) {
            fragmentManager.putFragment(outState, mapFragment.getClass().getSimpleName(), mapFragment);
        }
        if (cameraFragment.isAdded()) {
            fragmentManager.putFragment(outState, cameraFragment.getClass().getSimpleName(), cameraFragment);
        }
        outState.putInt(KEY_NAV_ITEM, selectedNavItem);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        log("onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == event.ACTION_DOWN) {
            if (System.currentTimeMillis() - lastExitTime < 2000) {
                finish();
            }else{
                Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
                lastExitTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        log("onDestroy()");
        super.onDestroy();
    }

    private void log(String s) {
        Log.i(TAG, TAG + " -- " + s);
    }

}
