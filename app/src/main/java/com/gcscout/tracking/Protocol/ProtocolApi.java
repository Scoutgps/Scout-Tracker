package com.gcscout.tracking.Protocol;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class ProtocolApi {
    private final static int SCOUT_SERVER_TIMEOUT = 5 * 1000;
    private final static byte SCOUT_SERVER_OK_MESSAGE = 0x55;
    private static String cachedAddressName;
    private static InetAddress cachedAddress;

    public static InetAddress getScoutServerAddress(String address) throws UnknownHostException {
        if (!address.equals(cachedAddressName) || cachedAddress == null) {
            cachedAddress = InetAddress.getByName(address);
            cachedAddressName = address;
        }
        return cachedAddress;
    }

    public static boolean ping(String serverAddress, int port) {
        try {
            Socket socket = new Socket(getScoutServerAddress(serverAddress), port);
            socket.setSoTimeout(SCOUT_SERVER_TIMEOUT);
            PrintStream out = new PrintStream(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            boolean isAvailable = ping(out, in);
            socket.close();
            return isAvailable;
        } catch (IOException ex) {
            Log.w(ProtocolApi.class.toString(), "Failed to connect to server." + ex.toString());
            return false;
        }
    }

    private static boolean ping(PrintStream out, BufferedReader in) {
        try {
            out.write(ByteBuffer.allocate(4).array());
            return in.read() == SCOUT_SERVER_OK_MESSAGE;
        } catch (IOException ex) {
            Log.w(ProtocolApi.class.toString(), "Failed to connect to server." + ex.toString());
            return false;
        }
    }

    private static int sendPackets(PrintStream out, BufferedReader in, ProtocolPacket[] packets) {
        int sentCount = 0;
        try {
            for (ProtocolPacket packet : packets) {
                out.write(TransferHelpers.toLEByteArray(1));
                out.write(TransferHelpers.toByteArray(packet));
                if (in.read() != SCOUT_SERVER_OK_MESSAGE)
                    break;
                sentCount++;
            }
        } catch (IOException ex) {
            Log.w(ProtocolApi.class.toString(), "Failed to connect to server." + ex.toString());
        }
        return sentCount;
    }

    public static int sendPackets(String serverAddress, int port, ProtocolPacket[] packets) {
        int sentCount = 0;
        Socket socket = null;
        try {
            socket = new Socket(getScoutServerAddress(serverAddress), port);
            socket.setSoTimeout(SCOUT_SERVER_TIMEOUT);
            PrintStream out = new PrintStream(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (!ping(out, in))
                return 0;
            sentCount = sendPackets(out, in, packets);

            if (sentCount > 0) {
                Log.i(ProtocolApi.class.toString(), "Packets successfully have sent:");
                for (int i = 0; i < sentCount; i++) {
                    ProtocolPacket packet = packets[i];
                    byte[] bytes = TransferHelpers.toByteArray(packet);
                    StringBuilder str = new StringBuilder();
                    String byteStr;
                    for (byte b : bytes) {
                        byteStr = Integer.toHexString(b & 0xFF);
                        if (byteStr.length() == 1)
                            str.append("0");
                        str.append(byteStr).append(' ');
                    }
                    Log.i(ProtocolApi.class.toString(), packet.toString() + "; Bytes:" + str.toString());
                }
            } else
                Log.w(ProtocolApi.class.toString(), "Packets failed on sending");
        } catch (IOException ex) {
            Log.w(ProtocolApi.class.toString(), "Failed to connect to server." + ex.toString());
        } finally {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
        }

        return sentCount;
    }
}