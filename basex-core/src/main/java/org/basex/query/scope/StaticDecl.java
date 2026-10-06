package org.basex.query.scope;

import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Common superclass for static functions and variables.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticDecl extends StaticScope {
  /** Annotations. */
  public AnnList anns;

  /** Cached properties of the expression. */
  private final FlagCache props = new FlagCache(flag -> expr.has(flag));

  /**
   * Constructor.
   * @param name name
   * @param declType declared return type (can be {@code null})
   * @param anns annotations
   * @param vs variable scope
   * @param info input info
   * @param doc xqdoc string
   */
  protected StaticDecl(final QNm name, final SeqType declType, final AnnList anns,
      final VarScope vs, final InputInfo info, final String doc) {
    super(info.sc());
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
   * Checks if the expression of this declaration has one of the specified compiler properties.
   * @param flags flags
   * @return result of check
   * @see Expr#has(Flag...)
   */
  protected final boolean check(final Flag... flags) {
    return expr != null && props.has(flags);
  }

  /**
   * Returns the type of this expression. If no type has been declared in the expression,
   * it is derived from the expression type.
   * @return return type
   */
  public SeqType seqType() {
    return declType != null ? declType : expr != null ? expr.seqType() : Types.ITEM_ZM;
  }
}
