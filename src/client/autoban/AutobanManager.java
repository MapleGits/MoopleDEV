/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.autoban;

import client.MapleCharacter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kevintjuh93
 */
public class AutobanManager {
    MapleCharacter chr;
    Map<AutobanFactory, Integer> points = new HashMap<AutobanFactory, Integer>();
    Map<AutobanFactory, Long> lastTime = new HashMap<AutobanFactory, Long>();
    private int misses = 0;
    private int lastmisses = 0;
    private int samemisscount = 0;


    public AutobanManager(MapleCharacter chr) {
        this.chr = chr;
    }

    public void addPoint(AutobanFactory fac) {
        int tpoints = points.get(fac);
        if (lastTime.containsKey(fac)) {
            if (lastTime.get(fac) < (System.currentTimeMillis() - fac.getExpire())) {
                points.remove(fac);
                points.put(fac, tpoints / 2); //So the points are not completely gone.
            }
        }
        if (fac.getExpire() != -1)
            lastTime.put(fac, System.currentTimeMillis());
        
        if (points.containsKey(fac)) {
            points.remove(fac);
            points.put(fac, tpoints + 1);
        } else
            points.put(fac, 1);

        if (points.get(fac) >= fac.getMaximum())
            chr.autoban("Autobanned for " + fac.name() + ". Info: 100% correct, so fuck this player", 1);
    }

    public void addMiss() {
        this.misses++;
    }

    public void resetMisses() {
        if (lastmisses == misses && misses < 7) {
            samemisscount++;
        }
        if (samemisscount > 4)
            chr.autoban("Autobanned for : " + misses + " Miss hack", 1);

        this.lastmisses = misses;
        this.misses = 0;
    }
}
