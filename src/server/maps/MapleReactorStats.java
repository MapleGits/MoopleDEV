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
package server.maps;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.Pair;

/**
 * @author Lerk
 */
public class MapleReactorStats {
    private Point tl;
    private Point br;
    private Map<Byte, Pair<StateData, StateData>> stateInfo = new HashMap<Byte, Pair<StateData, StateData>>();

    public void setTL(Point tl) {
        this.tl = tl;
    }

    public void setBR(Point br) {
        this.br = br;
    }

    public Point getTL() {
        return tl;
    }

    public Point getBR() {
        return br;
    }

    public void addState(byte state, StateData data1, StateData data2) {
        stateInfo.put(state, new Pair<StateData, StateData>(data1, data2));
    }

    public byte getNextState(byte state, boolean left) {
        System.out.println("Size: " + stateInfo.size());
        System.out.println("State: " + state);
        System.out.println("Left StateInfo: " + stateInfo.get(state).getLeft().toString());
        if (stateInfo.get(state) == null) return -1;
        StateData nextState = left ? stateInfo.get(state).getLeft() : stateInfo.get(state).getRight();
        if (nextState != null) {
            return nextState.getNextState();
        } else {
            return -1;
        }
    }

    public List<Integer> getActiveSkills(byte state) {
        StateData nextState = stateInfo.get(state).getLeft();
        if (nextState != null) {
            return nextState.getActiveSkills();
        } else {
            return null;
        }
    }

    public int getType(byte state) {
        StateData nextState = stateInfo.get(state).getLeft();
        if (nextState != null) {
            return nextState.getType();
        } else {
            return -1;
        }
    }

    public Pair<Integer, Integer> getReactItem(byte state) {
        StateData nextState = stateInfo.get(state).getLeft();
        if (nextState != null) {
            return nextState.getReactItem();
        } else {
            return null;
        }
    }


    public static class StateData {
        private int type;
        private Pair<Integer, Integer> reactItem;
        private List<Integer> activeSkills;
        private byte nextState;

        public StateData(int type, Pair<Integer, Integer> reactItem, List<Integer> activeSkills, byte nextState) {
            this.type = type;
            this.reactItem = reactItem;
            this.activeSkills = activeSkills;
            this.nextState = nextState;
        }

        private int getType() {
            return type;
        }

        private byte getNextState() {
            return nextState;
        }

        private Pair<Integer, Integer> getReactItem() {
            return reactItem;
        }

        private List<Integer> getActiveSkills() {
            return activeSkills;
        }
    }
}
