package me.rufia.fightorflight;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Species;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CobblemonFightOrFlight.MODID)
@Mod.EventBusSubscriber
public class CobblemonFightOrFlight {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "fightorflight";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final float AUTO_AGGRO_THRESHOLD = 50.0f;

    public CobblemonFightOrFlight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        //modEventBus.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public static void onEntityJoined(EntityJoinLevelEvent event) {
        //LOGGER.info("onEntityJoined");

        if (event.getEntity() instanceof PokemonEntity) {
            PokemonEntity pokemonEntity = (PokemonEntity)event.getEntity();
            //LOGGER.info("onEntityJoined -> instanceOf PokemonEntity");

            float fleeSpeed = 1.5f;
            float pursuitSpeed = 1.2f;

            pokemonEntity.goalSelector.addGoal(3, new PokemonAvoidGoal(pokemonEntity, 48.0f, 1.0f, fleeSpeed));
            pokemonEntity.goalSelector.addGoal(3, new PokemonMeleeAttackGoal(pokemonEntity, pursuitSpeed, true));
            pokemonEntity.goalSelector.addGoal(4, new PokemonPanicGoal(pokemonEntity, fleeSpeed));

            pokemonEntity.targetSelector.addGoal(1, new HurtByTargetGoal(pokemonEntity));
            pokemonEntity.targetSelector.addGoal(2, new CaughtByTargetGoal(pokemonEntity));
            pokemonEntity.targetSelector.addGoal(3, new PokemonNearestAttackableTargetGoal<>(pokemonEntity, Player.class, 48.0f, true,true));
            //pokemonEntity.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(pokemonEntity, Player.class, true));

            //pokemonEntity.getAttributes().assignValues();
        }
    }

    public static double getFightOrFlightCoefficient(PokemonEntity pokemonEntity){
        Pokemon pokemon = pokemonEntity.getPokemon();
        double pkmnLevel = pokemon.getLevel();
        //double levelAggressionCoefficient = (pokemon.getLevel() - 20);
        double lowStatPenalty = (pkmnLevel * 1.5)+30;
        double levelAggressionCoefficient = (pokemon.getAttack() + pokemon.getSpecialAttack()) - lowStatPenalty;
        double atkDefRatioCoefficient = (pokemon.getAttack() + pokemon.getSpecialAttack()) - (pokemon.getDefence() + pokemon.getSpecialDefence());
        double natureAggressionCoefficient = 0;
        switch (pokemon.getNature().getDisplayName().toLowerCase()){
            case "cobblemon.nature.docile":
            case "cobblemon.nature.timid":
            case "cobblemon.nature.gentle":
            case "cobblemon.nature.careful":
                natureAggressionCoefficient = -2;
                break;
            case "cobblemon.nature.relaxed":
            case "cobblemon.nature.lax":
            case "cobblemon.nature.quiet":
            case "cobblemon.nature.bashful":
            case "cobblemon.nature.calm":
                natureAggressionCoefficient = -1;
                break;
            case "cobblemon.nature.sassy":
            case "cobblemon.nature.hardy":
            case "cobblemon.nature.bold":
            case "cobblemon.nature.impish":
            case "cobblemon.nature.hasty":
                natureAggressionCoefficient = 1;
                break;
            case "cobblemon.nature.brave":
            case "cobblemon.nature.rash":
            case "cobblemon.nature.adamant":
            case "cobblemon.nature.naughty":
                natureAggressionCoefficient = 2;
                break;
            default:
                natureAggressionCoefficient = 0;
                break;
        }

        //Weights and Clamps:
        levelAggressionCoefficient = Math.max(-(pkmnLevel + 5), Math.min(pkmnLevel, 1.5d * levelAggressionCoefficient));//5.0d * levelAggressionCoefficient;
        atkDefRatioCoefficient = Math.max(-pkmnLevel, 1.0d * atkDefRatioCoefficient);
        natureAggressionCoefficient = (pkmnLevel * 0.5) * natureAggressionCoefficient;//25.0d * natureAggressionCoefficient;

        double finalResult = levelAggressionCoefficient + atkDefRatioCoefficient + natureAggressionCoefficient;


//        if (true){
//            var pkmnString = "[" + pokemon.getSpecies().getName() + "]";
//            LOGGER.info(pkmnString + " levelAggressionCoefficient: " + levelAggressionCoefficient);
//            LOGGER.info(pkmnString + " atkDefRatioCoefficient: " + atkDefRatioCoefficient);
//            LOGGER.info(pkmnString + " natureAggressionCoefficient: " + natureAggressionCoefficient
//                    + " (" + pokemon.getNature().getDisplayName().toLowerCase() + ")");
//
//            LOGGER.info("final FightOrFlightCoefficient: "
//                    + levelAggressionCoefficient + "+" + atkDefRatioCoefficient + "+" + natureAggressionCoefficient
//                    + " = " + finalResult);
//        }
        return finalResult;
    }

    public static void PokemonEmoteAngry(Mob mob){
        double particleSpeed = Math.random();
        double particleAngle = Math.random() * 2 * Math.PI;
        double particleXSpeed = Math.cos(particleAngle) * particleSpeed;
        double particleYSpeed = Math.sin(particleAngle) * particleSpeed;

        if (mob.level instanceof ServerLevel){
            ((ServerLevel)mob.level).sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    mob.position().x, mob.getBoundingBox().maxY, mob.position().z,
                    1, //Amount?
                    particleXSpeed,0.5d, particleYSpeed,
                    1.0f); //Scale?
        }
        else{
            mob.level.addParticle(ParticleTypes.ANGRY_VILLAGER,
                    mob.position().x, mob.getBoundingBox().maxY, mob.position().z,
                    particleXSpeed,0.5d, particleYSpeed);
        }
    }
