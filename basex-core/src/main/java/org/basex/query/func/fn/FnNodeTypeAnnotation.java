package org.basex.query.func.fn;

import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnNodeTypeAnnotation extends FnSchemaType {
  /** The function's argument type. */
  private static final SeqType ARG_TYPE = new ChoiceItemType(
      Arrays.asList(ELEMENT_O, ATTRIBUTE_O)).seqType();

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANode node = toNode(arg(0), qc);
    ARG_TYPE.coerce(node, null, qc, null, info);
    return annotate(qc, info, node.type == NodeType.ATTRIBUTE ? UNTYPED_ATOMIC : UNTYPED);
  }
}