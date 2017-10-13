package cn.iscas.xlab.xbotplayer.mvp.rvizmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;

import cn.iscas.xlab.xbotplayer.Constant;
import cn.iscas.xlab.xbotplayer.RosConnectionService;
import cn.iscas.xlab.xbotplayer.entity.MapInfo;
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
    private BitmapFactory.Options options;
    private ByteArrayOutputStream bos;

    public MapPresenter(Context context,MapContract.View view) {
        this.context = context;
        this.view = view;
        options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
    }

    @Override
    public void start() {
        compositeDisposable = new CompositeDisposable();
        EventBus.getDefault().register(this);

        if (serviceProxy != null) {
            serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_ODOM, true);
        } else {
            Log.e(TAG, "serviceProxy is null");
        }

    }

    @Override
    public void setServiceProxy(@NonNull Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;

    }


    public void onEvent(MapInfo mapInfo) {
        Disposable disposable = Observable.just(mapInfo)
                .subscribeOn(Schedulers.computation())
                .map(new Function<MapInfo, Bitmap>() {
                    @Override
                    public Bitmap apply(@io.reactivex.annotations.NonNull MapInfo mapInfo) throws Exception {

                        JSONArray data = mapInfo.getData();
                        Size canvasSize = view.getMapRealSize();

                        float locationX = mapInfo.getLocationX();
                        float locationY = mapInfo.getLocationY();


                        int circleCenterX = (int) (mapInfo.getOriginMapColumns() * (25.0 + locationX) / 50.0);
                        int circleCenterY = (int) (mapInfo.getOriginMapRows() * (25.0 + locationY) / 50.0);

                        int lightGreen = Color.parseColor("#9AFF9A");
                        Bitmap bigMap = Bitmap.createBitmap(canvasSize.getWidth(), canvasSize.getHeight(), Bitmap.Config.RGB_565);

                        int x,y;
                        for(int i=0;i<data.length();i++) {
                            if((int)data.get(i)==-1){
                                continue;
                            }
                            x = i / mapInfo.getOriginMapColumns();
                            y = i % mapInfo.getOriginMapColumns();
                            if (Math.abs(x - circleCenterX) < 10 && Math.abs(y - circleCenterY) < 10) {
                                bigMap.setPixel(x, y, Color.RED);
                            } else {
                                if((int)data.get(i) == 0) {
                                    bigMap.setPixel(x, y,lightGreen);
                                } else if((int)data.get(i)==100){
                                    bigMap.setPixel(x, y, Color.WHITE);
                                }
                            }

                        }

//                        bos = new ByteArrayOutputStream();
//                        bigMap.compress(Bitmap.CompressFormat.PNG, 80, bos);
//                        Bitmap small = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.size(), options);
//                        bos.reset();
//                        bigMap.recycle();
//                        return small;
                        return bigMap;
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
