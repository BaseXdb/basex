package org.basex.query.ann;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Annotation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Ann {
  /** Input info. */
  public final InputInfo info;
  /** Annotation signature (is {@code null} if {@link #name} is assigned). */
  public final Annotation sig;
  /** No-standard annotation (is {@code null} if {@link #sig} is assigned). */
  private final QNm name;
  /** Arguments. */
  private final Item[] args;

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
    name = null;
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
    sig = null;
  }

  /**
   * Returns the name of the annotation.
   * @return name
   */
  public QNm name() {
    return sig != null ? sig.qname() : name;
  }

  /**
   * Returns the value of the annotation.
   * @return value
   */
  public Item[] args() {
    return args;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Ann)) return false;
    final Ann ann = (Ann) obj;
    return (name != null ? ann.name != null && name.eq(ann.name) : sig == ann.sig) &&
        Array.equals(args, ann.args);
  }

  /**
   * Adds the annotation to a query string.
   * @param qs query string builder
   */
  public void plan(final QueryString qs) {
    qs.concat("%", sig != null ? sig.id() : name.prefixId(XQ_URI));
    if(args.length != 0) qs.params(args);
  }
}
