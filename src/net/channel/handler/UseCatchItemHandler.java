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
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author BubblesDev
 */
public final class UseCatchItemHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt(); // timestamp
        short type = slea.readShort();
        int itemid = slea.readInt();
        int monsobid = slea.readInt();
        if (c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid) <= 0 || c.getPlayer().getMap().getMonsterByOid(monsobid) == null) {
            return;
        }
        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, true);
        switch (type) {
            case 0x05:
            case 0x08: // osiris put this here
                if (c.getPlayer().getMap().getMonsterByOid(monsobid).getId() == 9300101) {
                    c.getSession().write(MaplePacketCreator.catchMonster(monsobid, itemid, (byte) 0));
                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.catchMonster(monsobid, itemid, (byte) 1));
                    c.getSession().write(MaplePacketCreator.killMonster(monsobid, 0));
                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.killMonster(monsobid, 0));
                    c.getSession().write(MaplePacketCreator.makeMonsterInvisible(c.getPlayer().getMap().getMonsterByOid(monsobid)));
                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.makeMonsterInvisible(c.getPlayer().getMap().getMonsterByOid(monsobid)));
                    MapleInventoryManipulator.addById(c, 1902000, (short) 1);
                }
                break;
            case 0x0D:
                int monsHp = c.getPlayer().getMap().getMonsterByOid(monsobid).getHp();
                int monsMaxHp = c.getPlayer().getMap().getMonsterByOid(monsobid).getMaxHp();
                if (c.getPlayer().getMap().getMonsterByOid(monsobid).getId() == 9300157 && monsHp < (monsMaxHp / 2)) {
                    if (monsHp + (monsMaxHp / 3) - Randomizer.getInstance().nextInt(monsMaxHp) <= 0) {
                        c.getSession().write(MaplePacketCreator.catchMonster(monsobid, itemid, (byte) 1));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.catchMonster(monsobid, itemid, (byte) 1));
                        c.getSession().write(MaplePacketCreator.killMonster(monsobid, 0));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.killMonster(monsobid, 0));
                        c.getSession().write(MaplePacketCreator.makeMonsterInvisible(c.getPlayer().getMap().getMonsterByOid(monsobid)));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.makeMonsterInvisible(c.getPlayer().getMap().getMonsterByOid(monsobid)));
                        c.getPlayer().getMap().getMonsterByOid(monsobid).setHp(0);
                        MapleInventoryManipulator.addById(c, 4031868, (short) 1);
                    } else {
                        c.getSession().write(MaplePacketCreator.catchMonster(monsobid, itemid, (byte) 0));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.catchMonster(monsobid, itemid, (byte) 0));
                    }
                } else {
                    c.getPlayer().dropMessage(5, "Unable to capture the monster, the monster is too strong.");
                }
                break;
            default:
                System.out.println("UseCatchItemHandler: \r\n" + slea);
        }
    }
}
