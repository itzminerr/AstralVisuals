package pl.astralvisuals.features.impl.movement;

import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.chat.ChatMessage;

public final class AntiBanChat extends Module {
   private static final String[] FORBIDDEN_WORDS = {
      "экспа", "экспенсив", "экспой", "нуриком", "целкой", "целка", "newcode", "ньюкод", "нурсултан", "целестиал",
      "нурик", "атернос", "aternos", "expa", "celka", "nurik", "expensive", "celestial", "nursultan", "фанпей", "funpay",
      "fluger", "флюгер", "акриен", "akrien", "фантайм", "funtime", "rich", "рич", "wild", "вилд", "excellent", "экселлент",
      "matix", "impact", "матикс", "импакт", "wurst", "monoton", "монотон", "катлаван", "catlavan", "catlawan", "dimasik",
      "димасик", "bro9i", "броя", "broya", "energy", "энерджи", "haruka", "haru", "харука", "holyworld", "холиворлд",
      "холиворд", "холик", "холике", "reallyworld", "релик", "рилик", "рилике", "риликс", "рили", "delta", "дельта", "делта",
      "wexside", "векс", "вексайд", "элитрабобик", "нурбек", "nurbek", "плейрок", "playerock", "сатурн", "saturn",
      "spookytime", "спукитайм", "спуки", "хв", "целк", "мам", "мама", "маму", "маме", "мамка", "мамке", "мамой",
      "пап", "папа", "папу", "папой", "папке", "родител", "семь", "семья", "семьи", "семье", "семью", "батя", "отец",
      "отца", "отцом", "матери", "мать", "бабушк", "бабка", "бабке", "бабул", "бабус", "дед", "дедушк", "дедок",
      "дедус", "внук", "внучк", "внучка", "внучек", "сын", "сына", "сынок", "сыну", "дочь", "дочка", "дочк", "дочур",
      "брат", "братик", "братишк", "сестр", "сестра", "сестрён", "сестрич", "тёт", "тет", "тёть", "тёта", "дяд",
      "дяде", "дядя", "дядь", "вну", "внуч", "родня", "родствен", "племян"
   };
   private static final char[] REPLACEMENTS = {'#', '$', '%', '&', '*', '1', '2', '3', '4'};
   private static final double[] CHANCES = {0.3, 0.5, 0.7};
   private final Random random = new Random();

   public AntiBanChat() {
      super("AntiBanChat", "AntiBanChat", ModuleCategory.PLAYER);
   }

   public static AntiBanChat getInstance() {
      return Instance.get(AntiBanChat.class);
   }

   public String protect(String message) {
      if (!this.isState() || message == null || message.isEmpty()) {
         return message;
      }
      String result = message;
      for (String word : FORBIDDEN_WORDS) {
         if (result.toLowerCase(Locale.ROOT).contains(word.toLowerCase(Locale.ROOT))) {
            Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(word));
            result = pattern.matcher(result).replaceAll(Matcher.quoteReplacement(this.encrypt(word)));
         }
      }
      return result;
   }

   public void notifyProtected() {
      ChatMessage.brandmessage("Сообщение обработано AntiBanChat.");
   }

   private String encrypt(String word) {
      double chance = CHANCES[this.random.nextInt(CHANCES.length)];
      StringBuilder result = new StringBuilder(word.length());
      for (int index = 0; index < word.length(); index++) {
         char character = word.charAt(index);
         if (Character.isLetter(character) && this.random.nextDouble() < chance) {
            result.append(REPLACEMENTS[this.random.nextInt(REPLACEMENTS.length)]);
         } else {
            result.append(character);
         }
      }
      return result.toString();
   }
}
