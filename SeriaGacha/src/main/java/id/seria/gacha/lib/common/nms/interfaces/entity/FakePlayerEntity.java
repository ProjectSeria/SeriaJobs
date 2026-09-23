package id.seria.gacha.lib.common.nms.interfaces.entity;

import id.seria.gacha.lib.common.nms.interfaces.packets.FakePacket;

public interface FakePlayerEntity extends FakeEntity {
   FakePacket preparePlayerInfoAddPacket();

   FakePacket preparePlayerInfoRemovePacket();
}
