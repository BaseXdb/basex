package org.basex.query.func;


import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Random functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dirk Kirsten
 */
public final class FNRandom extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNRandom(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _RANDOM_DOUBLE:  return Dbl.get(randomDouble());
      case _RANDOM_INTEGER: return Int.get(randomInt(ctx));
      case _RANDOM_UUID:    return Str.get(UUID.randomUUID());
      default:              return super.item(ctx, ii);
    }
  }
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _RANDOM_SEEDED_DOUBLE:  return randomSeededDouble(ctx);
      case _RANDOM_SEEDED_INTEGER: return randomSeededInt(ctx);
      case _RANDOM_GAUSSIAN:       return randomGaussian(ctx);
      default:                     return super.iter(ctx);
    }
  }

  /**
   * Returns a random integer, either in the whole integer range or
   * if a maximum is given between 0 (inclusive) and the given maximum (exclusive).
   * @param ctx query context
   * @return random integer
   * @throws QueryException query exception
   */
  private int randomInt(final QueryContext ctx) throws QueryException {
    final Random r = new Random();
    return expr.length == 1 ? r.nextInt((int) checkItr(expr[0], ctx)) : r.nextInt();
  }

  /**
   * Returns a sequence of random integers with exactly $num items
   * using a seed for initializing the random function, either in the
   * whole integer range or if a maximum is given between 0 (inclusive)
   * and the given maximum (exclusive).
   * @param ctx query context
   * @return random integer
   * @throws QueryException query exception
   */
  private Iter randomSeededInt(final QueryContext ctx) throws QueryException {
    return new Iter() {
      int count;
      long seed = checkItr(expr[0], ctx);
      int num = (int) checkItr(expr[1], ctx);
      Random r = new Random(seed);

      @Override
      public Item next() throws QueryException {
        if(expr.length == 3) {
          // max defined
          final int max = (int) checkItr(expr[2], ctx);
          return ++count <= num ? Int.get(r.nextInt(max)) : null;
        }
        // no max given
        return ++count <= num ? Int.get(r.nextInt()) : null;
      }
    };
  }

  /**
   * Returns a random double between 0.0 (inclusive) and 1.0 (exclusive).
   * @return random double
   */
  private double randomDouble() {
    return new Random().nextDouble();
  }

  /**
   * Returns a sequence of random double with exactly $num items
   * using a seed between 0.0 (inclusive) and 1.0 (exclusive).
   * @param ctx query context
   * @return random double
   * @throws QueryException query exception
   */
  private Iter randomSeededDouble(final QueryContext ctx) throws QueryException {
    return new Iter() {
      int count;
      long seed = checkItr(expr[0], ctx);
      int num = (int) checkItr(expr[1], ctx);
      Random r = new Random(seed);

      @Override
      public Item next() throws QueryException {
        return ++count <= num ? Dbl.get(r.nextDouble()) : null;
      }
    };
  }

  /**
   * Returns a sequence of random doubles with exactly $num items
   * using a Gaussian (i.e. normal) distribution with a mean of 0.0
   * and a derivation of 1.0
   * @param ctx query context
   * @return random double
   * @throws QueryException query exception
   */
  private Iter randomGaussian(final QueryContext ctx) throws QueryException {
    return new Iter() {
      int count;
      int num = (int) checkItr(expr[0], ctx);
      Random r = new Random();

      @Override
      public Item next() throws QueryException {
        return ++count <= num ? Dbl.get(r.nextGaussian()) : null;
      }
    };
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT || super.uses(u);
  }
}
