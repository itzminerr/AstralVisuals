package pl.astralvisuals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.api.ModInitializer;
import net.minecraft.class_310;
import net.minecraft.class_320;
import pl.astralvisuals.commands.CommandDispatcher;
import pl.astralvisuals.commands.manager.CommandRepository;
import pl.astralvisuals.common.repository.macro.MacroRepository;
import pl.astralvisuals.common.repository.way.WayRepository;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.display.screens.mainmenu.altmanager.impl.Account;
import pl.astralvisuals.display.screens.mainmenu.altmanager.impl.AccountRepository;
import pl.astralvisuals.features.module.ModuleProvider;
import pl.astralvisuals.features.module.ModuleRepository;
import pl.astralvisuals.features.module.ModuleSwitcher;
import pl.astralvisuals.main.client.ClientInfo;
import pl.astralvisuals.main.client.ClientInfoProvider;
import pl.astralvisuals.main.listener.ListenerRepository;
import pl.astralvisuals.utils.client.discord.DiscordManager;
import pl.astralvisuals.utils.client.logs.Logger;
import pl.astralvisuals.utils.client.managers.api.draggable.DraggableRepository;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.client.managers.file.DirectoryCreator;
import pl.astralvisuals.utils.client.managers.file.FileController;
import pl.astralvisuals.utils.client.managers.file.FileRepository;
import pl.astralvisuals.utils.client.managers.file.exception.FileProcessingException;
import pl.astralvisuals.utils.client.managers.file.impl.AccountFile;
import pl.astralvisuals.utils.client.session.SessionHelper;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.connection.tps.TPSCalculate;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.update.ClientVersion;
import pl.astralvisuals.utils.update.GitHubUpdater;

public class Force implements ModInitializer {
   static Force instance;
   private EventManager eventManager = new EventManager();
   private ModuleRepository moduleRepository;
   private ModuleSwitcher moduleSwitcher;
   private CommandRepository commandRepository;
   private CommandDispatcher commandDispatcher;
   private MacroRepository macroRepository = new MacroRepository(this.eventManager);
   private WayRepository wayRepository = new WayRepository(this.eventManager);
   private ModuleProvider moduleProvider;
   private DraggableRepository draggableRepository;
   private FileRepository fileRepository;
   private FileController fileController;
   private ScissorAssist scissorManager = new ScissorAssist();
   private ClientInfoProvider clientInfoProvider;
   private ListenerRepository listenerRepository;
   private AccountRepository accountRepository;
   private TPSCalculate tpsCalculate;
   private DiscordManager discordManager;
   private boolean initialized;

   public void onInitialize() {
      instance = this;
      GitHubUpdater.checkForUpdatesAsync();
      this.initClientInfoProvider();
      this.initModules();
      this.initDraggable();
      this.initFileManager();
      this.importLauncherAccount();
      this.initCommands();
      this.initListeners();
      SoundManager.init();
      this.loadCurrentAccount();
      this.initDiscord();
      MenuScreen menuScreen = new MenuScreen();
      menuScreen.initialize();
      this.initialized = true;
   }

   private void initDiscord() {
      this.discordManager = new DiscordManager();
      pl.astralvisuals.features.impl.movement.DiscordPresence module = pl.astralvisuals.features.impl.movement.DiscordPresence.getInstance();
      if (module == null || module.isState()) {
         this.discordManager.init();
      }
   }

   private void loadCurrentAccount() {
      if (this.accountRepository.currentAccount != null && !this.accountRepository.currentAccount.isEmpty()) {
         class_320 launcherSession = class_310.method_1551().method_1548();
         if (launcherSession == null || !this.accountRepository.currentAccount.equalsIgnoreCase(launcherSession.method_1676())) {
            Account currentAcc = this.accountRepository
               .accountList
               .stream()
               .filter(acc -> acc.name.equals(this.accountRepository.currentAccount))
               .findFirst()
               .orElse(null);
            if (currentAcc != null) {
               this.setSession(currentAcc);
            }
         }
      }
   }

   private void setSession(Account account) {
      SessionHelper.applyOfflineSession(account.name, this.resolveAccountUuid(account));
   }

