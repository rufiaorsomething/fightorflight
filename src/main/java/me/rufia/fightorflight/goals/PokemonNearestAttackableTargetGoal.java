package me.rufia.fightorflight.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import me.rufia.fightorflight.config.FightOrFlightCommonConfigs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.raid.Raider;

import javax.annotation.Nullable;
import java.util.function.Predicate;


public class PokemonNearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public int ticksUntilNewAngerParticle = 0;
    public boolean generateAngerParticles = false;
    public float safeDistanceSqr = 36;
    public PokemonNearestAttackableTargetGoal(Mob mob, Class<T> targetType, float safeDistanceSqr, boolean mustSee, boolean mustReach) {
        super(mob, targetType, mustSee, mustReach);
        this.safeDistanceSqr = safeDistanceSqr;
    }

    public boolean canUse() {
        //if (this.mob.getTarget() != null) { return false; }
        if (FightOrFlightCommonConfigs.DO_POKEMON_ATTACK_UNPROVOKED.get() == false) { return false; }

        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;


        if (pokemonEntity.getPokemon().getLevel() < FightOrFlightCommonConfigs.MINIMUM_ATTACK_UNPROVOKED_LEVEL.get()) { return false; }

        if (pokemonEntity.getPokemon().isPlayerOwned()) { return false; }
        if (CobblemonFightOrFlight.getFightOrFlightCoefficient(pokemonEntity) <= CobblemonFightOrFlight.AUTO_AGGRO_THRESHOLD)
        {
            generateAngerParticles = false;
            return false;
        }
        else{
            generateAngerParticles = true;
//          if (generateAngerParticles){
            if (ticksUntilNewAngerParticle < 1) {
                CobblemonFightOrFlight.PokemonEmoteAngry(this.mob);
                ticksUntilNewAngerParticle = 25;
            }
            else { ticksUntilNewAngerParticle = ticksUntilNewAngerParticle - 1; }
//          }
        }

        return super.canUse();
    }
//    public void tick() {
////        if (generateAngerParticles){
//        if (ticksUntilNewAngerParticle < 1) {
//            PokemonEmoteAngry(this.mob);
//            ticksUntilNewAngerParticle = 30;
//        }
//        else { ticksUntilNewAngerParticle = ticksUntilNewAngerParticle - 1; }
////        }
//        super.tick();
//    }
    protected void findTarget() {
        super.findTarget();
        if (this.target != null && this.target.distanceToSqr(this.mob) > safeDistanceSqr) {
            this.target = null;
        }
    }
}
