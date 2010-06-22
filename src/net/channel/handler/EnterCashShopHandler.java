/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

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
import java.rmi.RemoteException;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Flav
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler {
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter mc = c.getPlayer();

		if (mc.getCashShop().isOpened())
			return;

		if (mc.getNoPets() > 0)
			mc.unequipAllPets();

		try {
			c.getChannelServer().getWorldInterface().addBuffsToStorage(mc.getId(), mc.getAllBuffs());
		} catch (RemoteException re) {
			c.getChannelServer().reconnectWorld();
		}

		mc.cancelAllBuffs();
		mc.saveToDB(true);
		mc.getCashShop().open(true);
		mc.getMap().removePlayer(mc);
		c.getSession().write(MaplePacketCreator.openCashShop(c));
		c.getSession().write(MaplePacketCreator.showCashInventory(c));
		//c.getSession().write(MaplePacketCreator.showGifts(mc.getCashShop().loadGifts()));
		c.getSession().write(MaplePacketCreator.showWishList(mc, false));
		c.getSession().write(MaplePacketCreator.showCash(mc));
	}
}
