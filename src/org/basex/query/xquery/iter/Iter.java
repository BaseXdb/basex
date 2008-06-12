package org.basex.query.xquery.iter;

import static org.basex.query.xquery.XQText.*;
import org.basex.BaseX;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQParser;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.SeqBuilder;

/**
 * Iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Iter {
  /** Parser reference (should be stored outside expressions). */
  public XQParser parser;
  /** Position in original query. */
  public int qpos;
  
  /** Empty iterator. */
  public static final Iter EMPTY = new Iter() {
    @Override
    public Item next() { return null; }
    @Override
    public long size() { return 0; }
    @Override
    public String toString() { return "()"; }
  };
  
  /**
   * Returns the next item or null if no other items are found.
   * @return resulting item
   * @throws XQException evaluation exception
   */
  public abstract Item next() throws XQException;

  /**
   * Resets the iterator; can be optionally implemented.
   */
  public void reset() {
    BaseX.notexpected();
  }

  /**
   * Returns the number of entries. Warning: -1 is returned if the number
   * cannot be evaluated, so each method has to check and react on the
   * returned value.
   * @return number of entries
   */
  public long size() {
    return -1;
  }

  /**
   * Returns a sequence from all iterator values.
   * @return sequence
   * @throws XQException evaluation exception
   */
  public Item finish() throws XQException {
    return new SeqBuilder(this).finish();
  }

  /**
   * Checks if the iterator can be dissolved into an effective boolean value.
   * If not, returns an error. If yes, returns the first value (this makes
   * sense to evaluate position predicates).
   * Must be called before {@link #next} was called.
   * @return item
   * @throws XQException evaluation exception
   */
  public final Item ebv() throws XQException {
    final Item it = next();
    if(it == null) return Bln.FALSE;
    if(!it.node() && next() != null) Err.or(FUNSEQ, this);
    return it;
  }

  /**
   * Checks if the specified iterator contains a single item.
   * Returns null, the first item or an exception.
   * @param expr calling expression
   * @param empty allow empty sequences
   * @return item
   * @throws XQException evaluation exception
   */
  public final Item atomic(final Expr expr, final boolean empty)
      throws XQException {
    
    long s = size();
    if(s != -1) {
      if(s == 1) return next();
      if(s == 0) {
        if(!empty) Err.empty(expr);
        return null;
      }
      Err.or(XPSEQ, "(" + next() + "," + next() +
          (next() != null ? ",...)" : ")"), expr.info());
    }

    final Item it = next();
    if(it == null) {
      if(!empty) Err.empty(expr);
      return null;
    }

    final Item n = next();
    if(n != null) {
      final boolean m = next() != null;
      Err.or(XPSEQ, "(" + it + "," + n + (m ? ",...)" : ")"), expr.info());
    }
    return it;
  }
}
