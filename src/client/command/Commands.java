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
import client.ISkill;
import client.Item;
import client.ItemFactory;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import client.SkillFactory;
import constants.ServerConstants;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleOxQuiz;
import server.MapleShopFactory;
import server.events.MapleEvent;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.HiredMerchant;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;

public class Commands {
    public static boolean executeGMCommand(MapleClient c, String[] sub, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (sub[0].equals("ap")) {
            player.setRemainingAp(Integer.parseInt(sub[1]));
            player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
        } else if (sub[0].equals("buffme")) {
            final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i : array) {
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
            }
        } else if (sub[0].equals("spawn")) {
            if (sub.length < 2) {
                for (int i = 0; i < Integer.parseInt(sub[2]); i++) {
                    player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(sub[1])), player.getPosition());
                }
            } else {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(sub[1])), player.getPosition());
            }
        } else if (sub[0].equals("chattype")) {
            player.toggleGMChat();
            player.message("You now chat in " + (player.getGMChat() ? "white." : "black."));
        } else if (sub[0].equals("cleardrops")) {
            player.getMap().clearDrops(player, true);
        } else if (sub[0].equals("cody")) {
            NPCScriptManager.getInstance().start(c, 9200000, null, null);
        } else if (sub[0].equals("dc")) {
            cserv.getPlayerStorage().getCharacterByName(sub[1]).getClient().disconnect();
        } else if (sub[0].equals("dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            player.message("Done.");
        } else if (sub[0].equals("exprate")) {
            ServerConstants.EXP_RATE = (byte) (Integer.parseInt(sub[1]) % 128);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Exp Rate has been changed to " + Integer.parseInt(sub[1]) + "x."));
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
                    mc.setRates();
                }
            }
        } else if (sub[0].equals("fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
            victim.setFame(Integer.parseInt(sub[2]));
            victim.updateSingleStat(MapleStat.FAME, victim.getFame());
        } else if (sub[0].equals("giftnx")) {
            cserv.getPlayerStorage().getCharacterByName(sub[1]).getCashShop().gainCash(1, Integer.parseInt(sub[2]));
            player.message("Done");
        } else if (sub[0].equals("gmshop")) {
            MapleShopFactory.getInstance().getShop(1337).sendShop(c);
        } else if (sub[0].equals("heal")) {
            player.setHpMp(30000);
        } else if (sub[0].equals("id")) {
            try {
                BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + sub[1] + "&check=true").openConnection().getInputStream()));
                String s;
                while ((s = dis.readLine()) != null) {
                    player.dropMessage(s);
                }
                dis.close();
            } catch (Exception e) {
            }
        } else if (sub[0].equals("flag")) {
            try {
                for (Pair<IItem, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(player.getId(), false)) {
                    if (item.getLeft().getItemId() == 1322005) {
                        item.getLeft().setFlag(Byte.parseByte(sub[1]));
                        player.forceUpdateItem(item.getRight(), item.getLeft());
                    }
                }
            } catch (SQLException ex) {
            }
            MapleInventoryManipulator.addById(c, Integer.parseInt(sub[1]), (short) 1, null, null, Byte.parseByte(sub[2]), -1);
        } else if (sub[0].equals("item") || sub[0].equals("drop")) {
            int itemId = Integer.parseInt(sub[1]);
            short quantity = 1;
            try {
                quantity = Short.parseShort(sub[2]);
            } catch (Exception e) {
            }
            if (sub[0].equals("item")) {
                MaplePet pet = null;
                if (itemId >= 5000000 && itemId < 5000065) {
                    pet = MaplePet.createPet(itemId);
                } 
                MapleInventoryManipulator.addById(c, itemId, quantity, player.getName(), pet, -1);
            } else {
                IItem toDrop;
                if (MapleItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    toDrop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
                } else {
                    toDrop = new Item(itemId, (byte) 0, quantity);
                }
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
            }
        } else if (sub[0].equals("job")) {
            player.changeJob(MapleJob.getById(Integer.parseInt(sub[1])));
        } else if (sub[0].equals("jobperson")) {
            cserv.getPlayerStorage().getCharacterByName(sub[1]).changeJob(MapleJob.getById(Integer.parseInt(sub[2])));
        } else if (sub[0].equals("kill")) {
            cserv.getPlayerStorage().getCharacterByName(sub[1]).setHpMp(0);
        } else if (sub[0].equals("killall")) {
            List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                player.getMap().killMonster(monster, player, true);
                monster.giveExpToCharacter(player, monster.getExp() * c.getPlayer().getExpRate(), true, 1);
            }
            player.dropMessage("Killed " + monsters.size() + " monsters.");
        } else if (sub[0].equals("unbug")) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.enableActions());
        } else if (sub[0].equals("level")) {
            player.setLevel(Integer.parseInt(sub[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
        } else if (sub[0].equals("levelperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
            victim.setLevel(Integer.parseInt(sub[2]));
            victim.gainExp(-victim.getExp(), false, false);
            victim.updateSingleStat(MapleStat.LEVEL, victim.getLevel());
        } else if (sub[0].equals("levelpro")) {
            while (player.getLevel() < Math.min(255, Integer.parseInt(sub[1]))) {
                player.levelUp(false);
            }
        } else if (sub[0].equals("levelup")) {
            player.levelUp(false);
        } else if (sub[0].equals("maxstat")) {
            final String[] s = {"setall", String.valueOf(Short.MAX_VALUE)};
            executeGMCommand(c, s, heading);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
        } else if (sub[0].equals("maxskills")) {
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    player.changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel(), -1);
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                }
            }
        } else if (sub[0].equals("mesoperson")) {
            cserv.getPlayerStorage().getCharacterByName(sub[1]).gainMeso(Integer.parseInt(sub[2]), true);
        } else if (sub[0].equals("mesorate")) {
            ServerConstants.MESO_RATE = (byte) (Integer.parseInt(sub[1]) % 128);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Meso Rate has been changed to " + Integer.parseInt(sub[1]) + "x."));
        } else if (sub[0].equals("mesos")) {
            player.gainMeso(Integer.parseInt(sub[1]), true);
        } else if (sub[0].equals("notice")) {
            try {
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[Notice] " + joinStringFrom(sub, 1)).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
        } else if (sub[0].equals("openportal")) {
           c.getPlayer().getMap().getPortal(sub[1]).setPortalState(true);
        } else if (sub[0].equals("closeportal")) {
           c.getPlayer().getMap().getPortal(sub[1]).setPortalState(false);
        } else if (sub[0].equals("startevent")) {
            for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
                 c.getPlayer().getMap().startEvent(chr);
            }
            c.getChannelServer().setEvent(null);
        } else if (sub[0].equals("scheduleevent")) {
           if (c.getPlayer().getMap().hasEventNPC()) {
            if (sub[1].equals("treasure")) {
                c.getChannelServer().setEvent(new MapleEvent(109010000, 50));
            try {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
            } else if (sub[1].equals("ox")) {
                c.getChannelServer().setEvent(new MapleEvent(109020001, 50));
            try {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
            } else if (sub[1].equals("ola")) {
                c.getChannelServer().setEvent(new MapleEvent(109030101, 50)); // Wrong map but still Ola Ola
            try {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
            } else if (sub[1].equals("fitness")) {
                c.getChannelServer().setEvent(new MapleEvent(109040000, 50));
            try {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
            } else if (sub[1].equals("snowball")) {
                c.getChannelServer().setEvent(new MapleEvent(109060001, 50));
            try {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
            } else if (sub[1].equals("coconut")) {
                c.getChannelServer().setEvent(new MapleEvent(109080000, 50));
            try {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(0, "Hello Scania let's play an event in " + player.getMap().getMapName() + " CH " + c.getChannel() + "! " + player.getMap().getEventNPC()).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
            } else {
                player.message("Wrong Syntax: /scheduleevent treasure, ox, ola, fitness, snowball or coconut");
            }
           } else {
               player.message("You can only use this command in the following maps: 60000, 104000000, 200000000, 220000000");
           }

        } else if (sub[0].equals("online")) {
            for (ChannelServer ch : ChannelServer.getAllInstances()) {
                String s = "Characters online (Channel " + ch.getChannel() + " Online: " + ch.getPlayerStorage().getAllCharacters().size() + ") : ";
                if (ch.getPlayerStorage().getAllCharacters().size() < 50) {
                    for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                        s += MapleCharacter.makeMapleReadable(chr.getName()) + ", ";
                    }
                    player.dropMessage(s.substring(0, s.length() - 2));
                }
            }
        } else if (sub[0].equals("ox")) {
            if (sub[1].equals("on") && player.getMapId() == 109020001) {
                player.getMap().setOx(new MapleOxQuiz(player.getMap()));
                player.getMap().getOx().sendQuestion();
                player.getMap().setOxQuiz(true);
            } else {
                player.getMap().setOxQuiz(false);
                player.getMap().setOx(null);
            }
        } else if (sub[0].equals("pap")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
        } else if (sub[0].equals("pianus")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
        } else if (sub[0].equalsIgnoreCase("search")) {
            if (sub.length > 2) {
                String search = joinStringFrom(sub, 2);
                MapleData data = null;
                MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
                player.dropMessage("~Searching~ <<Type: " + sub[1] + " | Search: " + search + ">>");
                if (!sub[1].equalsIgnoreCase("ITEM")) {
                    if (sub[1].equalsIgnoreCase("NPC")) {
                        data = dataProvider.getData("Npc.img");
                    } else if (sub[1].equalsIgnoreCase("MAP")) {
                        data = dataProvider.getData("Map.img");
                    } else if (sub[1].equalsIgnoreCase("MOB")) {
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
                    } else if (sub[1].equalsIgnoreCase("SKILL")) {
                        data = dataProvider.getData("Skill.img");
                    } else {
                        player.dropMessage("Invalid search.\nSyntax: '/search [type] [name]', where [type] is NPC, MAP, ITEM, MOB, or SKILL.");
                        return true;
                    }
                    List<Pair<Integer, String>> searchList = new LinkedList<Pair<Integer, String>>();
                    for (MapleData searchData : data.getChildren()) {
                        int searchFromData = Integer.parseInt(searchData.getName());
                        String infoFromData = sub[1].equalsIgnoreCase("MAP") ? MapleDataTool.getString(searchData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(searchData.getChildByPath("mapName"), "NO-NAME") : MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
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
        } else if (sub[0].equals("servermessage")) {
            for (int i = 1; i <= ChannelServer.getAllInstances().size(); i++) {
                ChannelServer.getInstance(i).setServerMessage(joinStringFrom(sub, 1));
            }
        } else if (sub[0].equals("warpsnowball")) {
            for (MapleCharacter chr : player.getMap().getCharacters()) {
                 chr.changeMap(109060000, chr.getTeam());
            }
        } else if (sub[0].equals("team")) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
            victim.setTeam(Integer.parseInt(sub[2]));
        } else if (sub[0].equals("setall")) {
            final int x = Short.parseShort(sub[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        } else if (sub[0].equals("sp")) {
            player.setRemainingSp(Integer.parseInt(sub[1]));
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
        } else if (sub[0].equals("unban")) {
            try {
                PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + MapleCharacter.getIdByName(sub[1]));
                p.executeUpdate();
                p.close();
            } catch (Exception e) {
                player.message("Failed to unban " + sub[1]);
                return true;
            }
            player.message("Unbanned " + sub[1]);
        } else {
            if (player.gmLevel() == 1) {
                player.message("GM Command " + heading + sub[0] + " does not exist");
            }
            return false;
        }
        return true;
    }

    public static void executeAdminCommand(MapleClient c, String[] sub, char heading) {
        MapleCharacter player = c.getPlayer();
        if (sub[0].equals("horntail")) {
            for (int i = 8810002; i < 8810010; i++) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(i), player.getPosition());
            }
        } else if (sub[0].equals("merch")) {
            HiredMerchant merchant = new HiredMerchant(player, 5030012, "TEST");
            player.setHiredMerchant(merchant);
            player.announce(MaplePacketCreator.getHiredMerchant(player, merchant, true));
        } else if (sub[0].equals("merchp")) {
        player.announce(MaplePacketCreator.getHiredMerchant(player, player.getHiredMerchant(), true));
        } else if (sub[0].equals("packet")) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.customPacket(joinStringFrom(sub, 1)));
        } else if (sub[0].equals("npc")) {
            MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(sub[1]));
            if (npc != null) {
                npc.setPosition(player.getPosition());
                npc.setCy(player.getPosition().y);
                npc.setRx0(player.getPosition().x + 50);
                npc.setRx1(player.getPosition().x - 50);
                npc.setFh(player.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                player.getMap().addMapObject(npc);
                player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            }
        } else if (sub[0].equals("pinkbean")) {
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8820009), player.getPosition());
        } else if (sub[0].equals("playernpc")) {
            player.playerNPC(c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]), Integer.parseInt(sub[2]));
        } else if (sub[0].equals("setgmlevel")) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
            victim.setGM(Integer.parseInt(sub[2]));
            player.message("Done.");
            victim.getClient().disconnect();
        } else if (sub[0].equals("shutdown") || sub[0].equals("shutdownnow")) {
            int time = 60000;
            if (sub[0].equals("shutdownnow")) {
                time = 1;
            } else if (sub.length > 1) {
                time *= Integer.parseInt(sub[1]);
            }
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.shutdown(time);
            }
        } else if (sub[0].equals("sql")) {
            final String query = Commands.joinStringFrom(sub, 1);
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
                ps.executeUpdate();
                ps.close();
                player.message("Done " + query);
            } catch (SQLException e) {
                player.message("Query Failed: " + query);
            }
        } else if (sub[0].equals("sqlwithresult")) {
            String name = sub[1];
            final String query = Commands.joinStringFrom(sub, 2);
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    player.dropMessage(String.valueOf(rs.getObject(name)));
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                player.message("Query Failed: " + query);
            }
        } else if (sub[0].equals("zakum")) {
            player.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), player.getPosition());
            for (int x = 8800003; x < 8800011; x++) {
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), player.getPosition());
            }
        } else {
            player.message("Command " + heading + sub[0] + " does not exist.");
        }
    }

    private static String joinStringFrom(String arr[], int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}