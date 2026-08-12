package pl.astralvisuals.features.module;

import java.util.ArrayList;
import java.util.List;

import pl.astralvisuals.features.impl.movement.AutoDuel;
import pl.astralvisuals.features.impl.movement.AutoRespawn;
import pl.astralvisuals.features.impl.movement.AutoSprint;
import pl.astralvisuals.features.impl.movement.CardChecker;
import pl.astralvisuals.features.impl.movement.ClickPearl;
import pl.astralvisuals.features.impl.movement.CrystalOptimizer;
import pl.astralvisuals.features.impl.movement.Cooldowns;
import pl.astralvisuals.features.impl.movement.DiscordPresence;
import pl.astralvisuals.features.impl.movement.ItemScroller;
import pl.astralvisuals.features.impl.movement.ItemSwap;
import pl.astralvisuals.features.impl.movement.LockSlot;
import pl.astralvisuals.features.impl.movement.TapeMouse;
import pl.astralvisuals.features.impl.player.FakePlayer;
import pl.astralvisuals.features.impl.player.FreeLook;
import pl.astralvisuals.features.impl.render.AspectRatio;
import pl.astralvisuals.features.impl.render.BetterMinecraft;
import pl.astralvisuals.features.impl.render.BlockOverlay;
import pl.astralvisuals.features.impl.render.Camera;
import pl.astralvisuals.features.impl.render.ChinaHat;
import pl.astralvisuals.features.impl.render.ClientColor;
import pl.astralvisuals.features.impl.render.Cosmetic;
import pl.astralvisuals.features.impl.render.CrossHair;
import pl.astralvisuals.features.impl.render.CustomHitbox;
import pl.astralvisuals.features.impl.render.HitColor;
import pl.astralvisuals.features.impl.render.HitEffect;
import pl.astralvisuals.features.impl.render.HandShader;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.features.impl.render.JumpCircle;
import pl.astralvisuals.features.impl.render.KillEffect;
import pl.astralvisuals.features.impl.render.MotionBlur;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.features.impl.render.Particles;
import pl.astralvisuals.features.impl.render.Predictions;
import pl.astralvisuals.features.impl.render.SelfNametag;
import pl.astralvisuals.features.impl.render.SkyShader;
import pl.astralvisuals.features.impl.render.SwingAnimation;
import pl.astralvisuals.features.impl.render.TargetESP;
import pl.astralvisuals.features.impl.render.ViewModel;
import pl.astralvisuals.features.impl.render.WaypointESP;
import pl.astralvisuals.features.impl.render.WorldTweaks;

public class ModuleRepository {
   private final List<Module> modules = new ArrayList<>();

   public void setup() {
      this.register(
         new AutoSprint(),
         new CardChecker(),
         new ClickPearl(),
         new CrystalOptimizer(),
         new Cooldowns(),
         new DiscordPresence(),
         new ItemScroller(),
         new FreeLook(),
         new FakePlayer(),
         new JumpCircle(),
         new BetterMinecraft(),
         new AspectRatio(),
         new Interface(),
         new Particles(),
         new MotionBlur(),
         new SkyShader(),
         new Camera(),
         new SwingAnimation(),
         new ViewModel(),
         new BlockOverlay(),
         new WorldTweaks(),
         new NoRender(),
         new ChinaHat(),
         new TargetESP(),
         new CrossHair(),
         new CustomHitbox(),
         new WaypointESP(),
         new HitColor(),
         new HitEffect(),
         new HandShader(),
         new KillEffect(),
         new Cosmetic(),
         new ClientColor(),
         new AutoRespawn(),
         new AutoDuel(),
         new LockSlot(),
         new TapeMouse(),
         new ItemSwap(),
         new Predictions(),
         new SelfNametag()
      );
   }

   public void register(Module... module) {
      this.modules.addAll(List.of(module));
   }

   public List<Module> modules() {
      return this.modules;
   }
}
