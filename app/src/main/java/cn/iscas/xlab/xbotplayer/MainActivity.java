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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import cn.iscas.xlab.xbotplayer.mvp.controller.ControlFragment;
import cn.iscas.xlab.xbotplayer.mvp.rvizmap.MapFragment;

/**
 * Created by lisongting on 2017/10/9.
 */

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;

    private BottomNavigationView bottomNavigationView;

    private FragmentPagerAdapter fragmentPagerAdapter;

    private ControlFragment controlFragment;
    private MapFragment mapFragment;
    private long lastExitTime;
    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        controlFragment = new ControlFragment();
        mapFragment = new MapFragment();

        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {

                    return controlFragment;
                } else if (position == 1) {
                    return mapFragment;
                }
                return SimpleFragment.getInstance("临时页面");
            }

            @Override
            public int getCount() {
                return 3;
            }
        };

        viewPager.setAdapter(fragmentPagerAdapter);

        initListeners();
    }

    private void initListeners() {

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .hide(mapFragment)
                                .commit();
                        bottomNavigationView.setSelectedItemId(R.id.controller);
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .show(mapFragment)
                                .commit();
                        bottomNavigationView.setSelectedItemId(R.id.map);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.camera);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
//        viewPager.requestDisallowInterceptTouchEvent(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.controller:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.map:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.camera:
                        viewPager.setCurrentItem(2);
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
