package org.basex.examples.query;

import org.basex.query.QueryModule;
import org.basex.query.item.Int;
import org.basex.query.item.Str;

/**
 * This is a simple XQuery demo module written in Java.
 * It is derived from the abstract {@link QueryModule} class.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public class Fruits extends QueryModule {
  /** Fruits array. */
  private static final String[] FRUITS = { "Apple", "Banana", "Cherry" };

  /**
   * Returns the specified fruit.
   * This variant is more convenient, as it uses Java's standard data types.
   *
   * @param fruit index of fruit to be returned
   * @return fruit string
   */
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
  public Str fast(final Int fruit) {
    final int i = (int) fruit.itr(input);
    final String f = FRUITS[i % FRUITS.length];
    return Str.get(f);
  }
}
