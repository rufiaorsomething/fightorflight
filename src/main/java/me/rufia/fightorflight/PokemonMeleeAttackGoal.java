package me.rufia.fightorflight;

import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;


public class PokemonMeleeAttackGoal extends MeleeAttackGoal {
    public int ticksUntilNewAngerParticle = 0;

    public PokemonMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    public void tick() {
        if (ticksUntilNewAngerParticle < 1) {
            CobblemonFightOrFlight.PokemonEmoteAngry(this.mob);
            ticksUntilNewAngerParticle = 10;
        }
        else { ticksUntilNewAngerParticle = ticksUntilNewAngerParticle - 1; }

        super.tick();
    }

    public boolean shouldFightTarget(){
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        if (this.mob.getTarget() != null){
            if (CobblemonFightOrFlight.getFightOrFlightCoefficient(pokemonEntity) <= 0) { return false; }
        }
        if (pokemonEntity.getPokemon().isPlayerOwned()) { return false; }

        return !pokemonEntity.isBusy();
    }

    public boolean canUse() {
        return shouldFightTarget() && super.canUse();
    }

    public boolean canContinueToUse() {
        return shouldFightTarget() && super.canContinueToUse();
    }

    protected void checkAndPerformAttack(LivingEntity target, double distanceToSqr) {
        double d0 = this.getAttackReachSqr(target);
        if (distanceToSqr <= d0 && this.getTicksUntilNextAttack() <= 0) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            pokemonDoHurtTarget(target);
        }
    }

    public boolean pokemonDoHurtTarget(Entity hurtTarget) {
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        Pokemon pokemon = pokemonEntity.getPokemon();
        int pkmLevel = pokemon.getLevel();
        float maxAttack = Math.max(pokemonEntity.getPokemon().getAttack(), pokemonEntity.getPokemon().getSpecialAttack());

        ElementalType primaryType = pokemon.getPrimaryType();

        //LogUtils.getLogger().info("target took " + primaryType.getName() + " damage");


        float hurtDamage = maxAttack / 10f;
        float hurtKnockback = 1.0f;

        if (hurtTarget instanceof LivingEntity) {
            LivingEntity livingHurtTarget = (LivingEntity)hurtTarget;
            int effectStrength = Math.max(pkmLevel / 10, 1);

            switch (primaryType.getName()) {
                case "fire":
                    livingHurtTarget.setSecondsOnFire(effectStrength);
                    break;
                case "ice":
                    livingHurtTarget.setTicksFrozen(livingHurtTarget.getTicksFrozen() + effectStrength * 30);
                    break;
                case "poison":
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.POISON, effectStrength * 20, 0), this.mob);
                    break;
                case "psychic":
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.LEVITATION, effectStrength * 20, 0), this.mob);
                    break;
                case "fairy":
                case "fighting":
                case "steel":
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, effectStrength * 20, 0), this.mob);
                    break;
                case "ghost":
                case "dark":
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, (effectStrength + 2) * 25, 0), this.mob);
                    break;
                case "ground":
                case "rock":
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, (effectStrength + 2) * 25, 0), this.mob);
                    break;
                case "electric":
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (effectStrength + 2) * 25, 0), this.mob);
                    break;
                case "bug":
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.HUNGER, (effectStrength + 2) * 25, 0), this.mob);
                    break;
                case "grass":
                    this.mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, (effectStrength + 2) * 20, 0), this.mob);
                case "dragon":
                    hurtDamage = hurtDamage + 3;
                case "flying":
                    hurtKnockback = hurtKnockback * 2;
                case "water":
                    hurtKnockback = hurtKnockback * 2;
                    livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (effectStrength + 2) * 25, 0), this.mob);

                default:
                    break;
            }
        }




        boolean flag = hurtTarget.hurt(DamageSource.mobAttack(this.mob), hurtDamage);
        if (flag) {
            if (hurtKnockback > 0.0F && hurtTarget instanceof LivingEntity) {
                ((LivingEntity)hurtTarget).knockback((double)(hurtKnockback * 0.5F), (double) Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180F))));
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            }

            this.mob.setLastHurtMob(hurtTarget);
        }

        return flag;
    }

}
