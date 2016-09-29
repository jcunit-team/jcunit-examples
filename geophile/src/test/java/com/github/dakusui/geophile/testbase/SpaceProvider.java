package com.github.dakusui.geophile.testbase;

import com.geophile.z.Space;

public enum SpaceProvider {
  @SuppressWarnings("unused")NORMAL(new double[] { 0, 0 }, new double[] { 1_000_000, 1_000_000 }, new int[] { 20, 20 }, null),
  @SuppressWarnings("unused")SMALL(new double[] { 0, 0 }, new double[] { 300, 300 }, new int[] { 20, 20 }, null);

  private final int[]    interleave;
  private final int[]    gridBits;
  private final double[] hi;
  private final double[] lo;

  SpaceProvider(double[] hi, double[] lo, int[] gridBits, int[] interleave) {
    this.hi = hi;
    this.lo = lo;
    this.gridBits = gridBits;
    this.interleave = interleave;
  }

  public Space create() {
    if (interleave == null) {
      return Space.newSpace(this.hi, this.lo, this.gridBits);
    }

    return Space.newSpace(this.hi, this.lo, this.gridBits, this.interleave);
  }
}
