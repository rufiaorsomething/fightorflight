package me.rufia.fightorflight.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;
import com.mojang.logging.LogUtils;

public class PokemonAvoidGoal extends Goal {
    protected final PathfinderMob mob;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;
    @Nullable
    protected LivingEntity toAvoid;
    protected final float maxDist;
    @Nullable
    protected Path path;
    protected final PathNavigation pathNav;
    //protected final Class<LivingEntity> avoidClass;
    //protected final Predicate<LivingEntity> avoidPredicate;
    //protected final Predicate<LivingEntity> predicateOnAvoidEntity;
    private final TargetingConditions avoidEntityTargeting;

//    public PokemonAvoidGoal(PathfinderMob p_25040_, Class<T> p_25041_, Predicate<LivingEntity> p_25042_, float p_25043_, double p_25044_, double p_25045_, Predicate<LivingEntity> p_25046_) {
//        this.mob = p_25040_;
//        this.avoidClass = p_25041_;
//        this.avoidPredicate = p_25042_;
//        this.maxDist = p_25043_;
//        this.walkSpeedModifier = p_25044_;
//        this.sprintSpeedModifier = p_25045_;
//        this.predicateOnAvoidEntity = p_25046_;
//        this.pathNav = p_25040_.getNavigation();
//        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
//        this.avoidEntityTargeting = TargetingConditions.forCombat().range((double)p_25043_).selector(p_25046_.and(p_25042_));
//    }
    public PokemonAvoidGoal(PathfinderMob mob, float maxDist, float walkSpeedModifier, float sprintSpeedModifier){
        this.mob = mob;
        this.maxDist = maxDist;
        this.walkSpeedModifier = walkSpeedModifier;
        this.sprintSpeedModifier = sprintSpeedModifier;
        this.pathNav = this.mob.getNavigation();
        this.avoidEntityTargeting = TargetingConditions.forCombat().range((double)maxDist);
    }


    public boolean canUse() {
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        if (pokemonEntity.getPokemon().isPlayerOwned()) { return false; }
        if (pokemonEntity.isBusy()) { return false; }

        if (this.mob.getTarget() != null) {
            if (CobblemonFightOrFlight.getFightOrFlightCoefficient(pokemonEntity) > 0) { return false; }

            if (this.mob.getTarget().distanceToSqr(this.mob) < maxDist) {
                toAvoid = this.mob.getTarget();
            }

            //this.toAvoid = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(toAvoid.getClass(), this.mob.getBoundingBox().inflate((double)this.maxDist, 3.0D, (double)this.maxDist))
            //        , this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
        }
        if (this.toAvoid == null) {
            return false;
        } else {
            Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
            if (vec3 == null) {
                return false;
            } else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob)) {
                return false;
            } else {
                this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                return this.path != null;
            }
        }


//        this.toAvoid = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate((double)this.maxDist, 3.0D, (double)this.maxDist), (p_148078_) -> {
//            return true;
//        }), this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
//        if (this.toAvoid == null) {
//            return false;
//        } else {
//            Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
//            if (vec3 == null) {
//                return false;
//            } else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob)) {
//                return false;
//            } else {
//                this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
//                return this.path != null;
//            }
//        }
    }

    public boolean canContinueToUse() {
        return !this.pathNav.isDone();
    }

    public void start() {
        this.pathNav.moveTo(this.path, this.walkSpeedModifier);
    }

    public void stop() {
        this.toAvoid = null;
    }

    public void tick() {
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        LogUtils.getLogger().info(pokemonEntity.getPokemon().getSpecies().getName() + " is running away " + this.mob.distanceToSqr(this.toAvoid) + " distanceSqr from here");

        if (this.mob.distanceToSqr(this.toAvoid) < (maxDist * 0.5)) {
            this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
        } else {
            this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
        }

    }
}