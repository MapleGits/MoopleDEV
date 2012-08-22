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
License.te

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gm.server;

import client.MapleCharacter;
import gm.GMPacketCreator;
import gm.GMServerHandler;
import gm.mina.GMCodecFactory;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.server.channel.Channel;
import net.server.Server;
import net.server.world.World;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 *
 * @author kevintjuh93
 */
public class GMServer {

    private static IoAcceptor acceptor;
    private final static Map<String, IoSession> outGame = new HashMap<>();//LOL
    private final static Map<String, IoSession> inGame = new HashMap<>();
    private static boolean started = false;
    public final static String KEYWORD = "MOOPLEDEV";

    public static void startGMServer() {

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new GMServerHandler());
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new GMCodecFactory()));
        ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
        try {
            acceptor.bind(new InetSocketAddress(5252));
            System.out.println("\r\nGM Server online : Listening on port 5252.");
        } catch (Exception e) {
            System.out.println("Failed binding the GM Server to port : 5252");
        }

        for (World w : Server.getInstance().getWorlds()) {//For when 
            for (Channel c : w.getChannels()) {
                for (MapleCharacter chr : c.getPlayerStorage().getAllCharacters()) {
                    if (chr.isGM()) {
                        inGame.put(chr.getName(), chr.getClient().getSession());
                    }
                }
            }
        }
        started = true;
    }

    public static void broadcastOutGame(byte[] packet, String exclude) {
        for (IoSession ss : outGame.values()) {
            if (!ss.getAttribute("NAME").equals(exclude)) {
                ss.write(packet);
            }
        }
    }

    public static void broadcastInGame(byte[] packet) {
        for (IoSession ss : inGame.values()) {
            ss.write(packet);
        }
    }

    public static void addInGame(String name, IoSession session) {
        if (!inGame.containsKey(name)) {
            broadcastOutGame(GMPacketCreator.chat(name + " has logged in."), null);
            broadcastOutGame(GMPacketCreator.addUser(name), null);
        }
        inGame.put(name, session);//replace old one (:
    }

    public static void addOutGame(String name, IoSession session) {
        outGame.put(name, session);
    }

    public static void removeInGame(String name) {
        if (inGame.remove(name) != null) {
            broadcastOutGame(GMPacketCreator.removeUser(name), null);
            broadcastOutGame(GMPacketCreator.chat(name + " has logged out."), null);
        }
    }

    public static void removeOutGame(String name) {
        IoSession ss = outGame.remove(name);
        if (ss != null) {
            if (!ss.isClosing()) {
                broadcastOutGame(GMPacketCreator.removeUser(name), null);
                broadcastOutGame(GMPacketCreator.chat(name + " has logged out."), null);
            }
        }
    }

    public static boolean contains(String name) {
        return inGame.containsKey(name) || outGame.containsKey(name);
    }

    public static void closeAllSessions() {
        try {//I CAN AND IT'S FREE BITCHES
            Collection<IoSession> sss = Collections.synchronizedCollection(outGame.values());
            synchronized (sss) {
                final Iterator<IoSession> outIt = sss.iterator();
                while (outIt.hasNext()) {
                    outIt.next().close();
                    outIt.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getUserList(String exclude) {
        List<String> returnList = new ArrayList<>(outGame.keySet());
        returnList.remove(exclude);//Already sent in LoginHandler (So you are first on the list (:
        returnList.addAll(inGame.keySet());
        return returnList;
    }

    public static void shutdown() {//nothing to save o.o
        try {
            closeAllSessions();
            acceptor.unbind();
            System.out.println("GMServer is offline.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isStarted() {
        return started;
    }
}