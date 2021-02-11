package org.basex.query.value.node;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * PI node fragment.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FPI extends FNode {
  /** Opening characters. */
  public static final byte[] OPEN = { '<', '?' };
  /** Closing characters. */
  public static final byte[] CLOSE = { '?', '>' };

  /** PI name. */
  private final QNm name;

  /**
   * Constructor for creating a processing instruction.
   * @param name name
   * @param value value
   */
  public FPI(final String name, final String value) {
    this(new QNm(name), token(value));
  }

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
    this(pi.getTarget(), pi.getData());
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
  public FPI materialize(final QueryContext qc, final boolean copy) {
    return copy ? new FPI(name, value) : this;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FPI && name.eq(((FPI) obj).name) && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string(), VALUEE, value));
  }

  @Override
  public void plan(final QueryString qs) {
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
