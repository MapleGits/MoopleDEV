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

package server.events;

import client.MapleCharacter;
import java.util.concurrent.ScheduledFuture;
import net.channel.ChannelServer;
import server.TimerManager;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author kevintjuh93
 */
public class MonsterCarnival {
    private MonsterCarnivalParty red, blue;
    private MapleMap map;
    private long time = 0;
    private long timeStarted = 0;
    private ScheduledFuture<?> schedule = null;

        public MonsterCarnival(int room, int channel, MonsterCarnivalParty red, MonsterCarnivalParty blue) {
            this.map = ChannelServer.getInstance(channel).getMapFactory().getMap(980000001 + (room * 100));
            this.red = red;
            this.blue = blue;
            this.timeStarted = System.currentTimeMillis();
            this.time = 600000;
            this.schedule = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {

                }
            }, time);
        }

        public long getTimeLeft() {
            return time - (System.currentTimeMillis() - timeStarted);
        }

        public MonsterCarnivalParty getPartyRed() {
            return red;
        }

        public MonsterCarnivalParty getPartyBlue() {
            return blue;
        }

        public MonsterCarnivalParty oppositeTeam(MonsterCarnivalParty team) {
            if (team == red)
                return blue;
            else
                return red;
        }

        public void playerLeft(MapleCharacter chr) {
            map.broadcastMessage(chr, MaplePacketCreator.leaveCPQ(chr));
        }

}
