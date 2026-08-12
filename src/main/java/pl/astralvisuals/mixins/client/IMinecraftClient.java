package pl.astralvisuals.mixins.client;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_5520;
import net.minecraft.class_7574;
import net.minecraft.class_7853;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_310.class)
public interface IMinecraftClient {
   // method_1536 = doAttack, method_1583 = doItemUse (приватные в MinecraftClient).
   @Mutable
   @Accessor(value = "field_1752", remap = false)
   void astral$setItemUseCooldown(int value);

   @Invoker(value = "method_1536", remap = false)
   boolean astral$doAttack();

   @Invoker(value = "method_1583", remap = false)
   void astral$doItemUse();

   @Mutable
   @Accessor("session")
   void astralvisual$setSession(class_320 var1);

   @Mutable
   @Accessor("userApiService")
   void astralvisual$setUserApiService(UserApiService var1);

   @Mutable
   @Accessor("socialInteractionsManager")
   void astralvisual$setSocialInteractionsManager(class_5520 var1);

   @Mutable
   @Accessor("profileKeys")
   void astralvisual$setProfileKeys(class_7853 var1);

   @Mutable
   @Accessor("abuseReportContext")
   void astralvisual$setAbuseReportContext(class_7574 var1);
}
