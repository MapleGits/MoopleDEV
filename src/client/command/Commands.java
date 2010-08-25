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
package client.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import java.awt.Point;
import java.io.File;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import net.MaplePacket;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import net.world.remote.WorldLocation;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.npc.NPCScriptManager;
import scripting.reactor.ReactorScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.MapleTrade;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;

public class Commands {

    public static void processCommand(MapleClient c, String text, char header) {
        boolean ret = false;
        String[] sp = text.split(" ");
        sp[0] = sp[0].substring(1);
        if (executeGMCommand(c, sp)) {
            ret = true;
        } else if (executeHeadGMCommand(c, sp)) {
            ret = true;
        } else if (executeAdminCommand(c, sp)) {
            ret = true;
        }
        if (!ret) {
            switch (c.getPlayer().gmLevel()) {
                case 0:
                    if (header == '!') {
                        c.getPlayer().dropMessage(6, "You do not have enought privillages to use these commands.");
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    if (header == '!') {
                        c.getPlayer().dropMessage(6, "Invalid syntax OR Command OR You do not have enough privilege for the command.");
                    }
                    break;
            }
        }
    }

    private static String joinString(String[] splitted, int position) {
        StringBuilder builder = new StringBuilder();
        for (int i = position; i < splitted.length; i++) {
            builder.append(splitted[i]);
            if (i != splitted.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private static int optionalArgInt(String[] splitted, int pos, Integer def) {
        if (splitted.length > pos) {
            try {
                return Integer.parseInt(splitted[pos]);
            } catch (NumberFormatException nfe) {
                return def;
            }
        }
        return def;
    }

    public static boolean executeGMCommand(MapleClient c, String[] splitted) {
        MapleCharacter player = c.getPlayer();
        if (player.gmLevel() < 2) {
            return false;
        }
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equalsIgnoreCase("search")) {
            if (splitted.length > 2) {
                String search = joinString(splitted, 2);
                MapleData data = null;
                MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
                player.dropMessage("~Searching~ <<Type: " + splitted[1] + " | Search: " + search + ">>");
                if (!splitted[1].equalsIgnoreCase("ITEM")) {
                    if (splitted[1].equalsIgnoreCase("NPC")) {
                        data = dataProvider.getData("Npc.img");
                    } else if (splitted[1].equalsIgnoreCase("MAP")) {
                        data = dataProvider.getData("Map.img");
                    } else if (splitted[1].equalsIgnoreCase("MOB")) {
                        List<String> retMobs = new LinkedList<String>();
                        data = dataProvider.getData("Mob.img");
                        List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
                        for (MapleData mobIdData : data.getChildren()) {
                            int mobIdFromData = Integer.parseInt(mobIdData.getName());
                            String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
                            mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
                        }
                        for (Pair<Integer, String> mobPair : mobPairList) {
                            if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                                retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
                            }
                        }
                        if (retMobs != null && retMobs.size() > 0) {
                            for (String singleRetMob : retMobs) {
                                player.dropMessage(singleRetMob);
                            }
                        } else {
                            player.dropMessage("No Mob's Found");
                        }
                    } else if (splitted[1].equalsIgnoreCase("SKILL")) {
                        data = dataProvider.getData("Skill.img");
                    } else {
                        player.dropMessage("Invalid search.\nSyntax: '/search [type] [name]', where [type] is NPC, MAP, ITEM, MOB, or SKILL.");
                        return false;
                    }
                    List<Pair<Integer, String>> searchList = new LinkedList<Pair<Integer, String>>();
                    for (MapleData searchData : data.getChildren()) {
                        int searchFromData = Integer.parseInt(searchData.getName());
                        String infoFromData = splitted[1].equalsIgnoreCase("MAP") ? MapleDataTool.getString(searchData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(searchData.getChildByPath("mapName"), "NO-NAME") : MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
                        searchList.add(new Pair<Integer, String>(searchFromData, infoFromData));
                    }
                    for (Pair<Integer, String> searched : searchList) {
                        if (searched.getRight().toLowerCase().contains(search.toLowerCase())) {
                            player.dropMessage(searched.getLeft() + " - " + searched.getRight());
                        }
                    }
                } else {
                    for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                        if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            player.dropMessage(itemPair.getLeft() + " - " + itemPair.getRight());
                        }
                    }
                    player.dropMessage("Search Complete.");
                }
            } else {
                player.dropMessage("Invalid search.\nSyntax: '/search [type] [name]', where [type] is NPC, MAP, ITEM, MOB, or SKILL.");
            }
        } else if (splitted[0].equalsIgnoreCase("ap")) {
            player.setRemainingAp(optionalArgInt(splitted, 1, 1));
            player.updateSingleStat("AVAILABLEAP", player.getRemainingAp());
        } else if (splitted[0].equalsIgnoreCase("gmshop")) {
            MapleShopFactory.getInstance().getShop(1337).sendShop(c);
        } else if (splitted[0].equalsIgnoreCase("sp")) {
            player.setRemainingSp(optionalArgInt(splitted, 1, 1));
            player.updateSingleStat("AVAILABLESP", player.getRemainingSp());
        } else if (splitted[0].equalsIgnoreCase("job")) {
            player.changeJob(MapleJob.getById(optionalArgInt(splitted, 1, 0)));
        } else if (splitted[0].equalsIgnoreCase("whereami")) {
            player.dropMessage(6, "You are on map " + player.getMap().getId());
        } else if (splitted[0].equalsIgnoreCase("opennpc")) {
            int npcid = optionalArgInt(splitted, 1, 0);
            MapleNPC npc = MapleLifeFactory.getNPC(npcid);
            if (npc != null && !npc.getName().equalsIgnoreCase("MISSINGNO")) {
                NPCScriptManager.getInstance().start(c, npcid, null, player);
            } else {
                player.dropMessage(6, "Npc not found.");
            }
        } else if (splitted[0].equalsIgnoreCase("healmap")) {
            for (MapleCharacter map : player.getMap().getCharacters()) {
                if (map != null) {
                    map.setHp(map.getCurrentMaxHp());
                    map.updateSingleStat("HP", map.getHp());
                    map.setMp(map.getCurrentMaxMp());
                    map.updateSingleStat("MP", map.getMp());
                }
            }
        } else if (splitted[0].equalsIgnoreCase("item")) {
            int itemId = optionalArgInt(splitted, 1, 0);
            short quantity = (short) optionalArgInt(splitted, 2, 1);
            if (itemId >= 5000000 && itemId < 5000065) {
                MaplePet.createPet(itemId);
            } else {
                MapleInventoryManipulator.addById(c, itemId, quantity, player.getName(), -1);
            }
        } else if (splitted[0].equalsIgnoreCase("level")) {
            player.setLevel(optionalArgInt(splitted, 1, 1) - 1);
            player.levelUp(true);
        } else if (splitted[0].equalsIgnoreCase("online")) {
            for (ChannelServer ch : ChannelServer.getAllInstances()) {
                String s = "Characters online (Channel " + ch.getChannel() + " Online: " + ch.getPlayerStorage().getAllCharacters().size() + ") : ";
                if (ch.getPlayerStorage().getAllCharacters().size() < 50) {
                    for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                        s += MapleCharacter.makeMapleReadable(chr.getName()) + ", ";
                    }
                    player.dropMessage(s.substring(0, s.length() - 2));
                }
            }
        } else if (splitted[0].equalsIgnoreCase("gmtalk") || splitted[0].equalsIgnoreCase("!")) {
            try {
                c.getChannelServer().getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(6, c.getPlayer().getName() + " : " + joinString(splitted, 1)).getBytes());
            } catch (RemoteException ex) {
                c.getChannelServer().reconnectWorld();
                ex.printStackTrace();
            }
        } else if (splitted[0].equalsIgnoreCase("unbuffmap")) {
            for (MapleCharacter map : player.getMap().getCharacters()) {
                if (map != null && map != player) {
                    map.cancelAllBuffs();
                }
            }
        } else if (splitted[0].equalsIgnoreCase("mesos")) {
            player.gainMeso(optionalArgInt(splitted, 1, 1), true);
        } else if (splitted[0].equalsIgnoreCase("cancelbuffs")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).cancelAllBuffs();
        } else if (splitted[0].equalsIgnoreCase("slap")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int damage = optionalArgInt(splitted, 1, 1);
            if (victim.getHp() > damage) {
                victim.setHp(victim.getHp() - damage);
                victim.dropMessage(5, player.getName() + " picked up a big fish and slapped you across the head. You've lost " + damage + " hp");
                player.dropMessage(6, victim.getName() + " has " + victim.getHp() + " HP left");
            } else {
                victim.setHp(0);
                victim.dropMessage(5, player.getName() + " gave you a headslap with a fish.");
            }
        } else if (splitted[0].equalsIgnoreCase("rreactor")) {
            player.getMap().resetReactors();
        } else if (splitted[0].equalsIgnoreCase("nxslimes")) {
            for (int amnt = optionalArgInt(splitted, 1, 1); amnt > 0; amnt--) {
                player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9400202), player.getPosition());
            }
        } else if (splitted[0].equalsIgnoreCase("horntail")) {
            MapleMonster ht = MapleLifeFactory.getMonster(8810026);
            player.getMap().spawnMonsterOnGroundBelow(ht, player.getPosition());
            player.getMap().killMonster(ht, player, false);
            player.getMap().broadcastMessage(MaplePacketCreator.serverNotice(0, "As the cave shakes and rattles, here comes Horntail."));
        } else if (splitted[0].equalsIgnoreCase("kill")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.setHp(0);
            } else {
                player.dropMessage(6, "Player not found.");
            }
        } else if (splitted[0].equalsIgnoreCase("killall")) {
            List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                player.getMap().killMonster(monster, player, true);
                monster.giveExpToCharacter(player, monster.getExp() * c.getPlayer().getExpRate(), true, 1);
            }
            player.dropMessage("Killed " + monsters.size() + " monsters.");
        } else if (splitted[0].equalsIgnoreCase("say")) {
            String tag = null;
            switch (player.gmLevel()) {
                case 1:
                    tag = "Donator-";
                    break;
                case 2:
                    tag = "GM-";
                    break;
                case 3:
                    tag = "HeadGM-";
                    break;
                case 4:
                    tag = "Admin-";
                    break;
                case 5:
                    tag = "Owner-";
                    break;
                default:
                    break;
            }
            if (splitted.length > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                sb.append(tag);
                sb.append(c.getPlayer().getName());
                sb.append("] ");
                sb.append(joinString(splitted, 1));
                try {
                    ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(c.getPlayer().getName(), MaplePacketCreator.serverNotice(6, sb.toString()).getBytes());
                } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
            } else {
                c.getPlayer().dropMessage(6, "Syntax: !say <message>");
            }
        } else if (splitted[0].equalsIgnoreCase("gender")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.setGender(victim.getGender() == 1 ? 0 : 1);
                victim.getClient().getSession().write(MaplePacketCreator.getCharInfo(victim));
                victim.getMap().removePlayer(victim);
                victim.getMap().addPlayer(victim);
            } else {
                player.dropMessage(6, "Player is not on.");
            }
        } else if (splitted[0].equalsIgnoreCase("setall")) {
            final int x = optionalArgInt(splitted, 1, 1);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat("STR", x);
            player.updateSingleStat("DEX", x);
            player.updateSingleStat("INT", x);
            player.updateSingleStat("LUK", x);
        } else if (splitted[0].equalsIgnoreCase("fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.setFame(optionalArgInt(splitted, 2, 1));
            } else {
                player.dropMessage(6, "Player not found");
            }
        } else if (splitted[0].equalsIgnoreCase("heal")) {
            player.setHp(player.getCurrentMaxHp());
            player.updateSingleStat("HP", player.getHp());
            player.setMp(player.getCurrentMaxMp());
            player.updateSingleStat("MP", player.getMp());
        } else if (splitted[0].equalsIgnoreCase("unbuff")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.cancelAllBuffs();
            } else {
                player.dropMessage(6, "Player not found");
            }
        } else if (splitted[0].equalsIgnoreCase("dc")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null) {
                player.dropMessage(6, "Player not found");
            } else {
                victim.getClient().disconnect();
                player.dropMessage(6, "Player disconnected.");
            }
        } else if (splitted[0].equalsIgnoreCase("warp")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (splitted.length == 2) {
                    MapleMap target = victim.getMap();
                    player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                } else {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(optionalArgInt(splitted, 2, 0));
                    victim.changeMap(target, target.getPortal(0));
                }
            } else {
                try {
                    victim = player;
                    WorldLocation loc = cserv.getWorldInterface().getLocation(splitted[1]);
                    if (loc != null) {
                        player.dropMessage(6, "You will be cross-channel warped. This may take a few seconds.");
                        MapleMap target = cserv.getMapFactory().getMap(loc.map);
                        victim.cancelAllBuffs();
                        String ip = cserv.getIP(loc.channel);
                        victim.getMap().removePlayer(victim);
                        victim.setMap(target);
                        String[] socket = ip.split(":");
                        if (victim.getTrade() != null) {
                            MapleTrade.cancelTrade(player);
                        }
                        victim.saveToDB(true);
                        ChannelServer.getInstance(c.getChannel()).removePlayer(player);
                        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                        try {
                            c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        MapleMap target = cserv.getMapFactory().getMap(Integer.parseInt(splitted[1]));
                        player.changeMap(target, target.getPortal(0));
                    }
                } catch (Exception e) {
                }
            }
        } else if (splitted[0].equalsIgnoreCase("warphere")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));
        } else if (splitted[0].equalsIgnoreCase("map")) {
            player.changeMap(c.getChannelServer().getMapFactory().getMap(optionalArgInt(splitted, 1, 0)), c.getChannelServer().getMapFactory().getMap(optionalArgInt(splitted, 1, 0)).getPortal(optionalArgInt(splitted, 2, 0)));
        } else if (splitted[0].equalsIgnoreCase("warpallhere")) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != player.getMapId()) {
                    mch.changeMap(player.getMap(), player.getPosition());
                }
            }
        } else if (splitted[0].equalsIgnoreCase("goto")) {
            Map<String, Integer> maps = new HashMap<String, Integer>();
            maps.put("gmmap", 180000000);
            maps.put("southperry", 60000);
            maps.put("amherst", 1010000);
            maps.put("henesys", 100000000);
            maps.put("ellinia", 101000000);
            maps.put("perion", 102000000);
            maps.put("kerning", 103000000);
            maps.put("lith", 104000000);
            maps.put("sleepywood", 105040300);
            maps.put("florina", 110000000);
            maps.put("orbis", 200000000);
            maps.put("happy", 209000000);
            maps.put("elnath", 211000000);
            maps.put("ludi", 220000000);
            maps.put("aqua", 230000000);
            maps.put("leafre", 240000000);
            maps.put("mulung", 250000000);
            maps.put("herb", 251000000);
            maps.put("omega", 221000000);
            maps.put("korean", 222000000);
            maps.put("nlc", 600000000);
            maps.put("excavation", 990000000);
            maps.put("showa", 801000000);
            maps.put("shrine", 800000000);
            maps.put("fm", 910000000);
            maps.put("ariant", 260000100);
            maps.put("cbd", 540000000);
            maps.put("boatquay", 541000000);
            maps.put("jail", 980000010);
            //Boss Maps
            maps.put("pianus", 230040420);
            maps.put("mushmom", 100000005);
            maps.put("giffery", 240020101);
            maps.put("manon", 240020401);
            maps.put("jrbalrog", 105090900);
            maps.put("zakum", 280030000);
            maps.put("papu", 220080001);
            maps.put("bigfoot", 610010012);
            maps.put("headlesshorseman", 610010011);
            maps.put("zombiemushmom", 105070002);
            maps.put("crow", 800020130);
            maps.put("anego", 801040003);

            //Function
            if (splitted.length < 2) { //If no arguments, list options.
                player.dropMessage(6, "Syntax: !goto <mapname> <optional_target>");
                StringBuilder builder = new StringBuilder();
                for (String mapss : maps.keySet()) {
                    if (1 % 10 == 0) {// 10 maps per line
                        player.dropMessage(6, builder.toString());
                    } else {
                        builder.append(mapss).append(", ");
                    }
                }
                player.dropMessage(6, builder.toString());
            } else {
                String message = splitted[1];
                if (maps.containsKey(message)) {
                    if (splitted.length == 2) { //If no target name, continue
                        player.changeMap(c.getChannelServer().getMapFactory().getMap(maps.get(message)));
                    } else if (splitted.length == 3) { //If target name, new target
                        MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
                        victim.changeMap(c.getChannelServer().getMapFactory().getMap(maps.get(message)));
                    }
                } else {
                    player.dropMessage(6, "Could not find map");
                }
            }
            maps.clear();
        } else if (splitted[0].equalsIgnoreCase("servermessage")) {
            cserv.setServerMessage(joinString(splitted, 1));
        } else if (splitted[0].equalsIgnoreCase("fakerelog")) {
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
            player.getMap().removePlayer(player);
            player.getMap().addPlayer(player);
        } else if (splitted[0].equalsIgnoreCase("spawn")) {
            int mid = optionalArgInt(splitted, 1, 100100);
            int num = optionalArgInt(splitted, 2, 1);
            MapleMonster mob = MapleLifeFactory.getMonster(mid);
            for (int i = 0; i < num; i++) {
                player.getMap().spawnMonsterOnGroundBelow(mob, player.getPosition());
            }
        } else if (splitted[0].equalsIgnoreCase("ban")) {
            String reason = player.getName() + " banned " + splitted[1] + ": " + joinString(splitted, 2);
            MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (target != null) {
                String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
                String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                reason += readableTargetName + " (IP: " + ip + ")";
                target.ban(reason, true);
                try {
                    cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned.").getBytes());
                    cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, "Reason: " + joinString(splitted, 2)).getBytes());
                } catch (RemoteException e) {
                    cserv.reconnectWorld();
                }
                c.getSession().write(MaplePacketCreator.getGMEffect(4, (byte) 0));
            } else if (MapleCharacter.ban(splitted[1], reason, false)) {
                c.getSession().write(MaplePacketCreator.getGMEffect(4, (byte) 0));
            } else {
                c.getSession().write(MaplePacketCreator.getGMEffect(6, (byte) 1));
            }
        } else if (splitted[0].equalsIgnoreCase("msearch")) {
            try {
                if (joinString(splitted, 1).length() > 3) {
                    BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + joinString(splitted, 1) + "&check=true").openConnection().getInputStream()));
                    String s = null;
                    int page = optionalArgInt(splitted, 2, 1);
                    for (int i = 0; i < page * 20 + 20; i++) {
                        if (i >= page * 20 && i <= page * 20 + 20) {
                            s = dis.readLine();
                            if (s != null) {
                                player.dropMessage(s);
                            }
                            if (i > page * 20 || s == null) {
                                break;
                            }
                        }
                    }
                    dis.close();
                } else {
                    player.dropMessage(5, "Please specify a search name with more than 3 letters.");
                }
            } catch (Exception e) {
            }
        } else if (splitted[0].equalsIgnoreCase("position")) {
            Point pos = player.getPosition();
            player.dropMessage(6, "X: " + pos.x + " | Y: " + pos.y + "  | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + player.getMap().getFootholds().findBelow(pos).getId());
        } else if (splitted[0].equalsIgnoreCase("cleardrops")) {
            player.getMap().clearDrops(player, true);
        } else if (splitted[0].equalsIgnoreCase("chattype")) {
            player.toggleGMChat();
            player.message("You now chat in " + (player.getGMChat() ? "white." : "black."));
        } else {
            return false;
        }
        return true;
    }

    public static boolean executeHeadGMCommand(MapleClient c, String[] splitted) {
        MapleCharacter player = c.getPlayer();
        if (player.gmLevel() < 3) {
            return false;
        }
        if (splitted[0].equals("speakall")) {
            String text = joinString(splitted, 1);
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
            }
        } else if (splitted[0].equals("dcall")) {
            for (ChannelServer cservers : ChannelServer.getAllInstances()) {
                Collection<MapleCharacter> cmc = new LinkedHashSet<MapleCharacter>(cservers.getPlayerStorage().getAllCharacters());
                for (MapleCharacter mch : cmc) {
                    if (!mch.isGM() && mch != null) {
                        try {
                            mch.getClient().disconnect();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else if (splitted[0].equalsIgnoreCase("notice")) {
            int range = optionalArgInt(splitted, 1, -1);
            StringBuilder sb = new StringBuilder();
            if (range == -1) {
                sb.append(joinString(splitted, 1));
                range = 1;
            } else {
                sb.append(joinString(splitted, 2));
            }
            MaplePacket packet = MaplePacketCreator.serverNotice(6, sb.toString());
            if (range == 0) {
                player.getMap().broadcastMessage(packet);
            } else if (range == 1) {
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                try {
                    ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(player.getName(), packet.getBytes());
                } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
            }
        } else if (splitted[0].equalsIgnoreCase("speak")) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                String text = joinString(splitted, 2);
                victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), text, false, 0));
            } else {
                player.dropMessage(6, "Player not found");
            }
        } else if (splitted[0].equalsIgnoreCase("playernpc")) {
            int scriptId = optionalArgInt(splitted, 2, -1);
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (splitted.length != 3) {
                player.dropMessage(6, "Pleaase use the correct syntax. !playernpc <char name> <script name>");
            } else if (scriptId < 9901000 || scriptId > 9901319 || scriptId == -1) {
                player.dropMessage(6, "Please enter a script name between 9901000 and 9901319");
            } else if (victim == null) {
                player.dropMessage(6, "The character is not in this channel");
            } else {
                player.playerNPC(victim, scriptId);
            }
        } else if (splitted[0].equalsIgnoreCase("removeplayernpcs")) {
            int npcid = 0;
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                for (MapleMapObject object : channel.getMapFactory().getMap(player.getMapId()).getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER_NPC))) {
                    channel.getMapFactory().getMap(player.getMapId()).removeMapObject(object);
                }
            }
            Connection con = DatabaseConnection.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs WHERE map = ?");
                ps.setInt(1, player.getMapId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    npcid = rs.getInt("id");
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ?");
                ps.setInt(1, npcid);
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("DELETE FROM playernpcs WHERE map = ?");
                ps.setInt(1, player.getMapId());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                for (MapleCharacter characters : channel.getMapFactory().getMap(player.getMapId()).getCharacters()) {
                    characters.changeMap(characters.getMap(), characters.getMap().findClosestSpawnpoint(characters.getPosition()));
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public static boolean executeAdminCommand(MapleClient c, String[] splitted) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (player.gmLevel() < 5) {
            return false;
        }
        if (splitted[0].equals("drop")) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int itemId = optionalArgInt(splitted, 1, 0);
            short quantity = (short) optionalArgInt(splitted, 2, 1);
            if (itemId != 0) {
                IItem toDrop;
                if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    toDrop = ii.getEquipById(itemId);
                } else {
                    toDrop = new Item(itemId, (byte) 0, quantity);
                }
                player.getMap().spawnItemDrop(player, player, toDrop, player.getPosition(), true, true);
            }
        } else if (splitted[0].equals("shutdown")) {
            int time = optionalArgInt(splitted, 1, 5);
            if (time < 5) {
                player.dropMessage(6, "Please allow at least 5 minutes before shutting down.");
                return true;
            }
            c.getChannelServer().shutdown(time);
        } else if (splitted[0].equals("shutdownworld")) {
            int time = optionalArgInt(splitted, 1, 5);
            if (time < 5) {
                player.dropMessage(6, "Please allow at least 5 minutes before shutting down.");
                return true;
            }
            try {
                c.getChannelServer().getWorldInterface().shutdown(time);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        } else if (splitted[0].equalsIgnoreCase("mesoperson")) {
            int mesos = optionalArgInt(splitted, 2, 1);
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                player.dropMessage(6, splitted[1] + " gained " + mesos + " mesos.");
                victim.gainMeso(mesos, true, true, true);
            } else {
                player.dropMessage(6, "Player was not found");
            }
        } else if (splitted[0].equalsIgnoreCase("kill")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.setHp(0);
                victim.setMp(0);
                victim.updateSingleStat("HP", 0);
                victim.updateSingleStat("MP", 0);
            } else {
                player.dropMessage(6, "Player not found");
            }
        } else if (splitted[0].equalsIgnoreCase("jobperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            int job = optionalArgInt(splitted, 2, 0);
            if (victim != null) {
                victim.setJob(MapleJob.getById(job));
            } else {
                player.dropMessage(6, "Player not found");
            }
        } else if (splitted[0].equals("pspawn")) {
            int mobid = optionalArgInt(splitted, 1, 0);
            int mobtime = optionalArgInt(splitted, 2, 0);
            MapleMonster mob = MapleLifeFactory.getMonster(mobid);
            int xpos = player.getPosition().x;
            int ypos = player.getPosition().y;
            int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
            if (mob != null) {
                mob.setPosition(player.getPosition());
                mob.setCy(ypos);
                mob.setRx0(xpos + 50);
                mob.setRx1(xpos - 50);
                mob.setFh(fh);
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( nmid, f, fh, cy, rx0, rx1, type, x, y, mobtime, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, mobid);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "m");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, mobtime);
                    ps.setInt(11, player.getMapId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    player.dropMessage(6, "Failed to save MOB to the database.");
                    e.printStackTrace();
                }
                player.getMap().addMonsterSpawn(mob, mobtime);
            } else {
                player.dropMessage(6, "You have entered an invalid Monster id.");
            }
        } else if (splitted[0].equals("pnpc")) {
            int npcId = optionalArgInt(splitted, 1, 0);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            int xpos = player.getPosition().x;
            int ypos = player.getPosition().y;
            int fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(player.getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos + 50);
                npc.setRx1(xpos - 50);
                npc.setFh(fh);
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( nmid, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, npcId);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "n");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, player.getMapId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    player.dropMessage(6, "Failed to save NPC to the database.");
                    e.printStackTrace();
                }
                player.getMap().addMapObject(npc);
                player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            } else {
                player.dropMessage(6, "You have entered an invalid Npc id.");
            }
        } else if (splitted[0].equalsIgnoreCase("tdrops")) {
            player.getMap().toggleDrops();
        } else if (splitted[0].equalsIgnoreCase("reloadguilds")) {
            try {
                player.dropMessage(6, "Attempting to reload all guilds... this may take a while...");
                cserv.getWorldInterface().clearGuilds();
                player.dropMessage(6, "Completed.");
            } catch (RemoteException re) {
                player.dropMessage(6, "RemoteException occurred while attempting to reload guilds.");
            }
        } else if (splitted[0].equalsIgnoreCase("reloadreactordrops")) {
            ReactorScriptManager.getInstance().clearDrops();
        } else if (splitted[0].equalsIgnoreCase("reloaddrops")) {
            MapleMonsterInformationProvider.getInstance().clearDrops();
        } else if (splitted[0].equalsIgnoreCase("itemvac")) {
            List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
            for (MapleMapObject item : items) {
                MapleMapItem mapItem = (MapleMapItem) item;
                if (mapItem.getMeso() > 0) {
                    player.gainMeso(mapItem.getMeso(), true);
                } else if (mapItem.getItem().getItemId() >= 5000000 && mapItem.getItem().getItemId() <= 5000100) {
                    int petId = MaplePet.createPet(mapItem.getItem().getItemId());
                    if (petId == -1) {
                        return false;
                    }
                    MapleInventoryManipulator.addById(c, mapItem.getItem().getItemId(), mapItem.getItem().getQuantity(), null, petId);
                } else {
                    MapleInventoryManipulator.addFromDrop(c, mapItem.getItem(), true);
                }
                mapItem.setPickedUp(true);
                player.getMap().removeMapObject(item); // just incase ?
                player.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, player.getId()), mapItem.getPosition());
            }
        } else if (splitted[0].equalsIgnoreCase("levelperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setLevel(optionalArgInt(splitted, 2, victim.getLevel() + 1) - 1);
            victim.levelUp(true);
        } else if (splitted[0].equalsIgnoreCase("gc")) {
            try {
                System.gc();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            player.dropMessage(6, "Garbage Collection Completed.");
        } else {
            return false;
        }
        return true;
    }
}
