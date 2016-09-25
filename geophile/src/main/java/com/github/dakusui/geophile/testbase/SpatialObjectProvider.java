package com.github.dakusui.geophile.testbase;

import com.github.dakusui.geophile.mymodel.SpatialObject;

public interface SpatialObjectProvider {
  SpatialObject get();

  boolean forIndex();

  SpatialObjectProvider forIndex(boolean forIndex);

  class Impl implements SpatialObjectProvider {
    private final SpatialObject object;

    private final boolean forIndex;

    public Impl(SpatialObject object, boolean forIndex) {
      this.object = object;
      this.forIndex = forIndex;
    }

    @Override
    public SpatialObject get() {
      return this.object;
    }

    @Override
    public boolean forIndex() {
      return this.forIndex;
    }

    @Override
    public SpatialObjectProvider forIndex(boolean forIndex) {
      return new Impl(this.object, forIndex);
    }
  }

  enum Standard implements SpatialObjectProvider {
    BOX1(new SpatialObject.Box(10, 10, 20, 20)),
    BOX2(new SpatialObject.Box(15, 15, 25, 25)),
    BOX3(new SpatialObject.Box(25, 10, 35, 20)),
    POINT1(new SpatialObject.Point(18, 13)),
    POINT2(new SpatialObject.Point(18, 18)),
    POINT3(new SpatialObject.Point(40, 45)),
    ;

    private final SpatialObject object;

    Standard(SpatialObject object) {
      this.object = object;
    }

    @Override
    public SpatialObject get() {
      return this.object;
    }

    @Override
    public boolean forIndex() {
      return true;
    }

    @Override
    public SpatialObjectProvider forIndex(boolean forIndex) {
      return new Impl(this.object, forIndex);
    }
  }
}
