package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import pl.astralvisuals.events.render.DrawEvent;
import pl.astralvisuals.features.impl.render.handshader.HandShaderRenderer;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

/** Post-process шейдер для рук и предметов в первом лице. */
public class HandShader extends Module {
   public static final String GLOW = "Свечение";
   public static final String PRETTY = "Красивый";

   private final SelectSetting mode = new SelectSetting("Режим", "Тип шейдера рук")
      .value(GLOW, PRETTY)
      .selected(GLOW);
   private final ColorSetting primaryColor = new ColorSetting("Цвет", "Основной цвет эффекта")
      .setColor(new Color(125, 90, 255).getRGB());
   private final ColorSetting secondaryColor = new ColorSetting("Второй цвет", "Второй цвет волн")
      .setColor(new Color(70, 210, 255).getRGB())
      .visible(() -> this.mode.isSelected(PRETTY));
   private final SliderSettings waveSpeed = new SliderSettings("Скорость волн", "Скорость движения цвета")
      .range(0.1F, 5.0F)
      .setValue(1.2F)
      .visible(() -> this.mode.isSelected(PRETTY));
   private final SliderSettings waveScale = new SliderSettings("Масштаб волн", "Частота цветных волн")
      .range(1.0F, 3.0F)
      .setValue(1.0F)
      .visible(() -> this.mode.isSelected(PRETTY));
   private final SliderSettings outline = new SliderSettings("Обводка", "Толщина обводки")
      .range(0.0F, 5.0F)
      .setValue(1.2F);
   private final SliderSettings glow = new SliderSettings("Свечение", "Радиус и сила свечения")
      .range(0.0F, 5.0F)
      .setValue(1.0F);
   private final SliderSettings fill = new SliderSettings("Заливка", "Плотность цвета внутри маски")
      .range(0.0F, 1.0F)
      .setValue(0.6F);
   private final SliderSettings alpha = new SliderSettings("Прозрачность", "Общая непрозрачность эффекта")
      .range(0.0F, 1.0F)
      .setValue(1.0F);

   public HandShader() {
      super("HandShader", "Hand Shader", ModuleCategory.RENDER);
      this.setup(this.mode, this.primaryColor, this.secondaryColor, this.waveSpeed, this.waveScale, this.outline, this.glow, this.fill, this.alpha);
   }

   public static HandShader getInstance() {
      return Instance.get(HandShader.class);
   }

   @Override
   public void deactivate() {
      HandShaderRenderer.INSTANCE.invalidateState();
   }

   @EventHandler
   public void onDraw(DrawEvent event) {
      HandShaderRenderer.INSTANCE.renderOverlayIfPending(this);
   }

   public boolean isEffectEnabled() {
      return this.isState()
         && this.alpha.getValue() > 0.001F
         && (this.fill.getValue() > 0.001F || this.outline.getValue() > 0.001F || this.glow.getValue() > 0.001F);
   }

   public boolean isPrettyMode() {
      return this.mode.isSelected(PRETTY);
   }

   public int getPrimaryColor() {
      return this.primaryColor.getColor();
   }

   public int getSecondaryColor() {
      return this.secondaryColor.getColor();
   }

   public float getWaveSpeed() {
      return this.waveSpeed.getValue();
   }

   public float getWaveScale() {
      return this.waveScale.getValue();
   }

   public float getOutline() {
      return this.outline.getValue();
   }

   public float getGlow() {
      return this.glow.getValue();
   }

   public float getFill() {
      return this.fill.getValue();
   }

   public float getAlpha() {
      return this.alpha.getValue();
   }
}
