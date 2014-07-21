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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FAttr extends FNode {
  /** Attribute name. */
  private final QNm name;

  /**
   * Constructor for creating an attribute.
   * QNames will be cached and reused.
   * @param name name
   * @param value value
   */
  public FAttr(final String name, final String value) {
    this(token(name), token(value));
  }

  /**
   * Constructor for creating an attribute.
   * QNames will be cached and reused.
   * @param name name
   * @param value value
   */
  public FAttr(final byte[] name, final byte[] value) {
    this(QNm.get(name), value);
  }

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public FAttr(final QNm name, final byte[] value) {
    super(NodeType.ATT);
    this.name = name;
    this.value = value;
  }

  /**
   * Constructor for creating an attribute from a DOM node.
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
    return new FAttr(name, value).parent(parent);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name.string(), VAL, value));
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().bytes()).add(name.uri()).add(0).finish();
  }

  @Override
  public String toString() {
    return Util.info(" %=\"%\"", name.string(),
        Token.string(value).replaceAll("\"", "&quot;"));
  }
}
