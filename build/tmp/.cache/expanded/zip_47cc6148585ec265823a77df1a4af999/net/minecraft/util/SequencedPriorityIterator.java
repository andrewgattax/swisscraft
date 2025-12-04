package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public final class SequencedPriorityIterator<T> extends AbstractIterator<T> {
   private final Int2ObjectMap<Deque<T>> valuesByPriority = new Int2ObjectOpenHashMap<>();

   public void add(T p_312570_, int p_312199_) {
      this.valuesByPriority.computeIfAbsent(p_312199_, (p_310516_) -> {
         return Queues.newArrayDeque();
      }).addLast(p_312570_);
   }

   @Nullable
   protected T computeNext() {
      Optional<Deque<T>> optional = this.valuesByPriority.int2ObjectEntrySet().stream().filter((p_311260_) -> {
         return !p_311260_.getValue().isEmpty();
      }).max(Comparator.comparingInt(Map.Entry::getKey)).map(Map.Entry::getValue);
      return optional.map(Deque::removeFirst).orElseGet(() -> {
         return (T)this.endOfData();
      });
   }
}