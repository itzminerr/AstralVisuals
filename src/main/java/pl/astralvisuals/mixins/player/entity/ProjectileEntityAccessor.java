package pl.astralvisuals.mixins.player.entity;

import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1676;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1676.class)
public interface ProjectileEntityAccessor {
   @Accessor("field_33399")
   class_1297 astral$getOwner();

   @Accessor("field_22478")
   UUID astral$getOwnerUuid();
}
