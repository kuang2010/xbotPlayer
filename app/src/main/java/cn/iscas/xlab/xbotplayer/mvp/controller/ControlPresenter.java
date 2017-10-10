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

import android.content.Context;
import android.os.Binder;
import android.util.Log;

import cn.iscas.xlab.xbotplayer.RosConnectionService;
import cn.iscas.xlab.xbotplayer.entity.Twist;

/**
 * Created by lisongting on 2017/9/27.
 */

public class ControlPresenter implements  ControlContract.Presenter{

    private ControlContract.View view;
    private RosConnectionService.ServiceBinder serviceProxy;
    private Context context;

    public ControlPresenter(Context context,ControlContract.View view) {
        this.view = view;
        view.setPresenter(this);

    }
    @Override
    public void start() {

    }

    @Override
    public void publishCommand(Twist twist) {
        if (serviceProxy != null) {
            serviceProxy.publishCommand(twist);
        }else {
            Log.e("ControlPresenter", "RosConnectionService is null");
        }
    }

    @Override
    public void setServiceProxy( Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;
    }
}
