package com.github.dakusui.geophile.jcunit;

public interface Concretizer<T, E> {
  E concretize(T testObject);
}
