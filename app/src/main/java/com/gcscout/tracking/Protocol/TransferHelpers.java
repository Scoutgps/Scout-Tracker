package com.gcscout.tracking.Protocol;

import org.apache.http.util.ByteArrayBuffer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TransferHelpers {
    public static void putBytesInBuffer(ByteArrayBuffer buffer, byte[] data) {
        if (data == null || data.length == 0)
            return;

        buffer.append(data, 0, data.length);
    }

    private static byte[] getDataLength(byte[] data) {
        long size = data.length;
        if (size < 128)
            return new byte[]{(byte) size};
        else {
            byte[] length = toLEByteArray((short) size);
            length[0] = (byte) (length[0] | 0x80);
            return length;
        }
    }

    public static long toDotNetTicks(long javaMillis) {
        return 621355968000000000L + javaMillis * 10000;
    }

    public static byte[] toLEByteArray(float number) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(number).array();
    }

    public static byte[] toLEByteArray(int number) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(number).array();
    }

    public static byte[] toLEByteArray(long number) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(number).array();
    }

    public static byte[] toLEByteArray(short number) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(number).array();
    }

    public static byte[] toLimitLengthString(String source) throws UnsupportedEncodingException {
        byte[] result = new byte[source.length() + 1];
        byte[] strBytes = source.getBytes("UTF-8");
        result[0] = (byte) (source.length() % 256);
        System.arraycopy(strBytes, 0, result, 1, source.length() % 256);
        return result;
    }

    public static byte[] toByteArray(ProtocolPacket packet) throws UnsupportedEncodingException {
        ByteArrayBuffer buffer = new ByteArrayBuffer(0);

        putBytesInBuffer(buffer, toLimitLengthString(packet.getSerialId()));
        putBytesInBuffer(buffer, toLEByteArray(packet.getProtocolId()));
        putBytesInBuffer(buffer, toLEByteArray(packet.getDateTime()));

        putBytesInBuffer(buffer, toLEByteArray(packet.getLongitude()));
        putBytesInBuffer(buffer, toLEByteArray(packet.getLatitude()));
        putBytesInBuffer(buffer, toLEByteArray(packet.getSpeed()));
        putBytesInBuffer(buffer, toLEByteArray(packet.getCourse()));

        putBytesInBuffer(buffer, packet.getDigitIO());
        putBytesInBuffer(buffer, packet.getAnalogChannel1());
        putBytesInBuffer(buffer, packet.getAnalogChannel2());
        putBytesInBuffer(buffer, packet.getStatus0());
        putBytesInBuffer(buffer, packet.getStatus1());

        putBytesInBuffer(buffer, getDataLength(packet.getData()));
        putBytesInBuffer(buffer, packet.getData());

        return buffer.buffer();
    }
}
