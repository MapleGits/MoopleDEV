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
package net.login.handler;

import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleSkinColor;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CreateCharHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        if (!MapleCharacter.canCreateChar(name)) {
            return;
        }
        int job = slea.readInt();
        int face = slea.readInt();
        int hair = slea.readInt();

        int haircolor = slea.readInt();
        if (haircolor != 0 || haircolor != 7 || haircolor != 3 || haircolor != 2) return;

        int skincolor = slea.readInt();
        if (skincolor > 3) return;

        int top = slea.readInt();
        int bottom = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();
        byte gender = slea.readByte();
        if (job == 2) { //Lazy to do it for others lol
            if (gender == 0) {
                if (face != 20100 || face != 20401 || face != 20402)
                    return;
                else if (hair != 30030 || hair != 30020 || hair != 30000)
                    return;
            } else if (gender == 1 && job == 2) {
                if (face != 21700 || face != 21201 || face != 21002)
                    return;
                else if (hair != 31000 || hair != 31040 || hair != 31050)
                    return;
            }
            if (top != 1042167)
                return;
            else if (bottom != 1062115)
                return;
            else if (shoes != 1072383)
                return;
        }
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        newchar.setWorld(c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair + haircolor);
        newchar.setSkinColor(MapleSkinColor.getById(skincolor));
        newchar.setGender(gender);
        newchar.setName(name);

        if (job == 0) { // Knights of Cygnus
	    newchar.setJob(MapleJob.NOBLESSE);
	    newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1));
	} else if (job == 1) { // Adventurer
	    newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1));
	} else if (job == 2) { // Aran
	    newchar.setJob(MapleJob.LEGEND);
	    newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (byte) 0, (short) 1));
	} else {
	    System.out.println("[CHAR CREATION] A new job ID has been found: " + job); //I should ban for packet editing!
            return;
	}
        //CHECK FOR EQUIPS
        MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        IItem eq_top = MapleItemInformationProvider.getInstance().getEquipById(top);
        eq_top.setPosition((byte) -5);
        equip.addFromDB(eq_top);
        IItem eq_bottom = MapleItemInformationProvider.getInstance().getEquipById(bottom);
        eq_bottom.setPosition((byte) -6);
        equip.addFromDB(eq_bottom);
        IItem eq_shoes = MapleItemInformationProvider.getInstance().getEquipById(shoes);
        eq_shoes.setPosition((byte) -7);
        equip.addFromDB(eq_shoes);
        IItem eq_weapon = MapleItemInformationProvider.getInstance().getEquipById(weapon);
        eq_weapon.setPosition((byte) -11);
        equip.addFromDB(eq_weapon.copy());
        newchar.saveToDB(false);
        c.announce(MaplePacketCreator.addNewCharEntry(newchar));
    }
}
