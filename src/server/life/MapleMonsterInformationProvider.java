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
package server.life;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import tools.DatabaseConnection;

/**
 *
 * @author Matze
 */
public class MapleMonsterInformationProvider {
    public static class DropEntry {
        public DropEntry(int itemId, int chance) {
            this.itemId = itemId;
            this.chance = chance;
        }
        public int itemId;
        public int chance;
        public int assignedRangeStart;
        public int assignedRangeLength;
    }
    private static MapleMonsterInformationProvider instance = null;
    private Map<Integer, List<DropEntry>> drops = new HashMap<Integer, List<DropEntry>>();

    public static MapleMonsterInformationProvider getInstance() {
        if (instance == null) {
            instance = new MapleMonsterInformationProvider();
        }
        return instance;
    }

public List<DropEntry> retrieveDropChances(int monsterId) {
        if (drops.containsKey(monsterId)) {
            return drops.get(monsterId);
        }
        List<DropEntry> ret = new LinkedList<DropEntry>();
        if (monsterId > 9300183 && monsterId < 9300216) {
            for (int i = 2022359; i < 2022367; i++) {
                ret.add(new DropEntry(i, 10000));
            }
            drops.put(monsterId, ret);
            return ret;
        } else if (monsterId > 9300215 && monsterId < 9300269) {
            for (int i = 2022430; i < 2022434; i++) {
                ret.add(new DropEntry(i, 3333));
            }
            drops.put(monsterId, ret);
            return ret;
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT itemid, chance FROM monsterdrops WHERE (monsterid = ? AND chance >= 0) OR (monsterid <= 0)");
            ps.setInt(1, monsterId);
            ResultSet rs = ps.executeQuery();
            MapleMonster theMonster = MapleLifeFactory.getMonster(monsterId);
            while (rs.next()) {
                int chance = rs.getInt("chance");
                chance = (int) ((double) (1 / chance) * 10000);
                if (theMonster != null) {
                    chance += theMonster.getLevel();
                }
                ret.add(new DropEntry(rs.getInt("itemid"), chance));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        drops.put(monsterId, ret);
        return ret;
    }

    public void clearDrops() {
        drops.clear();
    }
}
