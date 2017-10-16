package cn.iscas.xlab.xbotplayer.mvp.rvizmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import cn.iscas.xlab.xbotplayer.Constant;
import cn.iscas.xlab.xbotplayer.RosConnectionService;
import cn.iscas.xlab.xbotplayer.util.ImageUtils;
import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lisongting on 2017/10/9.
 */

public class MapPresenter implements MapContract.Presenter{

    private static final String TAG = "MapPresenter";
    private Context context;
    private MapContract.View view ;
    private RosConnectionService.ServiceBinder serviceProxy;
    private CompositeDisposable compositeDisposable;
    private Size mapSize;

    public MapPresenter(Context context,MapContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void start() {
        compositeDisposable = new CompositeDisposable();

        EventBus.getDefault().register(this);

        if (serviceProxy != null) {

        } else {
            Log.e(TAG, "serviceProxy is null");
        }

    }

    @Override
    public void setServiceProxy(@NonNull Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;

    }


    public void onEvent(final String base64Map) {
        mapSize = view.getMapRealSize();
        Disposable disposable = Observable.just(base64Map)
                .subscribeOn(Schedulers.computation())
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(@io.reactivex.annotations.NonNull String mapInfo) throws Exception {
                        return ImageUtils.decodeBase64ToBitmap(base64Map,1,mapSize);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Bitmap>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                        log("onNext()");
                        view.updateMap(bitmap);
                        view.hideLoading();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        log("onError()");
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        log("onComplete() -- map loaded successfully");
                    }
                });
        compositeDisposable.add(disposable);
    }



    @Override
    public void destroy() {
        EventBus.getDefault().unregister(this);
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    @Override
    public void abortLoadMap() {
        compositeDisposable.clear();
    }

    @Override
    public void subscribeMapData() {
        if (serviceProxy != null) {
            serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_MAP,true);
        } else {
            Log.e(TAG, "serviceProxy is null");
        }
    }

    @Override
    public void unsubscribeMapData() {
        if (serviceProxy != null) {
            serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_MAP,false);
        } else {
            Log.e(TAG, "serviceProxy is null");
        }
    }

    private void log(String s) {
        Log.i(TAG, TAG + " -- " + s);
    }

}
