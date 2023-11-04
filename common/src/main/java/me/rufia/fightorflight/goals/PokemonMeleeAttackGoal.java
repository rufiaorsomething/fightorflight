package me.rufia.fightorflight.goals;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.battles.BattleBuilder;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.math.UMathKt;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;


public class PokemonMeleeAttackGoal extends MeleeAttackGoal {
    public int ticksUntilNewAngerParticle = 0;

    public int ticksUntilNewAngerCry = 0;

    public PokemonMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    public void tick() {
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        LivingEntity owner = pokemonEntity.getOwner();
        if (owner == null){
            if (ticksUntilNewAngerParticle < 1) {
                CobblemonFightOrFlight.PokemonEmoteAngry(this.mob);
                ticksUntilNewAngerParticle = 10;
            }
            else { ticksUntilNewAngerParticle = ticksUntilNewAngerParticle - 1; }

            if (ticksUntilNewAngerCry < 1) {
                pokemonEntity.cry();
                ticksUntilNewAngerCry = 100 + (int)(Math.random() * 200);
            }
            else { ticksUntilNewAngerCry = ticksUntilNewAngerCry - 1; }
        }

        super.tick();

        if (!CobblemonFightOrFlight.config().do_pokemon_attack_in_battle){
            if (isTargetInBattle()){
                this.mob.getNavigation().setSpeedModifier(0);
            }
        }
    }
    public boolean isTargetInBattle(){
        if (this.mob.getTarget() instanceof ServerPlayer targetAsPlayer){
            return BattleRegistry.INSTANCE.getBattleByParticipatingPlayer(targetAsPlayer) != null;
        }
        return false;
    }

    public boolean shouldFightTarget(){
        //if (FightOrFlightCommonConfigs.DO_POKEMON_ATTACK.get() == false) { return false; }

        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;

        if (pokemonEntity.getPokemon().getLevel() < CobblemonFightOrFlight.config().minimum_attack_level) { return false; }

        LivingEntity owner = pokemonEntity.getOwner();
        if (owner != null){
            if (!CobblemonFightOrFlight.config().do_pokemon_defend_owner) { return false; }
            if (this.mob.getTarget() == null || this.mob.getTarget() == owner) { return false; }

            if (this.mob.getTarget() instanceof PokemonEntity targetPokemon){
                LivingEntity targetOwner = targetPokemon.getOwner();
                if (targetOwner != null){
                    if (targetOwner == owner) { return false; }
                    if (!CobblemonFightOrFlight.config().do_player_pokemon_attack_other_player_pokemon) {
                        return false;
                    }
                }
            }
            if (this.mob.getTarget() instanceof Player){
                if (!CobblemonFightOrFlight.config().do_player_pokemon_attack_other_players){
                    return false;
                }
            }

        } else {
            if (this.mob.getTarget() != null){
                if (CobblemonFightOrFlight.getFightOrFlightCoefficient(pokemonEntity) <= 0) { return false; }

                LivingEntity targetEntity = this.mob.getTarget();
                if (this.mob.distanceToSqr(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ()) > 400) { return false; }
            }
        }
        //if (pokemonEntity.getPokemon().isPlayerOwned()) { return false; }

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
    public float calculatePokemonDamage(PokemonEntity pokemonEntity){
        float maxAttack = Math.max(pokemonEntity.getPokemon().getAttack(), pokemonEntity.getPokemon().getSpecialAttack());
        maxAttack = Math.min(maxAttack, 255.0f);
        maxAttack = maxAttack / 255.0f;
        //return maxAttack / 10f;

        float minDmg = CobblemonFightOrFlight.config().minimum_attack_damage;
        float maxDmg = CobblemonFightOrFlight.config().maximum_attack_damage;

        return minDmg + ((maxDmg - minDmg) * maxAttack);
    }

    public boolean pokemonDoHurtTarget(Entity hurtTarget) {
        if (!CobblemonFightOrFlight.config().do_pokemon_attack_in_battle) {
            if (isTargetInBattle()) { return false; }
        }
        PokemonEntity pokemonEntity = (PokemonEntity)this.mob;
        Pokemon pokemon = pokemonEntity.getPokemon();

        if (!pokemonTryForceEncounter(pokemonEntity, hurtTarget)){

            int pkmLevel = pokemon.getLevel();
            //float maxAttack = Math.max(pokemonEntity.getPokemon().getAttack(), pokemonEntity.getPokemon().getSpecialAttack());

            ElementalType primaryType = pokemon.getPrimaryType();

            //LogUtils.getLogger().info("target took " + primaryType.getName() + " damage");


            float hurtDamage = calculatePokemonDamage(pokemonEntity);
            float hurtKnockback = 1.0f;

            if (hurtTarget instanceof LivingEntity livingHurtTarget) {
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
                        break;
                    case "dragon":
                        hurtDamage = hurtDamage + 3;
                        break;
                    case "flying":
                        hurtKnockback = hurtKnockback * 2;
                        break;
                    case "water":
                        hurtKnockback = hurtKnockback * 2;
                        livingHurtTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (effectStrength + 2) * 25, 0), this.mob);
                        break;

                    default:
                        break;
                }
            }




