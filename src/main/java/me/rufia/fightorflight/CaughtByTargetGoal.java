package me.rufia.fightorflight;

import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CaughtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private LivingEntity lastCaughtByMob;
    private int lastCaughtByMobTimestamp;

    public CaughtByTargetGoal(Mob mob) {
        super(mob, true);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        List<Object> busyLocks = pokemonEntity.getBusyLocks();
        for (int i = 0; i < busyLocks.size(); i++){
            if (busyLocks.get(i) instanceof EmptyPokeBallEntity){
                LogUtils.getLogger().info("Pokemon in process of being caught");
                EmptyPokeBallEntity pokeBallEntity = (EmptyPokeBallEntity)busyLocks.get(i);

                if (pokeBallEntity.getOwner() instanceof LivingEntity){
                    lastCaughtByMob = (LivingEntity)pokeBallEntity.getOwner();
                    lastCaughtByMobTimestamp = this.mob.tickCount;
                }
            }
        }

        if (lastCaughtByMob != null) {
            if (lastCaughtByMob.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                return this.canAttack(lastCaughtByMob, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(lastCaughtByMob);
        this.targetMob = this.mob.getTarget();
        this.mob.setLastHurtByMob(this.mob.getTarget());
        if (this.mob.getTarget() instanceof Player){
            this.mob.setLastHurtByPlayer((Player)this.mob.getTarget());
        }
//        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
//        if (this.alertSameType) {
//            this.alertOthers();
//        }

        super.start();
    }
}
