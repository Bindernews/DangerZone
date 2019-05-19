package com.vortexel.dangerzone.common.trade;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vortexel.dangerzone.DangerZone;
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
import java.util.Optional;

public class MerchandiseManager {
    public static Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Offer.class, new OfferSerializer())
            .registerTypeAdapter(new TypeToken<Optional<Offer>>(){}.getType(), new SafeOfferSerializer())
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

    public boolean contains(@Nonnull Offer offer) {
        return offers.contains(offer);
    }

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

    /**
     * Read a JSON file from {@code r} and add its contents to the list of merchandise.
     * @param r the reader which will contain a JSON file
     * @throws JsonParseException if the JSON can't be parsed
     */
    public void addFromReader(Reader r) {
        try {
            for (val offer : parseOffers(r)) {
                if (offer.isPresent()) {
                    this.offers.add(offer.get());
                }
            }
        } catch (NullPointerException e) {
            // If the JSON is invalid then it throws a NPE
            throw new JsonParseException("Invalid JSON when reading merchandise file");
        }
    }

    protected static List<Optional<Offer>> parseOffers(Reader r) {
        val typeToken = new TypeToken<ArrayList<Optional<Offer>>>(){}.getType();
        return GSON.fromJson(r, typeToken);
    }

    protected static class SafeOfferSerializer implements JsonDeserializer<Optional<Offer>> {
        @Override
        public Optional<Offer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return Optional.of(context.deserialize(json, Offer.class));
            } catch (JsonParseException e) {
                DangerZone.getLog().error(e);
                return Optional.empty();
            }
        }
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
                val itemObj = Item.getByNameOrId(item);
                if (itemObj == null) {
                    throw new JsonParseException(
                            String.format("Item \"%1$s\" does not exist and can't be sold as merchandise!", item));
                }
                val stack = new ItemStack(itemObj, count, damage);
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
