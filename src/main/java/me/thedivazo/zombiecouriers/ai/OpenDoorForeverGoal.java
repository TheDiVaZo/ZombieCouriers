package me.thedivazo.zombiecouriers.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.InteractDoorGoal;

public class OpenDoorForeverGoal extends InteractDoorGoal {
    private int forgetTime;

    public OpenDoorForeverGoal(MobEntity entity) {
        super(entity);
    }

    public boolean canContinueToUse() {
        return this.forgetTime > 0 && super.canContinueToUse();
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
