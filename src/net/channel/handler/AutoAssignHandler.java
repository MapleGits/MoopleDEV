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

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Generic
 */
public class AutoAssignHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        slea.skip(8);
        int total = 0;
        if (chr.getRemainingAp() < 1) {
            return;
        }
        int togain = 0;
        for (int i = 0; i < 2; i++) {
            int type = slea.readInt();
            int tempVal = slea.readInt();
            total += tempVal;
            if (tempVal < 0 || tempVal > c.getPlayer().getRemainingAp()) {
                return;
            }
            togain += gainStatByType(chr, MapleStat.getBy5ByteEncoding(type), tempVal);
        }
        chr.setRemainingAp(chr.getRemainingAp() - (total + togain));
        chr.updateSingleStat(MapleStat.AVAILABLEAP, chr.getRemainingAp());
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    private int gainStatByType(MapleCharacter chr, MapleStat type, int gain) {
        int newVal = 0;
        if (type.equals(MapleStat.STR)) {
            newVal = chr.getStr() + gain;
            chr.setStr(newVal);
        } else if (type.equals(MapleStat.INT)) {
            newVal = chr.getInt() + gain;
            chr.setInt(newVal);
        } else if (type.equals(MapleStat.LUK)) {
            newVal = chr.getLuk() + gain;
            chr.setLuk(newVal);
        } else if (type.equals(MapleStat.DEX)) {
            newVal = chr.getDex() + gain;
            chr.setDex(newVal);
        }
        chr.updateSingleStat(type, Math.min(newVal, 999));
        if (newVal > 999) {
            return newVal - 999;
        }
        return 0;
    }
}
