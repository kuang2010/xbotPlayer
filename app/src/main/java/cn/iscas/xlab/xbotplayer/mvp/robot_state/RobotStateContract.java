package cn.iscas.xlab.xbotplayer.mvp.robot_state;

import android.os.Binder;
import android.support.annotation.NonNull;

import cn.iscas.xlab.xbotplayer.entity.RobotState;
import cn.iscas.xlab.xbotplayer.mvp.BasePresenter;
import cn.iscas.xlab.xbotplayer.mvp.BaseView;

/**
 * Created by lisongting on 2017/11/14.
 */

public interface RobotStateContract {

    interface Presenter extends BasePresenter{

        void setServiceProxy(@NonNull Binder binder);

        void subscribeRobotState();

        void unSubscribeRobotState();

        void publishLiftMsg(int heightPercent);

        void publishCloudCameraMsg(int cloudDegree,int cameraDegree);

        void publishElectricMachineryMsg(boolean activate);

    }


    interface View extends BaseView<Presenter> {
        void updateRobotState(RobotState state);

    }
}

