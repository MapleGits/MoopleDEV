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

import java.awt.Point;
import java.io.File;
import java.sql.PreparedStatement;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.PetDataFactory;
import client.SkillFactory;
import java.sql.SQLException;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SpawnPetHandler extends AbstractMaplePacketHandler {
    private static MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        byte slot = slea.readByte();
        slea.readByte();
        boolean lead = slea.readByte() == 1;
        int petid = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId();
        if (petid == 5000028 || petid == 5000047) //Handles Dragon AND Robos
        {
            if (c.getPlayer().haveItem(petid + 1)) {
                c.getPlayer().dropMessage(5, "You can't hatch your " + (petid == 5000028 ? "Dragon egg" : "Robo egg") + " if you already have a Baby " + (petid == 5000028 ? "Dragon." : "Robo."));
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            } else {
                int evolveid = MapleDataTool.getInt("info/evol1", dataRoot.getData("Pet/" + petid + ".img"));
                int petId = MaplePet.createPet(evolveid);
                if (petId == -1) {
                    return;
                }
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM pets WHERE `petid` = ?");
                    ps.setInt(1, c.getPlayer().getInventory(MapleInventoryType.CASH).findById(petid).getPetId());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException ex) {
                }
                MapleInventoryManipulator.addById(c, evolveid, (short) 1, null, petId, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getExpiration());
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, petid, (short) 1, false, false);
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
        }
        MaplePet pet = MaplePet.loadFromDb(c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId(), slot, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getPetId());
        if (c.getPlayer().getPetIndex(pet) != -1) {
            c.getPlayer().unequipPet(pet, true);
        } else {
            if (c.getPlayer().getSkillLevel(SkillFactory.getSkill(8)) == 0 && c.getPlayer().getPet(0) != null) {
                c.getPlayer().unequipPet(c.getPlayer().getPet(0), false);
            }
            if (lead) {
                c.getPlayer().shiftPetsRight();
            }
            Point pos = c.getPlayer().getPosition();
            pos.y -= 12;
            pet.setPos(pos);
            pet.setFh(c.getPlayer().getMap().getFootholds().findBelow(pet.getPos()).getId());
            pet.setStance(0);
            c.getPlayer().addPet(pet);
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showPet(c.getPlayer(), pet, false, false), true);
            c.getSession().write(MaplePacketCreator.petStatUpdate(c.getPlayer()));
            c.getSession().write(MaplePacketCreator.enableActions());
            c.getPlayer().startFullnessSchedule(PetDataFactory.getHunger(pet.getItemId()), pet, c.getPlayer().getPetIndex(pet));
        }
    }
}
