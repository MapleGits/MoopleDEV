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
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 */
public final class UseRemoteHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readInt();
        int mode = slea.readInt();
        if (type == 5451000) { // Incase there is more later.
            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, type, 1, true, true);
            int npcId = 9100100; // default henesys
            if (mode != 8 && npcId != 9) {
                npcId += mode;
            } else // weird ):
            {
                switch (mode) {
                    case 8: // New Leaf City
                        npcId = 9100109;
                        break;
                    case 9: // Nautilus
                        npcId = 9100117;
                        break;
                }
            }
            NPCScriptManager.getInstance().start(c, npcId, null, null);
        }
    }
}
