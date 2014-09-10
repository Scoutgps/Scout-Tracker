package com.gcscout.tracking.Protocol;

import com.gcscout.db.DatabaseHelper;
import com.gcscout.settings.Setting;
import com.gcscout.settings.Settings;
import com.j256.ormlite.dao.BaseDaoImpl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AndroidDevicePacketsSender extends ProtocolPacketsSender<AndroidDevicePacket> {
    public final static AndroidDevicePacketsSender Instance = new AndroidDevicePacketsSender();

    private static BaseDaoImpl<AndroidDevicePacket, Integer> getAndroidDevicePacketDao() {
        return DatabaseHelper.getHelper().dao(AndroidDevicePacket.class);
    }

    private int journalSize;

    private AndroidDevicePacketsSender() {
        super();
        journalSize = Settings.getSetting(Settings.JournalSize);
    }

    private long getJournalMinTime() {
        return new Date().getTime() - journalSize * 24 * 60 * 60 * 1000;
    }

    @Override
    protected void addPacketToDatabase(AndroidDevicePacket packet) {
        try {
            checkForSize();
            getAndroidDevicePacketDao().createOrUpdate(packet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkForSize() {
        try {
            getAndroidDevicePacketDao()
                    .deleteBuilder()
                    .where()
                    .lt(AndroidDevicePacket.DateTimeColumnName, getJournalMinTime());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean havePacketsToSend() {
        try {
            return getAndroidDevicePacketDao().countOf() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    protected AndroidDevicePacket[] loadPacketsFromDb(long limit) {
        try {
            List<AndroidDevicePacket> queryResult = getAndroidDevicePacketDao().queryBuilder().limit(limit).query();
            AndroidDevicePacket[] result = new AndroidDevicePacket[queryResult.size()];
            queryResult.toArray(result);
            return result;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    protected void removePacketsFromDatabase(List<AndroidDevicePacket> packets) {
        try {
            getAndroidDevicePacketDao().delete(packets);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onSettingChanged(Setting<?> setting) {
        super.onSettingChanged(setting);
        if (setting == Settings.JournalSize)
            journalSize = Settings.getSetting(Settings.JournalSize);
    }
}