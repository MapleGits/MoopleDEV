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

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendOk("Hmmmm...ok then.");//idiots :(
            cm.dispose();
            return;
        }
        status++;
		if (cm.getPlayer().getMapId() == 922240200) {
			if (selection == 1) {
				cm.dispose();//not yet
				return;
			}
				if (status == 0) {
					cm.sendSimple("Did you have something to say...?\r\n\r\n#b#L0#I want to rescue Gaga.#l\r\n#L1#I want to go to the Space Mine.#l");		
				} else if (status == 1) {
					cm.sendYesNo("What do we do now? It's just a rumor yet, but... I've heard that scary things happen to you if you get kidnapped by aliens... may be that's what happenning to Gaga right now! Please, please rescue Gaga! \r\n #bGaga may be a bit indetermined and clueless, but#k he has a really good heart. I can't let something terrible happen to him. Right! Grandpa from the moon might know how to rescue him! I will send you to the moon, so please go meet Grandpa and rescue Gaga");
				} else if (status == 2) {
					var number = -1;
					var mapFactory = cm.getClient().getChannelServer().getMapFactory();
					for (var i = 0; i < 20; i++) {
						if (mapFactory.getMap(922240000 + i).getCharacters().isEmpty() && mapFactory.getMap(922240100 + i).getCharacters().isEmpty()) {
							number = i;
							break;
						}	    
					}
					if (number > -1) 
						cm.warp(922240000 + i);
					else 
						cm.sendNext("There are currently no empty maps, please try again later.");
					
					cm.dispose();	
				}
		} else if (cm.getPlayer().getMapId() == 922240100) {
			if (status == 0) {
				var text = "You went through so much trouble to rescue Gaga, but it looks like we're back to square one. ";				
				var rgaga = cm.getPlayer().getEvents().getGagaRescue();
				if (rgaga.getCompleted() == 10 || rgaga.getCompleted() == 20) {
					text += "Please don't give up untill Gaga is rescued. To show you my appreciation for what you've accomplished thus far, I've given you a Spaceship. It's rather worn out, but it should still be operational. Check your #bSkill Window#k.";
					rgaga.giveSkill(cm.getPlayer());
				} else 
					text += "Let's go back now.";
					
				cm.sendNext(text);	
			} else if (status == 1) {
				cm.warp(922240200);
				cm.dispose();
			}
		} else if (cm.getPlayer().getMapId() == 922240000) {
			if (status == 0) 
				cm.sendYesNo("Are you sure you don't want to save Gaga?");
			if (status == 1) {
				cm.warp(922240200);
				cm.dispose();
			}
		}
    }
}