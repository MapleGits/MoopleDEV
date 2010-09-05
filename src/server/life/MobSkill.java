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
package server.life;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import client.MapleCharacter;
import client.MapleDisease;
import client.status.MonsterStatus;
import tools.Randomizer;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;

/**
 *
 * @author Danny (Leifde)
 */
public class MobSkill {
    private int skillId, skillLevel, mpCon;
    private List<Integer> toSummon = new ArrayList<Integer>();
    private int spawnEffect, hp, x, y;
    private long duration, cooltime;
    private float prop;
    private Point lt, rb;
    private int limit;

    public MobSkill(int skillId, int level) {
        this.skillId = skillId;
        this.skillLevel = level;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public void addSummons(List<Integer> toSummon) {
        for (Integer summon : toSummon) {
            this.toSummon.add(summon);
        }
    }

    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill) {
        MonsterStatus monStat = null;
        MapleDisease disease = null;
        boolean heal = false;
        boolean dispel = false;
        boolean seduce = false;
        boolean banish = false;
        switch (skillId) {
            case 100:
            case 110:
            case 150:
                monStat = MonsterStatus.WEAPON_ATTACK_UP;
                break;
            case 101:
            case 111:
            case 151:
                monStat = MonsterStatus.MAGIC_ATTACK_UP;
                break;
            case 102:
            case 112:
            case 152:
                monStat = MonsterStatus.WEAPON_DEFENSE_UP;
                break;
            case 103:
            case 113:
            case 153:
                monStat = MonsterStatus.MAGIC_DEFENSE_UP;
                break;
            case 114:
                heal = true;
                break;
            case 120:
                if (Math.random() > .3) {
                    disease = MapleDisease.SEAL;
                }
                break;
            case 121:
                if (Math.random() > .3) {
                    disease = MapleDisease.DARKNESS;
                }
                break;
            case 122:
                if (Math.random() > .3) {
                    disease = MapleDisease.WEAKEN;
                }
                break;
            case 123:
                if (Math.random() > .3) {
                    disease = MapleDisease.STUN;
                }
                break;
            case 124: //CURSE TODO
                break;
            case 125:
                if (Math.random() > .3) {
                    disease = MapleDisease.POISON;
                }
                break;
            case 126: // Slow
                if (Math.random() > .3) {
                    disease = MapleDisease.SLOW;
                }
                break;
            case 127:
                dispel = true;
                break;
            case 128: // Seduce
                if (Math.random() > .5) {
                    seduce = true;
                }
                break;
            case 129: // Banish
                if (lt != null && rb != null && skill) {
                    for (MapleCharacter chr : getPlayersInRange(monster, player)) {
                        chr.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
                    }
                } else {
                    player.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
                }
                break;
            case 131: // Mist
                monster.getMap().spawnMist(new MapleMist(calculateBoundingBox(monster.getPosition(), true), monster, this), x * 10, false, false);
                break;
            case 132:
                disease = MapleDisease.CONFUSE;
                break;
            case 133: // zombify
                break;
            case 140:
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    monStat = MonsterStatus.WEAPON_IMMUNITY;
                }
                break;
            case 141:
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
                    monStat = MonsterStatus.MAGIC_IMMUNITY;
                }
                break;
            case 143: //weapon damage reflect
            case 144: // magic damage reflect
            case 145: // any damage reflect
                break;
            case 154: // accuracy up
            case 155: // avoid up
            case 156: // speed up
                break;
            case 200:
                if (monster.getMap().getSpawnedMonstersOnMap() < 80) {
                    for (Integer mobId : getSummons()) {
                        MapleMonster toSpawn = MapleLifeFactory.getMonster(mobId);
                        toSpawn.setPosition(monster.getPosition());
                        int ypos, xpos;
                        xpos = (int) monster.getPosition().getX();
                        ypos = (int) monster.getPosition().getY();
                        switch (mobId) {
                            case 8500003: // Pap bomb high
                                toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
                                ypos = -590;
                                break;
                            case 8500004: // Pap bomb
                                xpos = (int) (monster.getPosition().getX() + Randomizer.getInstance().nextInt(1000) - 500);
                                if (ypos != -590) {
                                    ypos = (int) monster.getPosition().getY();
                                }
                                break;
                            case 8510100: //Pianus bomb
                                if (Math.ceil(Math.random() * 5) == 1) {
                                    ypos = 78;
                                    xpos = (int) Randomizer.getInstance().nextInt(5) + (Randomizer.getInstance().nextInt(2) == 1 ? 180 : 0);
                                } else {
                                    xpos = (int) (monster.getPosition().getX() + Randomizer.getInstance().nextInt(1000) - 500);
                                }
                                break;
                        }
                        switch (monster.getMap().getId()) {
                            case 220080001: //Pap map
                                if (xpos < -890) {
                                    xpos = (int) (Math.ceil(Math.random() * 150) - 890);
                                } else if (xpos > 230) {
                                    xpos = (int) (230 - Math.ceil(Math.random() * 150));
                                }
                                break;
                            case 230040420: // Pianus map
                                if (xpos < -239) {
                                    xpos = (int) (Math.ceil(Math.random() * 150) - 239);
                                } else if (xpos > 371) {
                                    xpos = (int) (371 - Math.ceil(Math.random() * 150));
                                }
                                break;
                        }
                        toSpawn.setPosition(new Point(xpos, ypos));
                        monster.getMap().spawnMonsterWithEffect(toSpawn, getSpawnEffect(), toSpawn.getPosition());
                    }
                }
                break;
        }
        if (heal) {
            if (lt != null && rb != null && skill) {
                List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
                if (heal) {
                    for (MapleMapObject mons : objects) {
                        ((MapleMonster) mons).heal(getX(), getY());
                    }
                }
            } else if (heal) {
                monster.heal(getX(), getY());
            }
        }
        if (disease != null || dispel || seduce || banish) {
            if (lt != null && rb != null && skill) {
                int i = 0;
                for (MapleCharacter character : getPlayersInRange(monster, player)) {
                    if (!character.isActiveBuffedValue(2321005)) {
                        if (dispel) {
                            character.dispel();
                        } else if (banish) {
                            MapleMap to = player.getMap().getReturnMap();
                            character.changeMap(to, to.getPortal((short) (10 * Math.random())));
                        } else if (seduce) {
                            if (i < 10) {
                                character.giveDebuff(MapleDisease.SEDUCE, this);
                                i++;
                            }
                        } else {
                            character.giveDebuff(disease, this);
                        }
                    }
                }
            } else if (dispel) {
                player.dispel();
            } else {
                player.giveDebuff(disease, this);
            }
        }
        monster.usedSkill(skillId, skillLevel, cooltime);
        monster.setMp(monster.getMp() - getMpCon());
    }

    private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
        List<MapleCharacter> players = new ArrayList<MapleCharacter>();
        players.add(player);
        return monster.getMap().getPlayersInRange(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), players);
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getMpCon() {
        return mpCon;
    }

    public List<Integer> getSummons() {
        return Collections.unmodifiableList(toSummon);
    }

    public int getSpawnEffect() {
        return spawnEffect;
    }

    public int getHP() {
        return hp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getDuration() {
        return duration;
    }

    public long getCoolTime() {
        return cooltime;
    }

    public Point getLt() {
        return lt;
    }

    public Point getRb() {
        return rb;
    }

    public int getLimit() {
        return limit;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        int multiplier = facingLeft ? 1 : -1;
        Point mylt = new Point(lt.x * multiplier + posFrom.x, lt.y + posFrom.y);
        Point myrb = new Point(rb.x * multiplier + posFrom.x, rb.y + posFrom.y);
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInBox(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), objectTypes);
    }
}
