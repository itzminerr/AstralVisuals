package pl.astralvisuals.mixins.client.screen;

import java.util.List;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.minecraft.class_6379;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_437.class)
public interface IScreen {
   @Accessor
   List<class_4068> getDrawables();

   @Accessor
   List<class_364> getChildren();

   @Accessor
   List<class_6379> getSelectables();
}
