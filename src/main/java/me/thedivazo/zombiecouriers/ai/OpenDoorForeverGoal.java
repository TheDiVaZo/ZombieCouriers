package me.thedivazo.zombiecouriers.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.InteractDoorGoal;

public class OpenDoorForeverGoal extends InteractDoorGoal {
    private final boolean closeDoor;
    private int forgetTime;

    public OpenDoorForeverGoal(MobEntity p_i1644_1_, boolean p_i1644_2_) {
        super(p_i1644_1_);
        this.mob = p_i1644_1_;
        this.closeDoor = p_i1644_2_;
    }

    public boolean canContinueToUse() {
        return this.closeDoor && this.forgetTime > 0 && super.canContinueToUse();
    }

    public void start() {
        this.forgetTime = 20;
        this.setOpen(true);
    }

    public void tick() {
        --this.forgetTime;
        super.tick();
    }
}
