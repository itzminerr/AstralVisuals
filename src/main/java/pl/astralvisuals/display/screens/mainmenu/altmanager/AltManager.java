package pl.astralvisuals.display.screens.mainmenu.altmanager;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.InOutBack;
import pl.astralvisuals.display.screens.mainmenu.altmanager.impl.Account;
import pl.astralvisuals.display.screens.mainmenu.altmanager.impl.AccountRepository;
import pl.astralvisuals.utils.client.managers.file.impl.AccountFile;
import pl.astralvisuals.utils.client.session.SessionHelper;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;

public class AltManager implements QuickImports {
   private static final class_2960 STEVE_TEXTURE = class_2960.method_60655("minecraft", "textures/entity/steve.png");
   private final AccountRepository accountRepository = Force.getInstance().getAccountRepository();
   private String currentAccount = "";
   private boolean typing = false;
   private String typedText = "";
   private int cursorPos = 0;
   private int selStart = -1;
   private int selEnd = -1;
   private long lastClick = 0L;
   private float textXOffset = 0.0F;
   private boolean dragging = false;
   private static final int MIN_LENGTH = 3;
   private static final int MAX_LENGTH = 16;
   private final Random rand = new Random();
   private float scroll = 0.0F;
   private float smoothedScroll = 0.0F;
   private float panelX;
   private float panelY;
   private final float panelWidth = 160.0F;
   private final float panelHeight = 210.0F;
   private final Map<String, Animation> accountAnimations = new HashMap<>();
   private final Map<String, Float> accountYPositions = new HashMap<>();
   private final Map<String, Animation> accountRemoveAnimations = new HashMap<>();
   private final Map<String, class_2960> skinTextureCache = new HashMap<>();
   private Animation emptyMessageAnimation = new InOutBack().setValue(1.0).setMs(300);
   private boolean wasEmpty = true;
   private long lastActionTime = 0L;
   private static final long ACTION_DELAY = 250L;
   private static final float ACCOUNT_SPACING = 25.0F;

   public AltManager(float x, float y) {
      this.panelX = x;
      this.panelY = y;
      this.currentAccount = this.accountRepository.currentAccount != null ? this.accountRepository.currentAccount : "";
      this.syncCurrentAccount(false);
      this.initializeAccountAnimations();
      this.emptyMessageAnimation.setDirection(Direction.FORWARDS);
      this.emptyMessageAnimation.reset();
      this.wasEmpty = this.accountRepository.accountList.isEmpty();
   }

   private void initializeAccountAnimations() {
      for (Account account : this.accountRepository.accountList) {
         if (!this.accountAnimations.containsKey(account.uuid)) {
            Animation anim = new InOutBack().setValue(1.0).setMs(300);
            anim.setDirection(Direction.FORWARDS);
            anim.reset();
            this.accountAnimations.put(account.uuid, anim);
         }
      }
   }

   public void updatePosition(float x, float y) {
      float deltaY = y - this.panelY;
      this.panelX = x;
      this.panelY = y;

      for (String key : this.accountYPositions.keySet()) {
         this.accountYPositions.put(key, this.accountYPositions.get(key) + deltaY);
      }
   }

   public float getPanelWidth() {
      return 160.0F;
   }

   public float getPanelHeight() {
      return 210.0F;
   }

   private void syncCurrentAccount(boolean applySession) {
      this.importLauncherAccount();
      String savedAccount = this.accountRepository.currentAccount == null ? "" : this.accountRepository.currentAccount;
      if (!savedAccount.isEmpty() && this.accountExists(savedAccount)) {
         this.currentAccount = savedAccount;
      } else {
         String sessionName = class_310.method_1551().method_1548().method_1676();
         if (sessionName != null && this.accountExists(sessionName)) {
            this.currentAccount = sessionName;
            this.accountRepository.currentAccount = sessionName;
            if (applySession) {
               Account account = this.findAccount(sessionName);
               if (account != null) {
                  this.setSession(account);
               }
            }

            this.saveAccounts();
         } else if (this.accountRepository.accountList.size() == 1) {
            Account account = this.accountRepository.accountList.get(0);
            this.currentAccount = account.name;
            this.accountRepository.currentAccount = account.name;
            if (applySession) {
               this.setSession(account);
            } else {
               this.saveAccounts();
            }
         } else {
            this.currentAccount = "";
            if (!savedAccount.isEmpty()) {
               this.accountRepository.currentAccount = "";
               this.saveAccounts();
            }
         }
      }
   }

   private Account findAccount(String accountName) {
      for (Account account : this.accountRepository.accountList) {
         if (account.name != null && account.name.equalsIgnoreCase(accountName)) {
            return account;
         }
      }

      return null;
   }

