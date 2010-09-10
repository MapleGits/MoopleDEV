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

import java.util.ArrayList;
import java.util.List;
import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.status.MonsterStatusEffect;
import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SummonDamageHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleCharacter player = c.getPlayer();
        if (!player.isAlive()) {
            return;
        }
        MapleSummon summon = null;
        for (MapleSummon sum : player.getSummons().values()) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }
        if (summon == null) {
            return;
        }
        ISkill summonSkill = SkillFactory.getSkill(summon.getSkill());
        MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        slea.skip(4);
        int animation = slea.readByte();
        List<Pair<MapleMonster, Integer>> allDamage = new ArrayList<Pair<MapleMonster, Integer>>();
        int numAttacked = slea.readByte();
        for (int i = 0; i < numAttacked; i++) {
            slea.skip(8);
            MapleMonster mob = player.getMap().getMonsterByOid(slea.readInt());
            slea.skip(18); // who knows
            int damage = slea.readInt();
            player.getMap().damageMonster(player, mob, damage);
            allDamage.add(new Pair<MapleMonster, Integer>(mob, damage));
        }
        player.getMap().broadcastMessage(player, MaplePacketCreator.summonAttack(player.getId(), summon.getSkill(), animation, allDamage), summon.getPosition());
        for (Pair<MapleMonster, Integer> attack : allDamage) {
            int damage = attack.getRight();
            MapleMonster mob = attack.getLeft();
            if (mob != null) {
                if (damage > 0 && summonEffect.getMonsterStati().size() > 0) {
                    if (summonEffect.makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(summonEffect.getMonsterStati(), summonSkill, null, false);
                        mob.applyStatus(player, monsterStatusEffect, summonEffect.isPoison(), 4000);
                    }
                }
                player.getMap().damageMonster(player, mob, damage);
            }
        }
    }
}
