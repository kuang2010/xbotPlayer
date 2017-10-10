package cn.iscas.xlab.xbotplayer.mvp.rvizmap;

import android.graphics.Bitmap;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Size;

import cn.iscas.xlab.xbotplayer.mvp.BasePresenter;
import cn.iscas.xlab.xbotplayer.mvp.BaseView;

/**
 * Created by lisongting on 2017/10/9.
 */


public interface MapContract {

    interface Presenter extends BasePresenter{
        void setServiceProxy(@NonNull Binder binder);

        void abortLoadMap();

        void destroy();

        void subscribeMapData();

        void unsubscribeMapData();
    }

    interface View extends BaseView<Presenter> {

        void showLoading();

        void hideLoading();

        void updateMap(Bitmap mapInfo);

        Size getMapRealSize();

        boolean isHided();
    }
}
