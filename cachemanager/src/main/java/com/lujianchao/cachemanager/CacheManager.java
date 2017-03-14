package com.lujianchao.cachemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lujianchao on 2016/11/29.
 *
 * @author lujianchao
 */

public class CacheManager {



    public static final String DBName = "appinfo.db";
    /**
     * 更改类文件必须更改版本号，否则不会更新缓存结构
     */
    public static final int DBVersion = 1;
    private static DBHelper mCacheDBHelper;
    private static SQLiteDatabase mSQLiteDatabase;
    private static Context mContext;

    public static void init (Context mContext1) {
        mContext = mContext1;

    }

    /**
     * 删除数据库
     */
    public synchronized static void deleteDB () {
        mContext.deleteDatabase (DBName);
    }

    /**
     * 更新缓存
     *
     * @param key   预定义名称
     * @param value 待缓存数据
     */
    public synchronized static void updateCache (String key, String value) {
        updateCache (new CacheEntity ().setKey (key).setValue (value));
    }

    /**
     * 更新缓存
     * 不能手动更新id、key和lasttime
     *
     * @param mCacheEntity
     */
    public synchronized static void updateCache (CacheEntity mCacheEntity) {
        if (mCacheDBHelper == null) {
            mCacheDBHelper = new DBHelper (mContext, DBName, null, DBVersion);
        }
        if (mSQLiteDatabase == null) {
            mSQLiteDatabase = mCacheDBHelper.getWritableDatabase ();
        }
        ContentValues m = new ContentValues ();
        m.put ("value", mCacheEntity.value);
        m.put ("lasttime", System.currentTimeMillis ());
        m.put ("bak", mCacheEntity.bak);
        m.put ("flag", mCacheEntity.flag);
        try {
            mSQLiteDatabase.update (HistoryCache.class.getSimpleName (), m, "key=?", new String[]{mCacheEntity.key});
        } catch (Exception mE) {
            mE.printStackTrace ();
        }
    }

    /**
     * 获取缓存数据
     *
     * @param key 预定义名称
     * @return 缓存数据，异常或者不存在则返回null
     */
    public static CacheEntity getCache (String key) {
        CacheEntity mCacheEntity = new CacheEntity ();
        if (mCacheDBHelper == null) {
            mCacheDBHelper = new DBHelper (mContext, DBName, null, DBVersion);
        }
        if (mSQLiteDatabase == null) {
            mSQLiteDatabase = mCacheDBHelper.getWritableDatabase ();
        }
        Cursor mCursor = null;
        try {
            mCursor = mSQLiteDatabase.rawQuery ("select * from " + HistoryCache.class.getSimpleName () + " where key=?", new String[]{key});
            if (mCursor != null && mCursor.getCount () == 1) {
                mCursor.moveToNext ();
                mCacheEntity.id = mCursor.getInt (0);
                mCacheEntity.key = mCursor.getString (1);
                mCacheEntity.value = mCursor.getString (2);
                mCacheEntity.lasttime = mCursor.getLong (3);
                mCacheEntity.bak = mCursor.getString (4);
                mCacheEntity.flag = mCursor.getString (5);

            }
        } catch (Exception mE) {
            mE.printStackTrace ();
        } finally {
            if (mCursor != null) {
                mCursor.close ();
            }
            return mCacheEntity;
        }
    }

    /**
     * 数据库表结构
     */
    public static class CacheEntity {
        private String value="";
        private String key="";
        private int id;
        private long lasttime;
        private String bak="";
        private String flag="";

        public String getValue () {
            return value;
        }

        public CacheEntity setValue (final String mValue) {
            value = mValue;
            return this;
        }

        public String getKey () {
            return key;
        }

        public CacheEntity setKey (final String mKey) {
            key = mKey;
            return this;
        }

        public int getId () {
            return id;
        }

        public CacheEntity setId (final int mId) {
            id = mId;
            return this;
        }

        public long getLasttime () {
            return lasttime;
        }

        public CacheEntity setLasttime (final long mLasttime) {
            lasttime = mLasttime;
            return this;
        }

        public String getBak () {
            return bak;
        }

        public CacheEntity setBak (final String mBak) {
            bak = mBak;
            return this;
        }

        public String getFlag () {
            return flag;
        }

        public CacheEntity setFlag (final String mFlag) {
            flag = mFlag;
            return this;
        }
    }

    /**
     * Created by lujianchao on 2016/11/29.
     * SQLiteOpenHelper
     *
     * @author lujianchao
     */
    public static class DBHelper extends SQLiteOpenHelper {
        private static final String CREATE_CacheTABLE = "create table historycache (id integer primary key autoincrement, key text, value text, lasttime long, bak text, flag text)";

        public DBHelper (final Context context, final String name, final SQLiteDatabase.CursorFactory factory, final int version) {
            super (context, name, factory, version);
        }

        @Override
        public void onCreate (final SQLiteDatabase db) {
            db.execSQL (CREATE_CacheTABLE);
            updatetable (db, HistoryCache.class);
        }

        @Override
        public void onUpgrade (final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            updatetable (db, HistoryCache.class);
        }

        /**
         * 传入的类名即为表名，传入的类的属性即为表内的记录，字段固定，用来实现动态增减记录，记录为缓存内容，所以数量较少，
         * 只需要更改实体类属性，就可以管理数据库了，动态升级。
         *
         * @param db
         * @param mClass
         */
        private void updatetable (final SQLiteDatabase db, Class mClass) {
            /**
             * 通过反射拿到当前所有cache名
             */
            List<String> mList = new ArrayList<> ();
            Field[] fields = mClass.getDeclaredFields ();
            for (Field fd : fields) {
                fd.setAccessible (true);
                mList.add (fd.getName ());
            }

            Cursor mCursor = db.rawQuery ("select * from " + mClass.getSimpleName (), null);
            while (mCursor.moveToNext ()) {
                boolean ishave = false;
                String string = mCursor.getString (1);
                Iterator<String> mStringIterator = mList.iterator ();
                while (mStringIterator.hasNext ()) {
                    if (mStringIterator.next ().equals (string)) {
                        ishave = true;
                        mStringIterator.remove ();
                        break;
                    }
                }
                /**
                 * 类里没有这个缓存名就将其删掉
                 */
                if (!ishave) {
                    db.delete (mClass.getSimpleName (), "key=?", new String[]{string});
                }
            }
            mCursor.close ();
            for (int mI = 0; mI < mList.size (); mI++) {
                ContentValues values = new ContentValues ();
                values.put ("key", mList.get (mI));
                values.put ("lasttime", System.currentTimeMillis ());
                db.insert (mClass.getSimpleName (), null, values);
            }
        }
    }

    /**
     * Created by lujianchao on 2016/11/29.
     * 数据结构
     * 添加或者删除属性变量值，都必须更改数据库版本号，否则不会修改
     *
     * @author lujianchao
     */

    public static class HistoryCache {
        /**
         * 首页
         */
        public static String IndexPage = "IndexPage";
        /**
         * 朋友圈
         */
        public static String FriendLife = "FriendLife";
        /**
         * 首页Banner图片
         */
        public static String BannerList = "BannerList";
    }
}
