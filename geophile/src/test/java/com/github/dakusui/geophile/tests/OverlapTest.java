package com.github.dakusui.geophile.tests;

import com.github.dakusui.geophile.mymodel.SpatialObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OverlapTest {

  SpatialObject.Box a = new SpatialObject.Box(10, 10, 20, 20);

  @Test
  public void southeast() {
    SpatialObject.Box b = new SpatialObject.Box(18, 18, 28, 28);
    assertTrue(a.overlaps(b));
  }

  @Test
  public void northeast() {
    SpatialObject.Box b = new SpatialObject.Box(18, 3, 28, 13);
    assertTrue(a.overlaps(b));
  }

  @Test
  public void northwest() {
    SpatialObject.Box b = new SpatialObject.Box(3, 3, 13, 13);
    assertTrue(a.overlaps(b));
  }

  @Test
  public void southwest() {
    SpatialObject.Box b = new SpatialObject.Box(3, 18, 13, 28);
    assertTrue(a.overlaps(b));
  }
}
