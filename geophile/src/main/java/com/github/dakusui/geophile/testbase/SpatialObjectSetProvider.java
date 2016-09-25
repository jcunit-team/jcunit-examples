package com.github.dakusui.geophile.testbase;

import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.google.common.base.Function;

import static com.github.dakusui.geophile.testbase.SpatialObjectProvider.Standard.*;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

public interface SpatialObjectSetProvider {
  SpatialObjectSet get();

  enum Standard implements SpatialObjectSetProvider {
    //            EMPTY(),
    //            ONE_BOX(SpatialObjectProvider.Standard.BOX1),
    //            ONE_POINT(SpatialObjectProvider.Standard.POINT1),
    MIXED(
        BOX1.forIndex(true),
        BOX2.forIndex(true),
        POINT1.forIndex(true),
        POINT2.forIndex(true),
        BOX3.forIndex(false),
        POINT3.forIndex(false)
    );

    private final SpatialObjectSet spatialObjectSet;

    Standard(SpatialObjectProvider... spatialObjectProviders) {
      this(new SpatialObjectSet.Impl(spatialObjectProviders));
    }

    Standard(SpatialObjectSet spatialObjectSet) {
      this.spatialObjectSet = spatialObjectSet;
    }

    @Override
    public SpatialObjectSet get() {
      return this.spatialObjectSet;
    }

    private static SpatialObject[] toSpatialObjects(SpatialObjectProvider[] spatialObjectProviders) {
      return toArray(
          transform(asList(spatialObjectProviders),
              new Function<SpatialObjectProvider, SpatialObject>() {
                @Override
                public SpatialObject apply(SpatialObjectProvider input) {
                  return input.get();
                }
              }
          ), SpatialObject.class);
    }

  }
}
