package org.basex.query.func.fn;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.index.*;
import org.junit.*;
import org.junit.Test;

/**
 * General test of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Jens Erat
 */
public final class FnIdTest extends SandboxTest {
  /** Test document. */
  private static final String DOC = "src/test/resources/xmark.xml";

  /** Finalize test. */
  @After public void finish() {
    execute(new Close());
  }

  /** Basic delete. */
  @Test public void delete() {
    execute(new CreateDB(NAME, DOC));
    execute(new DropIndex(IndexType.ATTRIBUTE));

    query("data(id('person0')/@id)", "person0");
  }
}