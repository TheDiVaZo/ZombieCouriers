package me.thedivazo.zombiecouriers.capability.iventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;

public interface ICourierInventory extends INBTSerializable<CompoundNBT> {

    boolean add(ItemStack itemStack);

    ItemStack pollOne();

    Collection<ItemStack> clearAndGet();

}
