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
    GACHA_EXP,
    TUBI(20, 15000),
    SHORT_ITEM_VAC,
    ITEM_VAC,
    FAST_ATTACK(10, 30000),
    MPCON(25, 30000);
    
    private int points;
    private long expiretime;

    private AutobanFactory() {
        this(1, -1);
    }

    private AutobanFactory(int points) {
        this.points = points;
        this.expiretime = -1;
    }

    private AutobanFactory(int points, long expire) {
        this.points = points;
        this.expiretime = expire;
    }

    public int getMaximum() {
        return points;
    }

    public long getExpire() {
        return expiretime;
    }

    public void addPoint(AutobanManager ban, String reason) {
        ban.addPoint(this, reason);
    }

    public void autoban(MapleCharacter chr, String value) {
        chr.autoban("Autobanned for (" + this.name() + ": " + value + ")", 1);
        chr.sendPolice("You have been blocked by #bMooplePolice for the HACK reason#k.");
    }
}
