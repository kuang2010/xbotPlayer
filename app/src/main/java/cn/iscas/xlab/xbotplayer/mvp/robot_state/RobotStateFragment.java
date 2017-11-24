package cn.iscas.xlab.xbotplayer.mvp.robot_state;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import cn.iscas.xlab.xbotplayer.App;
import cn.iscas.xlab.xbotplayer.Config;
import cn.iscas.xlab.xbotplayer.Constant;
import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.RosConnectionReceiver;
import cn.iscas.xlab.xbotplayer.customview.CustomSeekBar;
import cn.iscas.xlab.xbotplayer.customview.PercentCircleView;
import cn.iscas.xlab.xbotplayer.entity.RobotState;

/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotStateFragment extends Fragment implements RobotStateContract.View {
    private static final String TAG = "RobotStateFragment";

    private PercentCircleView batteryView;
    private CustomSeekBar cloudDegreeSeekBar;
    private CustomSeekBar liftHeightSeekBar;
    private CustomSeekBar cameraDegreeSeekBar;
    private RobotStateContract.Presenter presenter;
    private BroadcastReceiver receiver;

    private Button bt1,bt2;


    public RobotStateFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_robot_state, container, false);
        batteryView = (PercentCircleView) view.findViewById(R.id.battery_view);
        cloudDegreeSeekBar = (CustomSeekBar) view.findViewById(R.id.seekbar_cloud_degree);
        liftHeightSeekBar = (CustomSeekBar) view.findViewById(R.id.seekbar_lift_height);
        cameraDegreeSeekBar = (CustomSeekBar) view.findViewById(R.id.seekbar_cemera_degree);
        
        bt1 = (Button) view.findViewById(R.id.bt1);
        bt2 = (Button) view.findViewById(R.id.bt2);

        initListeners();

        return view;
    }

    @Override
    public void initView() {

    }

    private void initListeners() {
        //TODO :这些后面要去掉
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryView.setPercent(batteryView.getPercent() + 6);
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryView.setPercent(batteryView.getPercent() - 6);
            }
        });

        batteryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryView.startAnim();
            }
        });
        liftHeightSeekBar.setOnSeekChangeListener(new CustomSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(int value) {
                log("liftHeightSeekBar value changed:" + value);
                if (presenter != null && Config.isRosServerConnected) {
                    presenter.publishLiftMsg(value);
                }
            }

            @Override
            public void onProgressChangeCompleted(int value) {
                log("liftHeightSeekBar value change complete :" + value);
                if (presenter != null && Config.isRosServerConnected) {
                    presenter.publishLiftMsg(value);
                } else {
                    Toast.makeText(getActivity(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cloudDegreeSeekBar.setOnSeekChangeListener(new CustomSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(int value) {
                log("cloudDegreeSeekBar value changed:" + value);
                //TODO：暂时先用-200表示不对这个控制参数进行控制,后续根据底层处理方式来定
                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishCloudCameraMsg(value, -200);
                }
            }

            @Override
            public void onProgressChangeCompleted(int value) {
                log("cloudDegreeSeekBar value change complete :" + value);

                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishCloudCameraMsg(value, -200);
                }else {
                    Toast.makeText(getActivity(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cameraDegreeSeekBar.setOnSeekChangeListener(new CustomSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(int value) {
                log("cameraDegreeSeekBar value changed:" + value);
                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishCloudCameraMsg(-200,value);
                }
            }

            @Override
            public void onProgressChangeCompleted(int value) {
                log("cameraDegreeSeekBar value change complete:" + value);
                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishCloudCameraMsg(-200,value);
                }else {
                    Toast.makeText(getContext(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void initBroadcastReceiver() {
        receiver = new RosConnectionReceiver(new RosConnectionReceiver.RosCallback() {
            @Override
            public void onSuccess() {
                if (!Config.isRosServerConnected) {
                    Toast.makeText(getContext(), "Ros服务端连接成功", Toast.LENGTH_SHORT).show();
                    App app = (App) (getActivity().getApplication());
                    if (presenter == null) {
                        presenter = new RobotStatePresenter(RobotStateFragment.this);
                    }
                    presenter.setServiceProxy(app.getRosServiceProxy());
                    presenter.subscribeRobotState();

                }
            }

            @Override
            public void onFailure() {
                batteryView.stopAnimation();
                Config.isRosServerConnected = false;
            }
        });

        IntentFilter filter = new IntentFilter(Constant.ROS_RECEIVER_INTENTFILTER);
        getActivity().registerReceiver(receiver,filter);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        log("onCreate()");
        super.onCreate(savedInstanceState);
        initBroadcastReceiver();
    }

    @Override
    public void onResume() {
        log("onResume()");
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        log("isHidden:" + hidden);
        super.onHiddenChanged(hidden);
        if (!hidden && Config.isRosServerConnected) {
            App app = (App) (getActivity().getApplication());
            if (presenter == null) {
                presenter = new RobotStatePresenter(this);
                presenter.setServiceProxy(app.getRosServiceProxy());
                presenter.subscribeRobotState();
                presenter.start();
            }
        }
    }



    @Override
    public void setPresenter(RobotStateContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateRobotState(RobotState state) {

        batteryView.setPercent(state.getPowerPercent());
        cloudDegreeSeekBar.setProgress(state.getCloudDegree());
        cameraDegreeSeekBar.setProgress(state.getCameraDegree());
        liftHeightSeekBar.setProgress(state.getHeightPercent());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.unSubscribeRobotState();
        }
    }

    private void log(String s){
        Log.i(TAG,TAG+" -- "+ s);
    }
}
