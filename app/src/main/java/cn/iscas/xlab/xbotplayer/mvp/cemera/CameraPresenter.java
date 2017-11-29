/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iscas.xlab.xbotplayer.mvp.cemera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by lisongting on 2017/10/20.
 */

public class CameraPresenter implements CameraContract.Presenter {

    public static final String TAG = "CameraPresenter";
    private RosConnectionService.ServiceBinder serviceProxy;
    private CameraContract.View view;
    private Context context;
    private CompositeDisposable compositeDisposable;


    public CameraPresenter(Context context, CameraContract.View view) {
        this.context = context;
        this.view = view;
        view.setPresenter(this);
        compositeDisposable = new CompositeDisposable();
        EventBus.getDefault().register(this);
    }


    @Override
    public void start() {

    }


    @Override
    public void setServiceProxy(@NonNull Binder binder) {
        serviceProxy = (RosConnectionService.ServiceBinder) binder;
    }


    public void onEvent(final String jsonString) throws JSONException{
        JSONObject object = new JSONObject(jsonString);
        if (!object.get("topicName").equals(Constant.SUBSCRIBE_TOPIC_RGB_IMAGE)) {
            return;
        }
        final String base64Image = object.getString("data");
        Disposable disposable = Observable.just(base64Image)
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(@io.reactivex.annotations.NonNull String s) throws Exception {
                        return ImageUtils.decodeBase64ToBitmap(base64Image);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Bitmap>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                        view.updateRGBImage(bitmap);
                    }
                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        log("onError");
                    }
                    @Override
                    public void onComplete() {
                        log("onComplete()");
                    }
                });

        compositeDisposable.add(disposable);
    }

    @Override
    public void subscribeCameraImage() {
        if (serviceProxy == null) {
            Log.e(TAG, TAG + "ServiceProxy is null");
            return;
        }
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_RGB_IMAGE, true);
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_DEPTH_IMAGE, true);
    }


    @Override
    public void unSubscribeCameraImage() {
        if (serviceProxy == null) {
            Log.e(TAG, "ServiceProxy is null");
            return;
        }
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_RGB_IMAGE,false);
        serviceProxy.manipulateTopic(Constant.SUBSCRIBE_TOPIC_DEPTH_IMAGE,false);

    }

    @Override
    public void destroy() {
        compositeDisposable.clear();
        EventBus.getDefault().unregister(this);
    }

    private void log(String string) {
        Log.i(TAG, TAG + " -- " + string);
    }



}
