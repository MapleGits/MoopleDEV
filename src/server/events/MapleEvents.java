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

package server.events;

import client.MapleCharacter;
import client.SkillFactory;

/**
 * Gey method, improve it next revs
 * @author kevintjuh93
 */
public class MapleEvents {
    private RescueGaga rescueGaga = null;
    private ArtifactHunt artifacts = null;

    public MapleEvents(RescueGaga rgaga, ArtifactHunt ahunt) {
        this.rescueGaga = rgaga;
        this.artifacts = ahunt;
    }

    public RescueGaga getGagaRescue() {
        return rescueGaga;
    }

    public ArtifactHunt getArtifactHunt() {
        return artifacts;
    }

   public static class RescueGaga {
        private byte fallen;
        private int completed;

        public RescueGaga(int completed) {
            this.completed = completed;
            this.fallen = 0;
        }

        public int fallAndGet() {
            fallen++;
            if (fallen > 3) {
                fallen = 0;
                return 4;
            }
            return fallen;
        }

        public byte getFallen() {
            return fallen;
        }

        public int getCompleted() {
            return completed;
        }

        public void complete() {
            completed++;
        }

        public void giveSkill(MapleCharacter chr) {
            int skillid = 0;
            switch (chr.getJobType()) {
                case 0:
                    skillid = 1013;
                    break;
                case 1:
                case 2:
                    skillid = 10001014;
            }
            long expiration = (System.currentTimeMillis() + (long) (3600 * 24 * 20 * 1000));//20 days
            if (completed < 20) {
                chr.changeSkillLevel(SkillFactory.getSkill(skillid), 1, 1, expiration); 
                chr.changeSkillLevel(SkillFactory.getSkill(skillid + 1), 1, 1, expiration);
                chr.changeSkillLevel(SkillFactory.getSkill(skillid + 2), 1, 1, expiration);
            } else {
                chr.changeSkillLevel(SkillFactory.getSkill(skillid), 2, 2, chr.getSkillExpiration(skillid));
            }
        }
    }

    public static class ArtifactHunt {
        private int amount;

        public ArtifactHunt(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }
}