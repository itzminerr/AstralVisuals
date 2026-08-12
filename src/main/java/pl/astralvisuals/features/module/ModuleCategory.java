package pl.astralvisuals.features.module;

public enum ModuleCategory {
   COMBAT("Бой"),
   RENDER("Рендер"),
   PLAYER("Игрок"),
   MISC("Разное"),
   CONFIGS("Конфиги");

   final String readableName;

   private ModuleCategory(final String readableName) {
      this.readableName = readableName;
   }

   public String getReadableName() {
      return this.readableName;
   }
}
