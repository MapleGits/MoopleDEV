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
import client.MapleInventoryType;
import java.net.InetAddress;
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
        MapleCharacter chr = c.getPlayer();
        if (slea.available() == 0) { //is this even used?
            String ip = ChannelServer.getInstance(c.getChannel()).getIP(c.getChannel());
            String[] socket = ip.split(":");
            chr.saveToDB(true);
            chr.getCashShop().open(false);
            chr.setInMTS(false);
            ChannelServer.getInstance(c.getChannel()).removePlayer(chr);
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            try {
                c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                c.getSession().close(true);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else { 
            slea.readByte(); // 1 = from dying 2 = regular portals < Fking wrong fkers
            int targetid = slea.readInt();
            String startwp = slea.readMapleAsciiString();
            MaplePortal portal = chr.getMap().getPortal(startwp);
            slea.readByte();
            boolean wheel = slea.readShort() > 0;
            if (!portal.getPortalStatus()) {
            c.getPlayer().message("The portal is closed for now.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
            }
            if (chr.getMapId() == 109040004) {
                chr.getFitness().resetTimes();
            }
            if (chr.getMapId() == 109030003 || chr.getMapId() == 109030103) {
                chr.getOla().resetTimes();
            }
            if (targetid != -1 && !chr.isAlive()) {
		if (chr.getEventInstance() != null) {
		    chr.getEventInstance().revivePlayer(chr);
		}
		chr.setStance(0);

		if (!wheel) {
		    chr.setHp(50);

		    final MapleMap to = chr.getMap().getReturnMap();
		    chr.changeMap(to, to.getPortal(0));
		} else {
		    if (chr.haveItem(5510000)) { // Wheel of Fortune
			chr.setHp((chr.getMaxHp() / 100) * 40);
			MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);

			final MapleMap to = chr.getMap();
			chr.changeMap(to, to.getPortal(0));
		    }
		}
	    } else if (targetid != -1 && chr.isGM()) { //WHY DOESN'T THIS FKING WORKS -RAGE QUIT-
		final MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
		chr.changeMap(to, to.getPortal(0));
	    } else {
		if (portal != null) {
		    portal.enterPortal(c);
		} else {
		    c.getSession().write(MaplePacketCreator.enableActions());
		}
	    }
            chr.setRates();
        }

    }
}
