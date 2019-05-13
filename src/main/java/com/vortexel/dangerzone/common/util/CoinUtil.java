package com.vortexel.dangerzone.common.util;

import com.google.common.collect.Lists;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class CoinUtil {

    public static long takeFrom(@Nonnull ItemStack stack, long amount) {
        if (stack.getItem() == ModItems.coinPouch) {
            val toTake = Math.min(amount, ItemCoinPouch.getAmount(stack));
            ItemCoinPouch.addAmount(stack, -toTake);
            return toTake;
        } else if (stack.getItem() instanceof ItemLootCoin) {
            val typeAmount = ((ItemLootCoin) stack.getItem()).amount;
            // Figure out how many coins we need / can take
            int coins = 0;
            while (coins <= stack.getCount() && (typeAmount * coins) < amount) {
                coins++;
            }
            stack.setCount(stack.getCount() - coins);
            return coins * typeAmount;
        } else {
            return 0;
        }
    }

    public static List<ItemStack> makeChange(long amount) {
        List<ItemStack> change = Lists.newArrayList();
        long amountLeft = amount;
        for (int i = ItemLootCoin.AMOUNTS.length - 1; i >= 0; i--) {
            val coinValue = ItemLootCoin.AMOUNTS[i];
            long coinCount = amountLeft / coinValue;
            // We subtract this now, because after the loop coinCount = 0.
            amountLeft -= coinCount * coinValue;
            while (coinCount > 0) {
                val tempCount = (int)Math.min(coinCount, Consts.STACK_SIZE);
                change.add(new ItemStack(ItemLootCoin.fromAmount(coinValue), tempCount));
                coinCount -= tempCount;
            }
        }
        return change;
    }

    /**
     * Get the value of the items in the stack.
     *
     * @param stack the stack to examine
     * @return the value of the items in the stack, or -1 if they have no value
     */
    public static long getStackValue(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemCoinPouch) {
            return ItemCoinPouch.getAmount(stack);
        } else if (stack.getItem() instanceof ItemLootCoin) {
            return ((ItemLootCoin) stack.getItem()).amount * stack.getCount();
        } else {
            return -1;
        }
    }

    public static long getStackValue(@Nonnull Item item, long count) {
        if (item instanceof ItemLootCoin) {
            return ((ItemLootCoin) item).amount * count;
        } else {
            return 0;
        }
    }

    private CoinUtil() {}
}
