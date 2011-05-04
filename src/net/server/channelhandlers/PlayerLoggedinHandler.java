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
package net.server.channelhandlers;

import client.BuddylistEntry;
import client.CharacterNameAndId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleFamily;
import client.MapleInventoryType;
import client.SkillFactory;
import java.sql.SQLException;
import java.util.List;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import net.server.Channel;
import net.server.CharacterIdChannelPair;
import net.server.MaplePartyCharacter;
import net.server.PartyOperation;
import net.server.PlayerBuffValueHolder;
import net.server.Server;
import net.server.World;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler {
    @Override
    public final boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int cid = slea.readInt();
        MapleCharacter player = Server.getInstance().getPlayerStorage().removePlayer(cid);
        if (player == null) {
            try {
                player = MapleCharacter.loadCharFromDB(cid, c, true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            player.channelChanged(c);
        }
        c.setPlayer(player);
        c.setAccID(player.getAccountID());
        int state = c.getLoginState();
        boolean allowLogin = true;
        Channel cserv = c.getChannelServer();

        synchronized (this) {
                World world = Server.getInstance().getWorld(c.getWorld());
                if (state == MapleClient.LOGIN_SERVER_TRANSITION) {
                    for (String charName : c.loadCharacterNames(c.getWorld())) {
                        if (world.isConnected(charName))
                            allowLogin = false;
                            break;
                    }
                }

            if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
                c.setPlayer(null);
                c.getSession().close(true);
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
        }
        cserv.addPlayer(player);
        List<PlayerBuffValueHolder> buffs = Server.getInstance().getPlayerBuffStorage().getBuffsFromStorage(cid);
        if (buffs != null) {
            player.silentGiveBuffs(buffs);
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM cooldowns WHERE charid = ?");
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int skillid = rs.getInt("SkillID");
                final long length = rs.getLong("length"), startTime = rs.getLong("StartTime");
                if (length + startTime < System.currentTimeMillis()) {
                    continue;
                }
                player.giveCoolDowns(skillid, startTime, length);
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM cooldowns WHERE charid = ?");
            ps.setInt(1, player.getId());
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("SELECT Mesos FROM dueypackages WHERE RecieverId = ? and Checked = 1");
            ps.setInt(1, player.getId());
            rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    PreparedStatement pss = DatabaseConnection.getConnection().prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
                    pss.setInt(1, player.getId());
                    pss.executeUpdate();
                    pss.close();
                } catch (SQLException e) {
                }
                c.announce(MaplePacketCreator.sendDueyMSG((byte) 0x1B));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        c.announce(MaplePacketCreator.getCharInfo(player));
        if (!player.isHidden()) player.toggleHide(true);
        player.sendKeymap();
        player.sendMacros();
        player.getMap().addPlayer(player);
        World world = Server.getInstance().getWorld(c.getWorld());
        world.getPlayerStorage().addPlayer(player);
        int buddyIds[] = player.getBuddylist().getBuddyIds();
        world.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
        for (CharacterIdChannelPair onlineBuddy : Server.getInstance().getWorld(c.getWorld()).multiBuddyFind(player.getId(), buddyIds)) {
             BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
             ble.setChannel(onlineBuddy.getChannel());
             player.getBuddylist().put(ble);
        }
        c.announce(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        c.announce(MaplePacketCreator.loadFamily(player));
        if (player.getFamilyId() > 0) {
            MapleFamily f = world.getFamily(player.getFamilyId());
            if (f == null) {
                f = new MapleFamily(player.getId());
                world.addFamily(player.getFamilyId(), f);
            }
            player.setFamily(f);
            c.announce(MaplePacketCreator.getFamilyInfo(f.getMember(player.getId())));
        }
        Server server = Server.getInstance();
        if (player.getGuildId() > 0) {
            MapleGuild playerGuild = server.getGuild(player.getGuildId(), player.getMGC());
            if (playerGuild == null) {
                player.deleteGuild(player.getGuildId());
                player.resetMGC();
                player.setGuildId(0);
            } else {
                Server.getInstance().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.announce(MaplePacketCreator.showGuildInfo(player));
                int allianceId = player.getGuild().getAllianceId();
                if (allianceId > 0) {
                    MapleAlliance newAlliance = server.getAlliance(allianceId);
                    if (newAlliance == null) {
                        newAlliance = MapleAlliance.loadAlliance(allianceId);
                        if (newAlliance != null) {
                            server.addAlliance(allianceId, newAlliance);
                        } else {
                            player.getGuild().setAllianceId(0);
                        }
                    }
                    if (newAlliance != null) {
                        c.announce(MaplePacketCreator.getAllianceInfo(newAlliance));
                        c.announce(MaplePacketCreator.getGuildAlliances(newAlliance, c));
                        server.allianceMessage(allianceId, MaplePacketCreator.allianceMemberOnline(player, true), player.getId(), -1);
                    }
                }
            }
        }
        player.showNote();
        if (player.getParty() != null) {
            MaplePartyCharacter pchar = player.getMPC();
            pchar.setChannel(c.getChannel());
            pchar.setMapId(player.getMapId());
            pchar.setOnline(true);
            world.updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, pchar);
        }
        player.updatePartyMemberHP();
        CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
        if (pendingBuddyRequest != null) {
            player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), "Default Group", pendingBuddyRequest.getId(), -1, false));
            c.announce(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), player.getId(), pendingBuddyRequest.getName()));
        }
        if (player.getInventory(MapleInventoryType.EQUIPPED).findById(1122017) != null) {
            player.equipPendantOfSpirit();
        }
        c.announce(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
        c.announce(MaplePacketCreator.updateGender(player));
        player.checkMessenger();
        c.announce(MaplePacketCreator.enableReport());
        player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (player.getLinkedLevel() / 10), 20, -1);
        player.checkBerserk();
        player.expirationTask();
        player.setRates();
    }
}
