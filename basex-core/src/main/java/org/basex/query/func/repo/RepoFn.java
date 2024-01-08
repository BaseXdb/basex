package org.basex.query.func.repo;

import org.basex.core.locks.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;

/**
 * Functions on EXPath packages.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class RepoFn extends StandardFunc {
  /** QName. */
  static final QNm Q_PACKAGE = new QNm("package");
  /** QName. */
  static final QNm Q_NAME = new QNm("name");
  /** QName. */
  static final QNm Q_TYPE = new QNm("type");
  /** QName. */
  static final QNm Q_VERSION = new QNm("version");

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.REPO) && super.accept(visitor);
  }
}
