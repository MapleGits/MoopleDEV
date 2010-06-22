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

import java.rmi.RemoteException;
import java.util.List;
import client.MapleCharacter;
import java.util.ArrayList;
import net.world.remote.WorldChannelInterface;
import server.TimerManager;
import tools.MaplePacketCreator;

/*
 * MapleTVEffect
 * @author MrXotic
 */
public class MapleTVEffect {
    private List<String> message = new ArrayList<String>(5);
    private MapleCharacter user;
    private static boolean active;
    private int type;
    private MapleCharacter partner;

    public MapleTVEffect(MapleCharacter user_, MapleCharacter partner_, List<String> msg, int type_) {
        this.message = msg;
        this.user = user_;
        this.type = type_;
        this.partner = partner_;
        broadcastTV(true);
    }

    public static boolean isActive() {
        return active;
    }

    private void setActive(boolean set) {
        active = set;
    }

    private void broadcastTV(boolean active_) {
        WorldChannelInterface wci = user.getClient().getChannelServer().getWorldInterface();
        setActive(active_);
        try {
            if (active_) {
                wci.broadcastMessage(null, MaplePacketCreator.enableTV().getBytes());
                wci.broadcastMessage(null, MaplePacketCreator.sendTV(user, message, type <= 2 ? type : type - 3, partner).getBytes());
                int delay = 15000;
                if (type == 4) {
                    delay = 30000;
                } else if (type == 5) {
                    delay = 60000;
                }
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        broadcastTV(false);
                    }
                }, delay);
            } else {
                wci.broadcastMessage(null, MaplePacketCreator.removeTV().getBytes());
            }
        } catch (RemoteException re) {
        }
    }
}
