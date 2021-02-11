package org.basex.query.scope;

import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Common superclass for static functions and variables.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticDecl extends StaticScope {
  /** Annotations. */
  public final AnnList anns;
  /** Name of the declaration. */
  public final QNm name;

  /** Declared type, {@code null} if not specified. */
  protected SeqType declType;
  /** Flag that is set during compilation and execution and prevents infinite loops. */
  protected boolean dontEnter;

  /**
   * Constructor.
   * @param anns annotations
   * @param name name
   * @param declType declared return type (can be {@code null})
   * @param vs variable scope
   * @param doc xqdoc documentation
   * @param info input info
   */
  protected StaticDecl(final AnnList anns, final QNm name, final SeqType declType,
      final VarScope vs, final String doc, final InputInfo info) {
    super(vs.sc, vs, doc, info);
    this.anns = anns;
    this.name = name;
    this.declType = declType;
  }

  /**
   * Returns a unique identifier for this declaration.
   * @return a byte sequence that uniquely identifies this declaration
   */
  public abstract byte[] id();

  /**
   * Returns the type of this expression. If no type has been declared in the expression,
   * it is derived from the expression type.
   * @return return type
   */
  public final SeqType seqType() {
    return declType != null ? declType : expr != null ? expr.seqType() : SeqType.ITEM_ZM;
  }
}
