package com.github.dakusui.quadratic.tests.session5;

import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.quadratic.suts.session5.QuadraticEquation;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;


/**
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
@GenerateCoveringArrayWith(checker = @Checker(value = SmartConstraintChecker.class))
public class QuadraticEquationTest {
  @FactorField(intLevels = {1, 0, -1, 100, 101, -100, -101, Integer.MAX_VALUE, Integer.MIN_VALUE})
  public int   a;
  @FactorField(intLevels = {1, 0, -1, 100, 101, -100, -101, Integer.MAX_VALUE, Integer.MIN_VALUE})
  public int   b;
  @FactorField(intLevels = {1, 0, -1, 100, 101, -100, -101, Integer.MAX_VALUE, Integer.MIN_VALUE})
  public int   c;

  /**
   * 制約1: ```a```が0の場合、例外が送出される。(**問題4:a==0**)
   */
  @Condition(constraint = true)
  public boolean aIsNonZero() {
    return this.a != 0;
  }

  /**
   * 制約2: ```a```,```b```,```c```のいずれかの絶対値が100より大きい場合、例外が創出される。(**問題3:巨大な係数**)
   */
  @Condition(constraint = true)
  public boolean coefficientsAreValid() {
    return
        -100 <= a && a <= 100 &&
            -100 <= b && b <= 100 &&
            -100 <= c && c <= 100;
  }

  /**
   * 制約3: ```a```,```b```,```c```が```b * b - 4 * c * a >= 0```を満たさない場合、例外が送出される。(**問題1：虚数解**)
   */
  @Condition(constraint = true)
  public boolean discriminantIsNonNegative() {
    int a = this.a;
    int b = this.b;
    int c = this.c;
    return b * b - 4 * c * a >= 0;
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!aIsNonZero" })
  public void solveEquation1$thenThrowIllegalArgumentException() {
    new QuadraticEquation(
        a,
        b,
        c).solve();
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!discriminantIsNonNegative" })
  public void solveEquation2$thenThrowIllegalArgumentException() {
    new QuadraticEquation(
        a,
        b,
        c).solve();
  }

  @Test(expected = IllegalArgumentException.class)
  @When({ "!coefficientsAreValid" })
  public void solveEquation3$thenThrowIllegalArgumentException() {
    new QuadraticEquation(
        a,
        b,
        c).solve();
  }

  @Test
  @When({ "*" })
  public void solveEquation$thenSolved() {
    QuadraticEquation.Solutions s = new QuadraticEquation(a, b,
        c).solve();
    assertThat(
        String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        Math.abs(a * s.x1 * s.x1 + b * s.x1 + c),
        closeTo(0, 0.01)
    );
    assertThat(
        String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c,
        closeTo(0, 0.01)
    );
  }
}
