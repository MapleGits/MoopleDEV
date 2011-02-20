package net.login.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.login.LoginServer;
import client.MapleClient;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;

public final class ViewAllPicRegisterHandler extends AbstractMaplePacketHandler { //Gey class name lol
    private static Logger log = LoggerFactory.getLogger(ViewAllPicRegisterHandler.class);
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        int charId = slea.readInt();
        slea.readInt(); //world
        int channel = Randomizer.rand(0, ServerConstants.CHANNEL_NUMBER - 1);
	String mac = slea.readMapleAsciiString();
        c.updateMacs(mac);
        if (c.hasBannedMac()) {
            c.getSession().close(true);
            return;
	}
        slea.readMapleAsciiString();
        String pic = slea.readMapleAsciiString();
            c.setPic(pic);
            try {
                if (c.getIdleTask() != null) {
                    c.getIdleTask().cancel(true);
                }
                c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                String channelServerIP = MapleClient.getChannelServerIPFromSubnet(c.getSession().getRemoteAddress().toString().replace("/", "").split(":")[0], channel);

                if (channelServerIP.equals("0.0.0.0")) {
                    String[] socket = LoginServer.getInstance().getIP(channel).split(":");
                    c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
                } else {
                    String[] socket = LoginServer.getInstance().getIP(channel).split(":");
                    c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(channelServerIP), Integer.parseInt(socket[1]), charId));
                }
            } catch (UnknownHostException e) {
                log.error("Host not found", e);
            }
    }
}
