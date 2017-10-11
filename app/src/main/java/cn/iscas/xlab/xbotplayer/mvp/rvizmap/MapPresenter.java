package cn.iscas.xlab.xbotplayer.mvp.rvizmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import org.json.JSONArray;

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

import static android.content.ContentValues.TAG;

/**
 * Created by lisongting on 2017/10/9.
 */

public class MapPresenter implements MapContract.Presenter{

    private Context context;
    private MapContract.View view ;
    private RosConnectionService.ServiceBinder serviceProxy;
    private CompositeDisposable compositeDisposable;

    public MapPresenter(Context context,MapContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void start() {
        compositeDisposable = new CompositeDisposable();
        EventBus.getDefault().register(this);

        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_ODOM,true);
    }

    @Override
    public void setServiceProxy(@NonNull Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;
    }


    public void onEvent(MapInfo mapInfo) {
        if (view.isHided()) {
            return;
        }
        Disposable disposable = Observable.just(mapInfo)
                .subscribeOn(Schedulers.io())
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
                        Bitmap bitmap = Bitmap.createBitmap(canvasSize.getWidth(), canvasSize.getHeight(), Bitmap.Config.RGB_565);
                        int x,y;
                        for(int i=0;i<data.length();i++) {
                            if((int)data.get(i)==-1){
                                continue;
                            }
                            x = i / mapInfo.getOriginMapColumns();
                            y = i % mapInfo.getOriginMapColumns();
                            if((int)data.get(i) == 0) {
                                if (Math.abs(x - circleCenterX )<10 && Math.abs(y - circleCenterY)<10) {
                                    bitmap.setPixel(x,y,Color.RED);
                                }else{
                                    bitmap.setPixel(x, y,lightGreen);
                                }

                            } else if((int)data.get(i)==100){
                                if (Math.abs(x - circleCenterX )<10 && Math.abs(y - circleCenterY)<10) {
                                    bitmap.setPixel(x,y,Color.RED);
                                }else {
                                    bitmap.setPixel(x, y, Color.WHITE);
                                }
                            }
                        }

                        return bitmap;
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
                        log("onComplete()");
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
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_MAP,true);
    }

    @Override
    public void unsubscribeMapData() {
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_MAP,false);
    }

    private void log(String s) {
        Log.i(TAG, TAG + " -- " + s);
    }

}
