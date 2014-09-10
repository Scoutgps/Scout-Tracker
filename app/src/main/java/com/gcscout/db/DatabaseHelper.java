package com.gcscout.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.gcscout.tracking.Protocol.AndroidDevicePacket;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.HashMap;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private final static String DATABASE_NAME = "ScoutTrackerDemo.Database";
    private final static int DATABASE_VERSION = 1;

    private static DatabaseHelper helper;

    public static DatabaseHelper getHelper() {
        return helper;
    }

    public static void initialize(Context context) {
        if (helper == null)
            helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    public static void dispose() {
        OpenHelperManager.releaseHelper();
        helper.close();
        helper = null;
    }

    private final HashMap<Class<?>, BaseDaoImpl<?, Integer>> daos = new HashMap<>();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, AndroidDevicePacket.class);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {
            TableUtils.dropTable(connectionSource, AndroidDevicePacket.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> BaseDaoImpl<T, Integer> dao(Class<T> type) {
        if (daos.containsKey(type))
            return (BaseDaoImpl<T, Integer>) daos.get(type);
        try {
            BaseDaoImpl<T, Integer> newDao = getDao(type);
            daos.put(type, newDao);
            return newDao;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public void close() {
        super.close();
        daos.clear();
    }
}
