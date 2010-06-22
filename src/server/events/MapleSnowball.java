/*
 * This part of the source is copyrighted by Bassoe (c)
 * Using this can and will get you in troubles.
 * Oh Shut Up !!
 */

package server.events;

import java.awt.Point;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

/**
 *
 * @author Bassoe recoded by: kevintjuh93
 */
public class MapleSnowball {
    private Point position;
    private int team;
    private int pos = 0;
    private int snowmanhp = 7500;
    private boolean ishittable = true;

    public MapleSnowball(int teamz) {
        this.team = teamz;
        switch (teamz) {
            case 0:
                this.position = new Point(400,155);
                break;
            case 1:
                this.position = new Point(400,-84);
                break;
            default:
                this.position = new Point(0,0);
                break;
        }
    }

    public int getTeam() {
        return team;
    }

    public boolean isHittable() {
        return ishittable;
    }
    public void setHittable(boolean hit) {
        this.ishittable = hit;
    }

    public void setTeam(int teamz) {
        this.team = teamz;
    }

    public Point getPosition() {
        return position;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPosX() {
        return position.x;
    }
    public void setPosition(Point newpos) {
        this.position = newpos;
    }

    public void setPosX(int newpos) {
        this.position.x = newpos;
    }
    
    public int getSnowmanHP() {
     return snowmanhp;
    }

    public void setSnowmanHP(int snowmanhp) {
        this.snowmanhp = snowmanhp;
    }

    public void broadcast(MapleMap map, int message) {
        MaplePacketCreator.snowballMessage(team, message);
    }
}