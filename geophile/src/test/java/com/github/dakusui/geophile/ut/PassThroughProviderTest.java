package com.github.dakusui.geophile.ut;

import com.github.dakusui.geophile.jcunit.SubsetLevels;
import com.github.dakusui.geophile.jcunit.TestUtils;
import org.junit.Test;

import static com.github.dakusui.geophile.jcunit.SubsetLevels.Mode.NOT_ORDERED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PassThroughProviderTest {
  TestUtils.Out out = new TestUtils.Out();

  @Test
  public void givenPassThroughProvider5ElementsWithMin_2Max_3$whenInstantiate$thenOnly1LevelContainingAllElementsProvided() {
    SubsetLevels.PassThrough provider = new SubsetLevels.PassThrough(new String[]{"BOX1", "BOX2", "POINT1", "POINT2"}, -1, -1, NOT_ORDERED, false);
    printProvider(provider);
    assertThat(provider.size(), equalTo(1));
  }

  @Test
  public void givenPassThrough5ElementsProviderWithMin_2Max_3$whenInstantiate$then10LevelsProvided() {
    SubsetLevels.PassThrough provider = new SubsetLevels.PassThrough(new String[]{"BOX1", "BOX2", "POINT1", "POINT2"}, 2, 3, NOT_ORDERED, false);
    printProvider(provider);
    // 4C2 = 6
    // 4C3= 4
    assertThat(provider.size(), equalTo(10));
  }

  @Test
  public void givenPassThrough5ElementsProviderWithMin_0Max_1$whenInstantiate$then5LevelsProvided() {
    SubsetLevels.PassThrough provider = new SubsetLevels.PassThrough(new String[]{"BOX1", "BOX2", "POINT1", "POINT2"}, 0, 1, NOT_ORDERED, false);
    printProvider(provider);
    // [] // empty included!
    // [A], [B], [C], [D]
    assertThat(provider.size(), equalTo(5));
  }

  private void printProvider(SubsetLevels.PassThrough provider) {
    for (int i = 0; i < provider.size(); i++) {
      out.writeLine(provider.get(i));
    }
  }

}
