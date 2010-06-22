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
/* Dances with Balrog
	Warrior Job Advancement
	Victoria Road : Warriors' Sanctuary (102000003)

	Custom Quest 100003, 100005
*/

var status = 0;
var job;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 2) {
            cm.sendOk("Make up your mind and visit me again.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getJobId()==0) {
                if (cm.getLevel() >= 10)
                    cm.sendNext("So you decided to become a #rWarrior#k?");
                else {
                    cm.sendOk("Train a bit more and I can show you the way of the #rWarrior#k.")
                    cm.dispose();
                }
            } else {
                if (cm.getLevel() >= 30 && cm.getJobId()==100) {
                    if (cm.isQuestStarted(100003)) {
                        cm.completeQuest(100005);
                        if (cm.isQuestCompleted(100005)) {
                            status = 20;
                            cm.sendNext("I see you have done well. I will allow you to take the next step on your long road.");
                        } else {
                            cm.sendOk("Go and see the #rJob Instructor#k.")
                            cm.dispose();
                        }
                    } else {
                        status = 10;
                        cm.sendNext("The progress you have made is astonishing.");
                    }
                } else if (cm.isQuestStarted(100100)) {
                    cm.completeQuest(100101);
                    if (cm.isQuestCompleted(100101)) {
                        cm.sendOk("Alright, now take this to #bTylus#k.");
                    } else {
                        cm.sendOk("Hey, " + cm.getPlayer().getName() + "! I need a #bBlack Charm#k. Go and find the Door of Dimension.");
                        cm.startQuest(100101);
                    }
                    cm.dispose();
                } else {
                    cm.sendOk("You have chosen wisely.");
                    cm.dispose();
                }
            }
        } else if (status == 1) {
            cm.sendNextPrev("It is an important and final choice. You will not be able to turn back.");
        } else if (status == 2) {
            cm.sendYesNo("Do you want to become a #rWarrior#k?");
        } else if (status == 3) {
            if (cm.getJobId()==0)
                cm.changeJobById(100);
            cm.gainItem(1402001, 1);
            cm.sendOk("So be it! Now go, and go with pride.");
            cm.resetStats();
            cm.dispose();
        } else if (status == 11) {
            cm.sendNextPrev("You may be ready to take the next step as a #rFighter#k, #rPage#k or #rSpearman#k.")
        } else if (status == 12) {
            cm.sendSimple("What do you want to become?#b\r\n#L0#Fighter#l\r\n#L1#Page#l\r\n#L2#Spearman#l#k");
        } else if (status == 13) {
            var jobName;
            if (selection == 0) {
                jobName = "Fighter";
                job = 110;
            } else if (selection == 1) {
                jobName = "Page";
                job = 120;
            } else {
                jobName = "Spearman";
                job = 130;
            }
            cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
        } else if (status == 14) {
            cm.changeJobById(job);
            cm.sendOk("So be it! Now go, and go with pride.");
            cm.dispose();
        }
    }
}