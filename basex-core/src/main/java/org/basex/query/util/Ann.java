package org.basex.query.util;

import static org.basex.query.QueryText.*;

import org.basex.query.ann.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Annotation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Ann {
  /** Input info. */
  public final InputInfo info;
  /** Annotation signature (is {@code null} if {@link #name} is assigned). */
  public final Annotation sig;
  /** No-standard annotation (is {@code null} if {@link #sig} is assigned). */
  public final QNm name;
  /** Arguments. */
  public final Item[] args;

  /**
   * Constructor.
   * @param info input info
   * @param sig annotation signature
   * @param args arguments
   */
  public Ann(final InputInfo info, final Annotation sig, final Item... args) {
    this.info = info;
    this.args = args;
    this.sig = sig;
    this.name = null;
  }

  /**
   * Constructor.
   * @param info input info
   * @param name name of annotation
   * @param args arguments
   */
  public Ann(final InputInfo info, final QNm name, final Item... args) {
    this.info = info;
    this.args = args;
    this.name = name;
    this.sig = null;
  }

  /**
   * Compares two annotations.
   * @param ann annotation to be compared
   * @return result of check
   */
  public boolean eq(final Ann ann) {
    final long as = args.length;
    if(sig != null ? (sig != ann.sig) : (ann.name == null || !name.eq(ann.name))) return false;
    if(as != ann.args.length) return false;
    for(int a = 0; a < as; a++) {
      if(!args[a].sameAs(ann.args[a])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add('%');
    tb.add(sig == null ? name.prefixId(XQ_URI) : sig.id());
    if(args.length != 0) tb.add('(').addSep(args, SEP).add(')');
    return tb.add(' ').toString();
  }
}
