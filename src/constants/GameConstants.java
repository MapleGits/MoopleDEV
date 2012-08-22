/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;

import constants.skills.Aran;

/**
 *
 * @author Kevin
 */
public class GameConstants {

    public static int getHiddenSkill(final int skill) {
        switch (skill) {
            case Aran.HIDDEN_FULL_DOUBLE:
            case Aran.HIDDEN_FULL_TRIPLE:
                return Aran.FULL_SWING;
            case Aran.HIDDEN_OVER_DOUBLE:
            case Aran.HIDDEN_OVER_TRIPLE:
                return Aran.OVER_SWING;
        }
        return skill;
    }
}
