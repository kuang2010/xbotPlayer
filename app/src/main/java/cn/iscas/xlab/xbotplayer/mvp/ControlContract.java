package cn.iscas.xlab.xbotplayer.mvp;

import android.os.Binder;
import android.support.annotation.NonNull;

import cn.iscas.xlab.xbotplayer.Twist;

/**
 * Created by lisongting on 2017/9/27.
 */

public class ControlContract {
    public static final int CONN_ROS_SERVER_SUCCESS = 0x11;
    public static final int CONN_ROS_SERVER_ERROR = 0x12;
    public static final String ROS_RECEIVER_INTENTFILTER = "xbotplayer.rosconnection.receiver";


    interface Presenter extends BasePresenter{
        void publishCommand(Twist twist);

        void setServiceProxy(@NonNull Binder binder);
    }

    interface View extends BaseView<Presenter>{


    }

}
