package pl.astralvisuals.utils.client.managers.api.draggable;

import net.minecraft.class_332;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.events.container.SetScreenEvent;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public abstract class AbstractDraggable implements Draggable, QuickImports, QuickLogger {
   private String name;
   private int x;
   private int y;
   private int width;
   private int height;
   private boolean dragging;
   private boolean canDrag;
   private int dragX;
   private int dragY;
   public final Animation scaleAnimation = new Decelerate().setValue(1.0).setMs(200);

   public AbstractDraggable(String name, int x, int y, int width, int height, boolean canDrag) {
      this.name = name;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.canDrag = canDrag;
   }

   @Override
   public boolean visible() {
      return true;
   }

   @Override
   public void tick() {
   }

   @Override
   public void packet(PacketEvent e) {
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      if (!this.dragging) {
         this.dragX = 0;
         this.dragY = 0;
      }

      Interface hud = Interface.getInstance();
      float hudScale = this.getHudScale();
      float mouseDragX = mouseX + this.dragX;
      float mouseDragY = mouseY + this.dragY;
      int windowWidth = window.method_4486();
      int windowHeight = window.method_4502();
      int radius = 3;
      if (this.dragging) {
         this.x = Math.round(this.clampPosition(mouseDragX, this.width, windowWidth, hudScale));
         this.y = Math.round(this.clampPosition(mouseDragY, this.height, windowHeight, hudScale));
      }

      for (AbstractDraggable drag : Force.getInstance().getDraggableRepository().draggable()) {
         if (drag.canDraw(hud, drag) && drag.canDrag && drag != this) {
            float x1 = drag.visualRight(hudScale) + radius - this.visualLeftOffset(hudScale);
            float x2 = drag.visualLeft(hudScale) - radius - this.visualRightOffset(hudScale);
            float y1 = drag.visualBottom(hudScale) + radius - this.visualTopOffset(hudScale);
            float y2 = drag.visualTop(hudScale) - radius - this.visualBottomOffset(hudScale);
            float y3 = drag.visualTop(hudScale) - this.visualTopOffset(hudScale);
            if (Math.abs(x1 - mouseDragX) <= radius) {
               this.drawRect(drag.visualRight(hudScale) + radius - 1.5F, 0.0F, 1.0F, windowHeight);
               this.x = Math.round(x1);
            }

            if (Math.abs(x2 - mouseDragX) <= radius) {
               this.drawRect(drag.visualLeft(hudScale) - radius + 0.5F, 0.0F, 1.0F, windowHeight);
               this.x = Math.round(x2);
            }

            if (Math.abs(y1 - mouseDragY) <= radius) {
               this.drawRect(0.0F, drag.visualBottom(hudScale) + radius - 1.5F, windowWidth, 1.0F);
               this.y = Math.round(y1);
            }

            if (Math.abs(y2 - mouseDragY) <= radius) {
               this.drawRect(0.0F, drag.visualTop(hudScale) - radius + 0.5F, windowWidth, 1.0F);
               this.y = Math.round(y2);
            }

            if (Math.abs(y3 - mouseDragY) <= radius) {
               this.drawRect(0.0F, drag.visualTop(hudScale) - 1.5F, windowWidth, 1.0F);
               this.y = Math.round(y3);
            }
         }
      }

      if (Math.abs(this.x + (this.width - windowWidth) / 2) <= radius) {
         this.drawRect(windowWidth / 2.0F - 0.5F, 0.0F, 1.0F, windowHeight);
         this.x = (windowWidth - this.width) / 2;
      }

      if (Math.abs(this.y + (this.height - windowHeight) / 2) <= radius) {
         this.drawRect(0.0F, windowHeight / 2.0F - 0.5F, windowWidth, 1.0F);
         this.y = (windowHeight - this.height) / 2;
      }
   }

   @Override
   public void setScreen(SetScreenEvent e) {
      if (PlayerInteractionHelper.isChat(e.getScreen())) {
         this.dragging = false;
         this.dragX = 0;
         this.dragY = 0;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isHovered(mouseX, mouseY) && button == 0 && this.canDrag) {
         this.dragging = true;
         this.dragX = this.x - (int)mouseX;
         this.dragY = this.y - (int)mouseY;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.dragging = false;
      this.dragX = 0;
      this.dragY = 0;
      return true;
   }

   public abstract void drawDraggable(class_332 var1);

   public void drawRect(float x, float y, float width, float height) {
      Render2D.drawQuad(x, y, width, height, ColorAssist.getText(0.5F));
   }

   public void stopAnimation() {
      this.scaleAnimation.setDirection(Direction.BACKWARDS);
   }

   public void startAnimation() {
      this.scaleAnimation.setDirection(Direction.FORWARDS);
   }

   public void validPosition() {
      float hudScale = this.getHudScale();
      this.x = Math.round(this.clampPosition(this.x, this.width, window.method_4486(), hudScale));
      this.y = Math.round(this.clampPosition(this.y, this.height, window.method_4502(), hudScale));
   }

   public boolean isHovered(double mouseX, double mouseY) {
      float hudScale = this.getHudScale();
      return mouseX >= this.visualLeft(hudScale)
         && mouseX <= this.visualRight(hudScale)
         && mouseY >= this.visualTop(hudScale)
         && mouseY <= this.visualBottom(hudScale);
   }

   private float getHudScale() {
      Interface hud = Interface.getInstance();
      return hud == null ? 1.0F : hud.getHudScale();
   }

   /**
    * Position stores the unscaled top-left corner, while rendering scales around the element center.
    * These limits therefore allow a smaller HUD to use the freed space on both screen edges.
    */
   private float clampPosition(float position, int size, int screenSize, float scale) {
      float min = (size * scale - size) / 2.0F;
      float max = screenSize - (size + size * scale) / 2.0F;
      if (max < min) {
         return (screenSize - size) / 2.0F;
      }
      return Math.max(min, Math.min(position, max));
   }

   private float visualLeftOffset(float scale) {
      return (this.width - this.width * scale) / 2.0F;
   }

   private float visualRightOffset(float scale) {
      return (this.width + this.width * scale) / 2.0F;
   }

   private float visualTopOffset(float scale) {
      return (this.height - this.height * scale) / 2.0F;
   }

   private float visualBottomOffset(float scale) {
      return (this.height + this.height * scale) / 2.0F;
   }

   private float visualLeft(float scale) {
      return this.x + this.visualLeftOffset(scale);
   }

   private float visualRight(float scale) {
      return this.x + this.visualRightOffset(scale);
   }

   private float visualTop(float scale) {
      return this.y + this.visualTopOffset(scale);
   }

   private float visualBottom(float scale) {
      return this.y + this.visualBottomOffset(scale);
   }

   public boolean isCloseAnimationFinished() {
      return this.scaleAnimation.isFinished(Direction.BACKWARDS);
   }

   public boolean canDraw(Interface hud, AbstractDraggable draggable) {
      return hud.isState() && hud.interfaceSettings.isSelected(draggable.getName()) && this.visible();
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setX(int x) {
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public void setDragging(boolean dragging) {
      this.dragging = dragging;
   }

   public void setCanDrag(boolean canDrag) {
      this.canDrag = canDrag;
   }

   public void setDragX(int dragX) {
      this.dragX = dragX;
   }

   public void setDragY(int dragY) {
      this.dragY = dragY;
   }

   public String getName() {
      return this.name;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isDragging() {
      return this.dragging;
   }

   public boolean isCanDrag() {
      return this.canDrag;
   }

   public int getDragX() {
      return this.dragX;
   }

   public int getDragY() {
      return this.dragY;
   }

   public Animation getScaleAnimation() {
      return this.scaleAnimation;
   }
}
