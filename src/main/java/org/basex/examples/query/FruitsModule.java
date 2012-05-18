package org.basex.examples.query;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * This is a simple XQuery demo module written in Java.
 * It is derived from the abstract {@link QueryModule} class.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public class FruitsModule extends QueryModule {
  /** Fruits array. */
  private static final String[] FRUITS = { "Apple", "Banana", "Cherry" };

  /**
   * Returns the specified fruit.
   * This variant is more convenient, as it uses Java's standard data types.
   *
   * @param fruit index of fruit to be returned
   * @return fruit string
   */
  @Deterministic
  @Requires(Permission.READ)
  public String convenient(final int fruit) {
    final int i = fruit;
    final String f = FRUITS[i % FRUITS.length];
    return f;
  }

  /**
   * Returns the specified fruit.
   * This variant is faster, as it uses the internal data types of BaseX.
   *
   * @param fruit index of fruit to be returned
   * @return fruit string
   */
  @Deterministic
  @Requires(Permission.READ)
  public Str fast(final Int fruit) {
    final int i = (int) fruit.itr();
    final String f = FRUITS[i % FRUITS.length];
    return Str.get(f);
  }

  /**
   * Context-aware function: returns the default function namespace of this query.
   * @return function namespace
   */
  public String functionNamespace() {
    return Token.string(context.sc.nsFunc);
  }
}
