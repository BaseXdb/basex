package org.basex.query.value.node;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.w3c.dom.*;

/**
 * Attribute node fragment.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FAttr extends FNode {
  /** Attribute name. */
  private final QNm name;
  /** Attribute value. */
  private final byte[] value;

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public FAttr(final QNm name, final byte[] value) {
    super(NodeType.ATTRIBUTE);
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
  public byte[] string() {
    return value;
  }

  @Override
  public FAttr materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc) {
    return materialized(test, ii) ? this : new FAttr(name, value);
  }

  @Override
  public byte[] xdmInfo() {
    return new ByteList().add(typeId().asByte()).add(name.uri()).add(0).finish();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FAttr)) return false;
    final FAttr f = (FAttr) obj;
    return name.eq(f.name) && Token.eq(value, f.value) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string(), VALUEE, value));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.concat(name.string(), "=", QueryString.toQuoted(value));
  }
}
