package com.github.dakusui.geophile.jcunit;

import com.github.dakusui.combinatoradix.Combinator;
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
import static java.util.Arrays.asList;

public abstract class SubsetLevelsProvider<E> implements LevelsProvider {
  List<List<String>> elementNames;

  public SubsetLevelsProvider(int min, int max, String... names) {
    checkArgument(min <= max, "min must be smaller than or equal to max (given: min=%s, max=%s)", min, max);
    checkArgument(names.length == Sets.newHashSet(names).size(), "duplication found in %s", (Object[]) names);
    checkArgument(min >= 0 || min < 0 && min == -1 && max == -1);
    if (min < 0) {
      min = max = names.length;
    }
    this.elementNames = new ArrayList<>(calculateSize(min, max, names.length));
    for (int k = min; k <= max; k++) {
      Iterables.addAll(this.elementNames, new Combinator<>(asList(names), k));
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
    return this.elementNames.size();
  }

  @Override
  public List<E> get(final int n) {
    return new AbstractList<E>() {
      @Override
      public E get(int index) {
        return getTranslator().apply(SubsetLevelsProvider.this.elementNames.get(n).get(index));
      }

      @Override
      public int size() {
        return SubsetLevelsProvider.this.elementNames.size();
      }
    };
  }

  public static abstract class Base<E> extends SubsetLevelsProvider<E> {
    private final Function<String, E> func;

    public Base(int min, int max, String... names) {
      super(min, max, names);
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
        @Param(source = Param.Source.CONFIG) int min,
        @Param(source = Param.Source.CONFIG) int max,
        @Param(source = Param.Source.CONFIG) String... names) {
      super(min, max, names);
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
    public Concretizable(int min, int max, String... names) {
      super(min, max, names);
    }
  }

  public static class FieldBased<EE, E extends Concretizer<T, EE>, T> extends Concretizable<EE, E, T> {
    public FieldBased(int min, int max, String... names) {
      super(min, max, names);
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
          };
        }
      };
    }
  }

  public static class MethodBased<EE, E extends Concretizer<T, EE>, T> extends Concretizable<EE, E, T> {
    public MethodBased(int min, int max, String... names) {
      super(min, max, names);
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
