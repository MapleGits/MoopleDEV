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

import java.sql.Connection;
import java.sql.PreparedStatement;
import client.MapleClient;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import server.maps.FieldLimit;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class TrockAddMapHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        Connection con = DatabaseConnection.getConnection();
        byte addrem;
        addrem = slea.readByte();
        byte rocktype = slea.readByte();
        if (addrem == 0x00) {
            int mapId = slea.readInt();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM trocklocations WHERE characterid = ? AND mapid = ?");
                ps.setInt(1, c.getPlayer().getId());
                ps.setInt(2, mapId);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
            }
        } else if (addrem == 0x01) {
            if (FieldLimit.CANNOTVIPROCK.check(c.getPlayer().getMap().getFieldLimit())) {
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT into trocklocations (characterid, mapid) VALUES (?, ?)");
                    ps.setInt(1, c.getPlayer().getId());
                    ps.setInt(2, c.getPlayer().getMapId());
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                }
            } else {
                c.getPlayer().message("You may not save this map.");
            }
        }
        c.getSession().write(MaplePacketCreator.trockRefreshMapList(c.getPlayer().getId(), rocktype));
    }
}
