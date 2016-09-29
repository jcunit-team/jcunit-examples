package com.github.dakusui.geophile.jcunit;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.combinatoradix.Enumerator;
import com.github.dakusui.combinatoradix.Permutator;
import com.github.dakusui.combinatoradix.Utils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public abstract class SubsetLevels<E> implements LevelsProvider {
  public enum Mode {
    NOT_ORDERED {
      public Enumerator<String> createEnumerator(int k, String[] elementNames) {
        return new Combinator<>(asList(elementNames), k);
      }
    },
    ORDERED {
      public Enumerator<String> createEnumerator(int k, String[] elementNames) {
        return new Permutator<>(asList(elementNames), k);
      }
    };

    public abstract Enumerator<String> createEnumerator(int k, String[] elementNames);
  }

  List<List<String>> levels;

  public SubsetLevels(Mode mode, int min, int max, boolean includeNull, String... elementNames) {
    checkNotNull(mode);
    checkArgument(min <= max, "min must be smaller than or equal to max (given: min=%s, max=%s)", min, max);
    checkArgument(elementNames.length == Sets.newHashSet(elementNames).size(), "duplication found in %s", (Object[]) elementNames);
    checkArgument(max <= elementNames.length);
    checkArgument(min >= 0 || min < 0 && min == -1 && max == -1);
    if (min < 0) {
      min = max = elementNames.length;
    }
    this.levels = new ArrayList<>(calculateSize(min, max, elementNames.length));
    for (int k = min; k <= max; k++) {
      Iterables.addAll(this.levels, mode.createEnumerator(k, elementNames));
    }
    if (includeNull) {
      this.levels.add(null);
    }
  }

  private int calculateSize(int min, int max, int n) {
    int ret = 0;
    for (int k = min; k <= max; k++) {
      ret += Utils.nCk(n, k);
    }
    return ret;
  }

  protected abstract Function<String, E> getTranslator();

  @Override
  public int size() {
    return this.levels.size();
  }

  @Override
  public List<E> get(final int n) {
    return SubsetLevels.this.levels.get(n) == null ?
        null :
        new AbstractList<E>() {
          @Override
          public E get(int index) {
            return getTranslator().apply(SubsetLevels.this.levels.get(n).get(index));
          }

          @Override
          public int size() {
            return SubsetLevels.this.levels.get(n).size();
          }
        };
  }

  public static abstract class Base<E> extends SubsetLevels<E> {
    private final Function<String, E> func;

    public Base(Mode mode, int min, int max, boolean includeNull, String... names) {
      super(mode, min, max, includeNull, names);
      this.func = createTranslator();
    }

    protected abstract Function<String, E> createTranslator();

    @Override
    final protected Function<String, E> getTranslator() {
      return func;
    }
  }

  public static class PassThrough extends Base<String> {
    public PassThrough(
        @Param(source = Param.Source.CONFIG) String[] names,
        @Param(source = Param.Source.CONFIG, defaultValue = { "-1" }) int min,
        @Param(source = Param.Source.CONFIG, defaultValue = { "-1" }) int max,
        @Param(source = Param.Source.CONFIG, defaultValue = { "NOT_ORDERED" }) Mode mode,
        @Param(source = Param.Source.CONFIG, defaultValue = { "false" }) boolean includeNull
    ) {
      super(mode, min, max, includeNull, names);
    }

    @Override
    protected Function<String, String> createTranslator() {
      return new Function<String, String>() {
        @Override
        public String apply(String input) {
          return input;
        }
      };
    }
  }

  public static abstract class Concretizable<EE, E extends Concretizer<T, EE>, T> extends Base<E> {
    public Concretizable(Mode mode, int min, int max, boolean includeNull, String... names) {
      super(mode, min, max, includeNull, names);
    }
  }

  public static class FromFields<EE, E extends Concretizer<T, EE>, T> extends Concretizable<EE, E, T> {
    public FromFields(
        @Param(source = Param.Source.CONFIG) String[] names,
        @Param(source = Param.Source.CONFIG, defaultValue = { "-1" }) int min,
        @Param(source = Param.Source.CONFIG, defaultValue = { "-1" }) int max,
        @Param(source = Param.Source.CONFIG, defaultValue = { "NOT_ORDERED" }) Mode mode,
        @Param(source = Param.Source.CONFIG, defaultValue = { "false" }) boolean includeNull) {
      super(mode, min, max, includeNull, names);
    }

    @Override
    protected Function<String, E> createTranslator() {
      return new Function<String, E>() {
        @Override
        public E apply(final String input) {
          //noinspection unchecked
          return (E) new Concretizer<T, EE>() {
            @Override
            public EE concretize(T testObject) {
              try {
                //noinspection unchecked
                return (EE) checkNotNull(testObject).getClass().getField(input).get(testObject);
              } catch (IllegalAccessException | NoSuchFieldException e) {
                throw Checks.wrap(
                    e, "Failed to concretize. Exception was thrown during invocation of %s#%s()." +
                        "Check if it is present, public, and parameter-less. Or it threw an exception.",
                    testObject.getClass().getName(), input
                );
              }
            }

            public String toString() {
              return format("concretize(%s)", input);
            }
          };
        }
      };
    }
  }

  public static class FromMethods<EE, E extends Concretizer<T, EE>, T> extends Concretizable<EE, E, T> {
    public FromMethods(@Param(source = Param.Source.CONFIG) String[] names,
        @Param(source = Param.Source.CONFIG, defaultValue = { "-1" }) int min,
        @Param(source = Param.Source.CONFIG, defaultValue = { "-1" }) int max,
        @Param(source = Param.Source.CONFIG, defaultValue = { "NOT_ORDERED" }) Mode mode,
        @Param(source = Param.Source.CONFIG, defaultValue = { "false" }) boolean includeNull
    ) {
      super(mode, min, max, includeNull, names);
    }

    @Override
    protected Function<String, E> createTranslator() {
      return new Function<String, E>() {
        @Override
        public E apply(final String input) {
          //noinspection unchecked
          return (E) new Concretizer<T, EE>() {
            @Override
            public EE concretize(T testObject) {
              try {
                //noinspection unchecked
                return (EE) checkNotNull(testObject).getClass().getMethod(input).invoke(testObject);
              } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw Checks.wrap(
                    e, "Failed to concretize. Exception was thrown during invocation of %s#%s()." +
                        "Check if it is present, public, and parameter-less. Or it threw an exception.",
                    testObject.getClass().getName(), input
                );
              }
            }
          };
        }
      };
    }
  }
}
