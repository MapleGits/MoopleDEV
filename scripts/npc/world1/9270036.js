F/*
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
/* 	Eric
	Singapore VIP Hair/Color Changer
	MADE BY AAron and Cody from the FlowsionMS Forums
*/
var status = 0;
var beauty = 0;
var mhair = Array(30110, 30290, 30230, 30260, 30320, 30190, 30240, 30350, 30270, 30180);
var fhair = Array(31260, 31090, 31220, 31250, 31140, 31160, 31100, 31120, 31030, 31270, 31810);
var hairnew = Array();

function start() {
    cm.sendNext("Please no exploit, kthxbai.");
    cm.dispose();
}

function action(mode, type, selection) {

}
