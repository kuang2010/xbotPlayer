package cn.iscas.xlab.xbotplayer.mvp.cemera;

import android.graphics.Bitmap;
import android.os.Binder;
import android.support.annotation.NonNull;

import cn.iscas.xlab.xbotplayer.mvp.BasePresenter;
import cn.iscas.xlab.xbotplayer.mvp.BaseView;

/**
 * Created by lisongting on 2017/10/20.
 */

public interface CameraContract  {

    interface Presenter extends BasePresenter{

        void setServiceProxy(@NonNull Binder binder);

        void subscribeCameraImage();

        void unSubscribeCameraImage();

        void destroy();
    }

    interface View extends BaseView<Presenter> {

        void updateRGBImage(Bitmap bitmap);


    }




}
