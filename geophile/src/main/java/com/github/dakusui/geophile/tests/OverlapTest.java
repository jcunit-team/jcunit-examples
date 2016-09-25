package com.github.dakusui.geophile.tests;

import com.github.dakusui.geophile.mymodel.SpatialObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OverlapTest {

    SpatialObject.Box a = new SpatialObject.Box(10, 20, 10, 20);

    @Test
    public void southeast() {
        SpatialObject.Box b = new SpatialObject.Box(18, 28,18,  28);
        assertTrue(a.overlaps(b));
    }

    @Test
    public void northeast() {
        SpatialObject.Box b = new SpatialObject.Box(18, 28, 3, 13);
        assertTrue(a.overlaps(b));
    }

    @Test
    public void northwest() {
        SpatialObject.Box b = new SpatialObject.Box(3, 13,3,  13);
        assertTrue(a.overlaps(b));
    }

    @Test
    public void southwest() {
        SpatialObject.Box b = new SpatialObject.Box(3, 13, 18, 28);
        assertTrue(a.overlaps(b));
    }
}
