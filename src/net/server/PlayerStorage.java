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
package net.server;

import java.util.Collection;
import client.MapleCharacter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerStorage {
    private final Lock mutex = new ReentrantLock();
    private final Map<Integer, MapleCharacter> storage = new LinkedHashMap<Integer, MapleCharacter>();

    public void addPlayer(MapleCharacter chr) {
        mutex.lock();
        try {
            storage.put(chr.getId(), chr);
        } finally {
	    mutex.unlock();
	}
    }

    public MapleCharacter removePlayer(int chr) {
        mutex.lock();
        try {
            return storage.remove(chr);
        } finally {
            mutex.unlock();
        }
    }

    public MapleCharacter getCharacterByName(String name) {
        for (MapleCharacter chr : storage.values()) {            
            if (chr.getName().toLowerCase().equals(name.toLowerCase()))
                return chr;
        }
        return null;
    }

    public MapleCharacter getCharacterById(int id) {       
        return storage.get(id);
    }

    public Collection<MapleCharacter> getAllCharacters() {
        return storage.values();
    }

    public final void disconnectAll() {
	mutex.lock();
	try {	    
	    for (MapleCharacter chr : storage.values()) {
		if (!chr.isGM()) {
		    chr.getClient().disconnect();
		    storage.remove(chr);
		}
	    }
	} finally {
	    mutex.unlock();
	}
    }
}