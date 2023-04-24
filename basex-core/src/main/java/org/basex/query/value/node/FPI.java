package org.basex.query.value.node;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * Processing instruction node fragment.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FPI extends FNode {
  /** Opening characters. */
  public static final byte[] OPEN = { '<', '?' };
  /** Closing characters. */
  public static final byte[] CLOSE = { '?', '>' };

  /** PI name. */
  private final QNm name;
  /** PI value. */
  private final byte[] value;

  /**
   * Constructor for creating a processing instruction.
   * @param name name
   * @param value value
   */
  public FPI(final QNm name, final byte[] value) {
    super(NodeType.PROCESSING_INSTRUCTION);
    this.name = name;
    this.value = value;
  }

  /**
   * Constructor for creating a processing instruction from a DOM node.
   * Originally provided by Erdal Karaca.
   * @param pi DOM node
   */
  public FPI(final ProcessingInstruction pi) {
    this(new QNm(pi.getTarget()), token(pi.getData()));
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
  public FPI materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc) {
    return materialized(test, ii) ? this : new FPI(name, value);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FPI)) return false;
    final FPI f = (FPI) obj;
    return name.eq(f.name) && Token.eq(value, f.value) && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string(), VALUEE, value));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.concat(OPEN, name.string(), " ", QueryString.toValue(value), CLOSE);
  }

  /**
   * Checks the specified token for validity.
   * @param atom token to be checked
   * @param ii input info
   * @return token
   * @throws QueryException query exception
   */
  public static byte[] parse(final byte[] atom, final InputInfo ii) throws QueryException {
    if(contains(atom, CLOSE)) throw CPICONT_X.get(ii, atom);
    return atom;
  }
}
