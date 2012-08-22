package net.server.handlers.login;

import client.MapleClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public class ViewAllCharSelectedWithPicHandler extends AbstractMaplePacketHandler {

    private static Logger log = LoggerFactory.getLogger(ViewAllCharSelectedWithPicHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        String pic = slea.readMapleAsciiString();
        int charId = slea.readInt();
        int world = slea.readInt();//world
        c.setWorld(world);
        int channel = Randomizer.rand(0, Server.getInstance().getWorld(world).getChannels().size());
        c.setChannel(channel);
        String macs = slea.readMapleAsciiString();
        c.updateMacs(macs);

        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
        }
        if (c.checkPic(pic)) {
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);

            String[] socket = Server.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
            try {
                c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } catch (UnknownHostException e) {
            }

        } else {
            c.announce(MaplePacketCreator.wrongPic());
        }
    }
}
