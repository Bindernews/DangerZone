package com.vortexel.dangerzone.common;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MerchandiseManager {

    public static MerchandiseManager instance;

    private List<Offer> offers;

    public MerchandiseManager() {
        offers = Lists.newArrayList();
    }


    public int getTotalOffers() {
        return offers.size();
    }

    public long getCost(int index) {
        if (index < offers.size()) {
            return offers.get(index).cost;
        } else {
            return 0L;
        }
    }

    public ItemStack getItemStack(int index) {
        if (index < offers.size()) {
            return offers.get(index).stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @AllArgsConstructor
    protected static class Offer {
        public final int cost;
        public final ItemStack stack;
    }
}
