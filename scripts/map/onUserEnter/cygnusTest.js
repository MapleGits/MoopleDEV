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
	Author: Traitor
	Map(s):	Cygnus Intro Maps
	Desc:   Sends the disable UI packet, etc.
*/
importPackage(Packages.scripting.npc);
importPackage(Packages.tools);

function start(ms) {
    var mapid = ms.getPlayer().getMap().getId();
    switch (mapid) {
        case 913040000:
            ms.getClient().getSession().write(MaplePacketCreator.cygnusIntroDisableUI(true));
            ms.getClient().getSession().write(MaplePacketCreator.cygnusIntroLock(true));
        case 913040001:
        case 913040002:
        case 913040003:
        case 913040004:
        case 913040005:
            ms.getClient().getSession().write(MaplePacketCreator.showCygnusIntro(mapid - 913040000));
            ms.getPlayer().setAllowWarpToId(mapid + 1);
            break;
        case 913040006:
            ms.getClient().getSession().write(MaplePacketCreator.cygnusIntroDisableUI(false));
            ms.getClient().getSession().write(MaplePacketCreator.cygnusIntroLock(false));
            ms.getPlayer().setAllowWarpToId(-1);
            NPCScriptManager.getInstance().start(ms.getClient(), 1103005, null, null);
            break;
    }
}