//    @SubscribeEvent
//    public static void onEntityAttributes(EntityAttributeModificationEvent event){
//        LOGGER.info("onEntityAttributes");
//        event.add(CobblemonEntities.POKEMON.get(), Attributes.ATTACK_DAMAGE, 2.0D);
//        //event.add(CobblemonEntities.POKEMON.get(), Attributes.ATTACK_KNOCKBACK, 2.0D);
//    }



//    @SubscribeEvent
//    public void onUseEntity(PlayerInteractEvent.EntityInteract event) {
//        LOGGER.info("onUseEntity");
//        if (event.getTarget() instanceof PokemonEntity) {
//            Player player = event.getEntity();
//            ItemStack itemStack = player.getItemInHand(event.getHand());
//            PokemonEntity pokemonEntity = (PokemonEntity)event.getTarget();
//
//            LOGGER.info("instanceOf PokemonEntity");
//
//            if (itemStack.is(Items.BUCKET)) {
//                LOGGER.info("itemStack.is(Items.BUCKET)");
//                LOGGER.info("pokemon of species: " + pokemonEntity.getPokemon().getSpecies().getName());
//                if (pokemonEntity.getPokemon().getSpecies().getName().toLowerCase().equals("magmar")) {
//                    LOGGER.info("pokemon is Magmar");
//                    player.level.playSound(null, pokemonEntity, SoundEvents.BUCKET_FILL_LAVA, SoundSource.PLAYERS, 1.0f, 1.0f);
//                    //ItemStack itemstackLava = new ItemStack(Items.LAVA_BUCKET, 1);
//                    ////ItemStack itemstackLava2 = ItemUtils.createFilledResult(itemStack, player, itemstackLava1);
//                    //ItemUtils.createFilledResult(itemStack, player, itemstackLava);
//                    //if (!player.level.isClientSide) {
//                    //    CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemstackLava);
//                    //}
//                    //p_28298_.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
//                    ItemStack itemstackLava = ItemUtils.createFilledResult(itemStack, player, Items.LAVA_BUCKET.getDefaultInstance());
//                    player.setItemInHand(event.getHand(), itemstackLava);
//
//                }
//            }
//        }
//    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
