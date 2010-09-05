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
import tools.data.input.SeekableLittleEndianAccessor;

public final class SummonDamageHandler extends AbstractMaplePacketHandler {
    public final class SummonAttackEntry {
        private MapleMonster monster;
        private int damage;

        public SummonAttackEntry(MapleMonster mob, int damage) {
            this.monster = mob;
            this.damage = damage;
        }

        public MapleMonster getMonster() {
            return monster;
        }

        public int getDamage() {
            return damage;
        }
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        //B0 00 77 00 00 00 8F 20 C0 00 04 01 1C FF 24 F8 48 FF 1D F8 64 00 00 00 0A 71 8F 00 06 81 00 01 D0 FF 24 F8 D0 FF 24 F8 38 04 81 62 01 00 B4 3A 6A 62
        //B0 00 78 00 00 00 21 F4 C3 00 04 02 1E 03 A8 FB D8 02 A6 FB 73 00 00 00 05 71 8F 00 06 80 01 01 8B 03 A8 FB 90 03 A8 FB 38 04 C6 71 01 00 74 00 00 00 05 71 8F 00 06 80 01 01 C8 02 30 FB CC 02 30 FB 6A 04 81 6F 01 00 B4 3A 6A 62
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
        List<SummonAttackEntry> allDamage = new ArrayList<SummonAttackEntry>();
        int numAttacked = slea.readByte();
        for (int i = 0; i < numAttacked; i++) {
            slea.skip(8);
            MapleMonster mob = player.getMap().getMonsterByOid(slea.readInt());
            slea.skip(18); // who knows
            int damage = slea.readInt();
            player.getMap().damageMonster(player, mob, damage);
            allDamage.add(new SummonAttackEntry(mob, damage));
        }
        player.getMap().broadcastMessage(player, MaplePacketCreator.summonAttack(player.getId(), summon.getSkill(), animation, allDamage), summon.getPosition());
        for (SummonAttackEntry attackEntry : allDamage) {
            int damage = attackEntry.getDamage();
            MapleMonster mob = attackEntry.getMonster();
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
