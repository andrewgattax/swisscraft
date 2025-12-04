package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.ScoreAccess;

public class OperationArgument implements ArgumentType<OperationArgument.Operation> {
   private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
   private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType(Component.translatable("arguments.operation.invalid"));
   private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType(Component.translatable("arguments.operation.div0"));

   public static OperationArgument operation() {
      return new OperationArgument();
   }

   public static OperationArgument.Operation getOperation(CommandContext<CommandSourceStack> p_103276_, String p_103277_) {
      return p_103276_.getArgument(p_103277_, OperationArgument.Operation.class);
   }

   public OperationArgument.Operation parse(StringReader p_103274_) throws CommandSyntaxException {
      if (!p_103274_.canRead()) {
         throw ERROR_INVALID_OPERATION.create();
      } else {
         int i = p_103274_.getCursor();

         while(p_103274_.canRead() && p_103274_.peek() != ' ') {
            p_103274_.skip();
         }

         return getOperation(p_103274_.getString().substring(i, p_103274_.getCursor()));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_103302_, SuggestionsBuilder p_103303_) {
      return SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, p_103303_);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static OperationArgument.Operation getOperation(String p_103282_) throws CommandSyntaxException {
      return (p_103282_.equals("><") ? (p_308356_, p_308357_) -> {
         int i = p_308356_.get();
         p_308356_.set(p_308357_.get());
         p_308357_.set(i);
      } : getSimpleOperation(p_103282_));
   }

   private static OperationArgument.SimpleOperation getSimpleOperation(String p_103287_) throws CommandSyntaxException {
      OperationArgument.SimpleOperation operationargument$simpleoperation;
      switch (p_103287_) {
         case "=":
            operationargument$simpleoperation = (p_103298_, p_103299_) -> {
               return p_103299_;
            };
            break;
         case "+=":
            operationargument$simpleoperation = Integer::sum;
            break;
         case "-=":
            operationargument$simpleoperation = (p_103292_, p_103293_) -> {
               return p_103292_ - p_103293_;
            };
            break;
         case "*=":
            operationargument$simpleoperation = (p_103289_, p_103290_) -> {
               return p_103289_ * p_103290_;
            };
            break;
         case "/=":
            operationargument$simpleoperation = (p_264713_, p_264714_) -> {
               if (p_264714_ == 0) {
                  throw ERROR_DIVIDE_BY_ZERO.create();
               } else {
                  return Mth.floorDiv(p_264713_, p_264714_);
               }
            };
            break;
         case "%=":
            operationargument$simpleoperation = (p_103271_, p_103272_) -> {
               if (p_103272_ == 0) {
                  throw ERROR_DIVIDE_BY_ZERO.create();
               } else {
                  return Mth.positiveModulo(p_103271_, p_103272_);
               }
            };
            break;
         case "<":
            operationargument$simpleoperation = Math::min;
            break;
         case ">":
            operationargument$simpleoperation = Math::max;
            break;
         default:
            throw ERROR_INVALID_OPERATION.create();
      }

      return operationargument$simpleoperation;
   }

   @FunctionalInterface
   public interface Operation {
      void apply(ScoreAccess p_310471_, ScoreAccess p_312233_) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface SimpleOperation extends OperationArgument.Operation {
      int apply(int p_103309_, int p_103310_) throws CommandSyntaxException;

      default void apply(ScoreAccess p_311079_, ScoreAccess p_311087_) throws CommandSyntaxException {
         p_311079_.set(this.apply(p_311079_.get(), p_311087_.get()));
      }
   }
}