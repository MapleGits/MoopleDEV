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
/*	
	Author : HaruTheHero < lol you wish
	Map(s): 		Aran Training Map 2
	Description: 		Quest - Help Kid
	Quest ID : 		21000
*/

importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.sendNext("No, Aran...There's no point of leaving here if we're to just leave the kid here all by himself. I know it's a lot to ask... but please reconsider!");
		    qm.dispose();
			return;
		}
	}
	if (status == 0)
			qm.sendAcceptDecline("Wait, where's the kid? Oh no, he must be stuck in the forest! We need to bring the kid back here before the ark leaves! Aran... please go in there and find the kid for me! I know it's a lot to ask considering you're injured... but you're our only hope!");
		else if (status == 1) {
			qm.forceStartQuest();
			qm.sendNext("#bThe kid is probably somewhere deep in the forest!#k We need to leave right now before the Black Wizard finds us, so please hurry!");
		} else if (status == 2) {
			qm.sendNextPrev("The most important thing right now is not to panic, Aran. If you want to see how far you've gone with your quest, press #bQ#k to open the quest window.");
		} else if (status == 3) {
			qm.sendNextPrev("Please rescue the kid from the forest, Aran! We cannot afford any more casualties at the hands of the Black Wizard!");
		} else if (status == 4) {
			qm.showIntro("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1", 1);
			qm.dispose();
		}
		}

function end(mode, type, selection) {

}