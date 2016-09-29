package com.github.dakusui.geophile.testbase.fsm;

import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

public enum SpatialObjectSpec implements FSMSpec<SpatialObjectDriver> {
  @StateSpec I {
    @Override
    public Expectation<SpatialObjectDriver> create(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(CREATED).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> read(Expectation.Builder<SpatialObjectDriver> b) {
      return b.invalid(I, Exception.class).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> update(Expectation.Builder<SpatialObjectDriver> b) {
      return b.invalid(I, Exception.class).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> delete(Expectation.Builder<SpatialObjectDriver> b) {
      return b.invalid(I, Exception.class).build();
    }

    @Override
    public boolean check(SpatialObjectDriver spatialObject) {
      return !spatialObject.find();
    }
  },
  @StateSpec CREATED {
    @Override
    public Expectation<SpatialObjectDriver> create(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(CREATED).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> read(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(CREATED).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> update(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(UPDATED).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> delete(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(I).build();
    }

    @Override
    public boolean check(SpatialObjectDriver spatialObject) {
      return false;
    }
  },
  @StateSpec UPDATED {
    @Override
    public Expectation<SpatialObjectDriver> create(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(CREATED).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> read(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(UPDATED).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> update(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(UPDATED).build();
    }

    @Override
    public Expectation<SpatialObjectDriver> delete(Expectation.Builder<SpatialObjectDriver> b) {
      return b.valid(CREATED).build();
    }

    @Override
    public boolean check(SpatialObjectDriver spatialObject) {
      return spatialObject.find();
    }
  };

  @ActionSpec
  public abstract Expectation<SpatialObjectDriver> create(Expectation.Builder<SpatialObjectDriver> b);

  @ActionSpec
  public abstract Expectation<SpatialObjectDriver> read(Expectation.Builder<SpatialObjectDriver> b);

  @ActionSpec
  public abstract Expectation<SpatialObjectDriver> update(Expectation.Builder<SpatialObjectDriver> b);

  @ActionSpec
  public abstract Expectation<SpatialObjectDriver> delete(Expectation.Builder<SpatialObjectDriver> b);

  /**
   * Checks if a given {@code spatialObject} is in an expected state.
   *
   * @param spatialObject an SUT driver object.
   */
  @Override
  public abstract boolean check(SpatialObjectDriver spatialObject);
}
