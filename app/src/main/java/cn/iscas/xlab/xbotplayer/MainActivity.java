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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import cn.iscas.xlab.xbotplayer.mvp.controller.ControlFragment;
import cn.iscas.xlab.xbotplayer.mvp.rvizmap.MapFragment;

/**
 * Created by lisongting on 2017/10/9.
 */

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout container;

    private ControlFragment controlFragment;
    private MapFragment mapFragment;
    private SimpleFragment tmpFragment;
    private long lastExitTime;
    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (FrameLayout) findViewById(R.id.container);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        controlFragment = new ControlFragment();
        mapFragment = new MapFragment();
        tmpFragment = SimpleFragment.getInstance("临时页面");
        fragmentManager = getSupportFragmentManager();

        initListeners();

        fragmentManager.beginTransaction()
                .add(R.id.container, mapFragment)
                .add(R.id.container, controlFragment)
                .add(R.id.container, tmpFragment)
                .commit();
        bottomNavigationView.setSelectedItemId(R.id.controller);

    }



    private void initListeners() {

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.controller:
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(mapFragment)
                                .hide(tmpFragment)
                                .show(controlFragment)
                                .commit();
                        break;
                    case R.id.map:
                        fragmentManager.beginTransaction()
                                .hide(controlFragment)
                                .hide(tmpFragment)
                                .show(mapFragment)
                                .commit();
                        break;
                    case R.id.camera:
                        mapFragment.hideLoading();
                        fragmentManager.beginTransaction()
                                .hide(controlFragment)
                                .hide(mapFragment)
                                .show(tmpFragment)
                                .commit();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
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
        Log.i("MainActivity", "onDestroy()");
        super.onDestroy();
    }


}
