package pl.astralvisuals.utils.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;

public final class Instance {
   private static final ConcurrentMap<Class<? extends Module>, Module> instanceModules = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<? extends AbstractDraggable>, AbstractDraggable> instanceDraggables = new ConcurrentHashMap<>();

   public static <T extends Module> T get(Class<T> clazz) {
      return clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> Force.getInstance().getModuleProvider().get((Class<? extends Module>)instance)));
   }

   public static <T extends Module> T get(String module) {
      return Force.getInstance().getModuleProvider().get(module);
   }

   public static <T extends AbstractDraggable> T getDraggable(Class<T> clazz) {
      return clazz.cast(
         instanceDraggables.computeIfAbsent(clazz, instance -> Force.getInstance().getDraggableRepository().get((Class<? extends AbstractDraggable>)instance))
      );
   }

   public static <T extends AbstractDraggable> T getDraggable(String draggable) {
      return Force.getInstance().getDraggableRepository().get(draggable);
   }

   private Instance() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
