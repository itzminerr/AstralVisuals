package pl.astralvisuals.features.impl.movement;

import java.io.BufferedInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_490;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public final class EggMan extends Module {
   private final BooleanSetting flexMusic = new BooleanSetting("Флекс-музыка", "Проигрывать музыку во время покачивания").setValue(true);
   private final SliderSettings musicVolume = new SliderSettings("Громкость музыки", "Громкость фоновой музыки").setValue(0.25F).range(0.0F, 1.0F)
      .visible(this.flexMusic::isValue);
   private Clip musicClip;
   private float previousVolume = -1.0F;
   private long nextLoadAttempt;

   public EggMan() {
      super("EggMan", "EggMan", ModuleCategory.RENDER);
      this.setup(this.flexMusic, this.musicVolume);
   }

   public static EggMan getInstance() {
      return Instance.get(EggMan.class);
   }

   @EventHandler
   public void onTick(TickEvent event) {
      this.updateMusic();
   }

   @Override
   public void activate() {
      this.updateMusic();
   }

   @Override
   public void deactivate() {
      this.stopMusic();
   }

   public boolean shouldWobble(class_1309 entity) {
      if (!this.isState() || !(entity instanceof class_1657)) {
         return false;
      }
      return entity != mc.field_1724 || !(mc.field_1755 instanceof class_490);
   }

   public void applyWobble(class_1309 entity, class_4587 matrices) {
      long now = System.currentTimeMillis() + entity.method_5628() * 100L;
      float wobble = (float)(now % 400L) / 400.0F;
      wobble = (wobble > 0.5F ? 1.0F - wobble : wobble) * 2.0F;
      wobble = class_3532.method_15363(wobble, 0.0F, 1.0F);
      matrices.method_22905(wobble * 2.0F + 1.0F, 1.0F - 0.5F * wobble, wobble * 2.0F + 1.0F);
   }

   private void updateMusic() {
      if (!this.isState() || !this.flexMusic.isValue()) {
         this.stopMusic();
         return;
      }
      Clip clip = this.ensureLoaded();
      if (clip == null) {
         return;
      }
      this.applyVolume(clip);
      if (!clip.isRunning()) {
         clip.loop(Clip.LOOP_CONTINUOUSLY);
         clip.start();
      }
   }

   private Clip ensureLoaded() {
      if (this.musicClip != null && this.musicClip.isOpen()) {
         return this.musicClip;
      }
      long now = System.currentTimeMillis();
      if (now < this.nextLoadAttempt) {
         return null;
      }
      this.nextLoadAttempt = now + 3000L;
      try (var input = EggMan.class.getResourceAsStream("/assets/minecraft/eggman.wav")) {
         if (input == null) {
            return null;
         }
         try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(input))) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            this.musicClip = clip;
            this.previousVolume = -1.0F;
            return clip;
         }
      } catch (Exception ignored) {
         return null;
      }
   }

   private void applyVolume(Clip clip) {
      float volume = this.musicVolume.getValue();
      if (Math.abs(volume - this.previousVolume) < 0.0001F || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
         return;
      }
      this.previousVolume = volume;
      FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
      float decibels = volume <= 0.0F ? control.getMinimum() : (float)(20.0 * Math.log10(Math.min(1.0F, volume)));
      control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), decibels)));
   }

   private void stopMusic() {
      if (this.musicClip != null) {
         try {
            this.musicClip.stop();
            this.musicClip.close();
         } catch (Exception ignored) {
         }
         this.musicClip = null;
         this.previousVolume = -1.0F;
      }
   }
}
