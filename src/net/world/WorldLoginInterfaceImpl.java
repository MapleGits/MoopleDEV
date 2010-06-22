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
package net.world;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import net.channel.ChannelWorldInterface;
import net.world.guild.MapleGuildCharacter;
import net.world.remote.WorldLoginInterface;

/**
 *
 * @author Matze
 */
public class WorldLoginInterfaceImpl extends UnicastRemoteObject implements WorldLoginInterface {
    private static final long serialVersionUID = 2292230575358218818L;

    public WorldLoginInterfaceImpl() throws RemoteException {
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    public Properties getDatabaseProperties() throws RemoteException {
        return WorldServer.getInstance().getDbProp();
    }

    public boolean isAvailable() throws RemoteException {
        return true;
    }

    public Map<Integer, Integer> getChannelLoad() throws RemoteException {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getAllChannelServers()) {
            ret.put(cwi.getChannelId(), cwi.getConnected());
        }
        return ret;
    }

    @Override
    public void deleteGuildCharacter(MapleGuildCharacter mgc) throws RemoteException {
        WorldRegistryImpl wr = WorldRegistryImpl.getInstance();
        wr.setGuildMemberOnline(mgc, false, -1);
        if (mgc.getGuildRank() > 1) {
            wr.leaveGuild(mgc);
        } else {
            wr.disbandGuild(mgc.getGuildId());
        }
    }
}
