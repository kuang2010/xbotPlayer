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
package cn.iscas.xlab.xbotplayer.mvp.cemera;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import cn.iscas.xlab.xbotplayer.App;
import cn.iscas.xlab.xbotplayer.Config;
import cn.iscas.xlab.xbotplayer.Constant;
import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.RosConnectionReceiver;

/**
 * Created by lisongting on 2017/10/20.
 */

public class CameraFragment extends Fragment implements CameraContract.View {

    public static final String TAG = "CameraFragment";
    private ImageView imageView;
    private CameraContract.Presenter presenter;
    private RosConnectionReceiver receiver;
    private Button openCamera;
    private boolean isCameraOpened = false;

    public CameraFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, null);
        imageView = (ImageView) v.findViewById(R.id.img_rgb);
        openCamera = (Button) v.findViewById(R.id.openCamera);

        initOnClickListeners();
        return v;

    }

    @Override
    public void onStart() {
        log("onStart()");
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initBroadcastReceiver();
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        log("onResume()");
        super.onResume();
        presenter = new CameraPresenter(getContext(), this);
//        App app = (App) (getActivity().getApplication());

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        log("isHidden:" + hidden);
        if (!hidden) {
            if (Config.isRosServerConnected) {
                App app = (App) (getActivity().getApplication());
                if (presenter == null) {
                    presenter = new CameraPresenter(getContext(), this);
                    presenter.start();
                    presenter.subscribeCameraImage();
                }
                presenter.setServiceProxy(app.getRosServiceProxy());
            }
        }else{
//            presenter.unSubscribeCameraImage();
        }
    }

    private void initBroadcastReceiver() {
        receiver = new RosConnectionReceiver(new RosConnectionReceiver.RosCallback() {
            @Override
            public void onSuccess() {
                if (!Config.isRosServerConnected) {
                    Toast.makeText(getContext(), "Ros服务端连接成功", Toast.LENGTH_SHORT).show();
                    App app = (App) (getActivity().getApplication());
                    if (presenter == null) {
                        presenter = new CameraPresenter(getContext(),CameraFragment.this);
                        presenter.start();
                    }
                    presenter.setServiceProxy(app.getRosServiceProxy());
                    presenter.subscribeCameraImage();
                }
            }

            @Override
            public void onFailure() {
                Config.isRosServerConnected = false;
            }
        });

        IntentFilter filter = new IntentFilter(Constant.ROS_RECEIVER_INTENTFILTER);
        getActivity().registerReceiver(receiver,filter);

    }

    private void initOnClickListeners() {
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCameraOpened) {
                    if (!Config.isRosServerConnected) {
                        Toast.makeText(getContext(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                    } else {
                        openCamera.setText("关闭视频");
                        isCameraOpened = true;

                        presenter.subscribeCameraImage();
                    }
                } else {
                    openCamera.setText("打开摄像头数据");
                    isCameraOpened = false;
                    presenter.unSubscribeCameraImage();

                }
            }
        });

    }

    @Override
    public void updateRGBImage(Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
//            imageView.invalidate();
        }
    }

    @Override
    public void initView() {

    }

    @Override
    public void setPresenter(CameraContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private void log(String string) {
        Log.i(TAG, TAG + " -- " + string);
    }
}
