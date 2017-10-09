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
package cn.iscas.xlab.xbotplayer.mvp.controller;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.RosConnectionService;


/**
 * Created by lisongting on 2017/9/27.
 */

public class ControlActivity extends AppCompatActivity {

    private ControlFragment controlFragment;

    private ServiceConnection serviceConnection;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);

        controlFragment = new ControlFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, controlFragment)
                .commit();

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                controlFragment.setServicePresenter((RosConnectionService.ServiceBinder) service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, RosConnectionService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
