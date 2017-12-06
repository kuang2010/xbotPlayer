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

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.iscas.xlab.xbotplayer.App;
import cn.iscas.xlab.xbotplayer.Config;
import cn.iscas.xlab.xbotplayer.Constant;
import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.RosConnectionReceiver;
import cn.iscas.xlab.xbotplayer.customview.RockerView;
import cn.iscas.xlab.xbotplayer.entity.Twist;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by lisongting on 2017/10/20.
 */

public class CameraFragment extends Fragment implements CameraContract.View {

    public static final String TAG = "CameraFragment";
    private RosConnectionReceiver receiver;
    private SurfaceView surfaceView;
    private RelativeLayout topBar,bottomBar,infoView;
    private ImageButton btPlayState,btFullScreen,btVideoList;
    private RockerView rockerView;
    private ListView listView;
    private ImageView infoImage;
    private TextView infoText;
    private TextView title;

    private CameraContract.Presenter presenter;
    private SimpleAdapter simpleAdapter;
    private String[] infoList = {"彩色图像","深度图像"};
    private List<Map<String,String>> listData;
    private boolean isMenuOpened ;
    private boolean isLoadingFailed;

    private Animation waitAnimation;
    private float speed ;
    private Timer timer;
    private volatile Twist rockerTwist;
    private IjkMediaPlayer mediaPlayer;
    private String rtmpAddress;
    private Handler handler;
    private View.OnClickListener onClickListener;
    private String videoTitle ;

    public CameraFragment() {
        rockerTwist = new Twist();
        rtmpAddress = "";
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (topBar.getVisibility() == View.VISIBLE) {
                    topBar.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    isMenuOpened = false;
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, null);
        surfaceView = (SurfaceView) v.findViewById(R.id.sv_video);
        topBar = (RelativeLayout) v.findViewById(R.id.top_bar);
        bottomBar = (RelativeLayout) v.findViewById(R.id.bottom_bar);
        infoView = (RelativeLayout) v.findViewById(R.id.info_view);
        btPlayState = (ImageButton) v.findViewById(R.id.ib_play_state);
        btFullScreen = (ImageButton) v.findViewById(R.id.ib_screen_state);
        btVideoList = (ImageButton) v.findViewById(R.id.ib_list);
        listView = (ListView) v.findViewById(R.id.list_view);
        rockerView = (RockerView) v.findViewById(R.id.rocker_view);
        infoImage = (ImageView)v.findViewById(R.id.info_image);
        infoText = (TextView) v.findViewById(R.id.info_text);
        title = (TextView) v.findViewById(R.id.tv_title);
        isMenuOpened = true;

        initView();
        return v;

    }

    @Override
    public void initView() {
        listData = new ArrayList<>();
        for(int i=0;i<infoList.length;i++) {
            Map<String, String> map = new HashMap<>();
            map.put("text", infoList[i]);
            listData.add(map);
        }

        simpleAdapter = new SimpleAdapter(getContext(), listData, R.layout.video_list_item,
                new String[]{"text"}, new int[]{R.id.item_text});
        listView.setAdapter(simpleAdapter);

        waitAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.watting_anim);
        waitAnimation.setInterpolator(new LinearInterpolator());

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
        initOnClickListeners();