   private UUID resolveAccountUuid(Account account) {
      String name = account.name != null && !account.name.isBlank() ? account.name : "Player";
      if (account.uuid != null && !account.uuid.isBlank()) {
         try {
            return UUID.fromString(account.uuid);
         } catch (IllegalArgumentException var4) {
         }
      }

      UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
      account.uuid = uuid.toString();
      this.fileController.saveFile(AccountFile.class);
      return uuid;
   }

   private void importLauncherAccount() {
      class_320 session = class_310.method_1551().method_1548();
      if (session != null && session.method_1676() != null && !session.method_1676().isBlank()) {
         String username = session.method_1676();
         boolean exists = this.accountRepository.accountList.stream().anyMatch(account -> account.name != null && account.name.equalsIgnoreCase(username));
         boolean changed = false;
         if (!exists) {
            UUID uuid = session.method_44717();
            if (uuid == null) {
               uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
            }

            this.accountRepository.accountList.add(new Account(username, false, false, null, uuid.toString(), session.method_1674()));
            changed = true;
         }

         if (this.accountRepository.currentAccount == null || this.accountRepository.currentAccount.isBlank()) {
            this.accountRepository.currentAccount = username;
            changed = true;
         }

         if (changed) {
            this.fileController.saveFile(AccountFile.class);
         }
      }
   }

   private void initDraggable() {
      this.draggableRepository = new DraggableRepository();
      this.draggableRepository.setup();
   }

   private void initModules() {
      this.moduleRepository = new ModuleRepository();
      this.moduleRepository.setup();
      this.moduleProvider = new ModuleProvider(this.moduleRepository.modules());
      this.moduleSwitcher = new ModuleSwitcher(this.moduleRepository.modules(), this.eventManager);
   }

   private void initCommands() {
      this.commandRepository = new CommandRepository();
      this.commandDispatcher = new CommandDispatcher(this.eventManager);
   }

   private void initClientInfoProvider() {
      File clientDirectory = new File(class_310.method_1551().field_1697, "\\AstralVisuals\\");
      File filesDirectory = new File(clientDirectory, "\\Files\\");
      this.clientInfoProvider = new ClientInfo(ClientVersion.displayName(), clientDirectory, filesDirectory);
   }

   private void initFileManager() {
      DirectoryCreator directoryCreator = new DirectoryCreator();
      directoryCreator.createDirectories(this.clientInfoProvider.clientDir(), this.clientInfoProvider.filesDir());
      this.cleanupLegacyArtifacts(this.clientInfoProvider.clientDir(), this.clientInfoProvider.filesDir());
      File customDir = new File(this.clientInfoProvider.clientDir(), "Custom");
      if (!customDir.exists()) {
         customDir.mkdirs();
      }

      new File(customDir, "Capes").mkdirs();
      new File(customDir, "KillSounds").mkdirs();

      this.fileRepository = new FileRepository();
      this.fileRepository.setup(this);
      this.accountRepository = new AccountRepository();
      this.fileRepository.getClientFiles().add(new AccountFile(this.accountRepository));
      this.fileController = new FileController(this.fileRepository.getClientFiles(), this.clientInfoProvider.filesDir());

      try {
         this.fileController.loadFiles();
      } catch (FileProcessingException var5) {
         Logger.error("Failed to load files: " + var5.getMessage());
      }

      try {
         this.fileController.saveFiles();
      } catch (FileProcessingException var4) {
         Logger.error("Failed to normalize files: " + var4.getMessage());
      }
   }

   private void initListeners() {
      this.listenerRepository = new ListenerRepository();
      this.listenerRepository.setup();
      this.tpsCalculate = new TPSCalculate();
   }

   private void cleanupLegacyArtifacts(File clientDirectory, File filesDirectory) {
      this.deleteUnexpectedChildren(clientDirectory, Set.of("Custom", "Files"), Set.of());
      this.deleteUnexpectedChildren(filesDirectory, Set.of(), Set.of("Accounts.json", "AutoCfg.json", "Friends.json", "Macro.json", "Prefix.json", "Way.json"));
   }

   private void deleteRecursively(File file) {
      if (file != null && file.exists()) {
         if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
               for (File child : children) {
                  this.deleteRecursively(child);
               }
            }
         }

