package org.basex.query.pf;

import static org.basex.query.pf.PFT.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.PrintSerializer;
import org.basex.io.CachedOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Abstract XQuery Operator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class Opr {
  /** Pathfinder data reference. */
  static PFC pf;

  /** Table of operator. */
  Tbl tbl = new Tbl();
  /** Arguments of the operator (edges). */
  protected Opr[] arg;

  /** Evaluation flag (true if operator has been evaluated). */
  private boolean done;
  /** ID reference to the query table. */
  private int id;
  /** Operator id, used in debug output. */
  private int x;

  /**
   * Initializes the operator.
   * @param n node id
   * @param a arguments
   * @param i operator id
   */
  final void init(final int n, final Opr[] a, final int i) {
    id = n; arg = a; x = i;
  }

  /**
   * Evaluates an operator once. Returns false if operator has already
   * been evaluated before.
   * @return result of check
   * @throws QueryException query exception
   */
  final String ev() throws QueryException {
    if(done) return null;
    // evaluates arguments
    for(final Opr a : arg) pf.e(a);

    if(Prop.allInfo) dump();
    final Performance p = new Performance();
    e();
    done = true;
    return p.getTimer();
  }

  /**
   * Evaluates the operator.
   * @throws QueryException query exception
   */
  abstract void e() throws QueryException;

  /**
   * Adds a new column to the table.
   * @param n column name
   * @param t column type
   * @return added column
   */
  Col a(final int n, final int t) {
    return tbl.a(n, t);
  }

  /**
   * Evaluates the specified XPath and returns the column that matches
   * the name of the found 'name' attribute.
   * @param q XPath query
   * @param t table to look at
   * @return result nodes
   * @throws QueryException query exception
   */
  protected final Col c(final byte[] q, final Tbl t) throws QueryException {
    final int n = n(q);
    return n == -1 ? null : t.c(t.p(n(q)));
  }

  /**
   * Evaluates the specified XPath and returns the column that matches
   * the name of the found 'name' attribute.
   * @param q XPath query
   * @return result nodes
   * @throws QueryException query exception
   */
  protected final Col c(final byte[] q) throws QueryException {
    return c(q, tbl);
  }

  /**
   * Evaluates the specified XPath query and returns an integer that
   * serves as reference to the 'name' attribute in the names index.
   * @param q XPath query
   * @return result nodes
   * @throws QueryException query exception
   */
  protected final int n(final byte[] q) throws QueryException {
    final int[] p = pf.q(q, id);
    return p.length == 0 ? -1 : pf.a(NAME, p[0]);
  }

  /**
   * Evaluates the specified XPath query and returns the result nodes.
   * @param q XPath query
   * @return result nodes
   * @throws QueryException query exception
   */
  int[] q(final byte[] q) throws QueryException { return pf.q(q, id); }

  /**
   * Evaluates the specified XPath query at the specified position
   * and returns the first node as token.
   * @param q XPath query
   * @param i id to start from
   * @return result nodes
   * @throws QueryException query exception
   */
  byte[] t(final byte[] q, final int i) throws QueryException {
    return pf.t(q, i);
  }

  /**
   * Evaluates the specified XPath query and returns the first node as token.
   * @param q XPath query
   * @return result nodes
   * @throws QueryException query exception
   */
  byte[] t(final byte[] q) throws QueryException { return t(q, id); }

  /**
   * Prints debug information.
   */
  final void dbg() { if(!done) BaseX.errln(this); }

  /**
   * Returns the name of this class.
   * @return class name
   */
  private String name() { return getClass().getSimpleName(); }

  /**
   * Prints detailed information on the operator.
   */
  final void dump() {
    try {
      final CachedOutput out = new CachedOutput();
      out.print((x + 1) + ". " + name() + "(");
      for(int e = 0; e < arg.length; e++) {
        if(e != 0) out.print(",");
        out.print(Token.token(arg[e].x + 1));
      }
      //out.print("), Internal ID " + id);
      out.println(") ======================================================\n");
      for(final Opr a : arg) out.println("Edge " + a);
      out.println("");
      pf.d(new PrintSerializer(out), id);
      out.println("");
      if(Prop.debug) BaseX.debug(out.toString());
      else if(Prop.allInfo) pf.evalInfo(out.toString());
    } catch(final Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public final String toString() {
    final TokenBuilder tb = new TokenBuilder();
    final byte[] v = Token.token(x + 1);
    tb.add(v.length == 1 ? Token.concat(Token.SPACE, v) : v);
    tb.add(": " + name() + "(");
    for(int e = 0; e < arg.length; e++) {
      if(e != 0) tb.add(",");
      tb.add(Token.token(arg[e].x + 1));
    }
    tb.add(")\n");
    //while(tb.size < 16) tb.add(' ');
    if(tbl.size != 0) tb.add(pf.dump(tbl));
    return tb.toString();
  }
}

