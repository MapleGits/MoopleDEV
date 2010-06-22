/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.events.MapleCoconuts;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Kevin + bas ;$
 */
public final class CoconutHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*CB 00 A6 00 06 01
         * A6 00 = coconut id
         * 06 01 = ?
         */
        int id = slea.readShort();
        MapleMap map = c.getPlayer().getMap();
        MapleCoconuts nut = map.getCoconut(id);
	if (!nut.isHittable()) return;
        if (System.currentTimeMillis() < nut.getHitTime()) return;

        if (nut.getHits() > 2 && Math.random() < 0.4 && !nut.isStopped()) {
		if (Math.random() < 0.008 && map.getStopped() > 0) {
			nut.setStopped(true);
                        map.stopCoconut();
			map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
			return;
		}

		nut.setHittable(false); // for sure :)
		nut.resetHits(); // For next event (without restarts)

		if (Math.random() < 0.05 && map.getBombings() > 0) {
			map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 2));
			map.bombCoconut();
		} else if (map.getFalling() > 0) {
			map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 3));
			map.fallCoconut();
            		if (c.getPlayer().getCoconutTeam(c.getPlayer()) == 0) {
                		map.addMapleScore();
                		map.broadcastMessage(MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " of Team Maple knocks down a coconut."));
            		} else {
                		map.addStoryScore();
                		map.broadcastMessage(MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " of Team Story knocks down a coconut."));
            		}
        		map.broadcastMessage(MaplePacketCreator.coconutScore(map.getMapleScore(), map.getStoryScore()));
		}
        } else {
        	nut.hit();
        	map.broadcastMessage(MaplePacketCreator.hitCoconut(false, id, 1));
        }
    }
}
