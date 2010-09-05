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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleStat;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.skills.Aran;
import constants.skills.Assassin;
import constants.skills.Bandit;
import constants.skills.Bishop;
import constants.skills.Bowmaster;
import constants.skills.Brawler;
import constants.skills.ChiefBandit;
import constants.skills.Cleric;
import constants.skills.Corsair;
import constants.skills.FPArchMage;
import constants.skills.Gunslinger;
import constants.skills.ILArchMage;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Outlaw;
import constants.skills.Paladin;
import constants.skills.Rogue;
import constants.skills.Shadower;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.LittleEndianAccessor;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler {

    public static class AttackInfo {

        public int numAttacked, numDamage, numAttackedAndDamage, skill, skilllevel, stance, direction, rangedirection, charge, display;
        public List<Pair<Integer, List<Integer>>> allDamage;
        public boolean isHH = false;
        public int speed = 4;

        public MapleStatEffect getAttackEffect(MapleCharacter chr, ISkill theSkill) {
            ISkill mySkill = theSkill;
            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(skill);
            }
            int skillLevel = chr.getSkillLevel(mySkill);
            if (skillLevel == 0) {
                return null;
            }
            if (display > 80) { //Hmm
                if (!theSkill.getAction()) {
                    return null;
                }
            }
            return mySkill.getEffect(skillLevel);
        }
    }

    protected synchronized void applyAttack(AttackInfo attack, MapleCharacter player, int attackCount) {
        ISkill theSkill = null;
        MapleStatEffect attackEffect = null;
        
        if (attack.skill != 0) {
            theSkill = SkillFactory.getSkill(attack.skill);
            attackEffect = attack.getAttackEffect(player, theSkill);
            if (attackEffect == null) {
                player.getClient().getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (attack.skill != Cleric.HEAL) {
                if (player.isAlive()) {
                    attackEffect.applyTo(player);
                } else {
                    player.getClient().getSession().write(MaplePacketCreator.enableActions());
                }
            }
            if (attack.numAttacked > attackEffect.getMobCount()) {
                return;
            }
        }
        if (!player.isAlive()) {
            return;
        }
        if (attackCount != attack.numDamage && attack.skill != ChiefBandit.MESO_EXPLOSION && attack.skill != NightWalker.VAMPIRE && attack.skill != WindArcher.WIND_SHOT && attack.skill != Aran.COMBO_SMASH && attack.skill != Aran.COMBO_PENRIL && attack.skill != Aran.COMBO_TEMPEST && attack.skill != NightLord.NINJA_AMBUSH && attack.skill != Shadower.NINJA_AMBUSH) {
            return;
        }
        int totDamage = 0;
        final MapleMap map = player.getMap();

        if (attack.skill == ChiefBandit.MESO_EXPLOSION) {
            int delay = 0;
            for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
                MapleMapObject mapobject = map.getMapObject(oned.getLeft().intValue());
                if (mapobject != null && mapobject.getType() == MapleMapObjectType.ITEM) {
                    final MapleMapItem mapitem = (MapleMapItem) mapobject;
                    if (mapitem.getMeso() > 9) {
                        synchronized (mapitem) {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            TimerManager.getInstance().schedule(new Runnable() {

                                public void run() {
                                    map.removeMapObject(mapitem);
                                    map.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 4, 0), mapitem.getPosition());
                                    mapitem.setPickedUp(true);
                                }
                            }, delay);
                            delay += 100;
                        }
                    } else if (mapitem.getMeso() == 0) {
                        return;
                    }
                } else if (mapobject != null && mapobject.getType() != MapleMapObjectType.MONSTER) {
                    return;
                }
            }
        }
        for (Pair<Integer, List<Integer>> oned : attack.allDamage) {
            MapleMonster monster = map.getMonsterByOid(oned.getLeft().intValue());
            if (monster != null) {
                int totDamageToOneMonster = 0;
                for (Integer eachd : oned.getRight()) {
                    totDamageToOneMonster += eachd.intValue();
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null && (attack.skill == 0 || attack.skill == Rogue.DOUBLE_STAB || attack.skill == Bandit.SAVAGE_BLOW || attack.skill == ChiefBandit.ASSAULTER || attack.skill == ChiefBandit.BAND_OF_THIEVES || attack.skill == Shadower.ASSASSINATE || attack.skill == Shadower.TAUNT || attack.skill == Shadower.BOOMERANG_STEP)) {
                    ISkill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
                    int delay = 0;
                    int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();
                    Point monsterPosition = monster.getPosition();
                    for (Integer eachd : oned.getRight()) {
                        if (pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()) {
                            final int todrop = Math.min((int) Math.max(((double) eachd / (double) 200) * (double) maxmeso, (double) 1), maxmeso);
                            final MapleMap tdmap = player.getMap();
                            final Point tdpos = new Point((int) (monsterPosition.getX() + Randomizer.getInstance().nextInt(101) - 50), (int) (monsterPosition.getY()));
                            final MapleMonster tdmob = monster;
                            final MapleCharacter tdchar = player;
                            TimerManager.getInstance().schedule(new Runnable() {

                                public void run() {
                                    tdmap.spawnMesoDrop(todrop, todrop, tdpos, tdmob, tdchar, false);
                                }
                            }, delay);
                            delay += 100;
                        }
                    }
                } else if (attack.skill == Marksman.SNIPE) {
                    totDamageToOneMonster = 195000 + Randomizer.getInstance().nextInt(5000);
                } else if (attack.skill == Marauder.ENERGY_DRAIN || attack.skill == ThunderBreaker.ENERGY_DRAIN || attack.skill == NightWalker.VAMPIRE || attack.skill == Assassin.DRAIN) {
                    player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getMaxHp() / 2)));
                } else if (attack.skill == Bandit.STEAL) {
                    ISkill steal = SkillFactory.getSkill(Bandit.STEAL);
                    if (steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) {
                        int toSteal = monster.getDrop();
                        MapleInventoryManipulator.addById(player.getClient(), toSteal, (short) 1);
                        monster.addStolen(toSteal);
                    }
                } else if (attack.skill == FPArchMage.FIRE_DEMON) {
                    monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration() * 1000);
                } else if (attack.skill == ILArchMage.ICE_DEMON) {
                    monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration() * 1000);
                } else if (attack.skill == Outlaw.HOMING_BEACON || attack.skill == Corsair.BULLSEYE) {
                    player.setMarkedMonster(monster.getObjectId());
                }
                if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                    ISkill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
                    if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
                        monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
                    }
                }
                if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                    ISkill blind = SkillFactory.getSkill(Marksman.BLIND);
                    if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
                        monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
                    }
                }
                final int id = player.getJob().getId();
                if (id == 121 || id == 122) {
                    for (int charge = 1211005; charge < 1211007; charge++) {
                        ISkill chargeSkill = SkillFactory.getSkill(charge);
                        if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)) {
                            final ElementalEffectiveness iceEffectiveness = monster.getEffectiveness(Element.ICE);
                            if (totDamageToOneMonster > 0 && iceEffectiveness == ElementalEffectiveness.NORMAL || iceEffectiveness == ElementalEffectiveness.WEAK) {
                                monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.FREEZE, 1), chargeSkill, null, false), false, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 2000);
                            }
                            break;
                        }
                    }
                } else if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) {
                    final ISkill skill = SkillFactory.getSkill(21101003);
                    final MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));

                    if (eff.makeChanceResult()) {
                        monster.applyStatus(player, new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.NEUTRALISE, 1), skill, null, false), false, eff.getX() * 1000, false);
                    }
                } else if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                    final ISkill skill = SkillFactory.getSkill(21100005);
                    player.setHp(player.getHp() + ((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100), true);
                    player.updateSingleStat(MapleStat.HP, player.getHp());
                } else if (id == 412 || id == 422 || id == 1411) {
                    ISkill type = SkillFactory.getSkill(player.getJob().getId() == 412 ? 4120005 : (player.getJob().getId() == 1411 ? 14110004 : 4220005));
                    if (player.getSkillLevel(type) > 0) {
                        MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                        for (int i = 0; i < attackCount; i++) {
                            if (venomEffect.makeChanceResult()) {
                                if (monster.getVenomMulti() < 3) {
                                    monster.setVenomMulti((monster.getVenomMulti() + 1));
                                    MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                    monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                }
                            }
                        }
                    }
                }
                if (attack.skill != 0) {
                    if (attackEffect.getFixDamage() != -1) {
                        totDamageToOneMonster = attackEffect.getFixDamage();
                    }
                }
                if (totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0) {
                    if (attackEffect.makeChanceResult()) {
                        monster.applyStatus(player, new MonsterStatusEffect(attackEffect.getMonsterStati(), theSkill, null, false), attackEffect.isPoison(), attackEffect.getDuration());
                    }
                }
                if (attack.isHH && !monster.isBoss()) {
                    map.damageMonster(player, monster, monster.getHp() - 1);
                } else if (attack.isHH) {
                    int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
                    map.damageMonster(player, monster, (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8)));
                } else {
                    map.damageMonster(player, monster, totDamageToOneMonster);
                }
            }
        }
    }

    protected AttackInfo parseDamage(LittleEndianAccessor lea, MapleCharacter chr, boolean ranged) {
        AttackInfo ret = new AttackInfo();
        lea.readByte();
        ret.numAttackedAndDamage = lea.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new ArrayList<Pair<Integer, List<Integer>>>();
        ret.skill = lea.readInt();
        if (ret.skill > 0) {
            ret.skilllevel = chr.getSkillLevel(ret.skill);
        }
        if (ret.skill == FPArchMage.BIG_BANG || ret.skill == ILArchMage.BIG_BANG || ret.skill == Bishop.BIG_BANG || ret.skill == Gunslinger.GRENADE || ret.skill == Brawler.CORKSCREW_BLOW || ret.skill == ThunderBreaker.CORKSCREW_BLOW || ret.skill == NightWalker.POISON_BOMB) {
            ret.charge = lea.readInt();
        } else {
            ret.charge = 0;
        }
        if (ret.skill == Paladin.HEAVENS_HAMMER) {
            ret.isHH = true;
        }
        lea.skip(8);
        ret.display = lea.readByte();
        ret.direction = lea.readByte();
        ret.stance = lea.readByte();
        if (ret.skill == ChiefBandit.MESO_EXPLOSION) {
            if (ret.numAttackedAndDamage == 0) {
                lea.skip(10);
                int bullets = lea.readByte();
                for (int j = 0; j < bullets; j++) {
                    int mesoid = lea.readInt();
                    lea.skip(1);
                    ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(mesoid), null));
                }
                return ret;
            } else {
                lea.skip(6);
            }
            for (int i = 0; i < ret.numAttacked + 1; i++) {
                int oid = lea.readInt();
                if (i < ret.numAttacked) {
                    lea.skip(12);
                    int bullets = lea.readByte();
                    List<Integer> allDamageNumbers = new ArrayList<Integer>();
                    for (int j = 0; j < bullets; j++) {
                        int damage = lea.readInt();
                        allDamageNumbers.add(Integer.valueOf(damage));
                    }
                    ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(oid), allDamageNumbers));
                    lea.skip(4);
                } else {
                    int bullets = lea.readByte();
                    for (int j = 0; j < bullets; j++) {
                        int mesoid = lea.readInt();
                        lea.skip(1);
                        ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(mesoid), null));
                    }
                }
            }
            return ret;
        }
        if (ranged) {
            lea.readByte();
            ret.speed = lea.readByte();
            lea.readByte();
            ret.rangedirection = lea.readByte();
            lea.skip(7);
            if (ret.skill == Bowmaster.HURRICANE || ret.skill == Marksman.PIERCING_ARROW || ret.skill == Corsair.RAPID_FIRE || ret.skill == WindArcher.HURRICANE) {
                lea.skip(4);
            }
        } else {
            lea.readByte();
            ret.speed = lea.readByte();
            lea.skip(4);
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = lea.readInt();
            lea.skip(14);
            List<Integer> allDamageNumbers = new ArrayList<Integer>();
            for (int j = 0; j < ret.numDamage; j++) {
                int damage = lea.readInt();
                if (ret.skill == Marksman.SNIPE) {
                    damage += 0x80000000;
                }
                allDamageNumbers.add(Integer.valueOf(damage));
            }
            if (ret.skill != 5221004) {
                lea.skip(4);
            }
            ret.allDamage.add(new Pair<Integer, List<Integer>>(Integer.valueOf(oid), allDamageNumbers));
        }
        return ret;
    }
}
