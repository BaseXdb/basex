package org.basex.query.func.fn;

import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnNodeTypeAnnotation extends FnAtomicTypeAnnotation {
  /** The function's argument type. */
  private static final SeqType ARG_TYPE = new ChoiceItemType(
      Arrays.asList(ELEMENT_O, ATTRIBUTE_O)).seqType();

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode node = toNode(arg(0), qc);
    ARG_TYPE.coerce(node, null, qc, null, ii);
    return annotate(node.type == NodeType.ATTRIBUTE ? UNTYPED_ATOMIC : UNTYPED, qc, ii);
  }
}