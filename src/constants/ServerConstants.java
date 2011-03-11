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

public class ServerConstants {
    public static short VERSION = 83;
    public static String SERVERNAME = "MoopleDEV";
    // Rate Configuration
    public static byte EXP_RATE = 20;
    public static byte MESO_RATE = 15;
    public static final byte DROP_RATE = 5;
    public static final byte BOSS_DROP_RATE = 2;
    public static final byte QUEST_EXP_RATE = 4;
    public static final byte QUEST_MESO_RATE = 3;
    // Login Configuration
    public static final byte FLAG = 3;
    public static final int CHANNEL_NUMBER = 2;
    public static final int CHANNEL_LOAD = 150;
    public static final String EVENT_MESSAGE = "";
    public static final long RANKING_INTERVAL = 3600000;
    public static final boolean ENABLE_PIN = true;
    public static final boolean ENABLE_PIC = true;
    // Channel Configuration
    public static String SERVER_MESSAGE = "";
    public static String RECOMMEND_MESSAGE = "";
    public static final String EVENTS = "automsg KerningPQ Boats Subway AirPlane elevator";
    // IP Configuration
    public static final String HOST = "localhost";
}