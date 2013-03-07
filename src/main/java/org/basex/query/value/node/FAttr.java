package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.w3c.dom.*;

/**
 * Attribute node fragment.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FAttr extends FNode {
  /** Attribute name. */
  private final QNm name;

  /**
   * Convenience constructor.
   * @param n name
   * @param v value
   */
  public FAttr(final byte[] n, final byte[] v) {
    this(new QNm(n), v);
  }

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public FAttr(final QNm n, final byte[] v) {
    super(NodeType.ATT);
    name = n;
    val = v;
  }

  /**
   * Constructor for DOM nodes.
   * Originally provided by Erdal Karaca.
   * @param attr DOM node
   */
  public FAttr(final Attr attr) {
    this(new QNm(attr.getName()), token(attr.getValue()));
  }

  @Override
  public QNm qname() {
    return name;
  }

  @Override
  public byte[] name() {
    return name.string();
  }

  @Override
  public FNode copy() {
    return new FAttr(name, val).parent(par);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name.string(), VAL, val));
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().asByte()).add(name.uri()).add(0).toArray();
  }

  @Override
  public String toString() {
    return Util.info(" %=\"%\"", name.string(),
        Token.string(val).replaceAll("\"", "&quot;"));
  }
}
