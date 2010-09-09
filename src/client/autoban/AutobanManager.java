/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.autoban;

import client.autoban.AutobanFactory;
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

    public AutobanManager(MapleCharacter chr) {
        this.chr = chr;
    }

    public void addPoints(AutobanFactory fac) {
        if (points.containsKey(fac))
            points.put(fac, points.get(fac) + 1);
        else
            points.put(fac, 1);

        if (points.get(fac) >= fac.getMaximum())
            chr.autoban("Autobanned for " + fac.name() + ". Info: 100% correct, so fuck this player", 1);
    }
}
