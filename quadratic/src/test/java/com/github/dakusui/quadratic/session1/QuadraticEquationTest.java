package com.github.dakusui.quadratic.session1;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * An example that tests QuadraticEquationSolver.
 * <p/>
 * <ul>
 * <li>session 1: Initial version of QuadraticEquationSolverTest.(runCount     = 53, failureCount = 39, ignoreCount  =  0)
 * </li>
 * </ul>
 */
@RunWith(JCUnit.class)
public class QuadraticEquationTest {
  @FactorField
  public int a;
  @FactorField
  public int b;
  @FactorField
  public int c;

  @Test
  public void solveEquation() {
    QuadraticEquation.Solutions s = new QuadraticEquation(a, b,
        c).solve();
    double v1 = a * s.x1 * s.x1 + b * s.x1 + c, v2 = a * s.x2 * s.x2 + b * s.x2 + c;

    assertThat(String.format("%d*x1^2+%d*x1+%d=%f {x1=%f}", a, b, c, v1, s.x1), v1, is(0.0));
    assertThat(String.format("%d*x2^2+%d*x2+%d=%f {x2=%f}", a, b, c, v2, s.x2), v2, is(0.0));
  }
}
