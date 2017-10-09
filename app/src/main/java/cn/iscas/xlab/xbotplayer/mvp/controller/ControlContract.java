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

import android.os.Binder;
import android.support.annotation.NonNull;

import cn.iscas.xlab.xbotplayer.entity.Twist;
import cn.iscas.xlab.xbotplayer.mvp.BasePresenter;
import cn.iscas.xlab.xbotplayer.mvp.BaseView;

/**
 * Created by lisongting on 2017/9/27.
 */

public class ControlContract {
    public static final int CONN_ROS_SERVER_SUCCESS = 0x11;
    public static final int CONN_ROS_SERVER_ERROR = 0x12;
    public static final String ROS_RECEIVER_INTENTFILTER = "xbotplayer.rosconnection.receiver";


    interface Presenter extends BasePresenter {
        void publishCommand(Twist twist);

        void setServiceProxy(@NonNull Binder binder);
    }

    interface View extends BaseView<Presenter> {


    }

}
