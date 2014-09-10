package com.gcscout.tracking.Protocol;

public interface PacketConvertable {

    long getId();

    ProtocolPacket toProtocolPacket();
}
