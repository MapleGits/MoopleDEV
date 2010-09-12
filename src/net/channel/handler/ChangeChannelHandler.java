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

import java.net.InetAddress;
import java.rmi.RemoteException;
import client.MapleBuffStat;
import client.MapleClient;
import java.io.IOException;
import net.AbstractMaplePacketHandler;
import net.world.MapleMessengerCharacter;
import server.MapleTrade;
import server.maps.FieldLimit;
import server.maps.HiredMerchant;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class ChangeChannelHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int channel = slea.readByte() + 1;
        if (c.getPlayer().isBanned()) {
            c.disconnect();
            return;
        }
        if (!c.getPlayer().isAlive() || FieldLimit.CHANGECHANNEL.check(c.getPlayer().getMap().getFieldLimit())) {
            c.announce(MaplePacketCreator.enableActions());
            return;
        }
        String[] socket = c.getChannelServer().getIP(channel).split(":");
        if (c.getPlayer().getTrade() != null) {
            MapleTrade.cancelTrade(c.getPlayer());
        }
        c.getPlayer().cancelMagicDoor();
        c.getPlayer().saveCooldowns();
        if (c.getPlayer().getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }
        if (c.getPlayer().getBuffedValue(MapleBuffStat.PUPPET) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        if (c.getPlayer().getBuffedValue(MapleBuffStat.COMBO) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.COMBO);
        }
        c.getPlayer().expiretask.cancel(false);
        HiredMerchant merchant = c.getPlayer().getHiredMerchant();
        if (merchant != null) {
            if (merchant.isOwner(c.getPlayer())) {
                merchant.setOpen(true);
            } else {
                merchant.removeVisitor(c.getPlayer());
            }
        }
        try {
            c.getChannelServer().getWorldInterface().addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        c.getPlayer().saveToDB(true);
        if (c.getPlayer().getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
            try {
                c.getChannelServer().getWorldInterface().silentLeaveMessenger(c.getPlayer().getMessenger().getId(), messengerplayer);
            } catch (RemoteException e) {
                c.getChannelServer().reconnectWorld();
            }
        }
        c.getPlayer().getMap().removePlayer(c.getPlayer());
        c.getChannelServer().removePlayer(c.getPlayer());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        try {
            c.announce(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
        } catch (IOException e) {
        }
    }
}