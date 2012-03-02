package org.basex.query.path;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;

/**
 * Document kind test.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DocTest extends KindTest {
  /**
   * Constructor.
   * @param c child node
   */
  public DocTest(final NodeType c) {
    super(NodeType.DOC, null, c);
  }

  @Override
  public boolean comp(final QueryContext ctx) {
    return true;
  }

  @Override
  public boolean eval(final ANode node) {
    final AxisMoreIter ai = node.children();
    return ai.more() && ai.next().type == extype && !ai.more();
  }
}
