/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.channel.handler;

import client.MapleClient;
import client.MapleInventoryType;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Jay Estrella
 */
public final class FishingHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt(); // will load from xml I don't care.
        if (c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot).getItemId() != itemId || c.getPlayer().getInventory(MapleInventoryType.USE).countById(itemId) <= 0) {
            return;
        }
        for (MapleFish fish : MapleItemInformationProvider.getInstance().getFishReward(itemId)) {
            if (fish.getProb() >= Randomizer.getInstance().nextInt(9) + 1) { // out of 10 for now.
                MapleInventoryManipulator.addById(c, fish.getItemId(), (short) fish.getCount());
            }
        }
        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, false, true); // Make the item go away
    }

    public static final class MapleFish {
        private int itemId, prob, count;
        private String effect;

        public MapleFish(int itemId, int prob, int count, String effect) {
            this.itemId = itemId;
            this.prob = prob;
            this.count = count;
            this.effect = effect;
        }

        public int getItemId() {
            return itemId;
        }

        public int getProb() {
            return prob;
        }

        public int getCount() {
            return count;
        }

        public String getEffect() {
            return effect;
        }
    }
}
