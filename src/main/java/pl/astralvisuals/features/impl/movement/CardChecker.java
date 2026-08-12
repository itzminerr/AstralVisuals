package pl.astralvisuals.features.impl.movement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10142;
import net.minecraft.class_1688;
import net.minecraft.class_1694;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import net.minecraft.class_7833;
import java.awt.Color;
import org.joml.Matrix4f;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.common.repository.way.WayRepository;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.events.render.DrawEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.chat.ChatMessage;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.math.calc.Calculate;

public class CardChecker extends Module {
   private final Set<Integer> announced = new HashSet<>();

   // Стрелки-указатели на вагонетки с сундуком вокруг прицела (порт Player Arrows).
   private final class_2960 iconId = class_2960.method_60654("textures/features/arrows/arrow.png");
   private final Animation radiusAnim = new Decelerate().setMs(150).setValue(6.0);
   private final BooleanSetting arrowsSetting = new BooleanSetting("Стрелки", "Указатели на вагонетки с сундуком вокруг прицела").setValue(true);
   private final ColorSetting arrowColor = new ColorSetting("Цвет стрелок", "Цвет указателей")
      .setColor(new Color(255, 215, 90).getRGB())
      .presets(new Color(255, 215, 90).getRGB(), -1, -65536, -16711936, -16776961, -23178)
      .visible(this.arrowsSetting::isValue);
   private final SliderSettings radiusSetting = new SliderSettings("Радиус", "Радиус стрелок")
      .setValue(50.0F).range(30.0F, 100.0F).visible(this.arrowsSetting::isValue);
   private final SliderSettings sizeSetting = new SliderSettings("Размер", "Размер стрелок")
      .setValue(10.0F).range(8.0F, 20.0F).visible(this.arrowsSetting::isValue);
   private final SliderSettings opacitySetting = new SliderSettings("Прозрачность", "Непрозрачность стрелок")
      .setValue(1.0F).range(0.1F, 1.0F).visible(this.arrowsSetting::isValue);

   private final BooleanSetting autoMarkSetting = new BooleanSetting("Ставить метку автоматически", "Создавать вейпоинт при находке вагонетки с сундуком").setValue(false);

   public static CardChecker getInstance() {
      return Instance.get(CardChecker.class);
   }

   public CardChecker() {
      super("CardChecker", "Card Checker", ModuleCategory.PLAYER);
      this.setup(this.arrowsSetting, this.arrowColor, this.radiusSetting, this.sizeSetting, this.opacitySetting, this.autoMarkSetting);
   }

   @Override
   public void activate() {
      super.activate();
      this.announced.clear();
   }

   @Override
   public void deactivate() {
      super.deactivate();
      this.announced.clear();
   }

   @EventHandler
   public void onTick(TickEvent e) {
      if (mc.field_1687 == null || mc.field_1724 == null) {
         return;
      }

      this.radiusAnim.setDirection(mc.field_1724.method_5624() ? Direction.FORWARDS : Direction.BACKWARDS);

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1688 cart && this.announced.add(cart.method_5628())) {
            class_2338 pos = cart.method_24515();
            ChatMessage.brandmessage(
               "Найдена вагонетка: X " + pos.method_10263() + ", Y " + pos.method_10264() + ", Z " + pos.method_10260()
            );

            // Авто-метка: ставим вейпоинт на вагонетку с сундуком.
            if (this.autoMarkSetting.isValue() && cart instanceof class_1694) {
               this.placeWaypoint(pos);
            }
         }
      }
   }

   private void placeWaypoint(class_2338 pos) {
      Force force = Force.getInstance();
      if (force == null || force.getWayRepository() == null) {
         return;
      }

      WayRepository repo = force.getWayRepository();
      String name = "Вагонетка " + pos.method_10263() + ", " + pos.method_10260();
      if (!repo.hasWay(name)) {
         repo.addWay(name, pos, repo.getCurrentServerKey());
      }
   }

   @EventHandler
   public void onDraw(DrawEvent e) {
      if (!this.arrowsSetting.isValue()
         || mc.field_1724 == null
         || mc.field_1687 == null
         || mc.field_1690.field_1842
         || !mc.field_1690.method_31044().equals(class_5498.field_26664)) {
         return;
      }

      List<class_1297> carts = new java.util.ArrayList<>();
      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1694 cart && cart.method_5805()) {
            carts.add(cart);
         }
      }

      if (carts.isEmpty()) {
         return;
      }

      class_4587 matrix = e.getDrawContext().method_51448();
      float middleW = mc.method_22683().method_4486() / 2.0F;
      float middleH = mc.method_22683().method_4502() / 2.0F;
      float posY = middleH - this.radiusSetting.getValue() - this.radiusAnim.getOutput().floatValue();
      float size = this.sizeSetting.getValue();
      int color = ColorAssist.multAlpha(this.arrowColor.getColor(), this.opacitySetting.getValue());
      int shadowColor = ColorAssist.multAlpha(ColorAssist.multDark(color, 0.4F), 0.5F);

      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_CONSTANT_ALPHA);
      RenderSystem.setShaderTexture(0, this.iconId);
      RenderSystem.setShader(class_10142.field_53880);
      class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);

      for (class_1297 cart : carts) {
         float yaw = this.getRotations(cart) - mc.field_1724.method_36454();
         matrix.method_22903();
         matrix.method_46416(middleW, middleH, 0.0F);
         matrix.method_22907(class_7833.field_40718.rotationDegrees(yaw));
         matrix.method_46416(-middleW, -middleH, 0.0F);
         Matrix4f matrix4f = matrix.method_23760().method_23761();
         buffer.method_22918(matrix4f, middleW - size / 2.0F, posY + size, 0.0F).method_22913(0.0F, 1.0F).method_39415(shadowColor);
         buffer.method_22918(matrix4f, middleW + size / 2.0F, posY + size, 0.0F).method_22913(1.0F, 1.0F).method_39415(shadowColor);
         buffer.method_22918(matrix4f, middleW + size / 2.0F, posY, 0.0F).method_22913(1.0F, 0.0F).method_39415(color);
         buffer.method_22918(matrix4f, middleW - size / 2.0F, posY, 0.0F).method_22913(0.0F, 0.0F).method_39415(color);
         matrix.method_22909();
      }

      class_286.method_43433(buffer.method_60800());
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }

   private float getRotations(class_1297 ent) {
      double x = Calculate.interpolate(ent.field_6014, ent.method_23317()) - Calculate.interpolate(mc.field_1724.field_6014, mc.field_1724.method_23317());
      double z = Calculate.interpolate(ent.field_5969, ent.method_23321()) - Calculate.interpolate(mc.field_1724.field_5969, mc.field_1724.method_23321());
      return (float) (-(Math.atan2(x, z) * (180.0 / Math.PI)));
   }
}