         if (!file.delete()) {
            Logger.warn("Failed to delete legacy artifact: " + file.getAbsolutePath());
         }
      }
   }

   private void deleteUnexpectedChildren(File directory, Set<String> allowedDirectories, Set<String> allowedFiles) {
      File[] children = directory.listFiles();
      if (children != null) {
         for (File child : children) {
            if (child.isDirectory()) {
               if (!allowedDirectories.contains(child.getName())) {
                  this.deleteRecursively(child);
               }
            } else if (!allowedFiles.contains(child.getName())) {
               this.deleteRecursively(child);
            }
         }
      }
   }

   public EventManager getEventManager() {
      return this.eventManager;
   }

   public ModuleRepository getModuleRepository() {
      return this.moduleRepository;
   }

   public ModuleSwitcher getModuleSwitcher() {
      return this.moduleSwitcher;
   }

   public CommandRepository getCommandRepository() {
      return this.commandRepository;
   }

   public CommandDispatcher getCommandDispatcher() {
      return this.commandDispatcher;
   }

   public MacroRepository getMacroRepository() {
      return this.macroRepository;
   }

   public WayRepository getWayRepository() {
      return this.wayRepository;
   }

   public ModuleProvider getModuleProvider() {
      return this.moduleProvider;
   }

   public DraggableRepository getDraggableRepository() {
      return this.draggableRepository;
   }

   public FileRepository getFileRepository() {
      return this.fileRepository;
   }

   public FileController getFileController() {
      return this.fileController;
   }

   public ScissorAssist getScissorManager() {
      return this.scissorManager;
   }

   public ClientInfoProvider getClientInfoProvider() {
      return this.clientInfoProvider;
   }

   public ListenerRepository getListenerRepository() {
      return this.listenerRepository;
   }

   public AccountRepository getAccountRepository() {
      return this.accountRepository;
   }

   public TPSCalculate getTpsCalculate() {
      return this.tpsCalculate;
   }

   public DiscordManager getDiscordManager() {
      return this.discordManager;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public void setEventManager(EventManager eventManager) {
      this.eventManager = eventManager;
   }

   public void setModuleRepository(ModuleRepository moduleRepository) {
      this.moduleRepository = moduleRepository;
   }

   public void setModuleSwitcher(ModuleSwitcher moduleSwitcher) {
      this.moduleSwitcher = moduleSwitcher;
   }

   public void setCommandRepository(CommandRepository commandRepository) {
      this.commandRepository = commandRepository;
   }

   public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
      this.commandDispatcher = commandDispatcher;
   }

   public void setMacroRepository(MacroRepository macroRepository) {
      this.macroRepository = macroRepository;
   }

   public void setWayRepository(WayRepository wayRepository) {
      this.wayRepository = wayRepository;
   }

   public void setModuleProvider(ModuleProvider moduleProvider) {
      this.moduleProvider = moduleProvider;
   }

   public void setDraggableRepository(DraggableRepository draggableRepository) {
      this.draggableRepository = draggableRepository;
   }

   public void setFileRepository(FileRepository fileRepository) {
      this.fileRepository = fileRepository;
   }

   public void setFileController(FileController fileController) {
      this.fileController = fileController;
   }

   public void setScissorManager(ScissorAssist scissorManager) {
      this.scissorManager = scissorManager;
   }

   public void setClientInfoProvider(ClientInfoProvider clientInfoProvider) {
      this.clientInfoProvider = clientInfoProvider;
   }

   public void setListenerRepository(ListenerRepository listenerRepository) {
      this.listenerRepository = listenerRepository;
   }

   public void setAccountRepository(AccountRepository accountRepository) {
      this.accountRepository = accountRepository;
   }

   public void setTpsCalculate(TPSCalculate tpsCalculate) {
      this.tpsCalculate = tpsCalculate;
   }

   public void setDiscordManager(DiscordManager discordManager) {
      this.discordManager = discordManager;
   }

   public void setInitialized(boolean initialized) {
      this.initialized = initialized;
   }

   public static Force getInstance() {
      return instance;
   }
}
