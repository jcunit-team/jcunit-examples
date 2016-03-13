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
 * <pre>
 * QuadraticEquationクラス仕様 version 2
 * - 入力:三つの整数a,b,c
 * -- aが0の場合、例外が送出される。(問題4:a==0)
 * -- a,b,cのいずれかの絶対値が100より大きい場合、例外が創出される。(問題3:巨大な係数)
 * -- a,b,cがb * b - 4 * c * a >= 0を満たさない場合、例外が送出される。(問題1：虚数解)
 * - 出力:二次方程式、a x^2 + b x^2 + c = 0を満たす実数x1とx2
 * -- 出力される解x1またはx2を上述の2次方程式に代入した時、その絶対値は0.01未満になる。(問題2：丸め誤差)
 * </pre>
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
