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

import java.util.concurrent.ScheduledFuture;
import net.world.MapleParty;
import server.TimerManager;

/**
 *
 * @author kevintjuh93
 */
public class MonsterCarnival {
    private MapleParty red, blue;
    private int redcp = 0;
    private int bluecp = 0;
    private int obtainedredcp = 0;
    private int obtainedbluecp = 0;
    private long time = 0;
    private long timeStarted = 0;
    private int rsummoncount = 0;
    private int bsummoncount = 0;
    private ScheduledFuture<?> schedule = null;

        public MonsterCarnival(MapleParty red, MapleParty blue) {
            this.red = red;
            this.blue = blue;
            this.timeStarted = System.currentTimeMillis();
            this.time = 600000;
            this.schedule = TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    if (redCP() > blueCP()) {
                        getPartyRed().showEffect("quest/carnival/win");
                        getPartyRed().playSound("MobCarnival/Win");
                        getPartyBlue().showEffect("quest/carnival/lose");
                        getPartyBlue().playSound("MobCarnival/Lose");
                    } else {
                        getPartyRed().showEffect("quest/carnival/lose");
                        getPartyRed().playSound("MobCarnival/Lose");
                        getPartyBlue().showEffect("quest/carnival/win");
                        getPartyBlue().playSound("MobCarnival/Win");
                    }
                }
            }, time);
        }

        public long getTimeLeft() {
            return time - (System.currentTimeMillis() - timeStarted);
        }

        public MapleParty getPartyRed() {
            return red;
        }

        public MapleParty getPartyBlue() {
            return blue;
        }

        public int redCP() {
            return redcp;
        }

        public int blueCP() {
            return bluecp;
        }

        public void setRedCP(int cp) {
            this.redcp = cp;
        }

        public void setBlueCP(int cp) {
            this.bluecp = cp;
        }

        public int obtainedRedCP() {
            return obtainedredcp;
        }

        public int obtainedBlueCP() {
            return obtainedbluecp;
        }

        public int getRedSummonCount() {
            return rsummoncount;
        }

        public int getBlueSummonCount() {
            return bsummoncount;
        }

        public void addRedSummonCount() {
            this.rsummoncount += 1;
        }

        public void addBlueSummonCount() {
            this.bsummoncount += 1;
        }   
}
