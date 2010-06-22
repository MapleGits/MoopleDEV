/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server.maps;

import client.Equip;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import java.sql.SQLException;
import tools.DatabaseConnection;
import net.MaplePacket;
import server.MapleInventoryManipulator;
import server.MaplePlayerShopItem;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 *
 * @author XoticStory
 */
public class HiredMerchant extends AbstractMapleMapObject {
    private int ownerId;
    private int itemId;
    private String ownerName = "";
    private String description = "";
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<MaplePlayerShopItem> items = new LinkedList<MaplePlayerShopItem>();
    private boolean open;
    public ScheduledFuture<?> schedule = null;
    private MapleMap map;

    public HiredMerchant(final MapleCharacter owner, int itemId, String desc) {
        this.setPosition(owner.getPosition());
        this.ownerId = owner.getId();
        this.itemId = itemId;
        this.ownerName = owner.getName();
        this.description = desc;
        this.map = owner.getMap();
        this.schedule = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                HiredMerchant.this.closeShop(owner.getClient());
            }
        }, 1000 * 60 * 60 * 24);
    }

    public void broadcastToVisitors(MaplePacket packet) {
        for (MapleCharacter visitor : visitors) {
            if (visitor != null) {
                visitor.getClient().getSession().write(packet);
            }
        }
    }

    public void addVisitor(MapleCharacter visitor) {
        int i = this.getFreeSlot();
        if (i > -1) {
            visitors[i] = visitor;
            broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorAdd(visitor, i + 1));
        }
    }

    public void removeVisitor(MapleCharacter visitor) {
        int slot = getVisitorSlot(visitor);
        if (visitors[slot] == visitor) {
            visitors[slot] = null;
            if (slot != 0) {
                broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorLeave(slot + 1, false));
            }
        }
    }

    public int getVisitorSlot(MapleCharacter visitor) {
        for (int i = 1; i < 3; i++) {
            if (visitors[i] == visitor) {
                return i;
            }
        }
        return 0;
    }

    public void removeAllVisitors(String message) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                visitors[i].getClient().getSession().write(MaplePacketCreator.hiredMerchantForceLeave1());
                visitors[i].getClient().getSession().write(MaplePacketCreator.hiredMerchantForceLeave2());
                if (message.length() > 0) {
                    visitors[i].dropMessage(1, message);
                }
                visitors[i] = null;
            }
        }
    }

    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        synchronized (items) {
            IItem newItem = pItem.getItem().copy();
            newItem.setQuantity((short) (newItem.getQuantity() * quantity));
            if (quantity < 1 || pItem.getBundles() < 1 || newItem.getQuantity() > pItem.getBundles() || !pItem.isExist()) {
                return;
            } else if (newItem.getType() == 1 && newItem.getQuantity() > 1) {
                return;
            } else if (!pItem.isExist()) {
                return;
            }
            if (c.getPlayer().getMeso() >= pItem.getPrice() * quantity) {
                if (MapleInventoryManipulator.addFromDrop(c, newItem, true)) {
                    c.getPlayer().gainMeso(-pItem.getPrice() * quantity, false);
                    try {
                        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = MerchantMesos + " + pItem.getPrice() * quantity + " WHERE id = ?");
                        ps.setInt(1, ownerId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception e) {
                    }
                    pItem.setBundles((short) (pItem.getBundles() - quantity));
                    if (pItem.getBundles() < 1) {
                        pItem.setDoesExist(false);
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your inventory is full. Please clean a slot before buying this item.");
                }
            } else {
                c.getPlayer().dropMessage(1, "You do not have enough mesos.");
            }
        }
    }

    public void closeShop(MapleClient c) {
        map.removeMapObject(this);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(ownerId));
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?");
            ps.setInt(1, ownerId);
            ps.executeUpdate();
            ps.close();
            for (MaplePlayerShopItem mpsi : getItems()) {
                if (mpsi.getBundles() > 2) {
                    MapleInventoryManipulator.addById(c, mpsi.getItem().getItemId(), mpsi.getBundles(), null, -1);
                } else if (mpsi.isExist()) {
                    MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), true);
                }
            }
        } catch (Exception e) {
        }
        schedule.cancel(false);
    }

    public String getOwner() {
        return ownerName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getDescription() {
        return description;
    }

    public MapleCharacter[] getVisitors() {
        return visitors;
    }

    public List<MaplePlayerShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(MaplePlayerShopItem item) {
        items.add(item);
    }

    public void removeFromSlot(int slot) {
        items.remove(slot);
    }

    public int getFreeSlot() {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean set) {
        this.open = set;
    }

    public int getItemId() {
        return itemId;
    }

    public boolean isOwner(MapleCharacter chr) {
        return chr.getId() == ownerId;
    }

    public void saveItems() throws SQLException {
        PreparedStatement ps;
        for (MaplePlayerShopItem pItems : items) {
            if (pItems.getBundles() > 0) {
                if (pItems.getItem().getType() == 1) {
                    ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO hiredmerchant (ownerid, itemid, quantity, upgradeslots, level, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, owner, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)");
                    Equip eq = (Equip) pItems.getItem();
                    ps.setInt(2, eq.getItemId());
                    ps.setInt(3, 1);
                    ps.setInt(4, eq.getUpgradeSlots());
                    ps.setInt(5, eq.getLevel());
                    ps.setInt(6, eq.getStr());
                    ps.setInt(7, eq.getDex());
                    ps.setInt(8, eq.getInt());
                    ps.setInt(9, eq.getLuk());
                    ps.setInt(10, eq.getHp());
                    ps.setInt(11, eq.getMp());
                    ps.setInt(12, eq.getWatk());
                    ps.setInt(13, eq.getMatk());
                    ps.setInt(14, eq.getWdef());
                    ps.setInt(15, eq.getMdef());
                    ps.setInt(16, eq.getAcc());
                    ps.setInt(17, eq.getAvoid());
                    ps.setInt(18, eq.getHands());
                    ps.setInt(19, eq.getSpeed());
                    ps.setInt(20, eq.getJump());
                    ps.setString(21, eq.getOwner());
                } else {
                    ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO hiredmerchant (ownerid, itemid, quantity, owner, type) VALUES (?, ?, ?, ?, ?)");
                    ps.setInt(2, pItems.getItem().getItemId());
                    ps.setInt(3, pItems.getBundles());
                    ps.setString(4, pItems.getItem().getOwner());
                    ps.setInt(5, pItems.getItem().getType());
                }
                ps.setInt(1, getOwnerId());
                ps.executeUpdate();
                ps.close();
            }
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        return;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnHiredMerchant(this));
    }
}
