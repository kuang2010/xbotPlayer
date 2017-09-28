package cn.iscas.xlab.xbotplayer.mvp;

/**
 * Created by lisongting on 2017/9/27.
 */

public interface BaseView<T> {
    void initView();

    void setPresenter(T presenter);
}
