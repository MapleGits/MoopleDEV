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

import java.net.InetAddress;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import java.net.UnknownHostException;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MaplePortal;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ChangeMapHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (slea.available() == 0) {
            String ip = ChannelServer.getInstance(c.getChannel()).getIP(c.getChannel());
            String[] socket = ip.split(":");
            player.saveToDB(true);
            player.getCashShop().open(false);
            player.setInMTS(false);
            ChannelServer.getInstance(c.getChannel()).removePlayer(player);
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            try {
                c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                c.getSession().close(true);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else {
            slea.readByte(); // 1 = from dying 2 = regular portals
            int targetid = slea.readInt(); // FF FF FF FF
            String startwp = slea.readMapleAsciiString();
            MaplePortal portal = player.getMap().getPortal(startwp);
            slea.readByte();
            boolean wheel = slea.readShort() > 0;
            if (!portal.getPortalStatus()) {
            c.getPlayer().message("The portal is closed for now.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
            }
            if (player.getMapId() == 109040004) {
                player.getFitness().resetTimes();
            }
            if (player.getMapId() == 109030003 || player.getMapId() == 109030103) {
                player.getOla().resetTimes();
            }
            if (targetid != -1 && !player.isAlive()) {
                boolean executeStandardPath = true;
                if (player.getEventInstance() != null) {
                    executeStandardPath = player.getEventInstance().revivePlayer(player);
                }
                if (executeStandardPath) {
                    MapleMap to = player.getMap();
                    if (wheel && player.getItemQuantity(5510000, false) > 0) {
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);
                    } else {
                        player.cancelAllBuffs();
                        to = player.getMap().getReturnMap();
                        player.setStance(0);
                    }
                    player.setHp(50);
                    player.setComboCounter(0);
                    player.changeMap(to, to.getPortal(0));
                }
            } else if (targetid != -1 && player.isGM()) {
                player.setComboCounter(0);
                player.changeMap(targetid, 0);
            } else if (portal != null) {
                player.setComboCounter(0);
                portal.enterPortal(c);
            } else {
                c.getSession().write(MaplePacketCreator.enableActions());
            }
        }
        player.setRates();
    }
}
