package com.gcscout.tracking.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.util.Log;

import com.gcscout.settings.Setting;
import com.gcscout.settings.Settings;
import com.gcscout.trackerdemo.OnSettingChangedListener;

public abstract class ProtocolPacketsSender<PacketType extends PacketConvertable> implements OnSettingChangedListener {
	private final int DELAY_AFTER_FAIL_SEND = 5 * 1000;
	private final int MAX_PACKETS_TO_SEND = 1;

	private boolean mIsStarted = false;
	private final Object mStateLocker = new Object();
	private final Object mDbLocker = new Object();
	private CountDownLatch mStopWaiter;
	private CountDownLatch mPacketsWaiter;
	private boolean mIsSendByInterval;
	private long mPacketsSendInterval;
	private int mPacketsSendThreshold;
	private String mScoutServerAddress;
	private int mScoutServerPort;

	private int getCountDownValue() {
		return mIsSendByInterval ? 1 : mPacketsSendThreshold;
	}

	protected ProtocolPacketsSender() {
		Settings.addSettingsChangedListener(this);

		mIsSendByInterval = Settings
				.getSetting(Settings.PacketsSendMode)
				.equals(Settings.PACKETS_SEND_MODE_BY_INTERVAL);
		mPacketsSendInterval = Settings.getSetting(Settings.PacketsSendInterval);
		mPacketsSendThreshold = Settings.getSetting(Settings.PacketsSendThreshold);
		mScoutServerAddress = Settings.getSetting(Settings.ScoutServerAddress);
		mScoutServerPort = Settings.getSetting(Settings.ScoutServerPort);
	}

	public void addPacket(PacketType packet) {
		synchronized (mDbLocker) {
			addPacketToDatabase(packet);
			mPacketsWaiter.countDown();
		}
	}

	protected abstract void addPacketToDatabase(PacketType packet);

	protected abstract boolean havePacketsToSend();

	protected abstract PacketType[] loadPacketsFromDb(long limit);

	private void mainLoop() throws InterruptedException {
		PacketType[] packets = null;
		while (mIsStarted) {
			mPacketsWaiter.await();
			synchronized (mDbLocker) {
				packets = loadPacketsFromDb(MAX_PACKETS_TO_SEND);
				mPacketsWaiter = new CountDownLatch(getCountDownValue());
			}

			ProtocolPacket[] protocolPackets = new ProtocolPacket[packets.length];
			for (int i = 0; i < protocolPackets.length; i++) {
				Log.i(getClass().toString(), "Packet with ID=" + packets[i].getId() + " prepared to sent");
				protocolPackets[i] = packets[i].toProtocolPacket();
			}

			int sentCount = ProtocolApi.sendPackets(mScoutServerAddress, mScoutServerPort, protocolPackets);

			if (packets != null && packets.length > 0 && sentCount == 0) {
				mPacketsWaiter = new CountDownLatch(0);
				Thread.sleep(DELAY_AFTER_FAIL_SEND);
			} else {
				boolean havePacketsToSend = false;
				synchronized (mDbLocker) {
					if (packets != null && packets.length > 0) {
						ArrayList<PacketType> packetsToDelete = new ArrayList<PacketType>(sentCount);
						for (int i = 0; i < sentCount; i++) {
							packetsToDelete.add(packets[i]);
							Log.i(	getClass().toString(),
									"Packet with ID=" + packets[i].getId() + " deleting from database");
						}
						removePacketsFromDatabase(packetsToDelete);
					}
					if (havePacketsToSend = havePacketsToSend())
						mPacketsWaiter = new CountDownLatch(0);
				}
				if (!havePacketsToSend && mIsSendByInterval)
					Thread.sleep(mPacketsSendInterval);
			}
		}
		mStopWaiter.countDown();
	}

	protected abstract void removePacketsFromDatabase(List<PacketType> packets);

	public void start() {
		synchronized (mStateLocker) {
			if (mIsStarted)
				return;

			mIsStarted = true;
			mStopWaiter = new CountDownLatch(1);
		}

		mPacketsWaiter = new CountDownLatch(havePacketsToSend() ? 0 : getCountDownValue());

		new Thread(new Runnable() {
			public void run() {
				try {
					mainLoop();
				} catch (InterruptedException e) {
					mStopWaiter.countDown();
					stop();
				}
			}
		}).start();
	}

	public void stop() {
		synchronized (mStateLocker) {
			if (!mIsStarted)
				return;
			mIsStarted = false;
			mPacketsWaiter.countDown();
			try {
				mStopWaiter.await();
			} catch (InterruptedException e) {}
		}
	}

	public void onSettingChanged(Setting<?> setting) {
		if (setting == Settings.PacketsSendMode) {
			mIsSendByInterval = Settings
					.getSetting(Settings.PacketsSendMode)
					.equals(Settings.PACKETS_SEND_MODE_BY_INTERVAL);
			if (mIsSendByInterval)
				synchronized (mDbLocker) {
					for (int i = 0; i < mPacketsSendThreshold; i++)
						mPacketsWaiter.countDown();
				}
		} else if (setting == Settings.PacketsSendInterval)
			mPacketsSendInterval = Settings.getSetting(Settings.PacketsSendInterval);
		else if (setting == Settings.PacketsSendThreshold)
			synchronized (mDbLocker) {
				for (int i = 0; i < mPacketsSendThreshold; i++)
					mPacketsWaiter.countDown();
				mPacketsSendThreshold = Settings.getSetting(Settings.PacketsSendThreshold);
			}
		else if (setting == Settings.ScoutServerAddress)
			mScoutServerAddress = Settings.getSetting(Settings.ScoutServerAddress);
		else if (setting == Settings.ScoutServerPort)
			mScoutServerPort = Settings.getSetting(Settings.ScoutServerPort);
	}
}