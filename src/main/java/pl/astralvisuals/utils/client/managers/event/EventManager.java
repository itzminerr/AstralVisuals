package pl.astralvisuals.utils.client.managers.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import pl.astralvisuals.features.module.exception.ModuleException;
import pl.astralvisuals.utils.client.logs.Logger;
import pl.astralvisuals.utils.client.managers.event.events.Event;
import pl.astralvisuals.utils.client.managers.event.events.EventStoppable;
import pl.astralvisuals.utils.client.managers.event.types.Priority;

public final class EventManager {
   private static final Map<Class<? extends Event>, List<EventManager.MethodData>> REGISTRY_MAP = new HashMap<>();

   public void register(Object object) {
      for (Method method : object.getClass().getDeclaredMethods()) {
         if (!isMethodBad(method)) {
            this.register(method, object);
         }
      }
   }

   public void register(Object object, Class<? extends Event> eventClass) {
      for (Method method : object.getClass().getDeclaredMethods()) {
         if (!isMethodBad(method, eventClass)) {
            this.register(method, object);
         }
      }
   }

   public void unregister(Object object) {
      for (List<EventManager.MethodData> dataList : REGISTRY_MAP.values()) {
         for (EventManager.MethodData data : dataList) {
            if (data.source().equals(object)) {
               dataList.remove(data);
            }
         }
      }

      cleanMap(true);
   }

   public void unregister(Object object, Class<? extends Event> eventClass) {
      if (REGISTRY_MAP.containsKey(eventClass)) {
         for (EventManager.MethodData data : REGISTRY_MAP.get(eventClass)) {
            if (data.source().equals(object)) {
               REGISTRY_MAP.get(eventClass).remove(data);
            }
         }

         cleanMap(true);
      }
   }

   private void register(Method method, Object object) {
      Class<? extends Event> indexClass = (Class<? extends Event>)method.getParameterTypes()[0];
      final EventManager.MethodData data = new EventManager.MethodData(object, method, method.getAnnotation(EventHandler.class).value());
      if (!data.target().isAccessible()) {
         data.target().setAccessible(true);
      }

      if (REGISTRY_MAP.containsKey(indexClass)) {
         if (!REGISTRY_MAP.get(indexClass).contains(data)) {
            REGISTRY_MAP.get(indexClass).add(data);
            sortListValue(indexClass);
         }
      } else {
         REGISTRY_MAP.put(indexClass, new CopyOnWriteArrayList<EventManager.MethodData>() {
            private static final long serialVersionUID = 666L;

            {
               this.add(data);
            }
         });
      }
   }

   public void removeEntry(Class<? extends Event> indexClass) {
      Iterator<Entry<Class<? extends Event>, List<EventManager.MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();

      while (mapIterator.hasNext()) {
         if (mapIterator.next().getKey().equals(indexClass)) {
            mapIterator.remove();
            break;
         }
      }
   }

   public static void cleanMap(boolean onlyEmptyEntries) {
      Iterator<Entry<Class<? extends Event>, List<EventManager.MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();

      while (mapIterator.hasNext()) {
         if (!onlyEmptyEntries || mapIterator.next().getValue().isEmpty()) {
            mapIterator.remove();
         }
      }
   }

   private static void sortListValue(Class<? extends Event> indexClass) {
      List<EventManager.MethodData> sortedList = new CopyOnWriteArrayList<>();

      for (byte priority : Priority.VALUE_ARRAY) {
         for (EventManager.MethodData data : REGISTRY_MAP.get(indexClass)) {
            if (data.priority() == priority) {
               sortedList.add(data);
            }
         }
      }

      REGISTRY_MAP.put(indexClass, sortedList);
   }

   private static boolean isMethodBad(Method method) {
      return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventHandler.class);
   }

   private static boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
      return isMethodBad(method) || !method.getParameterTypes()[0].equals(eventClass);
   }

   public static Event callEvent(Event event) {
      List<EventManager.MethodData> dataList = REGISTRY_MAP.get(event.getClass());
      if (dataList != null) {
         if (event instanceof EventStoppable stoppable) {
            for (EventManager.MethodData data : dataList) {
               invokeSafely(data, event);
               if (stoppable.isStopped()) {
                  break;
               }
            }
         } else {
            for (EventManager.MethodData datax : dataList) {
               invokeSafely(datax, event);
            }
         }
      }

      return event;
   }

   private static void invokeSafely(EventManager.MethodData data, Event argument) {
      try {
         invoke(data, argument);
      } catch (Throwable var3) {
         Logger.error("Unhandled event handler error. Method: " + data.target().getName() + ", Event: " + argument.getClass().getSimpleName(), var3);
      }
   }

   private static void invoke(EventManager.MethodData data, Event argument) {
      try {
         data.target().invoke(data.source(), argument);
      } catch (IllegalAccessException var7) {
         String errorMessage = "Illegal access to method. ";
         errorMessage = errorMessage + "Method: " + data.target().getName() + ", ";
         errorMessage = errorMessage + "Argument: " + argument.toString() + ", ";
         errorMessage = errorMessage + "Log: " + var7.fillInStackTrace();
         Logger.error(errorMessage, (Throwable)var7);
      } catch (IllegalArgumentException var8) {
         String errorMessagex = "Illegal arguments passed to method. ";
         errorMessagex = errorMessagex + "Method: " + data.target().getName() + ", ";
         errorMessagex = errorMessagex + "Argument: " + argument.toString() + ", ";
         errorMessagex = errorMessagex + "Log: " + var8.getCause();
         Logger.error(errorMessagex, (Throwable)var8);
      } catch (InvocationTargetException var9) {
         Throwable cause = var9.getCause();
         if (cause instanceof ModuleException moduleException) {
            class_310 client = class_310.method_1551();
            class_2561 message = class_2561.method_43470("[" + moduleException.getModuleName() + "] ")
               .method_10852(class_2561.method_43470(moduleException.getMessage()).method_27692(class_124.field_1061));
            if (client.field_1724 != null) {
               client.field_1705.method_1743().method_1812(message);
            } else {
               Logger.error(message.getString(), cause);
            }
         } else {
            String errorMessagexx = "Exception occurred within invoked method. ";
            errorMessagexx = errorMessagexx + "Method: " + data.target().getName() + ", ";
            errorMessagexx = errorMessagexx + "Argument: " + argument.toString() + ", ";
            errorMessagexx = errorMessagexx + "Log: " + var9.getCause();
            Logger.error(errorMessagexx, (Throwable)(cause != null ? cause : var9));
         }
      }
   }

   private record MethodData(Object source, Method target, byte priority) {
   }
}
