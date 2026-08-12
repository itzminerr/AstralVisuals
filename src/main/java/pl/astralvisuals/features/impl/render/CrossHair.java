package pl.astralvisuals.features.impl.render;

import net.minecraft.class_3966;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.math.calc.Calculate;

public class CrossHair extends Module {
   private float red = 0.0F;
   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет прицела").setColor(-1).presets(-1, -9659651, -7569409, -23178, -33925);
   private final SliderSettings attackSetting = new SliderSettings("Отступ атаки", "Отступ для перезарядки атаки").setValue(10.0F).range(0, 20);
   private final SliderSettings indentSetting = new SliderSettings("Отступ", "Расстояние от центра экрана").setValue(0.0F).range(0, 5);
   private final SliderSettings size1Setting = new SliderSettings("Ширина", "Ширина прицела").setValue(4.0F).range(1, 10);
   private final SliderSettings size2Setting = new SliderSettings("Высота", "Высота прицела").setValue(1.0F).range(1, 4);

   public static CrossHair getInstance() {
      return Instance.get(CrossHair.class);
   }

   public CrossHair() {
      super("CrossHair", "Crosshair", ModuleCategory.RENDER);
      this.setup(this.colorSetting, this.attackSetting, this.indentSetting, this.size1Setting, this.size2Setting);
   }

   public void onRenderCrossHair() {
      this.red = Calculate.interpolateSmooth(2.0, this.red, mc.field_1765 instanceof class_3966 ? 5.0F : 1.0F);
      int firstColor = ColorAssist.multRed(this.colorSetting.getColor(), this.red);
      int secondColor = ColorAssist.BLACK;
      float x = window.method_4486() / 2.0F;
      float y = window.method_4502() / 2.0F;
      float cooldown = this.attackSetting.getInt() - this.attackSetting.getInt() * mc.field_1724.method_7261(tickCounter.method_60637(false));
      float size = this.size1Setting.getValue();
      float size2 = this.size2Setting.getValue();
      float offset = size2 / 2.0F;
      float indent = this.indentSetting.getInt() + cooldown;
      this.renderMain(x, y, size, size2, 1.0F, indent, offset, secondColor);
      this.renderMain(x, y, size, size2, 0.0F, indent, offset, firstColor);
   }

   private void renderMain(float x, float y, float size, float size2, float padding, float indent, float offset, int color) {
      Render2D.drawQuad(x - offset - padding / 2.0F, y - size - indent - padding / 2.0F, size2 + padding, size + padding, color);
      Render2D.drawQuad(x - offset - padding / 2.0F, y + indent - padding / 2.0F, size2 + padding, size + padding, color);
      Render2D.drawQuad(x - size - indent - padding / 2.0F, y - offset - padding / 2.0F, size + padding, size2 + padding, color);
      Render2D.drawQuad(x + indent - padding / 2.0F, y - offset - padding / 2.0F, size + padding, size2 + padding, color);
   }
}
