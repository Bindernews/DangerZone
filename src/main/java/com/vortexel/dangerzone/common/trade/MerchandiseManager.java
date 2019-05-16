package com.vortexel.dangerzone.common.trade;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vortexel.dangerzone.api.trading.IMerchandiseRegistry;
import com.vortexel.dangerzone.api.trading.Offer;
import com.vortexel.dangerzone.common.util.JsonUtil;
import lombok.val;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MerchandiseManager implements IMerchandiseRegistry {
    public static Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Offer.class, new OfferSerializer())
            .create();


    private List<Offer> offers;

    public MerchandiseManager() {
        offers = Lists.newArrayList();
    }

    @Override
    public void register(@Nonnull Offer offer) {
        offers.add(offer);
    }

    @Override
    public int getTotalOffers() {
        return offers.size();
    }

    @Override
    public boolean contains(@Nonnull Offer offer) {
        return offers.contains(offer);
    }

    @Override
    public Offer getOffer(int index) {
        return offers.get(index);
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


    public static class OfferSerializer implements JsonSerializer<Offer>, JsonDeserializer<Offer> {
        private static final String KEY_COST = "cost";
        private static final String KEY_COUNT = "count";
        private static final String KEY_META = "meta";
        private static final String KEY_ITEM = "item";
        private static final String KEY_TAG = "tag";

        @Override
        public JsonElement serialize(Offer src, Type typeOfSrc, JsonSerializationContext context) {
            val obj = new JsonObject();
            obj.addProperty(KEY_ITEM, src.stack.getItem().getRegistryName().toString());
            obj.addProperty(KEY_COUNT, src.stack.getCount());
            if (src.stack.getItemDamage() != 0) {
                obj.addProperty(KEY_META, src.stack.getItemDamage());
            }
            if (src.stack.getTagCompound() != null) {
                obj.add(KEY_TAG, JsonUtil.nbtToJson(src.stack.getTagCompound()));
            }
            obj.addProperty(KEY_COST, src.cost);
            return obj;
        }

        @Override
        public Offer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                val obj = json.getAsJsonObject();
                val item = obj.getAsJsonPrimitive(KEY_ITEM).getAsString();
                val count = JsonUtil.getOrDefault(obj, KEY_COUNT, 1);
                val damage = JsonUtil.getOrDefault(obj, KEY_META, 0);
                NBTTagCompound tag = null;
                if (obj.has(KEY_TAG)) {
                    tag = JsonToNBT.getTagFromJson(obj.get(KEY_TAG).toString());
                }
                val cost = obj.getAsJsonPrimitive(KEY_COST).getAsInt();
                val stack = new ItemStack(Item.getByNameOrId(item), count, damage);
                if (tag != null) {
                    stack.setTagCompound(tag);
                }
                return new Offer(cost, stack);
            } catch (NBTException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
