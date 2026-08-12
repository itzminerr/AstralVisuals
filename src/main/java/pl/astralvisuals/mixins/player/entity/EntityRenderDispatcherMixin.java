package pl.astralvisuals.mixins.player.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_898;
import net.minecraft.class_9974;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pl.astralvisuals.common.repository.friend.FriendUtils;
import pl.astralvisuals.features.impl.render.CustomHitbox;

// class_898 = EntityRenderDispatcher. Все цели — intermediary с remap=false,
// т.к. проект собирается с -proc:none и refmap не генерируется (см. остальные миксины).
@Mixin(class_898.class)
public class EntityRenderDispatcherMixin {
   // method_3954 = private render(...) — гейт показа хитбоксов читает поле field_4680 (renderHitboxes)
   // напрямую. Форсим true, когда модуль включён, чтобы хитбоксы рисовались без F3+B. (opcode 180 = GETFIELD)
   @ModifyExpressionValue(
      method = "method_3954",
      at = @At(value = "FIELD", target = "Lnet/minecraft/class_898;field_4680:Z", opcode = 180, remap = false),
      remap = false
   )
   private boolean astral$forceHitboxes(boolean original, @Local(argsOnly = true) class_1297 entity) {
      CustomHitbox module = CustomHitbox.getInstance();
      if (module == null || !module.isState()) {
         return original;
      }

      // "Только игроки": не форсим хитбокс у не-игроков (остаётся ванильное поведение F3+B).
      if (module.onlyPlayers() && !(entity instanceof class_1657)) {
         return original;
      }

      return true;
   }

   // method_3956 = renderHitbox. Главный бокс сущности (drawBox с Box, ordinal 0) — кастомный цвет.
   @Redirect(
      method = "method_3956",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_9974;method_62295(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;Lnet/minecraft/class_238;FFFF)V",
         ordinal = 0,
         remap = false
      ),
      remap = false
   )
   private static void astral$hitboxColor(
      class_4587 matrices, class_4588 vertices, class_238 box, float red, float green, float blue, float alpha,
      @Local(ordinal = 0, argsOnly = true) class_1297 entity
   ) {
      CustomHitbox module = CustomHitbox.getInstance();
      if (module != null && module.isState()) {
         // Друзьям — отдельный цвет, если включено; иначе общий цвет хитбокса.
         int color = module.useFriendColor() && FriendUtils.isFriend(entity) ? module.getFriendColor() : module.getColor();
         alpha = (color >> 24 & 0xFF) / 255.0F;
         red = (color >> 16 & 0xFF) / 255.0F;
         green = (color >> 8 & 0xFF) / 255.0F;
         blue = (color & 0xFF) / 255.0F;
      }

      class_9974.method_62295(matrices, vertices, box, red, green, blue, alpha);
   }

   // method_62292 = drawBox(...DDDDDDFFFF), ordinal 0 — красный квадрат на уровне глаз (квадрат линии зрения).
   @Redirect(
      method = "method_3956",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_9974;method_62292(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;DDDDDDFFFF)V",
         ordinal = 0,
         remap = false
      ),
      remap = false
   )
   private static void astral$eyeBox(
      class_4587 matrices,
      class_4588 vertices,
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      float red,
      float green,
      float blue,
      float alpha
   ) {
      CustomHitbox module = CustomHitbox.getInstance();
      if (module != null && module.isState() && module.hideEyeBox()) {
         return;
      }

      class_9974.method_62292(matrices, vertices, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
   }

   // method_62298 = drawVector — синяя линия направления взгляда (линия зрения).
   @Redirect(
      method = "method_3956",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_9974;method_62298(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;Lorg/joml/Vector3f;Lnet/minecraft/class_243;I)V",
         remap = false
      ),
      remap = false
   )
   private static void astral$sightLine(class_4587 matrices, class_4588 vertices, Vector3f start, class_243 direction, int color) {
      CustomHitbox module = CustomHitbox.getInstance();
      if (module != null && module.isState() && module.hideSightLine()) {
         return;
      }

      class_9974.method_62298(matrices, vertices, start, direction, color);
   }
}
