package me.thedivazo.zombiecouriers.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class SimpleCapabilityProvider<C, S extends INBT> implements ICapabilityProvider, INBTSerializable<S> {
    private final Capability<C> capability;
    private final LazyOptional<C> implementation;
    private final Direction direction;

    public SimpleCapabilityProvider(Capability<C> capability, LazyOptional<C> implementation, Direction direction) {
        this.capability = capability;
        this.implementation = implementation;
        this.direction = direction;
    }

    public static <C> SimpleCapabilityProvider<C, INBT> from(Capability<C> cap, NonNullSupplier<C> impl) {
        Objects.requireNonNull(cap, "cap cannot be null");
        return from(cap, Direction.NORTH, impl);
    }
    public static <C> SimpleCapabilityProvider<C, INBT> from(Capability<C> cap, @Nullable Direction dir, NonNullSupplier<C> impl) {
        Objects.requireNonNull(cap, "cap cannot be null");
        return new SimpleCapabilityProvider<>(cap, LazyOptional.of(impl), dir);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return capability.orEmpty(cap, implementation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public S serializeNBT() {
        return (S) capability.writeNBT(this.getInstance(), this.direction);
    }

    @Override
    public void deserializeNBT(@Nonnull final S nbt) {
        capability.readNBT(this.getInstance(), this.direction, nbt);
    }

    private C getInstance() {
        return this.implementation.orElseThrow(() -> new IllegalStateException("Unable to obtain capability instance"));
    }
}
