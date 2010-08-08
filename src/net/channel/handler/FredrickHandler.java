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

import client.IItem;
import client.ItemFactory;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author kevintjuh93
 */
public class FredrickHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        byte operation = slea.readByte();

        switch (operation) {
            case 0x19: //Will never come...
                //c.getSession().write(MaplePacketCreator.getFredrick((byte) 0x24));
                break;
            case 0x1A:
            List<Pair<IItem, MapleInventoryType>> items;
        try {
            items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
                if (!check(chr, items)) {
                    c.getSession().write(MaplePacketCreator.fredrickMessage((byte) 0x21));
                    return;
                }

            chr.gainMeso(chr.getMerchantMeso(), false);
            chr.setMerchantMeso(0);
            if (deleteItems(chr)) {
                for (int i = 0; i < items.size(); i++) {
                    MapleInventoryManipulator.addFromDrop(c, items.get(i).getLeft(), false);
                }
                c.getSession().write(MaplePacketCreator.fredrickMessage((byte) 0x1E));
            } else {
                chr.message("An unknown error has occured.");
                return;
            }
            break;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
                break;
            case 0x1C: //Exit
                break;
            default:

        }
    }

    private static final boolean check(MapleCharacter chr, List<Pair<IItem, MapleInventoryType>> items) {
        if (chr.getMeso() + chr.getMerchantMeso() < 0) {
            return false;
        }
	byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
	for (Pair<IItem, MapleInventoryType> item : items) {
	    final MapleInventoryType invtype = MapleItemInformationProvider.getInstance().getInventoryType(item.getLeft().getItemId());
	    if (invtype == MapleInventoryType.EQUIP) {
		eq++;
	    } else if (invtype == MapleInventoryType.USE) {
		use++;
	    } else if (invtype == MapleInventoryType.SETUP) {
		setup++;
	    } else if (invtype == MapleInventoryType.ETC) {
		etc++;
	    } else if (invtype == MapleInventoryType.CASH) {
		cash++;
	    }
	}
	if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() <= eq
		|| chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() <= use
		|| chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() <= setup
		|| chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() <= etc
		|| chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() <= cash) {
	    return false;
	}
	return true;
    }

    private static final boolean deleteItems(MapleCharacter chr) {
	try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM `inventoryitems` WHERE `type` = ? AND `characterid` = ?");

            ps.setInt(1, ItemFactory.MERCHANT.getValue());
            ps.setInt(2, chr.getId());
	    ps.execute();
	    ps.close();
	    return true;
	} catch (SQLException e) {
	    return false;
	}

    }
}
