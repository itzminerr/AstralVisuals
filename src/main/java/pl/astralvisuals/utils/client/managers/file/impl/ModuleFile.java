package pl.astralvisuals.utils.client.managers.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleRepository;
import pl.astralvisuals.features.module.setting.Setting;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.GroupSetting;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.features.module.setting.implement.TextSetting;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.client.managers.api.draggable.DraggableRepository;
import pl.astralvisuals.utils.client.managers.file.ClientFile;
import pl.astralvisuals.utils.client.managers.file.exception.FileLoadException;
import pl.astralvisuals.utils.client.managers.file.exception.FileSaveException;

public class ModuleFile extends ClientFile {
   private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final ModuleRepository moduleRepository;
   private final DraggableRepository draggableRepository;

   public ModuleFile(ModuleRepository moduleRepository, DraggableRepository draggableRepository) {
      super("AutoCfg");
      this.moduleRepository = moduleRepository;
      this.draggableRepository = draggableRepository;
   }

   @Override
   public void saveToFile(File path) throws FileSaveException {
      this.saveToFile(path, this.getName() + ".json");
   }

   @Override
   public void loadFromFile(File path) throws FileLoadException {
      this.loadFromFile(path, this.getName() + ".json");
   }

   @Override
   public void saveToFile(File path, String fileName) throws FileSaveException {
      JsonObject functionObject = this.createJsonObjectFromModules();
      File file = new File(path, fileName);
      this.writeJsonToFile(functionObject, file);
      super.saveToFile(path, fileName);
   }

   @Override
   public void loadFromFile(File path, String fileName) throws FileLoadException {
      File file = new File(path, fileName);
      JsonObject functionObject = this.readJsonFromFile(file);
      if (functionObject != null) {
         this.updateModulesFromJsonObject(functionObject);
      }

      super.loadFromFile(path, fileName);
   }

   private JsonObject createJsonObjectFromModules() {
      JsonObject functionObject = new JsonObject();

      for (Module module : this.moduleRepository.modules()) {
         JsonObject moduleObject = new JsonObject();
         moduleObject.addProperty("bind", module.getKey());
         moduleObject.addProperty("state", module.isState());
         module.settings().forEach(setting -> this.addSettingToJsonObject(moduleObject, setting));
         functionObject.add(module.getName().toLowerCase(), moduleObject);
      }

      for (AbstractDraggable draggable : this.draggableRepository.draggable()) {
         JsonObject draggableObject = new JsonObject();
         draggableObject.addProperty("posX", draggable.getX());
         draggableObject.addProperty("posY", draggable.getY());
         functionObject.add(draggable.getName().toLowerCase(), draggableObject);
      }

      return functionObject;
   }

   private void addSettingToJsonObject(JsonObject moduleObject, Setting setting) {
      if (setting instanceof BooleanSetting booleanSetting) {
         moduleObject.addProperty(setting.getName(), booleanSetting.isValue());
      }

      if (setting instanceof SliderSettings valueSetting) {
         moduleObject.addProperty(setting.getName(), valueSetting.getValue());
      }

      if (setting instanceof ColorSetting colorSetting) {
         // Сохраняем именно выбранный цвет (rawColor), а не синхронизированный из Client Color.
         moduleObject.addProperty(setting.getName(), colorSetting.rawColor());
         moduleObject.addProperty(setting.getName() + "_sync", colorSetting.isSync());
      }

      if (setting instanceof BindSetting bindSetting) {
         moduleObject.addProperty(setting.getName(), bindSetting.getKey());
      }

      if (setting instanceof TextSetting textSetting) {
         moduleObject.addProperty(setting.getName(), textSetting.getText());
      }

      if (setting instanceof SelectSetting selectSetting) {
         moduleObject.addProperty(setting.getName(), selectSetting.getSelected());
      }

      if (setting instanceof MultiSelectSetting multiSelectSetting) {
         List<String> selected = multiSelectSetting.getSelected();
         String selectedAsString = String.join(",", selected);
         moduleObject.addProperty(setting.getName(), selectedAsString);
      }

      if (setting instanceof GroupSetting groupSetting) {
         JsonObject groupObject = new JsonObject();
         groupObject.addProperty("state", groupSetting.isValue());

         for (Setting subSetting : groupSetting.getSubSettings()) {
            this.addSettingToJsonObject(groupObject, subSetting);
         }

         moduleObject.add(setting.getName(), groupObject);
      }
   }

