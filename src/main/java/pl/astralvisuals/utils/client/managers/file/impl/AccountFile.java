package pl.astralvisuals.utils.client.managers.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import pl.astralvisuals.display.screens.mainmenu.altmanager.impl.AccountData;
import pl.astralvisuals.display.screens.mainmenu.altmanager.impl.AccountRepository;
import pl.astralvisuals.utils.client.managers.file.ClientFile;
import pl.astralvisuals.utils.client.managers.file.exception.FileLoadException;
import pl.astralvisuals.utils.client.managers.file.exception.FileSaveException;

public class AccountFile extends ClientFile {
   private final AccountRepository accountRepository;

   public AccountFile(AccountRepository accountRepository) {
      super("Accounts");
      this.accountRepository = accountRepository;
   }

   @Override
   public void saveToFile(File path) throws FileSaveException {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      File file = new File(path, this.getName() + ".json");

      try {
         try (FileWriter writer = new FileWriter(file)) {
            AccountData data = new AccountData();
            data.accounts = this.accountRepository.accountList;
            data.currentAccount = this.accountRepository.currentAccount;
            gson.toJson(data, writer);
         }
      } catch (IOException | JsonIOException var9) {
         throw new FileSaveException("Failed to save accounts to file", var9);
      }
   }

   @Override
   public void loadFromFile(File path) throws FileLoadException {
      Gson gson = new Gson();
      File file = new File(path, this.getName() + ".json");
      if (file.exists()) {
         try {
            try (FileReader reader = new FileReader(file)) {
               AccountData data = (AccountData)gson.fromJson(reader, AccountData.class);
               if (data == null) {
                  return;
               }

               this.accountRepository.accountList.clear();
               if (data.accounts != null) {
                  this.accountRepository.accountList.addAll(data.accounts);
               }

               if (data.currentAccount != null) {
                  this.accountRepository.currentAccount = data.currentAccount;
               }
            }
         } catch (IOException var9) {
            throw new FileLoadException("Failed to load accounts from file", var9);
         } catch (JsonSyntaxException var10) {
            throw new FileLoadException("JSON syntax error, accounts config cannot be loaded", var10);
         } catch (JsonIOException var11) {
            throw new FileLoadException("JSON IO error, accounts config cannot be loaded", var11);
         }
      }
   }
}
