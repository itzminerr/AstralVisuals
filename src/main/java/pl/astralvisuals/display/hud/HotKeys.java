package pl.astralvisuals.display.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.utils.client.chat.StringHelper;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;
import pl.astralvisuals.utils.math.calc.Calculate;

public class HotKeys extends AbstractDraggable {
   private static final List<String> PREVIEW_KEYS = List.of("A", "B", "C", "D", "E");
   private final List<Module> keysList = new ArrayList<>();
   private final Random random = new Random();
   private long lastKeyChange = 0L;
   private String currentRandomKey = "NONE";

   public HotKeys() {
      super("Горячие клавиши", 300, 40, 80, 23, true);
   }

   @Override
   public boolean visible() {
      return Interface.getInstance().interfaceSettings.isSelected("Горячие клавиши") && (!this.keysList.isEmpty() || PlayerInteractionHelper.isChat(mc.field_1755));
   }

   @Override
   public void tick() {
      this.keysList.clear();

      for (Module module : Force.getInstance().getModuleProvider().getModules()) {
         if (module.getAnimation().getOutput().floatValue() != 0.0F && module.getKey() != -1) {
            this.keysList.add(module);
         }
      }

      if (this.keysList.isEmpty() && PlayerInteractionHelper.isChat(mc.field_1755)) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastKeyChange >= 1000L) {
            this.currentRandomKey = PREVIEW_KEYS.get(this.random.nextInt(PREVIEW_KEYS.size()));
            this.lastKeyChange = currentTime;
         }
      }
   }

   @Override
   public void drawDraggable(class_332 context) {
      class_4587 matrix = context.method_51448();
      FontRenderer font = Fonts.getSize(13, Fonts.Type.DEFAULT);
      FontRenderer fontModule = Fonts.getSize(13, Fonts.Type.DEFAULT);
      FontRenderer icon = Fonts.getSize(23, Fonts.Type.ICONS);
      GlassStyle.backdrop(matrix, this.getX(), this.getY(), this.getWidth(), 15.5F, new Vector4f(9.0F, 0.0F, 9.0F, 0.0F));
      icon.drawString(matrix, "B", this.getX() + 4.0F, this.getY() + 5.0F, new Color(225, 225, 255, 255).getRGB());
      font.drawString(matrix, this.getName(), this.getX() + 22, this.getY() + 6.5F, ColorAssist.getText());
      GlassStyle.backdrop(matrix, this.getX(), this.getY() + 16.5F, this.getWidth(), this.getHeight() - 17, new Vector4f(0.0F, 9.0F, 0.0F, 9.0F));
      float centerX = this.getX() + this.getWidth() / 2.0F;
      int offset = 23;
      int maxWidth = 80;
      if (this.keysList.isEmpty() && PlayerInteractionHelper.isChat(mc.field_1755)) {
         float centerY = this.getY() + offset;
         String name = "Пример модуля";
         String bind = "[" + this.currentRandomKey + "]";
         int textColor = ColorAssist.getText();
         int textAlpha = 255;
         int colorWithAlpha = ColorAssist.rgba(textColor >> 16 & 0xFF, textColor >> 8 & 0xFF, textColor & 0xFF, textAlpha);
         int color = new Color(225, 225, 255, 255).getRGB();
         float bindWidth = fontModule.getStringWidth(bind);
         Calculate.scale(matrix, centerX, centerY, 1.0F, 1.0F, () -> {
            fontModule.drawString(matrix, name, this.getX() + 5.0F, centerY + 1.0F, colorWithAlpha);
            fontModule.drawString(matrix, bind, this.getX() + this.getWidth() - bindWidth - 8.0F, centerY + 1.0F, color);
         });
         int width = (int)fontModule.getStringWidth(name + bind) + 15;
         maxWidth = Math.max(width, maxWidth);
         offset += 11;
      } else {
         for (Module module : this.keysList) {
            String bind = "[" + StringHelper.getBindName(module.getKey()) + "]";
            float centerY = this.getY() + offset;
            float animation = module.getAnimation().getOutput().floatValue();

            int textColor = ColorAssist.getText();
            int textAlpha = 255;
            int colorWithAlpha = ColorAssist.rgba(textColor >> 16 & 0xFF, textColor >> 8 & 0xFF, textColor & 0xFF, textAlpha);
            int color = new Color(225, 225, 255, 255).getRGB();
            float bindWidth = fontModule.getStringWidth(bind);
            Calculate.scale(
               matrix,
               centerX,
               centerY,
               1.0F,
               animation,
               () -> {
                  fontModule.drawString(matrix, module.getVisibleName(), this.getX() + 5.0F, centerY + 1.0F, colorWithAlpha);
                  fontModule.drawString(matrix, bind, this.getX() + this.getWidth() - bindWidth - 8.0F, centerY + 1.0F, color);
               }
            );
            float width = fontModule.getStringWidth(module.getVisibleName() + bind) + 15.0F;
            maxWidth = (int)Math.max(width, (float)maxWidth);
            offset += (int)(animation * 11.0F);
         }
      }

      this.setWidth(maxWidth + 10);
      this.setHeight(offset);
   }
}
