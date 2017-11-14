package cn.iscas.xlab.xbotplayer.mvp.robot_state;

import android.os.Binder;
import android.support.annotation.NonNull;

/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotStatePresenter implements RobotStateContract.Presenter {

    @Override
    public void setServiceProxy(@NonNull Binder binder) {

    }

    @Override
    public void subscribeRobotState() {

    }

    @Override
    public void unSubscribeRobotState() {

    }

    @Override
    public void publishLiftMsg(int heightPercent) {

    }

    @Override
    public void publishCloudCameraMsg(int cloudDegree, int cameraDegree) {

    }

    @Override
    public void start() {

    }
}