            boolean flag = hurtTarget.hurt(this.mob.level().damageSources().mobAttack(this.mob), hurtDamage);
            if (flag) {
                if (hurtKnockback > 0.0F && hurtTarget instanceof LivingEntity) {
                    ((LivingEntity)hurtTarget).knockback(hurtKnockback * 0.5F, Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180F)));
                    this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                }

                this.mob.setLastHurtMob(hurtTarget);
            }

            return flag;
        }

        return false;
    }

    public boolean pokemonTryForceEncounter(PokemonEntity attackingPokemon, Entity hurtTarget){
        if (hurtTarget instanceof PokemonEntity defendingPokemon)
        {
            if (attackingPokemon.getPokemon().isPlayerOwned()){
                if (defendingPokemon.getPokemon().isPlayerOwned()){
                    if (CobblemonFightOrFlight.config().force_player_battle_on_pokemon_hurt) {
                        return pokemonForceEncounterPvP(attackingPokemon, defendingPokemon);
                    }
                } else {
                    if (CobblemonFightOrFlight.config().force_wild_battle_on_pokemon_hurt) {
                        return pokemonForceEncounterPvE(attackingPokemon, defendingPokemon);
                    }
                }
            } else if (defendingPokemon.getPokemon().isPlayerOwned()) {
                if (CobblemonFightOrFlight.config().force_wild_battle_on_pokemon_hurt) {
                    return pokemonForceEncounterPvE(defendingPokemon, attackingPokemon);
                }
            }
        }
        return false;
    }

    public boolean pokemonForceEncounterPvP(PokemonEntity playerPokemon, PokemonEntity opponentPokemon){
        if (playerPokemon.getOwner() instanceof ServerPlayer serverPlayer
        && opponentPokemon.getOwner() instanceof ServerPlayer serverOpponent){

            if (serverPlayer == serverOpponent // I don't see why this should ever happen, but probably best to account for it
                    || !canBattlePlayer(serverPlayer)
                    || !canBattlePlayer(serverOpponent)) {
                return false;
            }

            BattleBuilder.INSTANCE.pvp1v1(serverPlayer,
                    serverOpponent,
                    null,
                    null,
                    BattleFormat.Companion.getGEN_9_SINGLES(),
                    false,
                    false);
        }
        return false;
    }
    public boolean pokemonForceEncounterPvE(PokemonEntity playerPokemon, PokemonEntity wildPokemon){
        if (playerPokemon.getOwner() instanceof ServerPlayer serverPlayer)
        {

            if (!canBattlePlayer(serverPlayer)) {
                return false;
            }

            BattleBuilder.INSTANCE.pve(serverPlayer,
                    wildPokemon,
                    playerPokemon.getPokemon().getUuid(),
                    BattleFormat.Companion.getGEN_9_SINGLES(),
                    false,
                    false,
                    Cobblemon.config.getDefaultFleeDistance(),
                    Cobblemon.INSTANCE.getStorage().getParty(serverPlayer));
        }
        return false;
    }

    public boolean canBattlePlayer(ServerPlayer serverPlayer){
        boolean playerHasAlivePokemon = false;
        for (Pokemon pokemon : Cobblemon.INSTANCE.getStorage().getParty(serverPlayer)) {
            if (!pokemon.isFainted()) {
                playerHasAlivePokemon = true;
                break;
            }
        }

        return BattleRegistry.INSTANCE.getBattleByParticipatingPlayer(serverPlayer) == null
                && playerHasAlivePokemon
                && serverPlayer.isAlive();
    }
}