        createMediaPlayer();
//        App app = (App) (getActivity().getApplication());

    }

    private void initBroadcastReceiver() {
        receiver = new RosConnectionReceiver(new RosConnectionReceiver.RosCallback() {
            @Override
            public void onSuccess() {
                if (!Config.isRosServerConnected) {
                    Toast.makeText(getContext(), "Ros服务端连接成功", Toast.LENGTH_SHORT).show();
                    rockerView.setAvailable(true);
                    App app = (App) (getActivity().getApplication());
                    if (presenter == null) {
                        presenter = new CameraPresenter(getContext(),CameraFragment.this);
                        presenter.start();
                    }
                    presenter.setServiceProxy(app.getRosServiceProxy());
                }
            }

            @Override
            public void onFailure() {
                rockerView.setAvailable(false);
                Config.isRosServerConnected = false;
            }
        });

        IntentFilter filter = new IntentFilter(Constant.ROS_RECEIVER_INTENTFILTER);
        getActivity().registerReceiver(receiver,filter);

    }

    private void initOnClickListeners() {

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMenuOpened) {
                    topBar.setVisibility(View.VISIBLE);
                    bottomBar.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessageDelayed(1, 5000);
                } else {
                    topBar.setVisibility(View.GONE);
                    bottomBar.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    handler.removeMessages(1);
                }
                isMenuOpened = !isMenuOpened;
            }
        });

        infoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoadingFailed) {
                    play(rtmpAddress);
                    isLoadingFailed = false;
                }
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StringBuilder addr = new StringBuilder("rtmp://");

                if (position == 0) {
                    addr.append(Config.ROS_SERVER_IP).append(Constant.CAMERA_RGB_RTMP_SUFFIX);
                    videoTitle = infoList[0];
                } else {
                    addr.append(Config.ROS_SERVER_IP).append(Constant.CAMERA_DEPTH_RTMP_SUFFIX);
                    videoTitle = infoList[1];

                }
                //如果选择的播放视频源与正在播放的不相同则开始播放，如果相同，则不播放
                if (!rtmpAddress.equals(addr.toString())) {
                    rtmpAddress = addr.toString();
                    showLoading();
                    play(rtmpAddress);
                }
            }
        });

        btPlayState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    btPlayState.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.pause();
                    mediaPlayer.release();
                    Canvas c = new Canvas();
                    c.drawColor(Color.BLACK);
                    surfaceView.draw(c);
                } else {
                    if (rtmpAddress.length() == 0) {
                        rtmpAddress = "rtmp://" + Config.ROS_SERVER_IP + Constant.CAMERA_RGB_RTMP_SUFFIX;
                    }
                    showLoading();
                    btPlayState.setBackgroundResource(R.drawable.ic_play);
                    play(rtmpAddress);
                }
            }
        });

        btFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(),FullScreenVideoActivity.class));
            }

        });

        btVideoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.getVisibility() == View.GONE) {
                    listView.setVisibility(View.VISIBLE);
                } else if(listView.getVisibility()==View.VISIBLE){
                    listView.setVisibility(View.GONE);
                }
            }
        });

        rockerView.setOnDirectionChangeListener(new RockerView.OnDirectionChangeListener() {
            @Override
            public void onStart() {
                if (!Config.isRosServerConnected) {
                    Toast.makeText(getContext(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                } else {
                    startTwistPublisher();
                }
            }

            @Override
            public void onDirectionChange(RockerView.Direction direction) {
//                log("当前的摇杆方向：" + direction.name());
                if (!Config.isRosServerConnected) {
                    return;
                }
                speed = (float) Config.speed;
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
                if (!Config.isRosServerConnected) {
                    return;
                }
                rockerTwist = new Twist(0F, 0F, 0F, 0F, 0F, 0F);
                presenter.publishCommand(rockerTwist);
                cancelTimerTask();
            }
        });


    }

    private void createMediaPlayer() {
        mediaPlayer = new IjkMediaPlayer();

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 16);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 50000);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"packet-buffering",0);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3000);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);

        mediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                if (i == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    hideLoading();
                    btPlayState.setBackgroundResource(R.drawable.ic_pause);
                    title.setText(videoTitle);
                }
                return true;
            }
        });
        mediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                showRetry();
                isLoadingFailed = true;
                btPlayState.setBackgroundResource(R.drawable.ic_play);
                return true;
            }
        });

    }

    private void play(String rtmpAddress) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        try {
            mediaPlayer.setDataSource(rtmpAddress);
        } catch (IOException e) {
            showRetry();
            e.printStackTrace();
        }
        mediaPlayer.setDisplay(surfaceView.getHolder());

        mediaPlayer.prepareAsync();

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        log("isHidden:" + hidden);
        log("idHidden:" + hidden);
        super.onHiddenChanged(hidden);
        if (!hidden ) {
            if (Config.isRosServerConnected) {
                App app = (App) (getActivity().getApplication());
                if (presenter == null) {
                    presenter = new CameraPresenter(getContext(),this);
                    presenter.setServiceProxy(app.getRosServiceProxy());
                    presenter.start();
                }
                rockerView.setAvailable(true);
            } else {
                rockerView.setAvailable(false);

            }

        }
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
        log("Stopped Control..TimerTask is canceled ");
    }

    @Override
    public void setPresenter(CameraContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.destroy();
        }
        getActivity().unregisterReceiver(receiver);
    }

    private void log(String string) {
        Log.i(TAG, TAG + " -- " + string);
    }

    @Override
    public void showLoading() {
        infoView.setVisibility(View.VISIBLE);
        infoImage.setBackgroundResource(R.drawable.loading);
        infoImage.startAnimation(waitAnimation);
        infoText.setText(R.string.text_loading);
    }

    @Override
    public void hideLoading() {
        infoImage.clearAnimation();
        infoView.setVisibility(View.GONE);

    }

    @Override
    public void showRetry() {
        infoView.setVisibility(View.VISIBLE);
        infoImage.clearAnimation();
        infoImage.setBackgroundResource(R.drawable.retry);
        infoText.setText(R.string.text_retry);
    }
}
