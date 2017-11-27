package cn.iscas.xlab.xbotplayer.mvp.robot_state;

import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Log;

import cn.iscas.xlab.xbotplayer.Constant;
import cn.iscas.xlab.xbotplayer.RosConnectionService;
import cn.iscas.xlab.xbotplayer.entity.RobotState;
import de.greenrobot.event.EventBus;

/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotStatePresenter implements RobotStateContract.Presenter {

    private static final String TAG = "RobotStatePresenter";
    private RosConnectionService.ServiceBinder serviceProxy;

    RobotStateContract.View view;

    public RobotStatePresenter(RobotStateContract.View v){
        view = v;
        view.setPresenter(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void setServiceProxy(@NonNull Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;
    }

    @Override
    public void subscribeRobotState() {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_ROBOT_STATE, true);
    }

    @Override
    public void unSubscribeRobotState() {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_ROBOT_STATE,false);
    }

    @Override
    public void publishLiftMsg(int heightPercent) {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.sendLiftHeightMsg(heightPercent);
    }

    @Override
    public void publishCloudCameraMsg(int cloudDegree, int cameraDegree) {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.sendCloudCameraMsg(cloudDegree, cameraDegree);
    }

    @Override
    public void publishElectricMachineryMsg(boolean activate) {
        if (serviceProxy == null) {
            Log.e(TAG,"serviceProxy is null");
            return;
        }
        serviceProxy.sendElectricMachineryMsg(activate);
    }


    public void onEvent(RobotState robotState) {
        view.updateRobotState(robotState);

    }

    @Override
    public void start() {

    }
}
