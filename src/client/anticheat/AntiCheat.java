package client.anticheat;

import client.MapleCharacter;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;
import net.channel.ChannelServer;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 *
 * @author Fate
 */
public class AntiCheat {

    //General
    private WeakReference<MapleCharacter> chr;
    private ScheduledFuture<?> validationTask;
    private int[] reason = new int[5];
    //Speed Hack
    private int numSequentialAttacks;
    private long lastAttackTime;
    private long attackingSince;
    //Fast Hp Regen
    private int numHPRegens;
    private long regenHPSince;
    //Fast MP Regen
    private int numMPRegens;
    private long regenMPSince;
    //Damage Hack
    private long lastDamage = 0;
    //Vac Hack
    private Point lastMonsterMove;
    private int monsterMoveCount;
    //Spamm
    private long[] spamtype = new long[5];

    public AntiCheat(MapleCharacter chr) {
        this.chr = new WeakReference<MapleCharacter>(chr);
        validationTask = TimerManager.getInstance().register(new ValidationTask(), 60000);
        attackingSince = regenMPSince = regenHPSince = System.currentTimeMillis();
        for (int i = 0; i < reason.length; i++) {
            reason[i] = 0;
        }
        for (int i = 0; i < spamtype.length; i++) {
            spamtype[i] = 0;
        }
    }

    /**
     * Type 0 - @gm
     * Type 1 - Meso drop
     * Type 2 - Npc Talk
     * Type 3 - #buffs
     * Type 4 - #go
     *
     * @param limit
     * @param type
     * @return
     */
    public synchronized boolean Spam(int limit, int type) {
        if (type < 0 || spamtype.length < type) {
            type = 1; // default xD
        }
        if (System.currentTimeMillis() < limit + spamtype[type]) {
            return true;
        }
        spamtype[type] = System.currentTimeMillis();
        return false;
    }

    private int getTotalPoints() {
        int ret = 0;
        for (int i = 0; i < reason.length; i++) {
            ret += reason[i];
        }
        return ret;
    }

    public void increaseReason(int reasonid) {
        if (getTotalPoints() == 0) {
            chr.get().setHasCheat(true);
        }
        reason[reasonid]++;
    }

    public void checkAttack(int skillId) {
        numSequentialAttacks++;
        long oldLastAttackTime = lastAttackTime;
        lastAttackTime = System.currentTimeMillis();
        long attackTime = lastAttackTime - attackingSince;
        if (numSequentialAttacks > 3) {
            final int divisor;
            if (skillId != 3121004) { // hurricane
                divisor = 150;
            } else {
                divisor = 400;
            }
            if (attackTime / divisor < numSequentialAttacks) {
                increaseReason(0);
            }
        }
        if (lastAttackTime - oldLastAttackTime > 1500) {
            attackingSince = lastAttackTime;
            numSequentialAttacks = 0;
        }
    }

    private void resetHPRegen() {
        regenHPSince = System.currentTimeMillis();
        numHPRegens = 0;
    }

    public void checkHPRegen() {
        if (chr.get().getHp() == chr.get().getMaxHp()) {
            resetHPRegen();
            return;
        }
        numHPRegens++;
        if ((System.currentTimeMillis() - regenHPSince) / 10000 < numHPRegens) {
            increaseReason(1);
        }
    }

    private void resetMPRegen() {
        regenMPSince = System.currentTimeMillis();
        numMPRegens = 0;
    }

    public void checkMPRegen() {
        if (chr.get().getMp() == chr.get().getMaxMp()) {
            resetMPRegen();
            return;
        }
        numMPRegens++;
        long allowedRegens = (System.currentTimeMillis() - regenMPSince) / 10000;
        if (allowedRegens < numMPRegens) {
            increaseReason(2);
        }
    }

    public void checkSameDamage(long dmg) {
        if (lastDamage == dmg) {
            increaseReason(3);
        } else {
            lastDamage = dmg;
            reason[4] = 0;
        }
    }

    public void checkMoveMonster(Point pos) {
        if (pos.equals(lastMonsterMove)) {
            monsterMoveCount++;
            if (monsterMoveCount > 3) {
                increaseReason(4);
            }
        } else {
            lastMonsterMove = pos;
            monsterMoveCount = 1;
        }
    }

    public String getCheats() {
        String reasons = null;
        if (reason[0] != 0) {
            if (reasons != null) {
                reasons += ", Fast Attack x " + reason[0];
            } else {
                reasons = "Fast Attack x " + reason[0];
            }
        }
        if (reason[1] != 0) {
            if (reasons != null) {
                reasons += ", Fast HP Regen x " + reason[1];
            } else {
                reasons = "Fast HP Regen x " + reason[1];
            }
        }
        if (reason[2] != 0) {
            if (reasons != null) {
                reasons += ", Fast MP Regen x " + reason[2];
            } else {
                reasons = "Fast MP Regen x " + reason[2];
            }
        }
        if (reason[3] != 0) {
            if (reasons != null) {
                reasons += ", Same Damage x " + reason[3];
            } else {
                reasons = "Same Damage x " + reason[3];
            }
        }
        if (reason[4] != 0) {
            if (reasons != null) {
                reasons += ", Vac Hack x " + reason[4];
            } else {
                reasons = "Vac Hack x " + reason[4];
            }
        }
        return reasons;
    }

    public void dispose() {
        chr = null;
        for (int i = 0; i < reason.length; i++) {
            reason[i] = 0;
        }
        if (validationTask != null) {
            validationTask.cancel(false);
        }
    }

    private class ValidationTask implements Runnable {

        @Override
        public void run() {
            if (chr.get() != null) {
                if (chr.get().getClient() != null) {
                    if (getTotalPoints() > 750) {
                        chr.get().ban("Autoban system banned " + chr.get().getName() + ": " + getCheats(), true);
                        for (ChannelServer cs : ChannelServer.getAllInstances()) {
                            cs.broadcastPacket(MaplePacketCreator.serverNotice(6, chr.get().getName() + " has been permanently banned."));
                            cs.broadcastPacket(MaplePacketCreator.serverNotice(6, "Reason: " + getCheats()));

                        }
                        dispose();
                    } else {
                        for (int i = 0; i < reason.length; i++) {
                            reason[i] = 0;
                        }
                        chr.get().setHasCheat(false);
                    }
                }
            } else {
                dispose();
            }
        }
    }
}
