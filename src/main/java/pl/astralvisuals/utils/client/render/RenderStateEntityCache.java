package pl.astralvisuals.utils.client.render;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_10042;
import net.minecraft.class_1309;

public final class RenderStateEntityCache {
   private static final Map<class_10042, class_1309> ENTITIES = Collections.synchronizedMap(new WeakHashMap<>());

   private RenderStateEntityCache() {
   }

   public static void put(class_10042 state, class_1309 entity) {
      if (state != null && entity != null) {
         ENTITIES.put(state, entity);
      }
   }

   public static class_1309 get(class_10042 state) {
      return state == null ? null : ENTITIES.get(state);
   }
}
