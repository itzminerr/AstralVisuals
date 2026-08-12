package pl.astralvisuals.utils.client.managers.api.command.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_2559;
import net.minecraft.class_2568.class_5247;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandInvalidTypeException;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public class Paginator<E> implements QuickLogger {
   public final List<E> entries;
   public int pageSize = 8;
   public int page = 1;

   public Paginator(List<E> entries) {
      this.entries = entries;
   }

   @SafeVarargs
   public Paginator(E... entries) {
      this.entries = Arrays.asList(entries);
   }

   public Paginator<E> setPageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
   }

   public int getMaxPage() {
      return (this.entries.size() - 1) / this.pageSize + 1;
   }

   public boolean validPage(int page) {
      return page > 0 && page <= this.getMaxPage();
   }

   public void skipPages(int pages) {
      this.page += pages;
   }

   public void display(Function<E, class_2561> transform, String commandPrefix) {
      int offset = (this.page - 1) * this.pageSize;

      for (int i = offset; i < offset + this.pageSize; i++) {
         if (i < this.entries.size()) {
            this.logDirect(transform.apply(this.entries.get(i)));
         } else {
            this.logDirect("", class_124.field_1063);
         }
      }

      boolean hasPrevPage = commandPrefix != null && this.validPage(this.page - 1);
      boolean hasNextPage = commandPrefix != null && this.validPage(this.page + 1);
      class_5250 prevPageComponent = class_2561.method_43470("<<");
      if (hasPrevPage) {
         prevPageComponent.method_10862(
            prevPageComponent.method_10866()
               .method_10958(new class_2558(class_2559.field_11750, String.format("%s %d", commandPrefix, this.page - 1)))
               .method_10949(new class_2568(class_5247.field_24342, class_2561.method_43470("Click to view previous page")))
         );
      } else {
         prevPageComponent.method_10862(prevPageComponent.method_10866().method_10977(class_124.field_1063));
      }

      class_5250 nextPageComponent = class_2561.method_43470(">>");
      if (hasNextPage) {
         nextPageComponent.method_10862(
            nextPageComponent.method_10866()
               .method_10958(new class_2558(class_2559.field_11750, String.format("%s %d", commandPrefix, this.page + 1)))
               .method_10949(new class_2568(class_5247.field_24342, class_2561.method_43470("Click to view next page")))
         );
      } else {
         nextPageComponent.method_10862(nextPageComponent.method_10866().method_10977(class_124.field_1063));
      }

      class_5250 pagerComponent = class_2561.method_43470("");
      pagerComponent.method_10862(pagerComponent.method_10866().method_10977(class_124.field_1080));
      pagerComponent.method_10852(prevPageComponent);
      pagerComponent.method_27693(" | ");
      pagerComponent.method_10852(nextPageComponent);
      pagerComponent.method_27693(String.format(" %d/%d", this.page, this.getMaxPage()));
      this.logDirect(pagerComponent);
   }

   public void display(Function<E, class_2561> transform) {
      this.display(transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
      int page = 1;
      consumer.requireMax(1);
      if (consumer.hasAny()) {
         page = consumer.getAs(Integer.class);
         if (!pagi.validPage(page)) {
            throw new CommandInvalidTypeException(consumer.consumed(), String.format("a valid page (1-%d)", pagi.getMaxPage()), consumer.consumed().getValue());
         }
      }

      pagi.skipPages(page - pagi.page);
      if (pre != null) {
         pre.run();
      }

      pagi.display(transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
      paginate(consumer, new Paginator<>(elems), pre, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
      paginate(consumer, Arrays.asList(elems), pre, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
      paginate(consumer, pagi, null, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
      paginate(consumer, new Paginator<>(elems), null, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, class_2561> transform, String commandPrefix) throws CommandException {
      paginate(consumer, Arrays.asList(elems), null, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, class_2561> transform) throws CommandException {
      paginate(consumer, pagi, pre, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, class_2561> transform) throws CommandException {
      paginate(consumer, new Paginator<>(elems), pre, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, class_2561> transform) throws CommandException {
      paginate(consumer, Arrays.asList(elems), pre, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, class_2561> transform) throws CommandException {
      paginate(consumer, pagi, null, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, class_2561> transform) throws CommandException {
      paginate(consumer, new Paginator<>(elems), null, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, class_2561> transform) throws CommandException {
      paginate(consumer, Arrays.asList(elems), null, transform, null);
   }
}
