package pl.astralvisuals.features.impl.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.class_243;
import net.minecraft.class_437;
import net.minecraft.class_476;
import net.minecraft.class_640;
import net.minecraft.class_1713;
import pl.astralvisuals.events.chat.ChatEvent;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

// Порт AutoDuel из Nexgen: рассылает /duel игрокам из таб-листа, выбирает набор в GUI,
// останавливается при старте поединка. class_476 = GenericContainerScreen, class_640 = PlayerListEntry.
public class AutoDuel extends Module {
   private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
   private static final String[] KITS = {"Щит", "Шипы", "Лук", "Тотемы", "Нодебафф", "Шары", "Классик", "Читер", "Незер"};

   private final SelectSetting mode = new SelectSetting("Режим", "Набор для поединка").value(KITS).selected("Шары");

   private final List<String> sent = new ArrayList<>();
   private long duelTimer;
   private long clrTimer;
   private long pickTimer;
   private long setTimer;
   private class_243 lastPos;
   private boolean inDuel;
   private boolean guiWasOpen;
   private int guiOpens;

   public static AutoDuel getInstance() {
      return Instance.get(AutoDuel.class);
   }

   public AutoDuel() {
      super("AutoDuel", "Auto Duel", ModuleCategory.PLAYER);
      this.setup(this.mode);
   }

   @Override
   public void activate() {
      this.sent.clear();
      this.inDuel = false;
      this.guiWasOpen = false;
      this.guiOpens = 0;
      if (mc.field_1724 != null) {
         this.lastPos = mc.field_1724.method_19538();
      }

      this.duelTimer = this.clrTimer = System.currentTimeMillis();
   }

   private int kitSlot() {
      for (int i = 0; i < KITS.length; i++) {
         if (this.mode.isSelected(KITS[i])) {
            return i;
         }
      }

      return 5;
   }

   @EventHandler
   public void onTick(TickEvent e) {
      if (mc.field_1724 == null || mc.field_1687 == null || this.inDuel) {
         return;
      }

      // Защита от телепортов/смены мира.
      if (this.lastPos != null && mc.field_1724.method_19538().method_1022(this.lastPos) > 500.0) {
         this.setState(false);
         return;
      }

      this.lastPos = mc.field_1724.method_19538();

      if (System.currentTimeMillis() - this.clrTimer > 30000L) {
         this.sent.clear();
         this.clrTimer = System.currentTimeMillis();
      }

      if (System.currentTimeMillis() - this.duelTimer > 1000L) {
         this.sendDuel();
         this.duelTimer = System.currentTimeMillis();
      }

      this.handleGui();
   }

   @EventHandler
   public void onChat(ChatEvent e) {
      if (mc.field_1724 == null) {
         return;
      }

      String msg = e.getMessage().toLowerCase();
      if ((msg.contains("начало") && msg.contains("через") && msg.contains("секунд"))
            || msg.contains("поединок начался")
            || msg.contains("во время поединка")) {
         this.inDuel = true;
         this.setState(false);
      }
   }

   private void sendDuel() {
      String self = mc.field_1724.method_7334().getName();
      for (String player : this.getPlayers()) {
         if (!this.sent.contains(player) && !player.equals(self)) {
            mc.field_1724.field_3944.method_45730("duel " + player);
            this.sent.add(player);
            break;
         }
      }
   }

   private void handleGui() {
      if (!(mc.field_1755 instanceof class_476 screen)) {
         this.guiWasOpen = false;
         return;
      }

      // Считаем открытия GUI: при повторном открытии выключаем модуль.
      if (!this.guiWasOpen) {
         this.guiWasOpen = true;
         if (++this.guiOpens >= 2) {
            this.setState(false);
            return;
         }
      }

      int id = screen.method_17577().field_7763;
      String title = ((class_437)screen).method_25440().getString();

      if (title.contains("Выбор набора") && System.currentTimeMillis() - this.pickTimer > 150L) {
         mc.field_1761.method_2906(id, this.kitSlot(), 0, class_1713.field_7794, mc.field_1724);
         this.pickTimer = System.currentTimeMillis();
      } else if (title.contains("Настройка поединка") && System.currentTimeMillis() - this.setTimer > 150L) {
         mc.field_1761.method_2906(id, 0, 0, class_1713.field_7794, mc.field_1724);
         this.setTimer = System.currentTimeMillis();
      }
   }

   private List<String> getPlayers() {
      List<String> list = new ArrayList<>();
      if (mc.field_1724 == null || mc.field_1724.field_3944 == null) {
         return list;
      }

      for (class_640 entry : mc.field_1724.field_3944.method_2880()) {
         String name = entry.method_2966().getName();
         if (name != null && NAME_PATTERN.matcher(name).matches()) {
            list.add(name);
         }
      }

      return list;
   }

   @Override
   public void deactivate() {
      this.sent.clear();
      this.inDuel = false;
   }
}
