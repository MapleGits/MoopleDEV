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
package server;

import client.MapleCharacter;
import net.server.Channel;
import net.server.Server;

/**
 * @author Frz
 */
public class ShutdownServer implements Runnable {
    private int myWorld, myChannel;

    public ShutdownServer(int world, int channel) {
        myWorld = world;
        myChannel = channel;
    }

    @Override
    public void run() {
        for (Channel cs : Server.getInstance().getAllChannels()) {
            for (MapleCharacter chr : cs.getPlayerStorage().getAllCharacters()) chr.getClient().disconnect();
        }
        /*int c = 200;
        for (int i = 0; i < Server.getInstance().getLoad().size(); i++) {
            try {
                Server.getInstance().getRegistry().deregisterChannelServer(i, myChannel);
            } catch (Exception e) {
            }
        }
        try {
            Channel.getInstance(myChannel).unbind();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        boolean allShutdownFinished = true;
        for (Channel cserv : Channel.getAllInstances()) {
            if (!cserv.hasFinishedShutdown()) {
                allShutdownFinished = false;
            }
        }
        if (allShutdownFinished) {
            TimerManager.getInstance().stop();
            System.exit(0);
        }*/
    }
}