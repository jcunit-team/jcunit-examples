package com.github.dakusui.geophile.jcunit.ut;

import com.geophile.z.util.ReverseRegex;
import com.github.dakusui.geophile.jcunit.TestUtils;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class ReverseRegexProviderTest {

  @RunWith(JCUnit.class)
  public static class TestClass {
    TestUtils.Out out = new TestUtils.Out();
    @FactorField(
        levelsProvider = ReverseRegex.class,
        args = { @Value({ "(hello|Hello)world(!)?, (e|E)veryone\\. How are you( doing)?\\?" }) }
    )
    public String factor;

    @FactorField
    public int i;
    @FactorField
    public int j;

    @Test
    public void test() {
      out.writeLine(TestCaseUtils.toTestCase(this));
    }
  }

  @Test
  public void testReverseRegexProvider() {
    Result result = JUnitCore.runClasses(TestClass.class);
    assertEquals(70, result.getRunCount());
    assertEquals(0, result.getFailureCount());
  }
}
