package pl.astralvisuals.utils.display.interfaces;

import com.google.gson.Gson;
import net.minecraft.class_1041;
import net.minecraft.class_289;
import net.minecraft.class_310;
import net.minecraft.class_9779;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.WindowManager;
import pl.astralvisuals.utils.display.draw.DrawEngine;
import pl.astralvisuals.utils.display.draw.DrawEngineImpl;
import pl.astralvisuals.utils.display.shape.implement.Arc;
import pl.astralvisuals.utils.display.shape.implement.Blur;
import pl.astralvisuals.utils.display.shape.implement.Image;
import pl.astralvisuals.utils.display.shape.implement.Rectangle;

public interface QuickImports extends QuickLogger {
   class_310 mc = class_310.method_1551();
   class_9779 tickCounter = mc.method_61966();
   class_1041 window = mc.method_22683();
   class_289 tessellator = class_289.method_1348();
   DrawEngine drawEngine = new DrawEngineImpl();
   Rectangle rectangle = new Rectangle();
   Blur blur = new Blur();
   Arc arc = new Arc();
   Image image = new Image();
   Gson gson = new Gson();
   WindowManager windowManager = new WindowManager();
}
