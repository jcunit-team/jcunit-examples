package com.github.dakusui.geophile.jcunit;

import java.util.*;

public enum TestUtils {
  ;

  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static class Out extends AbstractList<String> {
    private List<String> out = new LinkedList<>();

    public void writeLine(Object s) {
      if (!isRunUnderSurefire()) {
        System.out.println(s);
      }
      this.out.add(Objects.toString(s));
    }

    @Override
    public String get(int index) {
      return out.get(index);
    }

    @Override
    public Iterator<String> iterator() {
      return out.iterator();
    }

    @Override
    public int size() {
      return out.size();
    }
  }
}