   private void importLauncherAccount() {
      // Намеренно ничего не делаем: не добавляем аккаунт лаунчера/сессии автоматически.
      // Раньше это плодило новые записи "Player###" при каждом запуске (в dev-сессии ник случайный).
      // Аккаунты добавляются только вручную через менеджер.
   }

   public void tick() {
      this.syncCurrentAccount(false);

      for (Account account : this.accountRepository.accountList) {
         float target = account.starred ? 1.0F : 0.0F;
         account.starAnim = account.starAnim + (target - account.starAnim) * 0.2F;
         if (!this.accountAnimations.containsKey(account.uuid)) {
            Animation anim = new InOutBack().setValue(1.0).setMs(300);
            anim.setDirection(Direction.FORWARDS);
            anim.reset();
            this.accountAnimations.put(account.uuid, anim);
         }
      }

      this.accountAnimations.keySet().removeIf(uuid -> {
         boolean exists = false;

         for (Account acc : this.accountRepository.accountList) {
            if (acc.uuid.equals(uuid)) {
               exists = true;
               break;
            }
         }

         if (!exists) {
            this.accountYPositions.remove(uuid);
            this.skinTextureCache.remove(uuid);
         }

         return !exists;
      });
      boolean isEmpty = this.accountRepository.accountList.isEmpty();
      if (isEmpty != this.wasEmpty) {
         this.wasEmpty = isEmpty;
         if (isEmpty) {
            this.emptyMessageAnimation.setDirection(Direction.FORWARDS);
         } else {
            this.emptyMessageAnimation.setDirection(Direction.BACKWARDS);
         }

         this.emptyMessageAnimation.reset();
      }
   }

   public void render(class_332 context, Color buttonColor, Color outlineColor, Color gradientColor, Color textColor, Color bgColor) {
      blur.render(
         ShapeProperties.create(context.method_51448(), this.panelX, this.panelY, 160.0, 210.0)
            .round(10.0F)
            .softness(1.0F)
            .quality(15.0F)
            .color(bgColor.getRGB())
            .build()
      );
      rectangle.render(
         ShapeProperties.create(context.method_51448(), this.panelX, this.panelY, 160.0, 210.0).round(10.0F).softness(0.0F).color(bgColor.getRGB()).build()
      );
      String title = "Ваши аккаунты";
      FontRenderer titleFont = Fonts.getSize(16, Fonts.Type.DEFAULT);
      float titleWidth = titleFont.getStringWidth(title) + 8.0F;
      float titleX = this.panelX + 80.0F - titleWidth / 2.0F;
      float titleY = this.panelY - 13.0F;
      blur.render(
         ShapeProperties.create(context.method_51448(), titleX, titleY, titleWidth, 11.0)
            .round(4.0F)
            .softness(1.0F)
            .quality(15.0F)
            .color(bgColor.getRGB())
            .build()
      );
      rectangle.render(
         ShapeProperties.create(context.method_51448(), titleX, titleY, titleWidth, 11.0)
            .round(4.0F)
            .softness(0.0F)
            .color(new Color(255, 255, 255, 10).getRGB())
            .build()
      );
      rectangle.render(
         ShapeProperties.create(context.method_51448(), titleX, titleY, titleWidth, 11.0)
            .thickness(0.8F)
            .round(4.0F)
            .outlineColor(outlineColor.getRGB())
            .color(0)
            .build()
      );
      titleFont.drawCenteredString(context.method_51448(), title, this.panelX + 80.0F, this.panelY - 10.0F, textColor.getRGB());
      rectangle.render(
         ShapeProperties.create(context.method_51448(), this.panelX, this.panelY, 160.0, 210.0)
            .thickness(2.0F)
            .round(10.0F)
            .outlineColor(outlineColor.getRGB())
            .color(buttonColor.getRGB(), buttonColor.getRGB(), gradientColor.getRGB(), gradientColor.getRGB())
            .build()
      );
      this.renderTextField(context, buttonColor, outlineColor, gradientColor, textColor);
      this.renderAccountList(context, buttonColor, outlineColor, gradientColor, textColor);
      String displayAccount = this.currentAccount.isEmpty() ? "не выбран" : this.currentAccount;
      String currentText = "Ваш текущий аккаунт » " + displayAccount;
      displayAccount = this.currentAccount.isEmpty() ? "не выбран" : this.currentAccount;
      currentText = "Ваш текущий аккаунт » " + displayAccount;
      float currentWidth = Fonts.getSize(15, Fonts.Type.SEMI).getStringWidth(currentText) + 8.0F;
      float currentX = this.panelX + 80.0F - currentWidth / 2.0F;
      rectangle.render(
         ShapeProperties.create(context.method_51448(), currentX, this.panelY + 210.0F + 2.0F, currentWidth, 12.0)
            .thickness(2.0F)
            .round(3.0F)
            .outlineColor(outlineColor.getRGB())
            .color(buttonColor.getRGB(), buttonColor.getRGB(), gradientColor.getRGB(), gradientColor.getRGB())
            .build()
      );
      Fonts.getSize(15, Fonts.Type.SEMI)
         .drawCenteredString(context.method_51448(), currentText, this.panelX + 80.0F, this.panelY + 210.0F + 6.0F, textColor.getRGB());
   }

