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

import java.sql.PreparedStatement;
import client.MapleClient;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import net.AbstractMaplePacketHandler;

public final class NoteActionHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int action = slea.readByte();
        if (action == 1) {
            int num = slea.readByte();
            slea.readByte();
            slea.readByte();
            for (int i = 0; i < num; i++) {
                int id = slea.readInt();
                slea.readByte();
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM notes WHERE `id`=?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