   private void writeJsonToFile(JsonObject functionObject, File file) throws FileSaveException {
      try {
         try (FileWriter writer = new FileWriter(file)) {
            this.GSON.toJson(functionObject, writer);
         }
      } catch (IOException var8) {
         throw new FileSaveException("Failed to save module to file", var8);
      }
   }

   private JsonObject readJsonFromFile(File file) throws FileLoadException {
      if (!file.exists()) {
         return new JsonObject();
      } else {
         try {
            JsonObject var3;
            try (FileReader reader = new FileReader(file)) {
               var3 = JsonParser.parseReader(reader).getAsJsonObject();
            }

            return var3;
         } catch (IOException var7) {
            throw new FileLoadException("Failed to load module from file", var7);
         } catch (JsonIOException | JsonSyntaxException var8) {
            throw new FileLoadException("Failed to parse JSON from file", var8);
         }
      }
   }

   private void updateModulesFromJsonObject(JsonObject functionObject) {
      for (Module module : this.moduleRepository.modules()) {
         JsonObject moduleObject = functionObject.getAsJsonObject(module.getName().toLowerCase());
         if (moduleObject == null && module.getName().equalsIgnoreCase("Interface")) {
            moduleObject = functionObject.getAsJsonObject("hud");
         }

         if (moduleObject != null) {
            JsonObject resolvedModuleObject = moduleObject;
            if (moduleObject.has("bind") && moduleObject.has("state")) {
               module.setKey(moduleObject.get("bind").getAsInt());
               module.setState(moduleObject.get("state").getAsBoolean());
            }

            module.settings().forEach(setting -> this.updateSettingFromJsonObject(resolvedModuleObject, setting));
         }
      }

      for (AbstractDraggable draggable : this.draggableRepository.draggable()) {
         JsonObject draggableObject = functionObject.getAsJsonObject(draggable.getName().toLowerCase());
         if (draggableObject != null && draggableObject.has("posX") && draggableObject.has("posY")) {
            draggable.setX(draggableObject.get("posX").getAsInt());
            draggable.setY(draggableObject.get("posY").getAsInt());
         }
      }
   }

   private void updateSettingFromJsonObject(JsonObject moduleObject, Setting setting) {
      JsonElement settingElement = moduleObject.get(setting.getName());
      if (settingElement != null && !settingElement.isJsonNull()) {
         if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.setValue(settingElement.getAsBoolean());
         }

         if (setting instanceof SliderSettings valueSetting) {
            valueSetting.setValue(settingElement.getAsFloat());
         }

         if (setting instanceof ColorSetting colorSetting) {
            colorSetting.setColor(settingElement.getAsInt());
            JsonElement syncElement = moduleObject.get(setting.getName() + "_sync");
            if (syncElement != null && !syncElement.isJsonNull()) {
               colorSetting.setSync(syncElement.getAsBoolean());
            }
         }

         if (setting instanceof BindSetting bindSetting) {
            bindSetting.setKey(settingElement.getAsInt());
         }

         if (setting instanceof TextSetting textSetting) {
            textSetting.setText(settingElement.getAsString());
         }

         if (setting instanceof SelectSetting selectSetting) {
            selectSetting.setSelected(settingElement.getAsString());
         }

         if (setting instanceof MultiSelectSetting multiSelectSetting) {
            String asString = settingElement.getAsString();
            List<String> selectedList = new ArrayList<>(Arrays.asList(asString.split(",")));
            selectedList.removeIf(s -> !multiSelectSetting.getList().contains(s));
            multiSelectSetting.setSelected(selectedList);
         }

         if (setting instanceof GroupSetting groupSetting) {
            JsonObject groupObject = settingElement.getAsJsonObject();
            if (groupObject.has("state")) {
               groupSetting.setValue(groupObject.get("state").getAsBoolean());
            }

            for (Setting subSetting : groupSetting.getSubSettings()) {
               this.updateSettingFromJsonObject(groupObject, subSetting);
            }
         }
      }
   }
}
