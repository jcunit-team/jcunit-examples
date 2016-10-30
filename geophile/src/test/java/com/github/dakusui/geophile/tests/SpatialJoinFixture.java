package com.github.dakusui.geophile.tests;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.github.dakusui.geophile.mymodel.Record;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import com.github.dakusui.geophile.mymodel.index.Index;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;

public class SpatialJoinFixture {
  protected SpatialJoinTest.OperationType operation;

  protected Space space;

  protected Index index;

  protected SpatialIndex<Record> spatialIndex;

  protected List<SpatialObject> spatialObjects;

  protected Record.Builder recordBuilder;

  protected SpatialJoin session;

  protected Set<SpatialObject> query;

  protected Set<SpatialObject> expectationForQuery;

  public String toString() {
    return format("(%s,%s,%s,%s)", space, index, spatialIndex, spatialObjects);
  }
}
