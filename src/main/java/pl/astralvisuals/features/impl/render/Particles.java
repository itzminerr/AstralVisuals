package pl.astralvisuals.features.impl.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1309;
import pl.astralvisuals.events.player.AttackEvent;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.impl.render.particles.ParticleRender;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.math.calc.Calculate;

// Порт модуля Particles из EvaWare: частицы в мире (падающие вокруг игрока) и при ударе (разлёт от цели).
public class Particles extends Module {
   private final MultiSelectSetting spawn = new MultiSelectSetting("Спавн", "Где появляются частицы")
      .value("Мир", "Удар")
      .selected("Мир");
   private final SelectSetting texture = new SelectSetting("Текстура", "Текстура частицы")
      .value("Spark", "Star", "Dollar", "Glow", "Ball");
   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет частиц")
      .setColor(-7722014)
      .presets(-1, -65536, -23178, -16711936, -16776961, -7722014);
   private final SliderSettings count = new SliderSettings("Количество", "Число частиц").setValue(25.0F).range(10, 100);
   private final SliderSettings sizeSetting = new SliderSettings("Размер", "Размер частиц").setValue(0.2F).range(0.1F, 1.0F);
   private final SliderSettings lifeTime = new SliderSettings("Время жизни", "Время жизни в тиках").setValue(10.0F).range(2, 100);
   private final SliderSettings spawnDuration = new SliderSettings("Появление", "Длительность появления").setValue(15.0F).range(0, 40);
   private final SliderSettings dyingDuration = new SliderSettings("Затухание", "Длительность затухания").setValue(15.0F).range(0, 40);
   private final BooleanSetting rotate = new BooleanSetting("Вращение", "Вращать частицы").setValue(true);
   private final BooleanSetting trail = new BooleanSetting("След", "Рисовать след за частицей").setValue(false);
   private final SliderSettings trailLength = new SliderSettings("Длина следа", "Длина следа").setValue(5.0F).range(1, 20).visible(this.trail::isValue);
   private final SliderSettings distance = new SliderSettings("Радиус (мир)", "Радиус спавна вокруг игрока").setValue(15.0F).range(5, 50).visible(() -> this.spawn.isSelected("Мир"));
   private final SliderSettings height = new SliderSettings("Высота (мир)", "Высота спавна над игроком").setValue(8.0F).range(5, 15).visible(() -> this.spawn.isSelected("Мир"));
   private final SliderSettings gravity = new SliderSettings("Гравитация (мир)", "Сила падения").setValue(0.3F).range(0.1F, 1.0F).visible(() -> this.spawn.isSelected("Мир"));
   private final BooleanSetting hitDrop = new BooleanSetting("Падение (удар)", "Частицы удара падают вниз").setValue(true).visible(() -> this.spawn.isSelected("Удар"));
   private final SliderSettings hitMotion = new SliderSettings("Сила (удар)", "Сила разлёта при ударе").setValue(0.3F).range(0.1F, 1.0F).visible(() -> this.spawn.isSelected("Удар"));

   private final List<ParticleRender> worldParticles = new ArrayList<>();
   private final List<ParticleRender> hitParticles = new ArrayList<>();

   public static Particles getInstance() {
      return Instance.get(Particles.class);
   }

   public Particles() {
      super("Particles", "Particles", ModuleCategory.RENDER);
      this.setup(
         this.spawn,
         this.texture,
         this.colorSetting,
         this.count,
         this.sizeSetting,
         this.lifeTime,
         this.spawnDuration,
         this.dyingDuration,
         this.rotate,
         this.trail,
         this.trailLength,
         this.distance,
         this.height,
         this.gravity,
         this.hitDrop,
         this.hitMotion
      );
   }

   @Override
   public void deactivate() {
      this.worldParticles.clear();
      this.hitParticles.clear();
   }

   @EventHandler
   public void onTick(TickEvent e) {
      if (mc.field_1724 == null || mc.field_1687 == null) {
         return;
      }

      this.worldParticles.removeIf(ParticleRender::update);
      this.hitParticles.removeIf(ParticleRender::update);

      if (this.spawn.isSelected("Мир")) {
         int target = this.count.getInt();
         float d = this.distance.getValue();
         int toSpawn = target - this.worldParticles.size();

         for (int i = 0; i < toSpawn; i++) {
            ParticleRender p = new ParticleRender(
               (float)(mc.field_1724.method_23317() + Calculate.getRandom(-d, d)),
               (float)(mc.field_1724.method_23318() + this.height.getValue()),
               (float)(mc.field_1724.method_23321() + Calculate.getRandom(-d, d)),
               this.lifeTime.getInt(),
               this.spawnDuration.getInt(),
               this.dyingDuration.getInt()
            );
            p.gravityFalls = true;
            p.dropPhysics = true;
            p.motionY = -Calculate.getRandom(this.gravity.getValue() * 0.1F, this.gravity.getValue());
            this.configure(p);
            this.worldParticles.add(p);
         }
      } else if (!this.worldParticles.isEmpty()) {
         this.worldParticles.clear();
      }
   }

   @EventHandler
   public void onAttack(AttackEvent e) {
      if (!this.spawn.isSelected("Удар") || !(e.getEntity() instanceof class_1309 entity)) {
         return;
      }

      float m = this.hitMotion.getValue();
      int n = this.count.getInt();

      for (int i = 0; i < n; i++) {
         ParticleRender p = new ParticleRender(
            (float)(entity.method_23317() + Calculate.getRandom(-0.1F, 0.1F)),
            (float)(entity.method_23318() + entity.method_17682() / 2.0F + Calculate.getRandom(-0.1F, 0.1F)),
            (float)(entity.method_23321() + Calculate.getRandom(-0.1F, 0.1F)),
            this.lifeTime.getInt(),
            this.spawnDuration.getInt(),
            this.dyingDuration.getInt()
         );
         p.gravityFalls = false;
         p.dropPhysics = this.hitDrop.isValue();
         p.motionX = Calculate.getRandom(-m, m);
         p.motionY = Calculate.getRandom(-m, m);
         p.motionZ = Calculate.getRandom(-m, m);
         this.configure(p);
         this.hitParticles.add(p);
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent e) {
      for (ParticleRender p : this.worldParticles) {
         p.render();
      }

      for (ParticleRender p : this.hitParticles) {
         p.render();
      }
   }

   private void configure(ParticleRender p) {
      p.size = this.sizeSetting.getValue();
      p.identifier = ParticleRender.getTexture(this.texture.getSelected());
      p.rotating = this.rotate.isValue();
      p.trail = this.trail.isValue();
      p.trailLength = this.trailLength.getInt();
      p.baseColor = this.colorSetting.getColor();
   }
}
