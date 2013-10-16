package org.basex.query.value;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract value.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for (for-each) loops. The default
 * {@link #iter()} method will provide better performance.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Value extends Expr implements Iterable<Item> {
  /** Data type. */
  public Type type;

  /**
   * Constructor.
   * @param t data type
   */
  public Value(final Type t) {
    type = t;
  }

  @Override
  public final void checkUp() {
  }

  @Override
  public final Value compile(final QueryContext ctx, final VarScope scp) {
    return this;
  }

  @Override
  public final ValueIter iter(final QueryContext ctx) {
    return iter();
  }

  @Override
  public final Iterator<Item> iterator() {
    return iter().iterator();
  }

  /**
   * Returns an iterator.
   * @return iterator
   */
  public abstract ValueIter iter();

  @Override
  public final Value value(final QueryContext ctx) {
    return this;
  }

  /**
   * Returns the data reference (if) attached to this value. This method is overwritten
   * by {@link DBNode} and {@link DBNodeSeq}.
   * @return data reference
   */
  public Data data() {
    return null;
  }

  @Override
  public final boolean isValue() {
    return true;
  }

  @Override
  public abstract long size();

  /**
   * Returns a Java representation of the value.
   * @return Java object
   * @throws QueryException query exception
   */
  public abstract Object toJava() throws QueryException;

  @Override
  public boolean has(final Flag flag) {
    return false;
  }

  @Override
  public final boolean removable(final Var v) {
    return true;
  }

  @Override
  public final VarUsage count(final Var v) {
    return VarUsage.NEVER;
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {
    // values do not contain variable references
    return null;
  }

  @Override
  public Value copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return this;
  }

  @Override
  public String description() {
    return type + " " + SEQUENCE;
  }

  /**
   * Returns a hash code for this value.
   * @param ii input info
   * @return hash code
   * @throws QueryException if atomization can't be applied (e.g. function item)
   */
  public abstract int hash(final InputInfo ii) throws QueryException;

  /**
   * Writes this value's items out to the given array.
   * @param arr array to write to
   * @param start start position
   * @return number of written items
   */
  public abstract int writeTo(final Item[] arr, final int start);

  /**
   * Creates an {@link ValueBuilder}, containing all items of this value.
   * Use with care, as compressed Values are expanded, creating many objects.
   * @return cached items
   */
  public final ValueBuilder cache() {
    final ValueBuilder vb = new ValueBuilder((int) size());
    vb.size(writeTo(vb.item, 0));
    return vb;
  }

  /**
   * Serializes the value, using the standard XML serializer,
   * and returns the cached result.
   * @return serialized value
   * @throws QueryIOException query I/O exception
   */
  public final ArrayOutput serialize() throws QueryIOException {
    final ArrayOutput ao = new ArrayOutput();
    try {
      final Serializer ser = Serializer.get(ao);
      final ValueIter vi = iter();
      for(Item it; (it = vi.next()) != null;) ser.serialize(it);
      ser.close();
    } catch(final IOException ex) {
      SERANY.thrwIO(ex);
    }
    return ao;
  }

  /**
   * Gets the item at the given position in the value.
   * @param pos position
   * @return item
   */
  public abstract Item itemAt(final long pos);

  /**
   * Checks if all items of this value share the same type.
   * @return result of check
   */
  public abstract boolean homogeneous();

  @Override
  public boolean accept(final ASTVisitor visitor) {
    final Data data = data();
    return data == null || visitor.lock(data.meta.name);
  }

  @Override
  public final int exprSize() {
    return 1;
  }
}
