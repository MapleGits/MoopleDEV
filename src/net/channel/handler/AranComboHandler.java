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

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class AranComboHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player.getJobType() == 2) { //Keep it this till Evan comes in Private Servers.
            if (player.getCombo() > 0 && (System.currentTimeMillis() - player.getLastAttack() > 3000)) {
                player.setCombo(1);
                c.getSession().write(MaplePacketCreator.showCombo(1));
                player.cancelBuffStats(MapleBuffStat.ARAN_COMBO);
            } else {
                int combo = player.getCombo() + 1;
	    switch (combo) {
		case 10:
		case 20:
		case 30:
		case 40:
		case 50:
		case 60:
		case 70:
		case 80:
		case 90:
		case 100:
		    SkillFactory.getSkill(21000000).getEffect(combo / 10).applyComboBuff(player, combo);
		    break;
	    }
                //player.setLastAttack(System.currentTimeMillis());
                player.setCombo(combo);
                c.getSession().write(MaplePacketCreator.showCombo(combo));
            }
        }
    }
}
