package cn.iscas.xlab.xbotplayer.mvp;

import android.content.Context;
import android.os.Binder;

import cn.iscas.xlab.xbotplayer.RosConnectionService;
import cn.iscas.xlab.xbotplayer.Twist;

/**
 * Created by lisongting on 2017/9/27.
 */

public class ControlPresenter implements  ControlContract.Presenter{

    private ControlContract.View view;
    private RosConnectionService.ServiceBinder serviceProxy;
    private Context context;

    public ControlPresenter(Context context,ControlContract.View view) {
        this.view = view;
        view.setPresenter(this);

    }
    @Override
    public void start() {

    }

    @Override
    public void publishCommand(Twist twist) {
        if (serviceProxy != null) {
            serviceProxy.publishCommand(twist);
        }
    }

    @Override
    public void setServiceProxy( Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;
    }
}
