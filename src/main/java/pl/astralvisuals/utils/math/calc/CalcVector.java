package pl.astralvisuals.utils.math.calc;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class CalcVector {
   public static class_243 lerpPosition(class_1297 entity) {
      float tickDelta = QuickImports.mc.method_61966().method_60637(true);
      return new class_243(
         entity.field_6014 + (entity.method_23317() - entity.field_6014) * tickDelta,
         entity.field_6036 + (entity.method_23318() - entity.field_6036) * tickDelta,
         entity.field_5969 + (entity.method_23321() - entity.field_5969) * tickDelta
      );
   }
}
