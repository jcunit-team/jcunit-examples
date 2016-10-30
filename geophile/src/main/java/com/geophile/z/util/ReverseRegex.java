package com.geophile.z.util;

import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import nl.flotsam.xeger.Xeger;

import java.util.ArrayList;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ReverseRegex implements LevelsProvider {
  private final ArrayList<String> levels;

  public ReverseRegex(
      @Param(source = Param.Source.CONFIG) String regex,
      @Param(source = Param.Source.CONFIG, defaultValue = { "10" }) int numLevels,
      @Param(source = Param.Source.CONFIG, defaultValue = { "1" }) long randomSeed
  ) {
    checkNotNull(regex);
    checkArgument(numLevels > 0);
    this.levels = new ArrayList<>(numLevels);
    Xeger xeger = new Xeger(regex, new Random(randomSeed));
    for (int i = 0; i < numLevels; i++) {
      this.levels.add(xeger.generate());
    }
  }

  @Override
  public int size() {
    return this.levels.size();
  }

  @Override
  public String get(int n) {
    return this.levels.get(n);
  }
}
