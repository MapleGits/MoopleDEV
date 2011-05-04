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
package net.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import client.MapleCharacter;
import constants.ServerConstants;
import java.util.ArrayList;
import java.util.Map.Entry;
import tools.DatabaseConnection;
import net.MaplePacket;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.mina.MapleCodecFactory;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.TimerManager;
import server.maps.MapleMapFactory;
import tools.MaplePacketCreator;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import server.MapleSquad;
import server.MapleSquadType;
import server.events.gm.MapleEvent;
import server.maps.HiredMerchant;
import server.maps.MapleMap;

public class Channel {
    private int port = 7575;
    private PlayerStorage players = new PlayerStorage();
    private int channel;
    private int world;
    private IoAcceptor acceptor;
    private String ip;
    private boolean shutdown = false;
    private boolean finishedShutdown = false;
    private MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private Map<Integer, MapleGuildSummary> gsStore = new HashMap<Integer, MapleGuildSummary>();
    private Map<Integer, HiredMerchant> hiredMerchants = new HashMap<Integer, HiredMerchant>();
    private MapleEvent event;
    private Map<MapleSquadType, MapleSquad> mapleSquads = new HashMap<MapleSquadType, MapleSquad>();

    public Channel(int world, int channel) {
        this.world = world;
        this.channel = channel;
        this.mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));
        this.mapFactory.setChannel(channel);
        this.mapFactory.setWorld(world);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                    for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
                        mc.saveToDB(true);
                        if (mc.getHiredMerchant() != null) {
                            if (mc.getHiredMerchant().isOpen()) {
                                try {
                                    mc.getHiredMerchant().saveItems(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                }
            }
        });        
        try {
            eventSM = new EventScriptManager(this, ServerConstants.EVENTS.split(" "));
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0");
            ps.executeUpdate();
            ps.close();
            port = 7575 + this.channel - 1;
            port += (world * 100);
            ip = ServerConstants.HOST + ":" + port;
            IoBuffer.setUseDirectBuffer(false);
            IoBuffer.setAllocator(new SimpleBufferAllocator());
            acceptor = new NioSocketAcceptor();
            TimerManager.getInstance().register(new respawnMaps(), 10000);
            acceptor.setHandler(new MapleServerHandler(PacketProcessor.getProcessor(), channel, world));
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
            acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
            
            eventSM.init();
            System.out.println("    Channel " + getId() + ": Listening on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown = true;
        boolean error = true;
        while (error) {
            try {
                for (HiredMerchant hm : getHiredMerchants().values()) {
                    hm.saveItems(true);
                }
                for (MapleCharacter chr : players.getAllCharacters()) {
                    synchronized (chr) {
                        chr.getClient().disconnect();
                    }
                    error = false;
                }
            } catch (Exception e) {
                error = true;
            }
        }
        finishedShutdown = true;
    }

    public void unbind() {
        acceptor.unbind();
    }

    public boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public int getWorld() {
        return world;
    }

    public void addPlayer(MapleCharacter chr) {
        players.addPlayer(chr);
        chr.announce(MaplePacketCreator.serverMessage(ServerConstants.SERVER_MESSAGE));
    }

    public PlayerStorage getPlayerStorage() {
        return players;
    }

    public void removePlayer(MapleCharacter chr) {
        players.removePlayer(chr.getId());
    }

    public int getConnectedClients() {
        return players.getAllCharacters().size();
    }

    public void setServerMessage(String newMessage) {
        ServerConstants.SERVER_MESSAGE = newMessage;
        broadcastPacket(MaplePacketCreator.serverMessage(ServerConstants.SERVER_MESSAGE));
    }

    public void broadcastPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.announce(data);
        }
    }

    public final int getId() {
        return channel;
    }

    public String getIP() {
        return ip;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void shutdown(int time) {
        //TimerManager.getInstance().schedule(new ShutdownServer(getChannel()), time);
    }

    public MapleEvent getEvent() {
        return event;
    }

    public void setEvent(MapleEvent event) {
        this.event = event;
    }

    public EventScriptManager getEventSM() {
        return eventSM;
    }

    public MapleGuild getGuild(MapleGuildCharacter mgc) {
        int gid = mgc.getGuildId();
        MapleGuild g = null;
        g = Server.getInstance().getGuild(gid, mgc);
        if (gsStore.get(gid) == null) {
            gsStore.put(gid, new MapleGuildSummary(g));
        }
        return g;
    }

    public MapleGuildSummary getGuildSummary(int gid) {
        if (gsStore.containsKey(gid)) {
            return gsStore.get(gid);
        } else {
            MapleGuild g = Server.getInstance().getGuild(gid, null);
            if (g != null) {
                gsStore.put(gid, new MapleGuildSummary(g));
            }
            return gsStore.get(gid);
        }
    }

    public void updateGuildSummary(int gid, MapleGuildSummary mgs) {
        gsStore.put(gid, mgs);
    }

    public void reloadGuildSummary() {
            MapleGuild g;
            for (int i : gsStore.keySet()) {
                g = Server.getInstance().getGuild(i, null);
                if (g != null) {
                    gsStore.put(i, new MapleGuildSummary(g));
                } else {
                    gsStore.remove(i);
                }
            }
    }

    public void broadcastGMPacket(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.isGM()) {
                chr.announce(data);
            }
        }
    }

    public void yellowWorldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.announce(MaplePacketCreator.sendYellowTip(msg));
        }
    }

    public void worldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.dropMessage(msg);
        }
    }

    public List<MapleCharacter> getPartyMembers(MapleParty party) {
        List<MapleCharacter> partym = new ArrayList<MapleCharacter>(8);
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar.getChannel() == getId()) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    partym.add(chr);
                }
            }
        }
        return partym;
    }

    public class respawnMaps implements Runnable {
        @Override
        public void run() {
            for (Entry<Integer, MapleMap> map : mapFactory.getMaps().entrySet()) {
                map.getValue().respawn();
            }
        }
    }

    public MapleSquad getMapleSquad(MapleSquadType type) {
        return mapleSquads.get(type);
    }

    public boolean addMapleSquad(MapleSquad squad, MapleSquadType type) {
        if (mapleSquads.get(type) == null) {
            mapleSquads.remove(type);
            mapleSquads.put(type, squad);
            return true;
        } else {
            return false;
        }
    }

    public Map<Integer, HiredMerchant> getHiredMerchants() {
        return hiredMerchants;
    }

    public void addHiredMerchant(int chrid, HiredMerchant hm) {
        hiredMerchants.put(chrid, hm);
    }

    public void removeHiredMerchant(int chrid) {
        hiredMerchants.remove(chrid);
    }  
    
    public int[] multiBuddyFind(int charIdFrom, int[] characterIds) {
        List<Integer> ret = new ArrayList<Integer>(characterIds.length);
        PlayerStorage playerStorage = getPlayerStorage();
        for (int characterId : characterIds) {
            MapleCharacter chr = playerStorage.getCharacterById(characterId);
            if (chr != null) {
                if (chr.getBuddylist().containsVisible(charIdFrom)) {
                    ret.add(characterId);
                }
            }
        }
        int[] retArr = new int[ret.size()];
        int pos = 0;
        for (Integer i : ret) {
            retArr[pos++] = i.intValue();
        }
        return retArr;
    }    
}