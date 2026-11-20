package org.basex.query.func.bit;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Bit function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class BitFn extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Item item = atomItem(arg(0), qc);
    return item == Empty.VALUE ? Empty.VALUE : value(toLong(item, arg(0)), qc);
  }

  /**
   * Evaluates the function for the integer value of the first argument.
   * @param value value
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  abstract Value value(long value, QueryContext qc) throws QueryException;

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optFirst();
  }

  /**
   * Checks if an expression is a specific integer literal.
   * @param expr expression
   * @param value integer value
   * @return result of check
   */
  static boolean literal(final Expr expr, final long value) {
    return expr instanceof final Itr itr && itr.itr() == value;
  }

  /**
   * Checks if an expression yields items of type {@code xs:integer} (no subtypes).
   * @param expr expression
   * @return result of check
   */
  static boolean integer(final Expr expr) {
    return expr.seqType().type == BasicType.INTEGER;
  }

  /**
   * Returns the value of a literal bit size.
   * @param expr expression
   * @return non-negative size, or {@code -1} if the expression is no literal valid size
   */
  static long size(final Expr expr) {
    return expr instanceof final Itr itr && itr.itr() >= 0 ? itr.itr() : -1;
  }

  /**
   * Evaluates an expression to a bit size.
   * @param expr expression
   * @param max maximum size
   * @param qc query context
   * @return size, limited to 64
   * @throws QueryException query exception
   */
  final int toSize(final Expr expr, final long max, final QueryContext qc)
      throws QueryException {
    final long size = toLong(expr, qc);
    if(size < 0 || size > max) throw BIT_SIZE_X.get(info, size);
    return (int) Math.min(size, Long.SIZE);
  }

  /**
   * Returns the lowest bits of a value.
   * @param value value
   * @param size number of bits (0-64)
   * @return value with all higher bits cleared
   */
  static long unsigned(final long value, final int size) {
    return size == 0 ? 0 : value & -1L >>> Long.SIZE - size;
  }
}
