package pl.astralvisuals.features.impl.movement;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import pl.astralvisuals.events.player.AttackEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.sound.SoundManager;

public final class HitSound extends Module {
   private static final Map<String, String> SOUNDS = new LinkedHashMap<>();

   static {
      SOUNDS.put("Абмисс", "abmiss.ogg");
      SOUNDS.put("Дзиньк", "bell.wav");
      SOUNDS.put("Сахууур", "bonk.wav");
      SOUNDS.put("Барабулька", "bubble.wav");
      SOUNDS.put("Клик мыши", "click1.wav");
      SOUNDS.put("Пульк", "click3.wav");
      SOUNDS.put("Криты V1", "hit1.wav");
      SOUNDS.put("Хруст", "hit2.wav");
      SOUNDS.put("Криты", "hit3.wav");
      SOUNDS.put("Стоны", "moan1.wav");
      SOUNDS.put("Стоны v3", "moan2.wav");
      SOUNDS.put("Стоны v2", "moan3.wav");
      SOUNDS.put("Стоны v4", "moan4.wav");
      SOUNDS.put("Польк", "pop.wav");
      SOUNDS.put("Ювю", "uwu.wav");
   }

   private final SelectSetting sound = new SelectSetting("Звук", "Звук попадания")
      .value(SOUNDS.keySet().toArray(String[]::new))
      .selected("Криты");
   private final SliderSettings volume = new SliderSettings("Громкость", "Громкость звука попадания").setValue(1.0F).range(0.0F, 1.0F);
   private final BooleanSetting criticalOnly = new BooleanSetting("Только при крите", "Проигрывать звук только для критических ударов").setValue(false);

   public HitSound() {
      super("HitSound", "Hit Sound", ModuleCategory.PLAYER);
      this.setup(this.sound, this.volume, this.criticalOnly);
   }

   public static HitSound getInstance() {
      return Instance.get(HitSound.class);
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (!(event.getEntity() instanceof class_1309) || mc.field_1724 == null || mc.field_1687 == null) {
         return;
      }
      if (this.criticalOnly.isValue() && !this.isCriticalHit()) {
         return;
      }
      String file = SOUNDS.get(this.sound.getSelected());
      if (file != null) {
         SoundManager.playResource(class_2960.method_60654("minecraft:hitsounds/" + file), this.volume.getValue());
      }
   }

   public boolean shouldSuppressDefaults() {
      return this.isState();
   }

   public static boolean isAttackSound(class_3414 sound) {
      return sound == class_3417.field_14840
         || sound == class_3417.field_14625
         || sound == class_3417.field_15016
         || sound == class_3417.field_14706
         || sound == class_3417.field_14999
         || sound == class_3417.field_14914;
   }

   private boolean isCriticalHit() {
      return mc.field_1724.method_7261(0.5F) > 0.9F
         && !mc.field_1724.method_24828()
         && !mc.field_1724.method_6101()
         && !mc.field_1724.method_5799()
         && !mc.field_1724.method_5869()
         && !mc.field_1724.method_5771()
         && !mc.field_1724.method_5765()
         && !mc.field_1724.method_5624()
         && !mc.field_1724.method_31549().field_7479
         && !mc.field_1724.method_6059(class_1294.field_5919)
         && mc.field_1724.field_6017 > 0.0F;
   }
}
