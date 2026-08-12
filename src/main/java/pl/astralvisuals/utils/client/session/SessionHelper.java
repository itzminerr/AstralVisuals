package pl.astralvisuals.utils.client.session;

import com.mojang.authlib.minecraft.UserApiService;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_5520;
import net.minecraft.class_7569;
import net.minecraft.class_7574;
import net.minecraft.class_7853;
import net.minecraft.class_320.class_321;
import pl.astralvisuals.mixins.client.IMinecraftClient;

public final class SessionHelper {
   private SessionHelper() {
   }

   public static void applyOfflineSession(String name, UUID uuid) {
      class_310 client = class_310.method_1551();
      IMinecraftClient clientAccessor = (IMinecraftClient)client;
      class_320 session = new class_320(name, uuid, "0", Optional.empty(), Optional.empty(), class_321.field_1988);
      UserApiService apiService = UserApiService.OFFLINE;
      clientAccessor.astralvisual$setSession(session);
      client.method_53462().getProperties().clear();
      clientAccessor.astralvisual$setUserApiService(apiService);
      clientAccessor.astralvisual$setSocialInteractionsManager(new class_5520(client, apiService));
      clientAccessor.astralvisual$setProfileKeys(class_7853.method_46532(apiService, session, client.field_1697.toPath()));
      clientAccessor.astralvisual$setAbuseReportContext(class_7574.method_44599(class_7569.method_44586(), apiService));
   }
}
