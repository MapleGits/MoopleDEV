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
package server.maps;

import java.util.concurrent.ScheduledFuture;
import server.MaplePortal;
import server.TimerManager;
import tools.MaplePacketCreator;

public class MapMonitor {
    private ScheduledFuture<?> monitorSchedule;
    private MapleMap map;
    private MaplePortal portal;
    private MapleReactor reactor;

    public MapMonitor(final MapleMap map, MaplePortal portal, int ch, MapleReactor reactor) {
        this.map = map;
        this.portal = portal;
        this.reactor = reactor;
        this.monitorSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (map.getCharacters().size() < 1) {
                    cancelAction();
                }
            }
        }, 5000);
    }

    private void cancelAction() {
        monitorSchedule.cancel(false);
        map.killAllMonsters();
        if (portal != null) {
            portal.setPortalStatus(MaplePortal.OPEN);
        }
        if (reactor != null) {
            reactor.setState((byte) 0);
            reactor.getMap().broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
        }
        map.resetReactors();
    }
}
