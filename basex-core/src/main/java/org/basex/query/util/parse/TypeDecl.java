package org.basex.query.util.parse;

import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Declaration of a named item type.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TypeDecl {
  /** Type name. */
  public final QNm name;
  /** Declared type. */
  public final SeqType seqType;
  /** Names of the referenced types. */
  public final QNmSet refs;
  /** Declared and imported types of the declaring module. */
  public final QNmMap<TypeDecl> module;

  /**
   * Constructor.
   * @param name type name
   * @param seqType declared type
   * @param refs names of the referenced types
   * @param module declared and imported types of the declaring module
   */
  public TypeDecl(final QNm name, final SeqType seqType, final QNmSet refs,
      final QNmMap<TypeDecl> module) {
    this.name = name;
    this.seqType = seqType;
    this.refs = refs;
    this.module = module;
  }
}
