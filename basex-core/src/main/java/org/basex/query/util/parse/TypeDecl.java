package org.basex.query.util.parse;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
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
  /** Annotations. */
  private final AnnList anns;

  /**
   * Constructor.
   * @param name type name
   * @param seqType declared type
   * @param refs names of the referenced types
   * @param module declared and imported types of the declaring module
   * @param anns annotations
   */
  public TypeDecl(final QNm name, final SeqType seqType, final QNmSet refs,
      final QNmMap<TypeDecl> module, final AnnList anns) {
    this.name = name;
    this.seqType = seqType;
    this.refs = refs;
    this.module = module;
    this.anns = anns;
  }

  /**
   * Adds this declaration to a query string.
   * @param qs query string
   * @return query string
   */
  public QueryString declaration(final QueryString qs) {
    if(seqType.type instanceof final RecordType rt && rt.name() != null &&
        name.eq(rt.name())) {
      return rt.declaration(qs);
    }
    return qs.token(DECLARE).token(anns).token(TYPE).token(name.prefixId()).token(AS).
        token(seqType).token(';');
  }
}
