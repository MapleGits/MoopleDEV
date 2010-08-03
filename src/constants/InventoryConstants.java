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
package constants;

import server.MapleItemInformationProvider;

/**
 *
 * @author Jay Estrella
 */
public final class InventoryConstants {
    public final static int LOCK = 0x01;
    public final static int SPIKES = 0x02;
    public final static int COLD = 0x04;
    public final static int UNTRADEABLE = 0x08;
    public final static int KARMA = 0x10;
    public final static int PET_COME = 0x80;
    public final static int UNKNOWN_SKILL = 0x100;
    public final static float ITEM_ARMOR_EXP = 1 / 350000;
    public static final float ITEM_WEAPON_EXP = 1 / 700000;

    public static final int getFlagByInt(int type) {
        if (type == 128) {
            return PET_COME;
        } else if (type == 256) {
            return UNKNOWN_SKILL;
        }
        return 0;
    }

    public static final boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static final boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }

    public static final boolean isRechargable(int itemId) {
        return itemId / 10000 == 233 || itemId / 10000 == 207;
    }

    public static final boolean isArrowForCrossBow(int itemId) {
        return itemId / 1000 == 2061;
    }

    public static final boolean isArrowForBow(int itemId) {
        return itemId / 1000 == 2060;
    }

    public static final boolean isPet(int itemId) {
        return itemId / 1000 == 5000;
    }
}
