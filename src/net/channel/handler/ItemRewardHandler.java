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
import java.util.Collections;
import java.util.List;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Jay Estrella/ Modified by kevintjuh93
 */
public final class ItemRewardHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt(); // will load from xml I don't care.
        if (c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot).getItemId() != itemId || c.getPlayer().getInventory(MapleInventoryType.USE).countById(itemId) < 1) return;

        double rand = Math.random() * 1000;
        List<RewardItem> rewards = MapleItemInformationProvider.getInstance().getItemReward(itemId);
        Collections.shuffle(rewards);
        for (RewardItem reward : rewards) {
            if (!MapleInventoryManipulator.checkSpace(c, reward.getItemId(), reward.getCount(), "")) {
                c.announce(MaplePacketCreator.showInventoryFull());
                break;
            }
            if (rand <= 10 && reward.getProb() != 1) continue; //Lol else you would never get it
            if ((rand <= 10 && reward.getProb() == 1) || rand <= reward.getProb()) { //Golden Pig egg shet
                if (!reward.getEffect().equals("")) c.announce(MaplePacketCreator.showEffect(reward.getEffect()));
                MapleInventoryManipulator.addById(c, reward.getItemId(), reward.getCount(), reward.getPeriod() == -1 ? -1 : (long) (System.currentTimeMillis() + (reward.getPeriod() * 1000)));
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, false, true);
                if (reward.getProb() == 1) c.getChannelServer().broadcastPacket(MaplePacketCreator.sendYellowTip(c.getPlayer().getName() + " has obtained a " + MapleItemInformationProvider.getInstance().getName(reward.getItemId()) + " from the Golden Pig's Egg."));
                break;
            }
        }
        c.announce(MaplePacketCreator.enableActions());
    }

    public static final class RewardItem {
        private int itemId, prob, period;
        private short count;
        private String effect;

        public RewardItem(int itemId, int prob, short count, int period, String effect) {
            this.itemId = itemId;
            this.prob = prob;
            this.count = count;
            this.period = period;
            this.effect = effect;
        }

        public int getItemId() {
            return itemId;
        }

        public int getProb() {
            return prob;
        }

        public short getCount() {
            return count;
        }

        public int getPeriod() {
            return period;
        }

        public String getEffect() {
            return effect;
        }
    }
}
