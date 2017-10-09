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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cn.iscas.xlab.xbotplayer.Config;
import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.RegexCheckUtil;
import cn.iscas.xlab.xbotplayer.RockerView;
import cn.iscas.xlab.xbotplayer.RosConnectionService;
import cn.iscas.xlab.xbotplayer.entity.Twist;

/**
 * Created by lisongting on 2017/9/27.
 */

public class ControlFragment extends Fragment implements ControlContract.View{


    public static final String TAG = "ControlFragment";

    private RosConnectionReceiver receiver;
    private ControlContract.Presenter presenter;

    private TextView connectionState;
    private EditText ipEditText;
    private EditText speedEditText;
    private float speed ;
    private boolean isRosServerConnected = false;
    private Timer timer;

    private RockerView rockerView;
    private volatile Twist rockerTwist;

    public ControlFragment() {
        rockerTwist = new Twist();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        ipEditText = (EditText) view.findViewById(R.id.et_ros_ip);
        speedEditText = (EditText) view.findViewById(R.id.et_speed);
        connectionState = (TextView) view.findViewById(R.id.et_state);
        rockerView = (RockerView) view.findViewById(R.id.rocker_view);

        initView();
        return view;
    }

    @Override
    public void initView() {
        Config.ROS_SERVER_IP = ipEditText.getEditableText().toString();

        ipEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (RegexCheckUtil.isRightIP(s.toString())) {
                    Config.ROS_SERVER_IP = s.toString();
                }
            }
        });

        rockerView.setOnDirectionChangeListener(new RockerView.OnDirectionChangeListener() {
            @Override
            public void onStart() {
                startTwistPublisher();
            }

            @Override
            public void onDirectionChange(RockerView.Direction direction) {
                Log.i(TAG, "当前的摇杆方向：" + direction.name());
                speed = Float.parseFloat(speedEditText.getEditableText().toString());
                switch (direction) {
                    case DIRECTION_UP:
                        rockerTwist = new Twist(speed, 0F, 0F, 0F, 0F, 0F);
                        break;
                    case DIRECTION_DOWN:
                        rockerTwist = new Twist(-speed, 0F, 0F, 0F, 0F, 0F);
                        break;
                    case DIRECTION_LEFT:
                        rockerTwist = new Twist(0F, 0F, 0F, 0F, 0F, speed*3F);
                        break;
                    case DIRECTION_UP_LEFT:
                        rockerTwist = new Twist(speed, 0F, 0F, 0F, 0F, speed*3F);
                        break;
                    case DIRECTION_RIGHT:
                        rockerTwist = new Twist(0F, 0F, 0F, 0F, 0F, -speed*3F);
                        break;
                    case DIRECTION_UP_RIGHT:
                        rockerTwist = new Twist(speed, 0F, 0F, 0F, 0F, -speed*3F);
                        break;
                    case DIRECTION_DOWN_LEFT:
                        rockerTwist = new Twist(-speed, 0F, 0F, 0F, 0F, -speed*3F);
                        break;
                    case DIRECTION_DOWN_RIGHT:
                        rockerTwist = new Twist(-speed, 0F, 0F, 0F, 0F, speed*3F);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFinish() {
                rockerTwist = new Twist(0F, 0F, 0F, 0F, 0F, 0F);
                presenter.publishCommand(rockerTwist);
                cancelTimerTask();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        presenter = new ControlPresenter(getContext(),this);
        presenter.start();

        initBroadcastReceiver();
        
    }

    private void initBroadcastReceiver() {
        receiver = new RosConnectionReceiver(new RosConnectionReceiver.RosCallback() {
            @Override
            public void onSuccess() {
                if (!isRosServerConnected) {
                    connectionState.setTextColor(Color.GREEN);
                    connectionState.setText("连接成功");
                    Toast.makeText(getContext(), "Ros服务端连接成功", Toast.LENGTH_SHORT).show();
                    isRosServerConnected = true;
                }
            }

            @Override
            public void onFailure() {
                if (isRosServerConnected) {
                    connectionState.setTextColor(Color.RED);
                    connectionState.setText("未连接");
                    isRosServerConnected = false;
                }
            }
        });

        IntentFilter filter = new IntentFilter(ControlContract.ROS_RECEIVER_INTENTFILTER);
        getActivity().registerReceiver(receiver,filter);

    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(receiver);
    }

    public void setServicePresenter(RosConnectionService.ServiceBinder service) {
        presenter.setServiceProxy(service);
    }

    @Override
    public void setPresenter(ControlContract.Presenter presenter) {
        this.presenter = presenter;
    }


    public synchronized void startTwistPublisher() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                presenter.publishCommand(rockerTwist);
            }
        },0,200);
    }

    public synchronized void cancelTimerTask() {

        timer.cancel();
        Log.v(TAG, "TimerTask is killed ");
    }



    public static class RosConnectionReceiver extends BroadcastReceiver {

        RosCallback rosCallback;

        interface RosCallback {
            void onSuccess();

            void onFailure();
        }

        public RosConnectionReceiver(RosCallback callback) {
            this.rosCallback = callback;
        }

        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            switch (data.getInt("ros_conn_status")) {
                case ControlContract.CONN_ROS_SERVER_SUCCESS:
                    rosCallback.onSuccess();
                    break;
                case ControlContract.CONN_ROS_SERVER_ERROR:
                    rosCallback.onFailure();
                    break;
                default:
                    break;
            }
        }
    }


}
