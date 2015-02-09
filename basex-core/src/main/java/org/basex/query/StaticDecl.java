package org.basex.query;

import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Common superclass for static functions and variables.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public abstract class StaticDecl extends StaticScope {
  /** Annotations. */
  public final AnnList anns;
  /** This declaration's name. */
  public final QNm name;
  /** Declared type, {@code null} if not specified. */
  protected final SeqType declType;

  /** Flag that is set during compilation and execution and prevents infinite loops. */
  protected boolean dontEnter;

  /**
   * Constructor.
   * @param sc static context
   * @param anns annotations
   * @param name name
   * @param type declared return type
   * @param scope variable scope
   * @param doc xqdoc documentation
   * @param info input info
   */
  protected StaticDecl(final StaticContext sc, final AnnList anns, final QNm name,
      final SeqType type, final VarScope scope, final String doc, final InputInfo info) {

    super(scope, doc, sc, info);
    this.anns = anns;
    this.name = name;
    declType = type;
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
  public SeqType seqType() {
    return declType != null ? declType : expr.seqType();
  }
}
