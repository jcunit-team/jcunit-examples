package com.github.dakusui.geophile.mymodel;

import com.geophile.z.SpatialJoin;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpatialJoinFilter implements SpatialJoin.Filter<Object, Object> {
  public static SpatialJoinFilter INSTANCE = new SpatialJoinFilter();

  @Override
  public boolean overlap(Object record1, Object record2) {
    SpatialObject a = convertFromObject(record1);
    SpatialObject b = convertFromObject(record2);
    if (b instanceof SpatialObject.Point) {
      SpatialObject tmp = a;
      a = b;
      b = tmp;
    }
    if (a instanceof SpatialObject.Point) {
      if (b instanceof SpatialObject.Point) {
        return a.equals(b);
      } else {
        return ((SpatialObject.Box) b).contains((SpatialObject.Point) a);
      }
    }
    return ((SpatialObject.Box) a).overlaps((SpatialObject.Box) b);
  }

  private static SpatialObject convertFromObject(Object object) {
    SpatialObject ret = null;
    if (object instanceof Record) {
      object = ((Record) object).spatialObject();
    }
    if (object instanceof SpatialObject) {
      ret = (SpatialObject) object;
    }
    return checkNotNull(ret, "Failed to convert %s to a SpatialObject", object);
  }
}
