package com.github.dakusui.geophile.mymodel;

public interface SpatialObject extends com.geophile.z.SpatialObject {
  class Point extends com.geophile.z.spatialobject.d2.Point implements SpatialObject {
    /**
     * Creates a point at (x, y).
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public Point(double x, double y) {
      super(x, y);
    }
  }

  /**
   *
   */
  class Box extends com.geophile.z.spatialobject.d2.Box implements SpatialObject {
    /**
     * Be cautious that the order of parameters are a bit different from its super-class's.
     *
     * @param xLo xLo
     * @param yLo yLo
     * @param xHi xHi
     * @param yHi yHi
     */
    public Box(double xLo, double yLo, double xHi, double yHi) {
      super(xLo, xHi, yLo, yHi);
    }

    public boolean contains(Point point) {
      Box box = this;
      return box.xLo() <= point.x() && point.x() <= box.xHi() &&
          box.yLo() <= point.y() && point.y() <= box.yHi();
    }

    public boolean overlaps(Box b) {
      Box a = this;
      return a.xLo() < b.xHi() && b.xLo() < a.xHi() &&
          a.yLo() < b.yHi() && b.yLo() < a.yHi();
    }
  }
}
