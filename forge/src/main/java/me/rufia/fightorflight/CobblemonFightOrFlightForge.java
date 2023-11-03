package me.rufia.fightorflight;

import net.minecraftforge.fml.common.Mod;

@Mod(CobblemonFightOrFlight.MODID)
@Mod.EventBusSubscriber
public class CobblemonFightOrFlightForge {

    public CobblemonFightOrFlightForge() {
        CobblemonFightOrFlight.init((pokemonEntity, priority, goal) -> pokemonEntity.goalSelector.addGoal(priority, goal));
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
//                    ItemUtils.fi.createFilledResult(itemStack, player, itemstackLava);
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
}
