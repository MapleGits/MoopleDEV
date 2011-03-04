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

import client.MapleCharacter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 *
 * @author kevintjuh93
 */
public class PlayerStorage {
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    //private ReadLock readLock = lock.readLock();
    private WriteLock writeLock = lock.writeLock();

    private static PlayerStorage instance = null;
    private Map<Integer, MapleCharacter> storage = null;

    private PlayerStorage() {
        storage = new HashMap<Integer, MapleCharacter>();
    }

    public static PlayerStorage getInstance() {
        if (instance == null) instance = new PlayerStorage();

        return instance;
    }

    public void addPlayer(MapleCharacter chr) {
        writeLock.lock();
        try {
            storage.put(chr.getId(), chr);
        } finally {
            writeLock.unlock();
        }
    }

    public MapleCharacter removePlayer(int chrid) {
        writeLock.lock();
        try {
            return storage.remove(chrid);
        } finally {
            writeLock.unlock();
        }
    }
}
