package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import java.io.IOException;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.SeqType;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Node comparison.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CmpN extends Arr {
  /** Comparators. */
  public enum Op {
    /** Node comparison: same. */
    EQ("is") {
      @Override
      public boolean eval(final ANode a, final ANode b) {
        return a.is(b);
      }
    },

    /** Node comparison: before. */
    ET("<<") {
      @Override
      public boolean eval(final ANode a, final ANode b) {
        return a.diff(b) < 0;
      }
    },

    /** Node comparison: after. */
    GT(">>") {
      @Override
      public boolean eval(final ANode a, final ANode b) {
        return a.diff(b) > 0;
      }
    };

    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param n string representation
     */
    Op(final String n) {
      name = n;
    }

    /**
     * Evaluates the expression.
     * @param a first node
     * @param b second node
     * @return result
     */
    public abstract boolean eval(ANode a, ANode b);

    @Override
    public String toString() {
      return name;
    }
  }

  /** Comparator. */
  private final Op op;

  /**
   * Constructor.
   * @param ii input info
   * @param e1 first expression
   * @param e2 second expression
   * @param o comparator
   */
  public CmpN(final InputInfo ii, final Expr e1, final Expr e2, final Op o) {
    super(ii, e1, e2);
    op = o;
    type = SeqType.BLN_ZO;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    super.comp(ctx);
    return optPre(oneIsEmpty() ? null : allAreValues() ?
        item(ctx, input) : this, ctx);
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Item a = expr[0].item(ctx, input);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, input);
    if(b == null) return null;
    return Bln.get(op.eval(checkNode(a), checkNode(b)));
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, OP, Token.token(op.name));
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String description() {
    return "'" + op + "' operator";
  }

  @Override
  public String toString() {
    return toString(" " + op + ' ');
  }
}
