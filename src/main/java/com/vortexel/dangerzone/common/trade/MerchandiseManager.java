package com.vortexel.dangerzone.common.trade;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class MerchandiseManager {
    public static Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Offer.class, new Offer.Serializer())
            .create();


    private List<Offer> offers;

    public MerchandiseManager() {
        offers = Lists.newArrayList();
    }

    public void register(@Nonnull Offer offer) {
        offers.add(offer);
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

    public void addFromReader(Reader r) {
        this.offers.addAll(GSON.fromJson(r, new TypeToken<ArrayList<Offer>>(){}.getType()));
    }
}
