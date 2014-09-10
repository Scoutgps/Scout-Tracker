package com.gcscout.tracking.Protocol;

import java.util.Date;

public final class ProtocolPacket {
	private final String mSerialId;
	private final int mProtocolId;
	private final long mDateTime;
	private final float mLongitude;
	private final float mLatitude;
	private final float mSpeed;
	private final short mCourse;
	private final byte[] mDigitIO;
	private final byte[] mAnalogChannel1;
	private final byte[] mAnalogChannel2;
	private final byte[] mStatus0;
	private final byte[] mStatus1;
	private final byte[] mData;

	public String getSerialId() {
		return mSerialId;
	}

	protected int getProtocolId() {
		return mProtocolId;
	}

	public long getDateTime() {
		return mDateTime;
	}

	public float getLongitude() {
		return mLongitude;
	}

	public float getLatitude() {
		return mLatitude;
	}

	public float getSpeed() {
		return mSpeed;
	}

	public short getCourse() {
		return mCourse;
	}

	public byte[] getDigitIO() {
		return mDigitIO.clone();
	}

	public byte[] getAnalogChannel1() {
		return mAnalogChannel1.clone();
	}

	public byte[] getAnalogChannel2() {
		return mAnalogChannel2.clone();
	}

	public byte[] getStatus0() {
		return mStatus0.clone();
	}

	public byte[] getStatus1() {
		return mStatus1.clone();
	}

	public short getDataLength() {
		return (short) getData().length;
	}

	public byte[] getData() {
		return mData.clone();
	}

	public ProtocolPacket(String serialId,
                          int protocolId,
                          long dateTime,
                          float longitude,
                          float latitude,
                          float speed,
                          short course,
                          byte[] discreteIO,
                          byte[] analogChannel1,
                          byte[] analogChannel2,
                          byte[] status0,
                          byte[] status1,
                          byte[] data) {

		mSerialId = serialId;
		mProtocolId = protocolId;
		mDateTime = dateTime;
		mLongitude = longitude;
		mLatitude = latitude;
		mSpeed = speed;
		mCourse = course;
		mDigitIO = discreteIO;
		mAnalogChannel1 = analogChannel1;
		mAnalogChannel2 = analogChannel2;
		mStatus0 = status0;
		mStatus1 = status1;
		mData = data;
	}

	@Override
	public String toString() {
		return "Packet from " + getSerialId() + "(" + getProtocolId() + "): " + new Date(getDateTime()) + "; long=" + getLongitude() + "; lat=" + getLatitude() + "; speed=" + getSpeed() + "; course=" + getCourse();
	}
}