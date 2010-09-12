/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Kevin
 */
public class LeftKnockbackHandler extends AbstractMaplePacketHandler {
        public void handlePacket(SeekableLittleEndianAccessor slea, final MapleClient c) {
            c.announce(MaplePacketCreator.leftKnockBack());
            c.announce(MaplePacketCreator.enableActions());
        }
}
