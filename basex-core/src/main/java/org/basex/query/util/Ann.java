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
  public InputInfo info;
  /** Annotation signature. */
  public Annotation sig;
  /** Arguments. */
  public Item[] args;

  /**
   * Constructor.
   * @param info input info
   * @param sig annotation signature
   * @param args arguments
   */
  public Ann(final InputInfo info, final Annotation sig, final Item... args) {
    this.info = info;
    this.sig = sig;
    this.args = args;
  }

  /**
   * Compares two annotations.
   * @param an annotation to be compared
   * @return result of check
   */
  public boolean eq(final Ann an) {
    final long as = args.length;
    if(sig != an.sig || as != an.args.length) return false;
    for(int a = 0; a < as; a++) {
      if(!args[a].sameAs(an.args[a])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add('%').add(sig.id());
    if(args.length != 0) tb.add('(').addSep(args, SEP).add(')');
    return tb.add(' ').toString();
  }
}
