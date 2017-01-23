package org.basex.query.func.fn;

import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.query.*;
import org.junit.*;
import org.junit.Test;

/**
 * General test of the XQuery Update Facility implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Jens Erat
 */
public final class IdTest extends AdvancedQueryTest {
  /** Test document. */
  private static final String DOC = "src/test/resources/xmark.xml";
  /**
   * Closes the currently opened database.
   */
  @After
  public void finish() {
    execute(new Close());
  }

  /**
   * Basic delete.
   */
  @Test
  public void delete() {
    execute(new CreateDB(NAME, DOC));
    execute(new DropIndex(IndexType.ATTRIBUTE));

    query("data(id('person0')/@id)", "person0");
  }
}