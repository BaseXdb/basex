package org.basex.query.scope;

import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Common superclass for static functions and variables.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticDecl extends StaticScope {
  /** Annotations. */
  public final AnnList anns;

  /** Indicates if code is currently being compiled or evaluated. */
  protected boolean dontEnter;

  /**
   * Constructor.
   * @param name name
   * @param declType declared return type (can be {@code null})
   * @param anns annotations
   * @param vs variable scope
   * @param info input info (can be {@code null})
   * @param doc xqdoc string
   */
  protected StaticDecl(final QNm name, final SeqType declType, final AnnList anns,
      final VarScope vs, final InputInfo info, final String doc) {
    super(vs.sc);
    this.name = name;
    this.declType = declType;
    this.anns = anns;
    this.vs = vs;
    this.info = info;
    doc(doc);
  }

  @Override
  public final void reset() {
    compiled = false;
  }

  /**
   * Returns the type of this expression. If no type has been declared in the expression,
   * it is derived from the expression type.
   * @return return type
   */
  public final SeqType seqType() {
    return declType != null ? declType : expr != null ? expr.seqType() : SeqType.ITEM_ZM;
  }
}
