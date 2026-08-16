package pl.astralvisuals.features.impl.movement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1686;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.minecraft.class_9334;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.chat.ChatMessage;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public final class Tracker extends Module {
   private static final int MIN_USE_TICKS = 8;
   private static final long DUPLICATE_WINDOW_MS = 900L;
   private final SliderSettings radius = new SliderSettings("Радиус", "Дальность отслеживания игроков").setValue(50.0F).range(5.0F, 100.0F);
   private final BooleanSetting trackFood = new BooleanSetting("Отслеживать еду", "Сообщать, что съели игроки").setValue(true);
   private final BooleanSetting trackDrink = new BooleanSetting("Отслеживать питьё", "Сообщать, что выпили игроки").setValue(true);
   private final BooleanSetting trackSplash = new BooleanSetting("Отслеживать донки", "Сообщать о взрывных зельях").setValue(true);
   private final BooleanSetting showEffects = new BooleanSetting("Показывать эффекты", "Показывать эффекты и длительность").setValue(true);
   private final Map<UUID, UseState> useStates = new HashMap<>();
   private final Map<UUID, Map<String, class_1293>> lastEffects = new HashMap<>();
   private final Map<Integer, TrackedPotion> trackedPotions = new HashMap<>();
   private final Map<String, Long> recentEvents = new HashMap<>();

   public Tracker() {
      super("Tracker", "Tracker", ModuleCategory.PLAYER);
      this.setup(this.radius, this.trackFood, this.trackDrink, this.trackSplash, this.showEffects);
   }

   public static Tracker getInstance() {
      return Instance.get(Tracker.class);
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1724 == null || mc.field_1687 == null) {
         this.clearState();
         return;
      }
      this.trackConsumables();
      this.trackSplashPotions();
   }

   @Override
   public void deactivate() {
      this.clearState();
   }

   private void trackConsumables() {
      HashSet<UUID> visible = new HashSet<>();
      for (class_1657 player : mc.field_1687.method_18456()) {
         if (!this.isTrackable(player) || !this.inRange(player)) {
            continue;
         }
         UUID id = player.method_5667();
         visible.add(id);
         Map<String, class_1293> previous = this.lastEffects.getOrDefault(id, Map.of());
         Map<String, class_1293> current = this.snapshotEffects(player);
         this.processUse(player, this.addedEffects(previous, current));
         this.lastEffects.put(id, current);
      }
      this.useStates.keySet().removeIf(id -> !visible.contains(id));
      this.lastEffects.keySet().removeIf(id -> !visible.contains(id));
   }

   private void processUse(class_1657 player, List<class_1293> addedEffects) {
      UUID id = player.method_5667();
      UseState state = this.useStates.computeIfAbsent(id, ignored -> new UseState());
      if (player.method_6115()) {
         class_1799 active = player.method_6030();
         if (!state.using) {
            state.using = true;
            state.startAge = player.field_6012;
            state.item = active.method_7972();
         } else if (state.item.method_7960() && !active.method_7960()) {
            state.item = active.method_7972();
         }
         return;
      }
      if (!state.using) {
         return;
      }
      int ticks = Math.max(0, player.field_6012 - state.startAge);
      class_1799 used = state.item;
      state.reset();
      if (ticks < MIN_USE_TICKS || used == null || used.method_7960()) {
         return;
      }
      this.handleConsumed(player, used, addedEffects);
   }

   private void handleConsumed(class_1657 player, class_1799 stack, List<class_1293> addedEffects) {
      String playerName = player.method_5477().getString();
      String itemName = stack.method_7964().getString();
      if (this.trackDrink.isValue() && this.isDrinkable(stack)) {
         if (!this.duplicate("drink:" + player.method_5667() + ":" + this.itemKey(stack))) {
            ChatMessage.brandmessage(playerName + " выпил " + itemName);
            if (this.showEffects.isValue()) {
               List<class_1293> potionEffects = this.potionEffects(stack);
               this.printEffects(potionEffects.isEmpty() ? addedEffects : potionEffects, 1.0F);
            }
         }
      } else if (this.trackFood.isValue() && this.isFood(stack) && !this.duplicate("food:" + player.method_5667() + ":" + this.itemKey(stack))) {
         ChatMessage.brandmessage(playerName + " съел " + itemName);
         if (this.showEffects.isValue()) {
            this.printEffects(addedEffects, 1.0F);
         }
      }
   }

   private void trackSplashPotions() {
      if (!this.trackSplash.isValue()) {
         this.trackedPotions.clear();
         return;
      }
      HashSet<Integer> current = new HashSet<>();
      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (!(entity instanceof class_1686 potion)) {
            continue;
         }
         int id = potion.method_5628();
         if (!this.trackedPotions.containsKey(id) && !this.inRange(potion)) {
            continue;
         }
         current.add(id);
         TrackedPotion tracked = this.trackedPotions.get(id);
         if (tracked == null) {
            class_1297 owner = potion.method_24921();
            UUID ownerId = owner instanceof class_1657 player ? player.method_5667() : null;
            this.trackedPotions.put(id, new TrackedPotion(potion.method_7495().method_7972(), potion.method_19538(), ownerId));
         } else {
            tracked.position = potion.method_19538();
         }
      }
      Iterator<Map.Entry<Integer, TrackedPotion>> iterator = this.trackedPotions.entrySet().iterator();
      while (iterator.hasNext()) {
         Map.Entry<Integer, TrackedPotion> entry = iterator.next();
         if (!current.contains(entry.getKey())) {
            this.handleSplash(entry.getValue());
            iterator.remove();
         }
      }
   }

   private void handleSplash(TrackedPotion tracked) {
      if (tracked == null || tracked.item == null || tracked.item.method_7960()) {
         return;
      }
      List<class_1293> effects = this.potionEffects(tracked.item);
      if (effects.isEmpty()) {
         return;
      }
      class_1657 thrower = this.playerByUuid(tracked.owner);
      String throwerName = thrower == null ? "Кто-то" : thrower.method_5477().getString();
      class_238 area = new class_238(
         tracked.position.field_1352 - 4.0, tracked.position.field_1351 - 2.0, tracked.position.field_1350 - 4.0,
         tracked.position.field_1352 + 4.0, tracked.position.field_1351 + 2.0, tracked.position.field_1350 + 4.0
      );
      for (class_1657 target : mc.field_1687.method_8390(class_1657.class, area, this::isTrackable)) {
         double distance = target.method_19538().method_1022(tracked.position);
         float factor = (float)Math.max(0.0, 1.0 - distance / 4.0);
         String key = "splash:" + tracked.owner + ":" + target.method_5667() + ":" + this.itemKey(tracked.item);
         if (!this.inRange(target) || distance > 4.0 || factor <= 0.0F || this.duplicate(key)) {
            continue;
         }
         ChatMessage.brandmessage(
            throwerName + " забаффал " + target.method_5477().getString() + " " + tracked.item.method_7964().getString() + " (" + Math.round(factor * 100.0F) + "%)"
         );
         if (this.showEffects.isValue()) {
            this.printEffects(effects, factor);
         }
      }
   }

   private Map<String, class_1293> snapshotEffects(class_1657 player) {
      Map<String, class_1293> result = new HashMap<>();
      for (class_1293 effect : player.method_6026()) {
         class_1291 type = (class_1291)effect.method_5579().comp_349();
         class_2960 id = class_7923.field_41174.method_10221(type);
         if (id != null) {
            result.put(id.toString(), effect);
         }
      }
      return result;
   }

   private List<class_1293> addedEffects(Map<String, class_1293> previous, Map<String, class_1293> current) {
      List<class_1293> result = new ArrayList<>();
      current.forEach((key, effect) -> {
         class_1293 old = previous.get(key);
         if (old == null || effect.method_5578() > old.method_5578() || effect.method_5584() > old.method_5584() + 20) {
            result.add(effect);
         }
      });
      return result;
   }

   private List<class_1293> potionEffects(class_1799 stack) {
      class_1844 contents = (class_1844)stack.method_57824(class_9334.field_49651);
      if (contents == null) {
         return List.of();
      }
      List<class_1293> result = new ArrayList<>();
      contents.method_57397().forEach(result::add);
      return result;
   }

   private void printEffects(List<class_1293> effects, float potency) {
      for (class_1293 effect : effects) {
         class_1291 type = (class_1291)effect.method_5579().comp_349();
         String level = this.roman(effect.method_5578() + 1);
         String duration = type.method_5561() ? "" : " (" + this.formatDuration(Math.round(effect.method_5584() / 20.0F * potency)) + ")";
         ChatMessage.brandmessage("• " + type.method_5560().getString() + " " + level + duration);
      }
   }

   private boolean isTrackable(class_1657 player) {
      return player != null
         && player != mc.field_1724
         && player.method_5805()
         && !player.method_7325()
         && !player.method_5767()
         && !player.method_6059(class_1294.field_5905);
   }

   private boolean inRange(class_1297 entity) {
      float distance = this.radius.getValue();
      return entity != null && mc.field_1724 != null && mc.field_1724.method_5858(entity) <= distance * distance;
   }

   private boolean isFood(class_1799 stack) {
      return stack != null && !stack.method_7960() && stack.method_57824(class_9334.field_50075) != null;
   }

   private boolean isDrinkable(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      }
      class_1792 item = stack.method_7909();
      return item == class_1802.field_8574
         || item == class_1802.field_8103
         || item == class_1802.field_20417
         || stack.method_57824(class_9334.field_49651) != null;
   }

   private class_1657 playerByUuid(UUID id) {
      if (id == null || mc.field_1687 == null) {
         return null;
      }
      return mc.field_1687.method_18456().stream().filter(player -> id.equals(player.method_5667())).findFirst().orElse(null);
   }

   private String itemKey(class_1799 stack) {
      class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
      return id == null ? stack.method_7964().getString() : id.toString();
   }

   private boolean duplicate(String key) {
      long now = System.currentTimeMillis();
      this.recentEvents.entrySet().removeIf(entry -> now - entry.getValue() > 5000L);
      Long previous = this.recentEvents.put(key, now);
      return previous != null && now - previous <= DUPLICATE_WINDOW_MS;
   }

   private String formatDuration(int seconds) {
      return String.format(Locale.ROOT, "%d:%02d", Math.max(0, seconds) / 60, Math.max(0, seconds) % 60);
   }

   private String roman(int number) {
      return switch (number) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         case 5 -> "V";
         default -> Integer.toString(number);
      };
   }

   private void clearState() {
      this.useStates.clear();
      this.lastEffects.clear();
      this.trackedPotions.clear();
      this.recentEvents.clear();
   }

   private static final class UseState {
      private boolean using;
      private int startAge;
      private class_1799 item = class_1799.field_8037;

      private void reset() {
         this.using = false;
         this.startAge = 0;
         this.item = class_1799.field_8037;
      }
   }

   private static final class TrackedPotion {
      private final class_1799 item;
      private class_243 position;
      private final UUID owner;

      private TrackedPotion(class_1799 item, class_243 position, UUID owner) {
         this.item = item;
         this.position = position;
         this.owner = owner;
      }
   }
}
