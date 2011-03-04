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
package net.world;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class MapleMessenger implements Serializable {
    private static final long serialVersionUID = -873853186614934799L;
    private List<MapleMessengerCharacter> members = new LinkedList<MapleMessengerCharacter>();
    private int id;
    private boolean[] pos = new boolean[3];

    public MapleMessenger(int id, MapleMessengerCharacter chrfor) {
        this.members.add(chrfor);
        chrfor.setPosition(getLowestPosition());
        this.id = id;
    }

    public void addMember(MapleMessengerCharacter member) {
        members.add(member);
        member.setPosition(getLowestPosition());
    }

    public void removeMember(MapleMessengerCharacter member) {
        pos[member.getPosition()] = false;
        members.remove(member);
    }

    public void silentRemoveMember(MapleMessengerCharacter member) {
        members.remove(member);
    }

    public void silentAddMember(MapleMessengerCharacter member, int position) {
        members.add(member);
        member.setPosition(position);
    }

    public Collection<MapleMessengerCharacter> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public int getLowestPosition() {
        int position;
        if (pos[0]) {
            if (pos[1]) {
                this.pos[2] = true;
                position = 2;
            } else {
                this.pos[1] = true;
                position = 1;
            }
        } else {
            this.pos[0] = true;
            position = 0;
        }
        return position;
    }

    public int getPositionByName(String name) {
        for (MapleMessengerCharacter messengerchar : members) {
            if (messengerchar.getName().equals(name)) {
                return messengerchar.getPosition();
            }
        }
        return 4;
    }

    public int getId() {
        return id;
    }
}

