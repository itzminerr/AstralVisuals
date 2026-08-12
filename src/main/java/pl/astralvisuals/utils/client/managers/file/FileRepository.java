package pl.astralvisuals.utils.client.managers.file;

import java.util.ArrayList;
import java.util.List;
import pl.astralvisuals.Force;
import pl.astralvisuals.utils.client.managers.file.impl.FriendFile;
import pl.astralvisuals.utils.client.managers.file.impl.MacroFile;
import pl.astralvisuals.utils.client.managers.file.impl.ModuleFile;
import pl.astralvisuals.utils.client.managers.file.impl.PrefixFile;
import pl.astralvisuals.utils.client.managers.file.impl.WayFile;

public class FileRepository {
   private final List<ClientFile> clientFiles = new ArrayList<>();

   public void setup(Force main) {
      this.register(
         new ModuleFile(main.getModuleRepository(), main.getDraggableRepository()),
         new MacroFile(main.getMacroRepository()),
         new WayFile(main.getWayRepository()),
         new PrefixFile(),
         new FriendFile()
      );
   }

   public void register(ClientFile... clientFIle) {
      this.clientFiles.addAll(List.of(clientFIle));
   }

   public List<ClientFile> getClientFiles() {
      return this.clientFiles;
   }
}
