package org.basex.query;

import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Common superclass for static functions and variables.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticDecl extends StaticScope {
  /** Static context of this declaration. */
  public final StaticContext sc;
  /** Annotations. */
  public final Ann ann;
  /** This declaration's name. */
  public final QNm name;
  /** Declared type, {@code null} if not specified. */
  public final SeqType declType;

  /** Flag that is set during compilation and execution and prevents infinite loops. */
  protected boolean dontEnter;

  /**
   * Constructor.
   * @param sctx static context
   * @param a annotations
   * @param nm name
   * @param t declared return type
   * @param scp variable scope
   * @param xqdoc documentation
   * @param ii input info
   */
  public StaticDecl(final StaticContext sctx, final Ann a, final QNm nm, final SeqType t,
      final VarScope scp, final String xqdoc, final InputInfo ii) {

    super(scp, xqdoc, ii);
    sc = sctx;
    ann = a == null ? new Ann() : a;
    name = nm;
    declType = t;
  }

  /**
   * Returns a unique identifier for this declaration.
   * @return a byte sequence that uniquely identifies this declaration
   */
  public abstract byte[] id();

  /**
   * Returns the type of this expression. If no type has been declare in the expression,
   * it is derived from the expression type.
   * @return return type
   */
  public SeqType type() {
    return declType != null ? declType : expr.type();
  }
}
