package server.movement;

import java.awt.Point;
import tools.data.output.LittleEndianWriter;

/**
 *
 * @author Someone
 */
public class AranMovement extends AbstractLifeMovement {
    public AranMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public void serialize(LittleEndianWriter slea) {
        slea.write(getType());
        slea.write(getNewstate());
        slea.writeShort(getDuration());
    }
}