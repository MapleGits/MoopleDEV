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
package net.login.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class AfterLoginHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte c2 = slea.readByte();
        byte c3 = 5;
        if (slea.available() > 0) {
            c3 = slea.readByte();
        }
        if (c2 == 1 && c3 == 1) {
            if (c.getPin() == null) {
                c.getSession().write(MaplePacketCreator.registerPin());
            } else {
                c.getSession().write(MaplePacketCreator.requestPin());
            }
        } else if (c2 == 1 && c3 == 0) {           
            String pin = slea.readMapleAsciiString();
            if (c.checkPin(pin)) {
                c.getSession().write(MaplePacketCreator.pinAccepted());
            } else {
                c.getSession().write(MaplePacketCreator.requestPinAfterFailure());
            }
        } else if (c2 == 2 && c3 == 0) {
            String pin = slea.readMapleAsciiString();
            if (c.checkPin(pin)) {
                c.getSession().write(MaplePacketCreator.registerPin());
            } else {
                c.getSession().write(MaplePacketCreator.requestPinAfterFailure());
            }
        } else if (c2 == 0 && c3 == 5) {
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
        }
     }
   }
