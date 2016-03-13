package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This is a simple XQuery demo module written in Java.
 * It is derived from the abstract {@link QueryModule} class.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class QueryModuleTest extends QueryModule {
  /** Fruits array. */
  private static final String[] FRUITS = { "Apple", "Banana", "Cherry" };
  /** Lock string. */
  public static final String LOCK1 = "foo";
  /** Lock string. */
  public static final String LOCK2 = "bar";

  /**
   * Returns the specified fruit modulo number of fruits.
   * This variant is more convenient, as it uses Java's standard data types.
   *
   * @param fruit index of fruit to be returned
   * @return fruit string
   */
  @Requires(Permission.NONE)
  @Deterministic
  public String convenient(final int fruit) {
    final int i = fruit;
    return FRUITS[i % FRUITS.length];
  }

  /**
   * Returns the specified fruit modulo number of fruits.
   * This variant is faster, as it uses the internal data types of BaseX.
   *
   * @param fruit index of fruit to be returned
   * @return fruit string
   */
  @Requires(Permission.NONE)
  @Deterministic
  public Str fast(final Int fruit) {
    final int i = (int) fruit.itr();
    final String f = FRUITS[i % FRUITS.length];
    return Str.get(f);
  }

  /**
   * Returns the default function namespace.
   * @return default function namespace
   */
  @Requires(Permission.NONE)
  @ContextDependent
  public Str functionNS() {
    return Str.get(staticContext.funcNS);
  }

  /**
   * Read lock.
   */
  @Lock(read = { LOCK1, LOCK2})
  public void readLock() { }

  /**
   * Write locks.
   */
  @Lock(write = { LOCK1, LOCK2})
  public void writeLock() { }

  /**
   * Throws an exception.
   * @throws QueryException query exception
   */
  public void error() throws QueryException {
    throw new QueryException("Stopped with an error.");
  }
}
