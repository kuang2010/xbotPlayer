package cn.iscas.xlab.xbotplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import cn.iscas.xlab.xbotplayer.mvp.controller.ControlContract;

/**
 * Created by lisongting on 2017/10/10.
 */

public class RosConnectionReceiver extends BroadcastReceiver {

    RosCallback rosCallback;

    public interface RosCallback {
        void onSuccess();

        void onFailure();
    }

    public RosConnectionReceiver(RosCallback callback) {
        this.rosCallback = callback;
    }

    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        switch (data.getInt(Constant.KEY_BROADCAST_ROS_CONN)) {
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
