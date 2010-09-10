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

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.ItemFactory;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import java.sql.SQLException;
import java.util.ArrayList;
import tools.DatabaseConnection;
import net.MaplePacket;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePlayerShopItem;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author XoticStory
 */
public class HiredMerchant extends AbstractMapleMapObject {
    private int ownerId;
    private int itemId;
    private int channel;
    private long start;
    private String ownerName = "";
    private String description = "";
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<MaplePlayerShopItem> items = new LinkedList<MaplePlayerShopItem>();
    private boolean open;
    public ScheduledFuture<?> schedule = null;
    private MapleMap map;

    public HiredMerchant(final MapleCharacter owner, int itemId, String desc) {
        this.setPosition(owner.getPosition());
        this.start = System.currentTimeMillis();
        this.ownerId = owner.getId();
        this.channel = owner.getClient().getChannel();
        this.itemId = itemId;
        this.ownerName = owner.getName();
        this.description = desc;
        this.map = owner.getMap();
        this.schedule = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                HiredMerchant.this.closeShop(owner.getClient(), true);
            }
        }, 1000 * 60 * 60 * 24);
        owner.setHiredMerchant(this);
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
        for (int i = 1; i <= 3; i++) {
            if (visitors[i] == visitor) {
                return i;
            }
        }
        return 0;
    }

    public void removeAllVisitors(String message) {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null) {
                visitors[i].getClient().getSession().write(MaplePacketCreator.leaveHiredMerchant(i + 1, 0x11));
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
        try {
            this.saveItems();
        } catch (Exception e) {
        }
        }
    }

    public void closeShop(MapleClient c, boolean timeout) {
        map.removeMapObject(this);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(ownerId));

        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?");
            ps.setInt(1, ownerId);
            ps.executeUpdate();
            ps.close();
            if (check(c.getPlayer(), getItems()) && !timeout) {
                for (MaplePlayerShopItem mpsi : getItems()) {
                    if (mpsi.getBundles() > 1) {
                        MapleInventoryManipulator.addById(c, mpsi.getItem().getItemId(), (short) (mpsi.getBundles() * mpsi.getItem().getQuantity()), null, null, mpsi.getItem().getExpiration());
                    } else if (mpsi.isExist()) {
                        MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), true);
                    }
                }
                items.clear();
            }
                try {
                    this.saveItems();
                } catch (Exception e) {
                }
        items.clear();

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
        try {
            this.saveItems();
        } catch (SQLException ex) {
        }
    }

    public void removeFromSlot(int slot) {
        items.remove(slot);
        try {
            this.saveItems();
        } catch (SQLException ex) {
        }
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
        List<Pair<IItem, MapleInventoryType>> itemsWithType = new ArrayList<Pair<IItem, MapleInventoryType>>();

        for (MaplePlayerShopItem pItems : items) {
            IItem newItem = pItems.getItem();
            newItem.setQuantity((short) (pItems.getBundles() * pItems.getItem().getQuantity()));
            if (pItems.getBundles() > 0)
                itemsWithType.add(new Pair<IItem, MapleInventoryType>(newItem, MapleInventoryType.getByType(newItem.getType())));
        }
        ItemFactory.MERCHANT.saveItems(itemsWithType, this.ownerId);
    }
    private static final boolean check(MapleCharacter chr, List<MaplePlayerShopItem> items) {
	byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
	for (MaplePlayerShopItem item : items) {
	    final MapleInventoryType invtype = MapleItemInformationProvider.getInstance().getInventoryType(item.getItem().getItemId());
	    if (invtype == MapleInventoryType.EQUIP) {
		eq++;
	    } else if (invtype == MapleInventoryType.USE) {
		use++;
	    } else if (invtype == MapleInventoryType.SETUP) {
		setup++;
	    } else if (invtype == MapleInventoryType.ETC) {
		etc++;
	    } else if (invtype == MapleInventoryType.CASH) {
		cash++;
	    }
	}
	if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() <= eq
		|| chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() <= use
		|| chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() <= setup
		|| chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() <= etc
		|| chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() <= cash) {
	    return false;
	}
	return true;
    }

    public int getChannel() {
        return channel;
    }

    public int getTimeLeft() {
	return (int) ((System.currentTimeMillis() - start) / 1000);
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
