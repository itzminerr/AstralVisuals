package pl.astralvisuals.display.screens.clickgui.components.implement.other;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.joml.Matrix4f;

import net.minecraft.class_124;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import pl.astralvisuals.Force;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.Setting;
import pl.astralvisuals.utils.client.managers.file.exception.FileLoadException;
import pl.astralvisuals.utils.client.managers.file.exception.FileSaveException;
import pl.astralvisuals.utils.client.managers.file.impl.ModuleFile;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.update.ClientVersion;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.math.calc.Calculate;

public class BackgroundComponent extends AbstractComponent {
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.ROOT);
   private static final int MAX_NAME_LENGTH = 24;
   private final List<BackgroundComponent.ConfigEntry> configs = new ArrayList<>();
   private String editingConfig;
   private String editedName = "";
   private int editCursor = 0;
   private String searchText = "";
   private boolean editingSearch = false;
   private int searchCursor = 0;
   private boolean configsLoaded = false;
   private float localScroll = 0.0F;
   private float localSmoothedScroll = 0.0F;
   public static final float SIDEBAR_W = 38.0F;
   public static final float HEADER_H = 36.0F;

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      Force.getInstance().getScissorManager().push(matrix.method_23760().method_23761(), 0.0F, 0.0F, window.method_4486(), window.method_4502());
      GlassStyle.strongBackdrop(matrix, this.x - 8.0F, this.y - 5.0F, this.width + 9.5F, this.height + 5.0F, 14.0F);
      GlassStyle.surface(matrix, this.x + 2.0F, this.y + 39.0F, 36.0F, 207.0F, 11.0F, false);
      this.drawHeader(matrix);
      if (MenuScreen.INSTANCE.getCategory() == ModuleCategory.CONFIGS) {
         if (!this.configsLoaded) {
            this.refreshConfigs();
         }

         this.renderConfigs(context, mouseX, mouseY);
      } else {
         this.configsLoaded = false;
         this.editingSearch = false;
         this.renderResetButton(matrix, mouseX, mouseY);
      }

      Force.getInstance().getScissorManager().pop();
   }

   private void drawHeader(class_4587 matrix) {
      ModuleCategory cat = MenuScreen.INSTANCE.getCategory();
      String catName = cat.getReadableName();
      GlassStyle.surface(matrix, this.x + 2.0F, this.y + 5.0F, 74.0F, 25.0F, 11.0F, false);
      rectangle.render(
         ShapeProperties.create(matrix, this.x + 23.5F, this.y + 10.0F, 1.0, 15.0).round(4.0F).color(new Color(255, 255, 255, 25).getRGB()).build()
      );
      Fonts.getSize(22, Fonts.Type.BOLD).drawString(matrix, "A", this.x + 10.0F, this.y + 14.0F, ColorAssist.getText(0.78F));
      Fonts.getSize(14, Fonts.Type.DEFAULT).drawString(matrix, "AstralVisuals", this.x + 28.0F, this.y + 12.5F, ColorAssist.getText(0.78F));
      Fonts.getSize(14, Fonts.Type.DEFAULT).drawString(matrix, "Версия: " + ClientVersion.get(), this.x + 28.0F, this.y + 19.5F, ColorAssist.getText(0.78F));
      GlassStyle.surface(matrix, this.x + 81.0F, this.y + 5.0F, 184.0F, 25.0F, 11.0F, false);
      GlassStyle.surface(matrix, this.x + 269.0F, this.y + 5.0F, 123.0F, 25.0F, 11.0F, false);
      FontRenderer menuFont = Fonts.getSize(15, Fonts.Type.DEFAULT);
      String mainMenuText = "Главное меню";
      float mainMenuX = this.x + 88.0F;
      menuFont.drawString(matrix, mainMenuText, mainMenuX, this.y + 14.8F, ColorAssist.getText(0.78F));
      FontRenderer arrowFont = Fonts.getSize(13, Fonts.Type.BOLD);
      String arrows = "»";
      float arrowsX = mainMenuX + menuFont.getStringWidth(mainMenuText) + 8.0F;
      arrowFont.drawString(matrix, arrows, arrowsX, this.y + 14.8F, ColorAssist.getText(0.47F));
      FontRenderer categoryIconFont = Fonts.getSize(17, Fonts.Type.ICONSCATEGORY);
      String categoryIcon = this.getCategoryIcon(cat);
      float categoryIconX = arrowsX + arrowFont.getStringWidth(arrows) + 9.0F;
      categoryIconFont.drawString(matrix, categoryIcon, categoryIconX, this.y + 15.2F, ColorAssist.getText(0.78F));
      menuFont.drawString(matrix, catName, categoryIconX + categoryIconFont.getStringWidth(categoryIcon) + 9.0F, this.y + 14.8F, ColorAssist.getText(0.78F));
      Fonts.getSize(22, Fonts.Type.ICONS).drawString(matrix, "U", this.x + 366.0F, this.y + 13.5F, ColorAssist.getText(0.78F));
   }

   private String getCategoryIcon(ModuleCategory category) {
      return switch (category) {
         case COMBAT -> "A";
         case RENDER -> "C";
         case PLAYER -> "D";
         case CONFIGS -> "F";
      };
   }

   private void renderResetButton(class_4587 matrix, int mouseX, int mouseY) {
      float buttonX = this.getResetButtonX();
      float buttonY = this.getResetButtonY();
      float buttonW = this.getResetButtonWidth();
      float buttonH = this.getResetButtonHeight();
      boolean hovered = Calculate.isHovered(mouseX, mouseY, buttonX, buttonY, buttonW, buttonH);
      GlassStyle.strongBackdrop(matrix, buttonX, buttonY, buttonW, buttonH, 11.0F);
      if (hovered) {
         GlassStyle.surface(matrix, buttonX, buttonY, buttonW, buttonH, 11.0F, true);
      }

      FontRenderer font = Fonts.getSize(14, Fonts.Type.DEFAULT);
      String text = "Сбросить все";
      font.drawString(
         matrix, text, buttonX + (buttonW - font.getStringWidth(text)) / 2.0F, buttonY + buttonH / 2.0F - 2.1F, ColorAssist.getText(hovered ? 0.9F : 0.72F)
      );
   }

   private float getResetButtonX() {
      return this.x - 8.0F + (this.width + 9.5F - this.getResetButtonWidth()) / 2.0F;
   }

   private float getResetButtonY() {
      return this.y + this.height + 7.0F;
   }

   private float getResetButtonWidth() {
      return 120.0F;
   }

   private float getResetButtonHeight() {
      return 20.0F;
   }

   private boolean isResetButtonHovered(double mouseX, double mouseY) {
      return MenuScreen.INSTANCE.getCategory() != ModuleCategory.CONFIGS
         && Calculate.isHovered(mouseX, mouseY, this.getResetButtonX(), this.getResetButtonY(), this.getResetButtonWidth(), this.getResetButtonHeight());
   }

   private void resetAllModules() {
      for (Module module : Force.getInstance().getModuleRepository().modules()) {
         module.setState(false);
         module.setKey(-1);

         for (Setting setting : module.settings()) {
            setting.resetToDefault();
         }
      }

      windowManager.getWindows().forEach(window -> window.startCloseAnimation());
   }

   private void renderConfigs(class_332 context, int mouseX, int mouseY) {
      class_4587 matrix = context.method_51448();
      float contentX = this.x + 38.0F + 8.0F;
      float contentY = this.y + 36.0F + 8.0F;
      float contentW = this.width - 38.0F - 16.0F;
      float searchWidth = contentW - 110.0F;
      float buttonHeight = 16.0F;
      this.drawButton(matrix, contentX + searchWidth + 8.0F, contentY, 44.0F, buttonHeight, "Создать");
      this.drawButton(matrix, contentX + searchWidth + 56.0F, contentY, 44.0F, buttonHeight, "Очистить");
      this.drawInput(matrix, contentX, contentY, searchWidth, buttonHeight);
      Fonts.getSize(11, Fonts.Type.DEFAULT)
         .drawString(matrix, "Нажми на имя конфига чтобы переименовать", contentX, contentY + 22.0F, ColorAssist.getText(0.45F));
      List<BackgroundComponent.ConfigEntry> displayedConfigs = this.getDisplayedConfigs();
      float listX = contentX;
      float listY = contentY + 34.0F;
      float cardGap = 8.0F;
      float cardWidth = (contentW - cardGap) / 2.0F;
      float cardHeight = 60.0F;
      float viewHeight = this.height - 36.0F - 50.0F;
      int rows = (displayedConfigs.size() + 1) / 2;
      float contentHeight = rows > 0 ? rows * (cardHeight + cardGap) - cardGap : 0.0F;
      float maxScroll = Math.max(0.0F, contentHeight - viewHeight);
      this.localScroll = class_3532.method_15363(this.localScroll, -maxScroll, 0.0F);
      this.localSmoothedScroll = Calculate.interpolate(this.localSmoothedScroll, this.localScroll, 0.2F);
      Matrix4f positionMatrix = matrix.method_23760().method_23761();
      Force.getInstance().getScissorManager().push(positionMatrix, contentX, listY, contentW, viewHeight);

      for (int index = 0; index < displayedConfigs.size(); index++) {
         BackgroundComponent.ConfigEntry entry = displayedConfigs.get(index);
         float cardX = listX + index % 2 * (cardWidth + cardGap);
         float cardY = listY + index / 2 * (cardHeight + cardGap) + this.localSmoothedScroll;
         if (!(cardY + cardHeight < listY - 4.0F) && !(cardY > listY + viewHeight + 4.0F)) {
            this.drawConfigCard(matrix, cardX, cardY, cardWidth, cardHeight, entry, mouseX, mouseY);
         }
      }

      Force.getInstance().getScissorManager().pop();
      if (displayedConfigs.isEmpty()) {
         String msg = this.configs.isEmpty() ? "Нет конфигов" : "Ничего не найдено";
         Fonts.getSize(16, Fonts.Type.DEFAULT)
            .drawCenteredString(matrix, msg, contentX + contentW / 2.0F, this.y + this.height / 2.0F + 10.0F, ColorAssist.getText(0.6F));
      }
   }

   private void drawInput(class_4587 matrix, float ix, float iy, float iw, float ih) {
      GlassStyle.surface(matrix, ix, iy, iw, ih, 5.0F, false);
      String displayText = this.searchText.isEmpty() && !this.editingSearch ? "Поиск или имя..." : this.searchText;
      FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);
      font.drawCenteredString(
         matrix, displayText, ix + iw / 2.0F, iy + ih / 2.0F - 1.0F, ColorAssist.getText(this.searchText.isEmpty() && !this.editingSearch ? 0.4F : 0.9F)
      );
      if (this.editingSearch && System.currentTimeMillis() % 1000L < 500L) {
         float textWidth = font.getStringWidth(displayText);
         float co = font.getStringWidth(this.searchText.substring(0, Math.min(this.searchCursor, this.searchText.length())));
         float offset = co - textWidth / 2.0F;
         font.drawString(matrix, "|", ix + iw / 2.0F + offset, iy + ih / 2.0F - 1.0F, ColorAssist.getText(0.9F));
      }
   }

   private void drawButton(class_4587 matrix, float bx, float by, float bw, float bh, String text) {
      GlassStyle.surface(matrix, bx, by, bw, bh, 5.0F, false);
      Fonts.getSize(12, Fonts.Type.DEFAULT).drawCenteredString(matrix, text, bx + bw / 2.0F, by + bh / 2.0F - 1.0F, ColorAssist.getText(0.85F));
   }

   private void drawConfigCard(class_4587 matrix, float cx, float cy, float cw, float ch, BackgroundComponent.ConfigEntry entry, int mouseX, int mouseY) {
      GlassStyle.surface(matrix, cx, cy, cw, ch, 7.0F, false);
      boolean hoveredName = Calculate.isHovered(mouseX, mouseY, cx + 8.0F, cy + 6.0F, cw - 16.0F, 14.0);
      String title = entry.name().equals(this.editingConfig) ? this.editedName : entry.name();
      Fonts.getSize(13, Fonts.Type.DEFAULT)
         .drawString(matrix, title, cx + 8.0F, cy + 11.0F, hoveredName ? ColorAssist.getText(1.0F) : ColorAssist.getText(0.85F));
      if (entry.name().equals(this.editingConfig) && System.currentTimeMillis() % 1000L < 500L) {
         float co = Fonts.getSize(13, Fonts.Type.DEFAULT).getStringWidth(this.editedName.substring(0, Math.min(this.editCursor, this.editedName.length())));
         Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, "|", cx + 8.0F + co, cy + 11.0F, ColorAssist.getText(0.9F));
      }

      Fonts.getSize(10, Fonts.Type.DEFAULT).drawString(matrix, "Создан: " + this.formatDate(entry.created()), cx + 8.0F, cy + 26.0F, ColorAssist.getText(0.5F));
      Fonts.getSize(10, Fonts.Type.DEFAULT)
         .drawString(matrix, "Изменен: " + this.formatDate(entry.updated()), cx + 8.0F, cy + 35.0F, ColorAssist.getText(0.5F));
      this.drawAction(matrix, cx + 8.0F, cy + 44.0F, 36.0F, 11.0F, "Загрузить");
      this.drawAction(matrix, cx + 48.0F, cy + 44.0F, 36.0F, 11.0F, "Сохранить");
      this.drawAction(matrix, cx + cw - 44.0F, cy + 44.0F, 36.0F, 11.0F, "Удалить");
   }

   private void drawAction(class_4587 matrix, float ax, float ay, float aw, float ah, String text) {
      GlassStyle.surface(matrix, ax, ay, aw, ah, 3.0F, false);
      Fonts.getSize(10, Fonts.Type.DEFAULT).drawCenteredString(matrix, text, ax + aw / 2.0F, ay + ah / 2.0F - 1.0F, ColorAssist.getText(0.85F));
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.isResetButtonHovered(mouseX, mouseY)) {
         SoundManager.playSound(SoundManager.ENABLE_MODULE, 1.0F, 1.0F);
         this.resetAllModules();
         return true;
      } else if (MenuScreen.INSTANCE.getCategory() != ModuleCategory.CONFIGS) {
         this.editingConfig = null;
         this.editingSearch = false;
         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         float contentX = this.x + 38.0F + 8.0F;
         float contentY = this.y + 36.0F + 8.0F;
         float contentW = this.width - 38.0F - 16.0F;
         float searchWidth = contentW - 110.0F;
         if (button == 0) {
            this.editingSearch = Calculate.isHovered(mouseX, mouseY, contentX, contentY, searchWidth, 16.0);
            if (this.editingSearch) {
               this.searchCursor = this.searchText.length();
               return true;
            }

            if (Calculate.isHovered(mouseX, mouseY, contentX + searchWidth + 8.0F, contentY, 44.0, 16.0)) {
               this.createConfig();
               return true;
            }

            if (Calculate.isHovered(mouseX, mouseY, contentX + searchWidth + 56.0F, contentY, 44.0, 16.0)) {
               this.clearAllConfigs();
               this.refreshConfigs();
               return true;
            }

            List<BackgroundComponent.ConfigEntry> displayedConfigs = this.getDisplayedConfigs();
            float listX = contentX;
            float listY = contentY + 34.0F;
            float cardGap = 8.0F;
            float cardWidth = (contentW - cardGap) / 2.0F;
            float cardHeight = 60.0F;

            for (int index = 0; index < displayedConfigs.size(); index++) {
               BackgroundComponent.ConfigEntry entry = displayedConfigs.get(index);
               float cardX = listX + index % 2 * (cardWidth + cardGap);
               float cardY = listY + index / 2 * (cardHeight + cardGap) + this.localSmoothedScroll;
               if (Calculate.isHovered(mouseX, mouseY, cardX + 8.0F, cardY + 6.0F, cardWidth - 16.0F, 14.0)) {
                  this.startRename(entry.name());
                  return true;
               }

               if (Calculate.isHovered(mouseX, mouseY, cardX + 8.0F, cardY + 44.0F, 36.0, 11.0)) {
                  this.loadConfig(entry.name());
                  return true;
               }

               if (Calculate.isHovered(mouseX, mouseY, cardX + 48.0F, cardY + 44.0F, 36.0, 11.0)) {
                  this.saveConfig(entry.name());
                  this.refreshConfigs();
                  return true;
               }

               if (Calculate.isHovered(mouseX, mouseY, cardX + cardWidth - 44.0F, cardY + 44.0F, 36.0, 11.0)) {
                  this.deleteConfig(entry.name());
                  this.refreshConfigs();
                  return true;
               }
            }
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (MenuScreen.INSTANCE.getCategory() == ModuleCategory.CONFIGS) {
         float contentX = this.x + 38.0F + 8.0F;
         float contentY = this.y + 36.0F + 42.0F;
         float contentW = this.width - 38.0F - 16.0F;
         if (Calculate.isHovered(mouseX, mouseY, contentX, contentY, contentW, this.height - 36.0F - 50.0F)) {
            this.localScroll += (float)amount * 18.0F;
            return true;
         }
      }

      return super.mouseScrolled(mouseX, mouseY, amount);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.editingSearch) {
         return this.handleTextInput(keyCode, modifiers, false) || super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         return this.editingConfig == null
            ? super.keyPressed(keyCode, scanCode, modifiers)
            : this.handleTextInput(keyCode, modifiers, true) || super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (!this.isAllowedCharacter(chr)) {
         return super.charTyped(chr, modifiers);
      } else if (this.editingSearch && this.searchText.length() < 24) {
         this.searchText = this.searchText.substring(0, this.searchCursor) + chr + this.searchText.substring(this.searchCursor);
         this.searchCursor++;
         return true;
      } else if (this.editingConfig != null && this.editedName.length() < 24) {
         this.editedName = this.editedName.substring(0, this.editCursor) + chr + this.editedName.substring(this.editCursor);
         this.editCursor++;
         return true;
      } else {
         return super.charTyped(chr, modifiers);
      }
   }

   private boolean handleTextInput(int keyCode, int modifiers, boolean renameMode) {
      if (keyCode == 257) {
         if (renameMode) {
            this.commitRename();
         } else {
            this.editingSearch = false;
         }

         return true;
      } else if (keyCode == 256) {
         if (renameMode) {
            this.editingConfig = null;
         } else {
            this.editingSearch = false;
         }

         return true;
      } else {
         return this.handleEditorKeys(keyCode, modifiers, renameMode);
      }
   }

   private boolean handleEditorKeys(int keyCode, int modifiers, boolean renameMode) {
      String value = renameMode ? this.editedName : this.searchText;
      int cursor = renameMode ? this.editCursor : this.searchCursor;
      if (keyCode == 259) {
         if (cursor > 0) {
            value = value.substring(0, cursor - 1) + value.substring(cursor);
            cursor--;
         }
      } else if (keyCode == 263) {
         cursor = Math.max(0, cursor - 1);
      } else if (keyCode == 262) {
         cursor = Math.min(value.length(), cursor + 1);
      } else {
         if (keyCode != 86 || (modifiers & 2) == 0) {
            return false;
         }

         String clip = class_310.method_1551().field_1774.method_1460();
         String cleaned = this.normalizeName(clip);
         int avail = 24 - value.length();
         if (avail > 0) {
            cleaned = cleaned.substring(0, Math.min(cleaned.length(), avail));
            value = value.substring(0, cursor) + cleaned + value.substring(cursor);
            cursor += cleaned.length();
         }
      }

      if (renameMode) {
         this.editedName = value;
         this.editCursor = cursor;
      } else {
         this.searchText = value;
         this.searchCursor = cursor;
      }

      return true;
   }

   private void refreshConfigs() {
      this.configs.clear();
      File directory = this.getConfigDirectory();
      File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
      if (files != null) {
         for (File file : files) {
            try {
               Path path = file.toPath();
               this.configs
                  .add(
                     new BackgroundComponent.ConfigEntry(
                        file.getName().replace(".json", ""),
                        Files.readAttributes(path, BasicFileAttributes.class).creationTime().toMillis(),
                        Files.getLastModifiedTime(path).toMillis()
                     )
                  );
            } catch (IOException var8) {
               this.configs.add(new BackgroundComponent.ConfigEntry(file.getName().replace(".json", ""), file.lastModified(), file.lastModified()));
            }
         }
      }

      this.configs.sort(Comparator.comparingLong(BackgroundComponent.ConfigEntry::updated).reversed());
      this.configsLoaded = true;
   }

   private List<BackgroundComponent.ConfigEntry> getDisplayedConfigs() {
      if (this.searchText.isBlank()) {
         return new ArrayList<>(this.configs);
      } else {
         String filter = this.searchText.toLowerCase(Locale.ROOT);
         return this.configs.stream().filter(c -> c.name().toLowerCase(Locale.ROOT).contains(filter)).toList();
      }
   }

   private void createConfig() {
      String baseName = this.normalizeName(this.searchText);
      if (baseName.isBlank()) {
         baseName = "AstralConfig";
      }

      String uniqueName = this.createUniqueName(baseName);
      this.saveConfig(uniqueName);
      this.searchText = uniqueName;
      this.searchCursor = uniqueName.length();
      this.editingSearch = false;
      this.refreshConfigs();
   }

   private void saveConfig(String name) {
      try {
         new ModuleFile(Force.getInstance().getModuleRepository(), Force.getInstance().getDraggableRepository())
            .saveToFile(this.getConfigDirectory(), name + ".json");
         this.logDirect("Config " + name + " saved.");
      } catch (FileSaveException var3) {
         this.logDirect("Failed to save: " + var3.getMessage(), class_124.field_1061);
      }
   }

   private void loadConfig(String name) {
      try {
         new ModuleFile(Force.getInstance().getModuleRepository(), Force.getInstance().getDraggableRepository())
            .loadFromFile(this.getConfigDirectory(), name + ".json");
         this.logDirect("Config " + name + " loaded.");
      } catch (FileLoadException var3) {
         this.logDirect("Failed to load: " + var3.getMessage(), class_124.field_1061);
      }
   }

   private void deleteConfig(String name) {
      File file = new File(this.getConfigDirectory(), name + ".json");
      if (file.exists() && !file.delete()) {
         this.logDirect("Failed to delete " + name, class_124.field_1061);
      } else {
         if (name.equals(this.editingConfig)) {
            this.editingConfig = null;
            this.editedName = "";
            this.editCursor = 0;
         }

         this.logDirect("Config " + name + " deleted.");
      }
   }

   private void clearAllConfigs() {
      File[] files = this.getConfigDirectory().listFiles((dir, name) -> name.endsWith(".json"));
      if (files != null && files.length != 0) {
         for (File file : files) {
            if (!file.delete()) {
               this.logDirect("Failed to clear configs.", class_124.field_1061);
               return;
            }
         }

         this.editingConfig = null;
         this.editedName = "";
         this.editCursor = 0;
         this.logDirect("All configs deleted.");
      }
   }

   private void startRename(String currentName) {
      this.editingConfig = currentName;
      this.editedName = currentName;
      this.editCursor = this.editedName.length();
      this.editingSearch = false;
   }

   private void commitRename() {
      if (this.editingConfig != null) {
         String normalized = this.normalizeName(this.editedName);
         if (normalized.isBlank()) {
            this.logDirect("Name cannot be empty.", class_124.field_1061);
         } else if (normalized.equals(this.editingConfig)) {
            this.editingConfig = null;
         } else {
            File dir = this.getConfigDirectory();
            File oldFile = new File(dir, this.editingConfig + ".json");
            File newFile = new File(dir, normalized + ".json");
            if (newFile.exists()) {
               this.logDirect("Name already exists.", class_124.field_1061);
            } else {
               try {
                  Files.move(oldFile.toPath(), newFile.toPath());
                  this.logDirect("Renamed to " + normalized + ".");
                  this.editingConfig = null;
                  this.editedName = "";
                  this.editCursor = 0;
                  this.refreshConfigs();
               } catch (IOException var6) {
                  this.logDirect("Failed to rename: " + var6.getMessage(), class_124.field_1061);
               }
            }
         }
      }
   }

   private File getConfigDirectory() {
      File dir = new File(Force.getInstance().getClientInfoProvider().clientDir(), "Custom");
      if (!dir.exists()) {
         dir.mkdirs();
      }

      return dir;
   }

   private String createUniqueName(String baseName) {
      String base = this.normalizeName(baseName);
      if (base.isBlank()) {
         base = "AstralConfig";
      }

      String candidate = base;
      int idx = 1;

      while (new File(this.getConfigDirectory(), candidate + ".json").exists()) {
         candidate = base + "_" + idx++;
      }

      return candidate;
   }

   private String normalizeName(String value) {
      if (value == null) {
         return "";
      } else {
         String n = Normalizer.normalize(value.trim(), Form.NFKC).replace(' ', '_').replaceAll("[^\\p{L}\\p{N}_-]", "");
         return n.length() > 24 ? n.substring(0, 24) : n;
      }
   }

   private boolean isAllowedCharacter(char chr) {
      return Character.isLetterOrDigit(chr) || chr == '_' || chr == '-' || chr == ' ';
   }

   private String formatDate(long time) {
      return time <= 0L ? "unknown" : LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(DATE_FORMATTER);
   }

   public BackgroundComponent setEditingConfig(String editingConfig) {
      this.editingConfig = editingConfig;
      return this;
   }

   public BackgroundComponent setEditedName(String editedName) {
      this.editedName = editedName;
      return this;
   }

   public BackgroundComponent setEditCursor(int editCursor) {
      this.editCursor = editCursor;
      return this;
   }

   public BackgroundComponent setSearchText(String searchText) {
      this.searchText = searchText;
      return this;
   }

   public BackgroundComponent setEditingSearch(boolean editingSearch) {
      this.editingSearch = editingSearch;
      return this;
   }

   public BackgroundComponent setSearchCursor(int searchCursor) {
      this.searchCursor = searchCursor;
      return this;
   }

   public BackgroundComponent setConfigsLoaded(boolean configsLoaded) {
      this.configsLoaded = configsLoaded;
      return this;
   }

   public BackgroundComponent setLocalScroll(float localScroll) {
      this.localScroll = localScroll;
      return this;
   }

   public BackgroundComponent setLocalSmoothedScroll(float localSmoothedScroll) {
      this.localSmoothedScroll = localSmoothedScroll;
      return this;
   }

   private record ConfigEntry(String name, long created, long updated) {
   }
}
