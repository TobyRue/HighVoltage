package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.IRandomTickWeatherEffect;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Optional;

public record SummonEntityEffect(EntityType<?> entity, Optional<CompoundTag> data, int chance, int mob_cap) implements WeatherProfile.WeatherEffect, IRandomTickWeatherEffect {
    public static final Codec<SummonEntityEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(SummonEntityEffect::entity),
            CompoundTag.CODEC.optionalFieldOf("data").forGetter(SummonEntityEffect::data),
            Codec.INT.fieldOf("chance").forGetter(SummonEntityEffect::chance),
            Codec.INT.optionalFieldOf("mob_cap", 12).forGetter(SummonEntityEffect::mob_cap)
    ).apply(instance, SummonEntityEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.SUMMON_ENTITY.get();
    }

    @Override
    public int getChance() {
        return this.chance();
    }

    @Override
    public void execute(ServerLevel world, ServerPlayer player, BlockPos targetPos, boolean isOutside) {
        AABB areaBox = new AABB(player.blockPosition()).inflate(16, 64, 16);

        long currentStormEntityCount = world.getEntities(this.entity(), areaBox, entity -> {
            if (entity instanceof Mob mob) {
                CompoundTag nbt = new CompoundTag();
                mob.saveWithoutId(nbt);

                return nbt.contains("forge:spawn_type") && "EVENT".equals(nbt.getString("forge:spawn_type"));
            }
            return false;
        }).size();

        if (currentStormEntityCount >= this.mob_cap()) {
            return;
        }

        var entity = this.entity().create(world);
        if (entity != null) {
            this.data().ifPresent(entity::load);

            entity.moveTo(Vec3.atBottomCenterOf(targetPos));

            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(world, world.getCurrentDifficultyAt(targetPos), net.minecraft.world.entity.MobSpawnType.EVENT, null, null);
            }

            world.addFreshEntity(entity);
        }
    }
}