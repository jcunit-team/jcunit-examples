package com.github.dakusui.geophile.testbase;

import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.github.dakusui.geophile.tests.OverlapTest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

public interface SpatialObjectSet {
  Iterable<SpatialObject> getObjectsMatchingWith(SpatialObject object);

  Iterable<SpatialObject> all();

  Iterable<SpatialObject> forQuery();

  Iterable<SpatialObject> forIndex();

  /**
   * This implementation relies on {@link SpatialJoinFilter#INSTANCE} to check if a pair of objects overlap
   * each other. And this means you are not able to verify the object's behavior, because even if it behaves incorrectly
   * the entire SUT (geophile + the {@link com.github.dakusui.geophile.mymodel} suite) will give incorrect behaviors
   * consistently and no test will fail.
   * This class should only be used for checking geophile's behavior and data model's correctness (in this case
   * {@link SpatialJoinFilter#INSTANCE}, especially) needs to be verified independently somewhere else.
   *
   * @see OverlapTest this test verifies {@link SpatialJoinFilter}'s overlap method's behavior.
   */
  class Impl implements SpatialObjectSet {
    private static final Function<SpatialObjectProvider, SpatialObject> GET       = new Function<SpatialObjectProvider, SpatialObject>() {
      @Override
      public SpatialObject apply(SpatialObjectProvider input) {
        return input.get();
      }
    };
    private static final Predicate<? super SpatialObjectProvider>       FOR_INDEX = new Predicate<SpatialObjectProvider>() {
      @Override
      public boolean apply(SpatialObjectProvider input) {
        return input.forIndex();
      }
    };

    private final Iterable<SpatialObjectProvider> spatialObjectProviders;

    public Impl(Iterable<SpatialObjectProvider> spatialObjectProviders) {
      this.spatialObjectProviders = spatialObjectProviders;
    }

    public Impl(SpatialObjectProvider... spatialObjectProviders) {
      this(asList(spatialObjectProviders));
    }

    @Override
    public Iterable<SpatialObject> getObjectsMatchingWith(final SpatialObject object) {
      return filter(
          this.forIndex(),
          new Predicate<SpatialObject>() {
            @Override
            public boolean apply(SpatialObject input) {
              return SpatialJoinFilter.INSTANCE.overlap(object, input);
            }
          });
    }

    @Override
    public Iterable<SpatialObject> all() {
      return transform(Impl.this.spatialObjectProviders, GET);
    }

    @Override
    public Iterable<SpatialObject> forQuery() {
      return all();
    }

    @Override
    public Iterable<SpatialObject> forIndex() {
      return transform(filter(this.spatialObjectProviders, FOR_INDEX), GET);
    }

    @Override
    public String toString() {
      return all().toString();
    }
  }
}
