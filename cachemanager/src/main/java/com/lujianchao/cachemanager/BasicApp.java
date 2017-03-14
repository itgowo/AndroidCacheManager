package com.lujianchao.cachemanager;

import android.app.Application;

/**
 * Created by hnvfh on 2017/3/14.
 */

public class BasicApp extends Application {

    @Override
    public void onCreate () {
        super.onCreate ();
        CacheManager.init (this);
    }
}
