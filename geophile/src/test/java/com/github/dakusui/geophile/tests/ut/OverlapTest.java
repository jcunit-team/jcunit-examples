package com.github.dakusui.geophile.tests.ut;

import com.github.dakusui.geophile.mymodel.SpatialJoinFilter;
import com.github.dakusui.geophile.mymodel.SpatialObject;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverlapTest {

  SpatialObject.Box a = new SpatialObject.Box(10, 10, 20, 20);

  @Test
  public void southeast() {
    SpatialObject.Box b = new SpatialObject.Box(18, 18, 28, 28);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }

  @Test
  public void northeast() {
    SpatialObject.Box b = new SpatialObject.Box(18, 3, 28, 13);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }

  @Test
  public void northwest() {
    SpatialObject.Box b = new SpatialObject.Box(3, 3, 13, 13);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }

  @Test
  public void southwest() {
    SpatialObject.Box b = new SpatialObject.Box(3, 18, 13, 28);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }


  @Test
  public void cover() {
    SpatialObject.Box b = new SpatialObject.Box(1, 1, 30, 30);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }


  @Test
  public void inside() {
    SpatialObject.Box b = new SpatialObject.Box(13, 13, 18, 18);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }

  @Test
  public void containsPoint() {
    SpatialObject.Point b = new SpatialObject.Point(15, 15);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }

  @Test
  public void notContainsPoint() {
    SpatialObject.Point b = new SpatialObject.Point(25, 25);
    assertFalse(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }

  @Test
  public void samePoint() {
    SpatialObject.Point a = new SpatialObject.Point(25, 25);
    SpatialObject.Point b = new SpatialObject.Point(25, 25);
    assertTrue(SpatialJoinFilter.INSTANCE.overlap(a, b));
  }
}