   private void renderTextField(class_332 context, Color buttonColor, Color outlineColor, Color gradientColor, Color textColor) {
      float inputY = this.panelY + 210.0F - 25.0F;
      rectangle.render(
         ShapeProperties.create(context.method_51448(), this.panelX + 5.0F, inputY, 149.0, 20.0)
            .thickness(2.0F)
            .round(6.0F)
            .outlineColor(outlineColor.getRGB())
            .color(buttonColor.getRGB(), buttonColor.getRGB(), gradientColor.getRGB(), gradientColor.getRGB())
            .build()
      );
      rectangle.render(
         ShapeProperties.create(context.method_51448(), this.panelX + 5.0F, inputY, 149.0, 1.0)
            .thickness(2.0F)
            .round(5.0F)
            .outlineColor(outlineColor.getRGB())
            .color(
               new Color(buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), 5).getRGB(),
               new Color(buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), textColor.getAlpha()).getRGB(),
               new Color(gradientColor.getRed(), gradientColor.getGreen(), gradientColor.getBlue(), textColor.getAlpha()).getRGB(),
               new Color(gradientColor.getRed(), gradientColor.getGreen(), gradientColor.getBlue(), 5).getRGB()
            )
            .build()
      );
      rectangle.render(
         ShapeProperties.create(context.method_51448(), this.panelX + 160.0F - 25.0F, inputY + 2.5F, 15.0, 15.0)
            .thickness(2.0F)
            .round(4.0F)
            .outlineColor(outlineColor.getRGB())
            .color(buttonColor.getRGB(), buttonColor.getRGB(), gradientColor.getRGB(), gradientColor.getRGB())
            .build()
      );
      Fonts.getSize(24, Fonts.Type.ICONS).drawString(context.method_51448(), "R", this.panelX + 160.0F - 24.5F, inputY + 7.0F, textColor.getRGB());
      float textFieldX = this.panelX + 5.0F;
      float textFieldY = inputY - 8.0F;
      float textFieldWidth = 149.0F;
      float textFieldHeight = 20.0F;
      FontRenderer font = Fonts.getSize(16, Fonts.Type.DEFAULT);
      long currentTime = System.currentTimeMillis();
      boolean blink = currentTime % 1000L < 500L;
      context.method_44379((int)(textFieldX + 3.0F), (int)textFieldY, (int)(textFieldX + textFieldWidth - 3.0F), (int)(textFieldY + textFieldHeight) + 5);
      if (this.typing && this.hasSelection()) {
         int start = Math.min(this.selStart, this.selEnd);
         int end = Math.max(this.selStart, this.selEnd);
         float selXStart = textFieldX + 5.0F - this.textXOffset + font.getStringWidth(this.typedText.substring(0, start));
         float selWidth = font.getStringWidth(this.typedText.substring(start, end));
         Color selColor = new Color(85, 133, 232, textColor.getAlpha());
         rectangle.render(
            ShapeProperties.create(context.method_51448(), selXStart, textFieldY + 13.5F, selWidth, textFieldHeight - 10.0F).color(selColor.getRGB()).build()
         );
      }

      String visibleText = this.typedText.isEmpty() && !this.typing ? "Введите никнейм" : this.typedText;
      if (this.typedText.isEmpty() && !this.typing && visibleText.isEmpty()) {
         font.drawString(context.method_51448(), "Введите никнейм", textFieldX + 5.0F, textFieldY + 16.0F, textColor.getRGB());
      } else {
         font.drawString(context.method_51448(), visibleText, textFieldX + 5.0F - this.textXOffset, textFieldY + 16.0F, textColor.getRGB());
      }

      if (this.typing && blink && !this.hasSelection()) {
         float cursorX = textFieldX + 5.0F - this.textXOffset + font.getStringWidth(this.typedText.substring(0, this.cursorPos));
         rectangle.render(
            ShapeProperties.create(context.method_51448(), cursorX + 1.0F, textFieldY + 15.0F, 0.5, textFieldHeight - 13.0F).color(textColor.getRGB()).build()
         );
      }

      context.method_44380();
   }

   private void renderAccountList(class_332 context, Color buttonColor, Color outlineColor, Color gradientColor, Color textColor) {
      float accountSpacing = 25.0F;
      class_4587 matrix = context.method_51448();
      Matrix4f positionMatrix = matrix.method_23760().method_23761();
      ScissorAssist scissorManager = Force.getInstance().getScissorManager();
      float listY = this.panelY + 5.0F;
      float listHeight = 175.0F;
      this.clampListScroll(listHeight);
      scissorManager.push(positionMatrix, this.panelX, listY, 160.0F, listHeight);
      this.smoothedScroll = class_3532.method_16439(0.1F, this.smoothedScroll, this.scroll);
      this.clampListScroll(listHeight);
      if (this.accountRepository.accountList.isEmpty()) {
         Fonts.getSize(16, Fonts.Type.DEFAULT)
            .drawCenteredString(context.method_51448(), "Пусто    ", this.panelX + 80.0F - 5.0F, this.panelY + 105.0F - 10.0F, textColor.getRGB());
         Fonts.getSize(36, Fonts.Type.ICONS)
            .drawCenteredString(context.method_51448(), "   W", this.panelX + 80.0F + 7.0F, this.panelY + 105.0F - 16.0F, textColor.getRGB());
      } else {
         for (int i = 0; i < this.accountRepository.accountList.size(); i++) {
            Account account = this.accountRepository.accountList.get(i);
            float targetY = this.panelY + 10.0F + i * accountSpacing - this.smoothedScroll;
            String key = account.uuid;
            this.accountYPositions.putIfAbsent(key, targetY);
            float currentY = this.accountYPositions.get(key);
            currentY = class_3532.method_16439(0.15F, currentY, targetY);
            this.accountYPositions.put(key, currentY);
            Animation anim = this.accountAnimations.get(account.uuid);
            Animation removeAnim = this.accountRemoveAnimations.get(account.uuid);
            if (anim != null) {
               float animProgress = anim.getOutput().floatValue();
               if (removeAnim != null) {
                  animProgress *= removeAnim.getOutput().floatValue();
               }

               float scale = 0.5F + animProgress * 0.5F;
               int alpha = (int)(textColor.getAlpha() * animProgress);
               if (currentY + 20.0F >= listY && currentY <= listY + listHeight) {
                  matrix.method_22903();
                  float centerX = this.panelX + 80.0F;
                  float centerY = currentY + 10.0F;
                  matrix.method_46416(centerX, centerY, 0.0F);
                  matrix.method_22905(scale, scale, 1.0F);
                  matrix.method_46416(-centerX, -centerY, 0.0F);
                  int clampedAlpha = Math.max(0, Math.min(255, alpha));
                  Color animButtonColor = new Color(buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), clampedAlpha);
                  Color animGradientColor = new Color(gradientColor.getRed(), gradientColor.getGreen(), gradientColor.getBlue(), clampedAlpha);
                  Color animOutlineColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), clampedAlpha);
                  Color animTextColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), clampedAlpha);
                  Color rowBaseColor = new Color(buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), Math.max(0, Math.min(245, clampedAlpha)));
                  rectangle.render(
                     ShapeProperties.create(context.method_51448(), this.panelX + 5.0F, currentY, 149.0, 20.0).round(5.0F).color(rowBaseColor.getRGB()).build()
                  );
                  rectangle.render(
                     ShapeProperties.create(context.method_51448(), this.panelX + 5.0F, currentY, 149.0, 20.0)
                        .thickness(2.0F)
                        .round(5.0F)
                        .outlineColor(animOutlineColor.getRGB())
                        .color(animButtonColor.getRGB(), animButtonColor.getRGB(), animGradientColor.getRGB(), animGradientColor.getRGB())
                        .build()
                  );
                  rectangle.render(
                     ShapeProperties.create(context.method_51448(), this.panelX + 5.0F, currentY, 149.0, 1.0)
                        .thickness(2.0F)
                        .round(5.0F)
                        .outlineColor(animOutlineColor.getRGB())
                        .color(
                           new Color(buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), Math.max(0, Math.min(255, 5 * clampedAlpha / 255)))
                              .getRGB(),
                           new Color(buttonColor.getRed(), buttonColor.getGreen(), buttonColor.getBlue(), clampedAlpha).getRGB(),
                           new Color(gradientColor.getRed(), gradientColor.getGreen(), gradientColor.getBlue(), clampedAlpha).getRGB(),
                           new Color(
                                 gradientColor.getRed(), gradientColor.getGreen(), gradientColor.getBlue(), Math.max(0, Math.min(255, 5 * clampedAlpha / 255))
                              )
                              .getRGB()
                        )
                        .build()
                  );
                  Color starColor = this.interpolateColor(animTextColor, new Color(255, 255, 0, clampedAlpha), account.starAnim);
                  Color faceOutline = new Color(64, 64, 64, clampedAlpha);
                  rectangle.render(
                     ShapeProperties.create(context.method_51448(), this.panelX + 9.5F, currentY + 2.5, 16.0, 16.0)
                        .thickness(4.0F)
                        .round(8.0F)
                        .outlineColor(faceOutline.getRGB())
                        .color(animButtonColor.getRGB(), animButtonColor.getRGB(), animGradientColor.getRGB(), animGradientColor.getRGB())
                        .build()
                  );
                  Fonts.getSize(25, Fonts.Type.ICONS)
                     .drawString(context.method_51448(), "★", this.panelX + 160.0F - 23.5F, currentY + 4.5F, starColor.getRGB());
                  this.drawAccountFace(context, account, this.panelX + 10.0F, currentY + 3.0F, alpha);
                  Fonts.getSize(15, Fonts.Type.SEMI)
                     .drawString(context.method_51448(), account.name, this.panelX + 28.0F, currentY + 8.5F, animTextColor.getRGB());
                  matrix.method_22909();
               }
            }
         }
      }

      scissorManager.pop();
      if (this.accountRepository.accountList.size() * accountSpacing > listHeight) {
         this.renderScrollbar(context, listY, listHeight, accountSpacing, textColor.getAlpha());
      }
   }

   private void renderScrollbar(class_332 context, float listY, float listHeight, float accountSpacing, int alpha) {
      float contentHeight = this.accountRepository.accountList.size() * accountSpacing;
      float maxScroll = Math.max(0.0F, contentHeight - listHeight);
      this.scroll = class_3532.method_15363(this.scroll, 0.0F, maxScroll);
      this.smoothedScroll = class_3532.method_15363(this.smoothedScroll, 0.0F, maxScroll);
      float scrollbarWidth = 2.0F;
      float scrollbarX = this.panelX + 160.0F - scrollbarWidth - 2.5F;
      float scrollbarY = listY + 1.0F;
      float scrollbarHeight = listHeight - 1.0F;
      Color bgScrollColor = new Color(30, 30, 30, (int)(100.0 * (alpha / 255.0)));
      rectangle.render(
         ShapeProperties.create(context.method_51448(), scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight)
            .round(1.0F)
            .color(bgScrollColor.getRGB())
            .build()
      );
      float handleHeight = Math.max(20.0F, listHeight * (listHeight / (contentHeight + listHeight)));
      float scrollRatio = this.smoothedScroll / maxScroll;
      float handleY = scrollbarY + (scrollbarHeight - handleHeight) * scrollRatio;
      Color handleColor = new Color(100, 100, 100, (int)(150.0 * (alpha / 255.0)));
      rectangle.render(
         ShapeProperties.create(context.method_51448(), scrollbarX, handleY, scrollbarWidth, handleHeight).round(1.0F).color(handleColor.getRGB()).build()
      );
   }

   private void drawAccountFace(class_332 context, Account account, float x, float y, int alpha) {
      UUID uuid = this.resolveAccountUuid(account);
      class_2960 skinTexture = this.skinTextureCache.computeIfAbsent(uuid.toString(), key -> {
         GameProfile profile = new GameProfile(uuid, account.name);
         class_2960 texture = class_310.method_1551().method_1582().method_52862(profile).comp_1626();
         return texture == null ? STEVE_TEXTURE : texture;
      });
      class_4587 matrices = context.method_51448();
      matrices.method_22903();
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);
      Render2D.drawTexture(context, skinTexture, x, y, 15.0F, 7.0F, 8, 8, 64, ColorAssist.getRect(1.0F), -1);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      matrices.method_22909();
   }

   private Color interpolateColor(Color start, Color end, float t) {
      int r = (int)(start.getRed() + (end.getRed() - start.getRed()) * t);
      int g = (int)(start.getGreen() + (end.getGreen() - start.getGreen()) * t);
      int b = (int)(start.getBlue() + (end.getBlue() - start.getBlue()) * t);
      int a = (int)(start.getAlpha() + (end.getAlpha() - start.getAlpha()) * t);
      return new Color(r, g, b, a);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      float inputY = this.panelY + 210.0F - 25.0F;
      float textFieldX = this.panelX + 5.0F;
      float textFieldY = inputY - 8.0F;
      if (button == 0 && this.isInBounds(mouseX, mouseY, textFieldX, textFieldY + 8.0F, 130.0F, 20.0F)) {
         this.handleTextFieldClick(mouseX);
         return true;
      } else {
         this.typing = false;
         this.clearSelection();
         if (button == 0 && this.isInBounds(mouseX, mouseY, this.panelX + 160.0F - 25.0F, inputY + 2.5F, 15.0F, 15.0F)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastActionTime >= 250L) {
               this.lastActionTime = currentTime;
               this.addRandomAccount();
            }

            return true;
         } else {
            return this.handleAccountListClick(mouseX, mouseY, button);
         }
      }
   }

   private void handleTextFieldClick(double mouseX) {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastClick < 250L) {
         this.selStart = 0;
         this.selEnd = this.typedText.length();
      } else {
         this.typing = true;
         this.cursorPos = this.getCursorIndexAt(mouseX);
         this.selStart = this.cursorPos;
         this.selEnd = this.cursorPos;
         this.lastClick = currentTime;
      }

      this.dragging = true;
   }

   private void addRandomAccount() {
      String accountName = this.generateRandomName();
      if (!this.accountExists(accountName)) {
         String offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + accountName).getBytes(StandardCharsets.UTF_8)).toString();
         Account newAccount = new Account(accountName, false, false, null, offlineUuid, "0");
         this.accountRepository.accountList.add(newAccount);
         this.accountRepository.accountList.sort((a1, a2) -> Boolean.compare(a2.starred, a1.starred));
         Animation anim = new InOutBack().setValue(1.0).setMs(300);
         anim.setDirection(Direction.FORWARDS);
         anim.reset();
         this.accountAnimations.put(offlineUuid, anim);
         this.typedText = "";
         this.cursorPos = 0;
         this.clearSelection();
         this.setSession(newAccount);
         this.saveAccounts();
      }
   }

   private boolean accountExists(String accountName) {
      return accountName != null && !accountName.isBlank()
         ? this.accountRepository.accountList.stream().anyMatch(account -> account.name != null && account.name.equalsIgnoreCase(accountName))
         : false;
   }

   private String generateRandomName() {
      char[] vowels = new char[]{'a', 'e', 'i', 'o', 'u'};
      char[] consonants = new char[]{'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};
      StringBuilder generatedName = new StringBuilder();
      int length = 6 + this.rand.nextInt(5);
      boolean startWithVowel = this.rand.nextBoolean();

      for (int i = 0; i < length; i++) {
         if (i % 2 == 0) {
            generatedName.append(startWithVowel ? vowels[this.rand.nextInt(vowels.length)] : consonants[this.rand.nextInt(consonants.length)]);
         } else {
            generatedName.append(startWithVowel ? consonants[this.rand.nextInt(consonants.length)] : vowels[this.rand.nextInt(vowels.length)]);
         }
      }

      if (this.rand.nextInt(100) < 30) {
         generatedName.append(this.rand.nextInt(100));
      }

      String result = generatedName.substring(0, 1).toUpperCase() + generatedName.substring(1);
      String candidate = result;
      if (this.accountRepository.accountList.stream().anyMatch(account -> account.name.equals(candidate))) {
         result = candidate + System.currentTimeMillis() % 1000L;
      }

      return result;
   }

   private boolean handleAccountListClick(double mouseX, double mouseY, int button) {
      float accountSpacing = 25.0F;
      float listY = this.panelY + 5.0F;
      float listHeight = 175.0F;

      for (int i = 0; i < this.accountRepository.accountList.size(); i++) {
         Account account = this.accountRepository.accountList.get(i);
         float accY = this.accountYPositions.getOrDefault(account.uuid, this.panelY + 10.0F + i * accountSpacing - this.smoothedScroll);
         if (!(accY + 20.0F < listY) && !(accY > listY + listHeight)) {
            if (button == 0 && this.isInBounds(mouseX, mouseY, this.panelX + 160.0F - 25.0F, accY + 6.5F, 15.0F, 15.0F)) {
               account.starred = !account.starred;
               this.accountRepository.accountList.sort((a1, a2) -> Boolean.compare(a2.starred, a1.starred));
               this.saveAccounts();
               return true;
            }

            if (button == 0 && this.isInBounds(mouseX, mouseY, this.panelX + 5.0F, accY, 149.0F, 20.0F)) {
               this.currentAccount = account.name;
               this.accountRepository.currentAccount = account.name;
               this.setSession(account);
               this.saveAccounts();
               return true;
            }

            if (button == 1 && this.isInBounds(mouseX, mouseY, this.panelX + 5.0F, accY, 149.0F, 20.0F)) {
               long currentTime = System.currentTimeMillis();
               if (currentTime - this.lastActionTime >= 250L) {
                  this.lastActionTime = currentTime;
                  Account accountToRemove = this.accountRepository.accountList.get(i);
                  if (accountToRemove.name.equals(this.currentAccount) || accountToRemove.name.equals(this.accountRepository.currentAccount)) {
                     this.accountRepository.currentAccount = "";
                     this.currentAccount = "";
                     this.saveAccounts();
                     if (Force.getInstance().getDiscordManager() != null) {
                        Force.getInstance().getDiscordManager().updatePresenceImmediately();
                     }
                  }

                  Animation removeAnim = new InOutBack().setValue(1.0).setMs(250);
                  removeAnim.setDirection(Direction.BACKWARDS);
                  removeAnim.reset();
                  this.accountRemoveAnimations.put(accountToRemove.uuid, removeAnim);
                  new Thread(() -> {
                     try {
                        Thread.sleep(250L);
                        this.accountRepository.accountList.remove(accountToRemove);
                        this.accountYPositions.remove(accountToRemove.uuid);
                        this.accountAnimations.remove(accountToRemove.uuid);
                        this.accountRemoveAnimations.remove(accountToRemove.uuid);
                        this.skinTextureCache.remove(accountToRemove.uuid);
                        this.clampListScroll(175.0F);
                        this.saveAccounts();
                     } catch (InterruptedException var3) {
                        Thread.currentThread().interrupt();
                     }
                  }).start();
               }

               return true;
            }
         }
      }

      return false;
   }

   private void setSession(Account account) {
      SessionHelper.applyOfflineSession(account.name, this.resolveAccountUuid(account));
      this.accountRepository.currentAccount = account.name;
      this.currentAccount = account.name;
      if (Force.getInstance().getDiscordManager() != null) {
         Force.getInstance().getDiscordManager().updatePresenceImmediately();
      }
   }

   private UUID resolveAccountUuid(Account account) {
      String name = account.name != null && !account.name.isBlank() ? account.name : "Player";
      if (account.uuid != null && !account.uuid.isBlank()) {
         try {
            return UUID.fromString(account.uuid);
         } catch (IllegalArgumentException var4) {
         }
      }

      UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
      account.uuid = offlineUuid.toString();
      return offlineUuid;
   }

   private void saveAccounts() {
      Force.getInstance().getFileController().saveFile(AccountFile.class);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
      float listY = this.panelY + 5.0F;
      float listHeight = 175.0F;
      this.clampListScroll(listHeight);
      if (this.accountRepository.accountList.size() * 25.0F > listHeight && this.isInBounds(mouseX, mouseY, this.panelX, listY, 160.0F, listHeight)) {
         float maxScroll = this.getMaxListScroll(listHeight);
         this.scroll = (float)(this.scroll - vertical * 20.0);
         this.scroll = class_3532.method_15363(this.scroll, 0.0F, maxScroll);
         return true;
      } else {
         return false;
      }
   }

   private float getMaxListScroll(float listHeight) {
      return Math.max(0.0F, this.accountRepository.accountList.size() * 25.0F - listHeight);
   }

   private void clampListScroll(float listHeight) {
      float maxScroll = this.getMaxListScroll(listHeight);
      float previousScroll = this.scroll;
      float previousSmoothedScroll = this.smoothedScroll;
      if (maxScroll <= 0.0F) {
         this.scroll = 0.0F;
         this.smoothedScroll = 0.0F;
         if (previousScroll != this.scroll || previousSmoothedScroll != this.smoothedScroll) {
            this.accountYPositions.clear();
         }
      } else {
         this.scroll = class_3532.method_15363(this.scroll, 0.0F, maxScroll);
         this.smoothedScroll = class_3532.method_15363(this.smoothedScroll, 0.0F, maxScroll);
         if (previousScroll != this.scroll || previousSmoothedScroll != this.smoothedScroll) {
            this.accountYPositions.clear();
         }
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button) {
      if (this.dragging && button == 0) {
         this.cursorPos = this.getCursorIndexAt(mouseX);
         this.selEnd = this.cursorPos;
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased() {
      this.dragging = false;
      return false;
   }

   public boolean charTyped(char chr) {
      if (this.typing && this.typedText.length() < 16) {
         this.deleteSelectedText();
         this.typedText = this.typedText.substring(0, this.cursorPos) + chr + this.typedText.substring(this.cursorPos);
         this.cursorPos++;
         this.clearSelection();
         this.updateTextXOffset();
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode) {
      if (!this.typing) {
         return false;
      } else if (keyCode == 341 || keyCode == 345) {
         return false;
      } else {
         if (class_310.method_1551().field_1755 != null && class_437.method_25441()) {
            switch (keyCode) {
               case 65:
                  this.selStart = 0;
                  this.selEnd = this.typedText.length();
                  return true;
               case 67:
                  if (this.hasSelection()) {
                     GLFW.glfwSetClipboardString(class_310.method_1551().method_22683().method_4490(), this.getSelectedText());
                  }

                  return true;
               case 86:
                  String clipboard = GLFW.glfwGetClipboardString(class_310.method_1551().method_22683().method_4490());
                  if (clipboard != null) {
                     this.deleteSelectedText();
                     this.typedText = this.typedText.substring(0, this.cursorPos) + clipboard + this.typedText.substring(this.cursorPos);
                     this.cursorPos = this.cursorPos + clipboard.length();
                     this.clearSelection();
                     this.updateTextXOffset();
                  }

                  return true;
            }
         }

         switch (keyCode) {
            case 257:
               if (this.typedText.length() >= 3 && this.typedText.length() <= 16) {
                  long currentTime = System.currentTimeMillis();
                  if (currentTime - this.lastActionTime >= 250L) {
                     if (this.accountExists(this.typedText)) {
                        this.typedText = "";
                        this.cursorPos = 0;
                        this.typing = false;
                        this.clearSelection();
                        return true;
                     }

                     this.lastActionTime = currentTime;
                     String offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.typedText).getBytes(StandardCharsets.UTF_8)).toString();
                     Account newAccount = new Account(this.typedText, false, false, null, offlineUuid, "0");
                     this.accountRepository.accountList.add(newAccount);
                     this.accountRepository.accountList.sort((a1, a2) -> Boolean.compare(a2.starred, a1.starred));
                     Animation anim = new InOutBack().setValue(1.0).setMs(300);
                     anim.setDirection(Direction.FORWARDS);
                     anim.reset();
                     this.accountAnimations.put(offlineUuid, anim);
                     this.typedText = "";
                     this.cursorPos = 0;
                     this.typing = false;
                     this.clearSelection();
                     this.setSession(newAccount);
                     this.saveAccounts();
                  }
               }

               return true;
            case 258:
            case 260:
            case 261:
            default:
               break;
            case 259:
               if (this.hasSelection()) {
                  this.deleteSelectedText();
               } else if (this.cursorPos > 0) {
                  this.typedText = this.typedText.substring(0, this.cursorPos - 1) + this.typedText.substring(this.cursorPos);
                  this.cursorPos--;
               }

               this.updateTextXOffset();
               return true;
            case 262:
               if (this.cursorPos < this.typedText.length()) {
                  this.cursorPos++;
               }

               this.updateSelectionAfterMove();
               this.updateTextXOffset();
               return true;
            case 263:
               if (this.cursorPos > 0) {
                  this.cursorPos--;
               }

               this.updateSelectionAfterMove();
               this.updateTextXOffset();
               return true;
         }

         return false;
      }
   }

   public void reset() {
      this.typing = false;
      this.clearSelection();
   }

   private boolean isInBounds(double mx, double my, float x, float y, float w, float h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   private boolean hasSelection() {
      return this.selStart != this.selEnd;
   }

   private String getSelectedText() {
      int start = Math.min(this.selStart, this.selEnd);
      int end = Math.max(this.selStart, this.selEnd);
      return this.typedText.substring(start, end);
   }

   private void deleteSelectedText() {
      if (this.hasSelection()) {
         int start = Math.min(this.selStart, this.selEnd);
         int end = Math.max(this.selStart, this.selEnd);
         this.typedText = this.typedText.substring(0, start) + this.typedText.substring(end);
         this.cursorPos = start;
         this.clearSelection();
      }
   }

   private void clearSelection() {
      this.selStart = this.cursorPos;
      this.selEnd = this.cursorPos;
   }

   private void updateSelectionAfterMove() {
      if (class_310.method_1551().field_1755 != null && class_437.method_25442()) {
         this.selEnd = this.cursorPos;
      } else {
         this.clearSelection();
      }
   }

   private int getCursorIndexAt(double mouseX) {
      float textFieldX = this.panelX + 10.0F;
      FontRenderer font = Fonts.getSize(16, Fonts.Type.DEFAULT);
      float relX = (float)mouseX - textFieldX + this.textXOffset;
      int pos = 0;

      while (pos < this.typedText.length() && !(font.getStringWidth(this.typedText.substring(0, pos + 1)) > relX)) {
         pos++;
      }

      return pos;
   }

   private void updateTextXOffset() {
      float textFieldWidth = 120.0F;
      FontRenderer font = Fonts.getSize(16, Fonts.Type.DEFAULT);
      float cursorX = font.getStringWidth(this.typedText.substring(0, this.cursorPos));
      if (cursorX < this.textXOffset) {
         this.textXOffset = cursorX;
      } else if (cursorX > this.textXOffset + textFieldWidth - 10.0F) {
         this.textXOffset = cursorX - (textFieldWidth - 10.0F);
      }
   }
}
