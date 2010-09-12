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
import constants.ExpTable;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PetFoodHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        int previousFullness = 100;
        for (MaplePet pet : chr.getPets()) {
            if (pet.isSummoned()) {
                if (pet.getFullness() < previousFullness) {
		    previousFullness = pet.getFullness();

                    slea.skip(6);
                    int itemId = slea.readInt();
                    boolean gainCloseness = false;
                    if (Randomizer.getInstance().nextInt(101) > 50) {
                        gainCloseness = true;
                    }
                    if (pet.getFullness() < 100) {
                        int newFullness = pet.getFullness() + 30;
                        if (newFullness > 100) {
                            newFullness = 100;
                        }
                        pet.setFullness(newFullness);
                        final byte index = chr.getPetIndex(pet);

                        if (gainCloseness && pet.getCloseness() < 30000) {
                            int newCloseness = pet.getCloseness() + 1;
                            if (newCloseness > 30000) {
                                newCloseness = 30000;
                            }
                            pet.setCloseness(newCloseness);
                            if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                                pet.setLevel(pet.getLevel() + 1);
                                c.announce(MaplePacketCreator.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
                            }
                        }
                        c.announce(MaplePacketCreator.updatePet(pet));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), index, 1, true), true);
                    } else {
                        if (gainCloseness) {
                            int newCloseness = pet.getCloseness() - 1;
                            if (newCloseness < 0) {
                                newCloseness = 0;
                            }
                            pet.setCloseness(newCloseness);
                            if (newCloseness < ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                                pet.setLevel(pet.getLevel() - 1);
                            }
                        }
                        c.announce(MaplePacketCreator.updatePet(pet));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), chr.getPetIndex(pet), 1, false), true);
                    }

                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
                return;
                }
            }
        }
        chr.broadcast(MaplePacketCreator.enableActions());
    }
}
