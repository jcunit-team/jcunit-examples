package com.github.dakusui.geophile.testbase;

import com.github.dakusui.geophile.mymodel.SpatialObject;


/*

     BOX1             BOX3
     +---------+      +---------+
     |    P1*  |      |         |
     |         |      |         |
     |    +----+----+ |         |
     |    | P2 |    | |         |
     |    | *  |    | |         |
     +----+----+    | +---------+
          |         |
          |         |
          +---------+
                 BOX2

                                    *P3
 */
public interface SpatialObjectInstance {
  SpatialObject.Box   BOX1   = new SpatialObject.Box(10, 10, 20, 20);
  SpatialObject.Box   BOX2   = new SpatialObject.Box(15, 15, 25, 25);
  SpatialObject.Box   BOX3   = new SpatialObject.Box(25, 10, 35, 20);
  SpatialObject.Point POINT1 = new SpatialObject.Point(18, 13);
  SpatialObject.Point POINT2 = new SpatialObject.Point(18, 18);
  SpatialObject.Point POINT3 = new SpatialObject.Point(40, 45);

  SpatialObject get();

  enum Index implements SpatialObjectInstance {
    BOX1(SpatialObjectInstance.BOX1),
    BOX2(SpatialObjectInstance.BOX2),
    POINT1(SpatialObjectInstance.POINT1),
    POINT2(SpatialObjectInstance.POINT2);

    private final SpatialObject object;

    Index(SpatialObject object) {
      this.object = object;
    }

    @Override
    public SpatialObject get() {
      return this.object;
    }
  }

  enum Queried implements SpatialObjectInstance {
    BOX1(SpatialObjectInstance.BOX1),
    BOX2(SpatialObjectInstance.BOX2),
    BOX3(SpatialObjectInstance.BOX3),
    POINT1(SpatialObjectInstance.POINT1),
    POINT2(SpatialObjectInstance.POINT2),
    POINT3(SpatialObjectInstance.POINT3),;

    private final SpatialObject object;

    Queried(SpatialObject object) {
      this.object = object;
    }

    @Override
    public SpatialObject get() {
      return this.object;
    }
  }

  enum JoinedIndex implements SpatialObjectInstance {
    BOX1(SpatialObjectInstance.BOX1),
    BOX2(SpatialObjectInstance.BOX2),
    POINT1(SpatialObjectInstance.POINT1),
    POINT2(SpatialObjectInstance.POINT2);

    private final SpatialObject object;

    JoinedIndex(SpatialObject object) {
      this.object = object;
    }

    @Override
    public SpatialObject get() {
      return this.object;
    }
  }
}
