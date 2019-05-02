package com.vortexel.dangerzone.common.tile;

import com.google.common.collect.Sets;
import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.util.MCUtil;
import com.vortexel.dangerzone.common.inventory.SlotConfig;
import com.vortexel.dangerzone.common.inventory.SlotInventoryHandler;
import com.vortexel.dangerzone.common.item.ItemLootBag;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import lombok.var;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Set;

public class TileLootBagExtractor extends TileEntity implements ITickable {

    private static final String LOOT_BUFFER_KEY = "LootBuffer";

    public static final ResourceLocation RESOURCE_LOCATION = DangerZone.prefix("machine_loot_bag_extractor");

    public static final SlotConfig[] SLOT_CONFIGS = new SlotConfig[5];
    static {
        int id = 0;
        var builder = SlotConfig.builder();
        SLOT_CONFIGS[id] = builder.index(id++).allowInsert(true).allowExtract(false)
                .insertFilter((s) -> s.getItem() == ModItems.lootBag).build();
        builder = SlotConfig.builder().allowInsert(false).allowExtract(true);
        SLOT_CONFIGS[id] = builder.index(id++).build();
        SLOT_CONFIGS[id] = builder.index(id++).build();
        SLOT_CONFIGS[id] = builder.index(id++).build();
        SLOT_CONFIGS[id] = builder.index(id++).build();
    }

    private final SlotInventoryHandler inventory = new SlotInventoryHandler(SLOT_CONFIGS, (slot) -> markDirty());
    private Set<ItemStack> lootBuffer = Sets.newHashSet();
    private Set<ItemStack> backLootBuffer = Sets.newHashSet();


    public static void init() {
        GameRegistry.registerTileEntity(TileLootBagExtractor.class, RESOURCE_LOCATION);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        if (MCUtil.isWorldLocal(getWorld())) {
            moveLootOut();
            processLootBag();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        val bufferTag = new NBTTagList();
        for (val stack : lootBuffer) {
            val item = new NBTTagCompound();
            stack.writeToNBT(item);
            bufferTag.appendTag(item);
        }
        nbt.setTag(LOOT_BUFFER_KEY, bufferTag);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        val bufferTag = nbt.getTagList(LOOT_BUFFER_KEY, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < bufferTag.tagCount(); i++) {
            lootBuffer.add(new ItemStack(bufferTag.getCompoundTagAt(i)));
        }
    }

    /**
     * Move loot out of our internal buffer and into the target inventory.
     */
    protected void moveLootOut() {
        if (!lootBuffer.isEmpty()) {
            backLootBuffer.clear();
            for (var stack : lootBuffer) {
                for (int i = 1; i <= 4; i++) {
                    stack = inventory.bypassInsert(i, stack, false);
                }
                if (!stack.isEmpty()) {
                    backLootBuffer.add(stack);
                }
            }
            val temp = lootBuffer;
            lootBuffer = backLootBuffer;
            backLootBuffer = temp;
        }
    }

    /**
     * Open a loot bag.
     */
    protected void processLootBag() {
        if (lootBuffer.isEmpty() && !inventory.getStackInSlot(0).isEmpty()) {
            val bag = inventory.bypassExtract(0, 1, false);
            final int level = ItemLootBag.getLootBagLevel(bag);
            if (level > 0) {
                lootBuffer.addAll(DangerZone.proxy.getLootManager().getLootBagLoot((WorldServer)getWorld(), level));
            }
        }
    }
}
