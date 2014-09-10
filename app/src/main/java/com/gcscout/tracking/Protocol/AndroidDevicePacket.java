package com.gcscout.tracking.Protocol;

import android.location.Location;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "AndroidDevicePacket")
public class AndroidDevicePacket implements PacketConvertable {
    private final static int PROTOCOL_ID = 219;
    private final static byte[] STATUS_0 = new byte[]{(byte) 0x05, (byte) 0x00};
    private final static byte[] EMPTY = new byte[]{(byte) 0x00, (byte) 0x00};
    private final static int MIN_SATELLITES_COUNT = 3;
    public final static String DateTimeColumnName = "DateTime";

    @DatabaseField(dataType = DataType.LONG, generatedId = true)
    private long mId;
    @DatabaseField(canBeNull = false, dataType = DataType.STRING)
    private final String mSerialId;
    @DatabaseField(dataType = DataType.LONG, columnName = DateTimeColumnName)
    private final long mDateTime;
    @DatabaseField(dataType = DataType.DOUBLE)
    private double mLongitude;
    @DatabaseField(dataType = DataType.DOUBLE)
    private double mLatitude;
    @DatabaseField(dataType = DataType.FLOAT)
    private float mSpeed; // в км/ч
    @DatabaseField(dataType = DataType.FLOAT)
    private float mCourse;
    @DatabaseField(dataType = DataType.FLOAT)
    private float mAccuracy;
    @DatabaseField(dataType = DataType.INTEGER)
    private int mSatellites;

    public long getId() {
        return mId;
    }

    public long getTime() {
        return mDateTime;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public float getCourse() {
        return mCourse;
    }

    public float getAccuracy() {
        return mAccuracy;
    }

    public void setLongitude(double value) {
        mLongitude = value;
    }

    public void setLatitude(double value) {
        mLatitude = value;
    }

    public void setSpeed(float value) {
        mSpeed = value;
    }

    public void setCourse(float value) {
        mCourse = value;
    }

    protected AndroidDevicePacket() {
        mSerialId = null;
        mDateTime = 0;
        mLongitude = 0;
        mLatitude = 0;
        mSpeed = 0;
        mCourse = 0;
    }

    public AndroidDevicePacket(String serialId, Location location) {
        mSerialId = serialId;
        mDateTime = location.getTime();
        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();
        mSpeed = location.getSpeed() * 3.6f;
        mCourse = location.getBearing();
        mAccuracy = location.getAccuracy();
        mSatellites = Math.min(MIN_SATELLITES_COUNT,
                location.getExtras() != null ? location.getExtras().getInt("satellites", MIN_SATELLITES_COUNT)
                        : MIN_SATELLITES_COUNT);
    }

    public ProtocolPacket toProtocolPacket() {
        byte[] status1 = new byte[]{(byte) ((byte) 0xc0 | (byte) mSatellites), (byte) 0x00};
        return new ProtocolPacket(mSerialId, PROTOCOL_ID, TransferHelpers.toDotNetTicks(mDateTime), (float) mLongitude,
                (float) mLatitude, mSpeed, (short) Math.round(mCourse), EMPTY, EMPTY, EMPTY, STATUS_0, status1,
                new byte[]{});
    }
}