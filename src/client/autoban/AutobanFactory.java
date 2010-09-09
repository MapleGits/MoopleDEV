/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.autoban;

import client.MapleCharacter;

/**
 *
 * @author kevintjuh93
 */
public enum AutobanFactory {
    MOB_COUNT,
    FIX_DAMAGE,
    
    ;
    private int points;
    private long expiretime = -1;

    private AutobanFactory() {
        this(1, -1);
    }
    
    private AutobanFactory(int points, long expire) {
        this.points = points;
        this.expiretime = expire;
    }

    public int getMaximum() {
        return points;
    }

    public void addPoints(AutobanManager ban, int amount) {
        ban.addPoints(this);
    }

    public void autoban(MapleCharacter chr, int value) {
        chr.autoban("Autobanned for (" + this.name() + ": " + value + ")", 1);
    }
}
