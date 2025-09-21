package me.thedivazo.zombiecouriers.capability.iventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

public class CourierInventory implements ICourierInventory {
    private final static String ITEMS_NBT_KEY = "Items";

    protected Deque<ItemStack> stacks = new ArrayDeque<>();
    private int count = 0;

    @Override
    public boolean add(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ItemStack existing = this.stacks.peekLast();
        if (existing != null && !existing.isEmpty() && ItemHandlerHelper.canItemStacksStack(stack, existing)) {
            existing.grow(stack.getCount());

        }
        else {
            stacks.addLast(stack.copy());
        }
        count+= stack.getCount();

        return true;
    }

    @Override
    public ItemStack pollOne() {
        ItemStack first = stacks.peekFirst();
        if (first == null || first.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack removedStack;

        if (first.getCount() <= 1) {
            removedStack = stacks.removeFirst();
        }
        else {
            first.shrink(1);
            removedStack = ItemHandlerHelper.copyStackWithSize(first, 1);
        }
        count--;
        return removedStack;
    }

    @Override
    public Item peekItemOne() {
        ItemStack first = stacks.peekFirst();
        return first != null ? first.getItem() : Items.AIR;
    }

    @Override
    public Collection<ItemStack> clearAndGet() {
        Collection<ItemStack> items = this.stacks;
        this.stacks = new LinkedList<>();
        this.count = 0;
        return items;
    }

    @Override
    public int getCount() {
        return stacks.size();
    }

    @Override
    public CompoundNBT serializeNBT() {
        ListNBT nbtTagList = new ListNBT();

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;

            CompoundNBT itemTag = new CompoundNBT();
            stack.save(itemTag);
            nbtTagList.add(itemTag);
        }

        CompoundNBT nbt = new CompoundNBT();
        nbt.put(ITEMS_NBT_KEY, nbtTagList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        stacks = new LinkedList<>();
        count = 0;

        ListNBT nbtTagList = nbt.getList(ITEMS_NBT_KEY, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < nbtTagList.size(); i++) {

            CompoundNBT itemTags = nbtTagList.getCompound(i);
            ItemStack itemStack = ItemStack.of(itemTags);

            if (!itemStack.isEmpty()) {
                stacks.addLast(itemStack);
                count += itemStack.getCount();
            }
        }
    }
}
