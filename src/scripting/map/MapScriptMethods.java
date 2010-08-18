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
package scripting.map;

import client.MapleClient;
import client.SkillFactory;
import scripting.AbstractPlayerInteraction;
import server.TimerManager;
import tools.MaplePacketCreator;

public class MapScriptMethods extends AbstractPlayerInteraction {
    public MapScriptMethods(MapleClient c) {
    	super(c);
    }

    public void displayAranIntro() {
        switch (c.getPlayer().getMapId()) {
            case 914090010:
                lockUI();
		c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene0"));
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                        public void run() {
                            c.getPlayer().changeMap(914090011);
                        }                   
                }, 14000);

                break;
            case 914090011:
                    c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene1"  + c.getPlayer().getGender()));
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                        public void run() {
                            c.getPlayer().changeMap(914090012);
                        }
                }, 16000);
                break;
            case 914090012:
                    c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene2" + c.getPlayer().getGender()));
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                        public void run() {
                            c.getPlayer().changeMap(914090013);
                        }                   
                }, 12000);
                break;
            case 914090013:
		c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene3"));
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                        public void run() {                      
                            c.getPlayer().changeMap(140090000);
                        }                   
                }, 12000);
                break;
            case 914090100:
                lockUI();
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/HandedPoleArm" + c.getPlayer().getGender()));
                
                TimerManager.getInstance().schedule(new Runnable() {
                    @Override
                        public void run() {                    
                            c.getPlayer().changeMap(140000000);
                        }                   
                }, 5000);
                break;
        }
    }

    public void arriveIceCave() {
        unlockUI();
        c.getPlayer().changeSkillLevel(SkillFactory.getSkill(20000014), -1, 0, -1);
	c.getPlayer().changeSkillLevel(SkillFactory.getSkill(20000015), -1, 0, -1);
	c.getPlayer().changeSkillLevel(SkillFactory.getSkill(20000016), -1, 0, -1);
	c.getPlayer().changeSkillLevel(SkillFactory.getSkill(20000017), -1, 0, -1);
	c.getPlayer().changeSkillLevel(SkillFactory.getSkill(20000018), -1, 0, -1);
        c.getPlayer().setRemainingSp(0);
        c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/ClickLilin"));
    }

    public void startExplorerExperience() {
        lockUI();
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                c.getPlayer().changeMap(1020000);
            }
        }, (c.getPlayer().getMapId() == 1020200 || c.getPlayer().getMapId() == 1020500) ? 4000 : 3000);
        
            if (c.getPlayer().getMapId() == 1020100)  //Swordman
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction3.img/swordman/Scene" + c.getPlayer().getGender()));
            else if (c.getPlayer().getMapId() == 1020200) //Magician
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction3.img/magician/Scene" + c.getPlayer().getGender()));
            else if (c.getPlayer().getMapId() == 1020300) //Archer
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction3.img/archer/Scene" + c.getPlayer().getGender()));
            else if (c.getPlayer().getMapId() == 1020400) //Rogue
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction3.img/rogue/Scene" + c.getPlayer().getGender()));
             else if (c.getPlayer().getMapId() == 1020500) //Pirate
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction3.img/pirate/Scene" + c.getPlayer().getGender()));
    }


    public void enterRien() {
        if (c.getPlayer().getJob().getId() == 2100 && !c.getPlayer().getAranIntroState("ck=1")) {
            c.getPlayer().addAreaData(21019, "miss=o;arr=o;ck=1;helper=clear");
            c.getSession().write(MaplePacketCreator.updateAreaInfo("miss=o;arr=o;ck=1;helper=clear", 21019));
            unlockUI();
        }
    }

    public void goAdventure() {
        lockUI();
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                c.getPlayer().changeMap(10000, 4);
            }
        }, 14200);
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction3.img/goAdventure/Scene" + c.getPlayer().getGender()));
    }

    public void goLith() {
        lockUI();
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                c.getPlayer().changeMap(104000000);
            }
        }, 5000);
                c.getSession().write(MaplePacketCreator.showIntro("Effect/Direction3.img/goLith/Scene" + c.getPlayer().getGender()));
    }

    public void lockUI() {
        c.getSession().write(MaplePacketCreator.disableUI(true));
	c.getSession().write(MaplePacketCreator.lockUI(true));
    }

    public void unlockUI() {
        c.getSession().write(MaplePacketCreator.disableUI(false));
	c.getSession().write(MaplePacketCreator.lockUI(false));
    }


}

