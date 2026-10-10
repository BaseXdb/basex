package org.basex.query.value;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.tree.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the value builder.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValueBuilderTest extends SandboxTest {
  /** A tree is built once the specified number of items has been added. */
  @Test public void tree() {
    try(QueryContext qc = new QueryContext(context)) {
      final ValueBuilder vb = new ValueBuilder(qc).tree(8);
      for(int i = 0; i < 10; i++) vb.add(Str.get("s" + i));
      final Value value = vb.value();
      assertEquals(10, value.size());
      assertInstanceOf(TreeSeq.class, value);
    }
  }
}
