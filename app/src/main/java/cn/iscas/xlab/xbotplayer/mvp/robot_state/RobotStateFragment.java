package cn.iscas.xlab.xbotplayer.mvp.robot_state;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.customview.CustomSeekBar;
import cn.iscas.xlab.xbotplayer.customview.PercentCircleView;
import cn.iscas.xlab.xbotplayer.entity.RobotState;

/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotStateFragment extends Fragment implements RobotStateContract.View {
    private static final String TAG = RobotStateFragment.class.getSimpleName();

    private PercentCircleView batteryView;
    private CustomSeekBar cloudDegreeSeekBar;
    private CustomSeekBar liftHeightSeekBar;
    private CustomSeekBar cameraDegreeSeekBar;
    private RobotStateContract.Presenter presenter;

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
        
        initListeners();
        bt1 = (Button) view.findViewById(R.id.bt1);
        bt2 = (Button) view.findViewById(R.id.bt2);
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

        return view;
    }

    private void initListeners() {
        
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        log("onCreate()");
        super.onCreate(savedInstanceState);
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

    }

    @Override
    public void initView() {

    }

    @Override
    public void setPresenter(RobotStateContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateRobotState(RobotState state) {

    }

    private void log(String s){
        Log.i(TAG,TAG+" -- "+ s);
    }
}
