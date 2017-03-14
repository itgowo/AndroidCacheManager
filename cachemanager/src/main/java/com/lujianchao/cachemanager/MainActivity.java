package com.lujianchao.cachemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        CacheManager.updateCache (CacheManager.HistoryCache.BannerList,"{\"name\":\"张三\",\"age\":34}");
    }
    public void test(View mView){
        CacheManager.updateCache (CacheManager.HistoryCache.BannerList,"{\"name\":\"张三\",\"age\":34}");
        Toast.makeText (this,CacheManager.getCache (CacheManager.HistoryCache.BannerList).getValue (),Toast.LENGTH_LONG).show ();
    }
}
