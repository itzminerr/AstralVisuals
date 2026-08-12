package pl.astralvisuals.features.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import net.minecraft.class_10142;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import net.minecraft.class_5498;
import net.minecraft.class_742;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_4587.class_4665;
import org.joml.Matrix4f;
import pl.astralvisuals.common.repository.friend.FriendUtils;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render3D;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ChinaHat extends Module {
   private final int segments = 48;
   private final BooleanSetting selfSetting = new BooleanSetting("Себя", "Показывать шляпу на себе").setValue(true);
   private final BooleanSetting friendsSetting = new BooleanSetting("Друзья", "Показывать шляпу на друзьях").setValue(true);
   private final BooleanSetting friendColorSetting = new BooleanSetting("Цвет друзей", "Использовать цвет друзей")
      .setValue(true)
      .visible(this.friendsSetting::isValue);
   private final SliderSettings radiusSetting = new SliderSettings("Радиус", "Радиус шляпы").setValue(0.65F).range(0.35F, 1.2F);
   private final SliderSettings heightSetting = new SliderSettings("Высота", "Высота кончика шляпы").setValue(0.3F).range(0.1F, 0.7F);
   private final SliderSettings offsetSetting = new SliderSettings("Смещение Y", "Вертикальное смещение шляпы").setValue(0.08F).range(0.0F, 0.35F);
   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет шляпы").setColor(new Color(40, 40, 44, 210).getRGB());

   public ChinaHat() {
      super("ChinaHat", "China Hat", ModuleCategory.RENDER);
      this.setup(this.selfSetting, this.friendsSetting, this.friendColorSetting, this.radiusSetting, this.heightSetting, this.offsetSetting, this.colorSetting);
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         List<class_742> players = mc.field_1687.method_18456();
         boolean hasPlayers = false;

         for (class_742 player : players) {
            if (this.shouldRenderHat(player)) {
               hasPlayers = true;
               break;
            }
         }

         if (hasPlayers) {
            this.renderHats(e.getStack(), players);
         }
      }
   }

   private boolean shouldRenderHat(class_742 player) {
      if (player == null || player.method_31481() || !player.method_5805() || player.method_7325()) {
         return false;
      } else {
         return player == mc.field_1724
            ? this.selfSetting.isValue() && mc.field_1690.method_31044() != class_5498.field_26664
            : this.friendsSetting.isValue() && FriendUtils.isFriend(player);
      }
   }

   private void renderHats(class_4587 matrices, List<class_742> players) {
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
      RenderSystem.setShader(class_10142.field_53876);

      try {
         for (class_742 player : players) {
            if (this.shouldRenderHat(player)) {
               this.renderSingleHat(matrices, player);
            }
         }
      } finally {
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private void renderSingleHat(class_4587 matrices, class_742 player) {
      class_243 position = Calculate.interpolate(player);
      class_243 center = new class_243(position.field_1352, position.field_1351 + player.method_17682() + this.offsetSetting.getValue(), position.field_1350);
      class_243 tip = center.method_1031(0.0, this.heightSetting.getValue(), 0.0);
      int color = this.resolveColor(player);
      int brimColor = ColorAssist.multAlpha(color, 0.85F);
      int tipColor = ColorAssist.multAlpha(ColorAssist.interpolateColor(color, Color.WHITE.getRGB(), 0.35F), 0.78F);
      int undersideColor = ColorAssist.multAlpha(ColorAssist.multDark(color, 0.65F), 0.22F);
      this.renderFill(matrices.method_23760().method_23761(), center, tip, this.radiusSetting.getValue(), tipColor, brimColor, undersideColor);
      this.renderOutline(matrices.method_23760(), center, this.radiusSetting.getValue(), color);
   }

   private int resolveColor(class_742 player) {
      return player != mc.field_1724 && FriendUtils.isFriend(player) && this.friendColorSetting.isValue()
         ? ColorAssist.setAlpha(ColorAssist.getFriendColor(), ColorAssist.getAlpha(this.colorSetting.getColor()))
         : this.colorSetting.getColor();
   }

   private void renderFill(Matrix4f matrix, class_243 center, class_243 tip, double radius, int tipColor, int brimColor, int undersideColor) {
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27379, class_290.field_1576);
      class_243 undersideCenter = center.method_1031(0.0, -0.01, 0.0);
      int softUndersideColor = ColorAssist.multAlpha(undersideColor, 0.55F);

      for (int i = 0; i < 48; i++) {
         double angle1 = (Math.PI * 2) * i / 48.0;
         double angle2 = (Math.PI * 2) * (i + 1) / 48.0;
         class_243 rimStart = center.method_1031(Math.cos(angle1) * radius, 0.0, Math.sin(angle1) * radius);
         class_243 rimEnd = center.method_1031(Math.cos(angle2) * radius, 0.0, Math.sin(angle2) * radius);
         buffer.method_22918(matrix, (float)tip.field_1352, (float)tip.field_1351, (float)tip.field_1350).method_39415(tipColor);
         buffer.method_22918(matrix, (float)rimStart.field_1352, (float)rimStart.field_1351, (float)rimStart.field_1350).method_39415(brimColor);
         buffer.method_22918(matrix, (float)rimEnd.field_1352, (float)rimEnd.field_1351, (float)rimEnd.field_1350).method_39415(brimColor);
         buffer.method_22918(matrix, (float)undersideCenter.field_1352, (float)undersideCenter.field_1351, (float)undersideCenter.field_1350)
            .method_39415(undersideColor);
         buffer.method_22918(matrix, (float)rimEnd.field_1352, (float)rimEnd.field_1351, (float)rimEnd.field_1350).method_39415(softUndersideColor);
         buffer.method_22918(matrix, (float)rimStart.field_1352, (float)rimStart.field_1351, (float)rimStart.field_1350).method_39415(softUndersideColor);
      }

      class_286.method_43433(buffer.method_60800());
   }

   private void renderOutline(class_4665 entry, class_243 center, double radius, int color) {
      int rimColor = ColorAssist.multAlpha(color, 0.95F);

      for (int i = 0; i < 48; i++) {
         double angle1 = (Math.PI * 2) * i / 48.0;
         double angle2 = (Math.PI * 2) * (i + 1) / 48.0;
         class_243 rimStart = center.method_1031(Math.cos(angle1) * radius, 0.0, Math.sin(angle1) * radius);
         class_243 rimEnd = center.method_1031(Math.cos(angle2) * radius, 0.0, Math.sin(angle2) * radius);
         Render3D.drawLine(entry, rimStart, rimEnd, rimColor, rimColor, 1.6F, true);
      }
   }
}
