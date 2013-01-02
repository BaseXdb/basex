package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the fetch module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNFetchTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /** Test method. */
  @Test
  public void content() {
    query(_FETCH_CONTENT.args(FILE));
    error(_FETCH_CONTENT.args(FILE + 'x'), Err.BXFE_IO);
    error(_FETCH_CONTENT.args(FILE, "xxx"), Err.BXFE_ENCODING);
  }

  /** Test method. */
  @Test
  public void contentBinary() {
    query(_FETCH_CONTENT_BINARY.args(FILE));
    error(_FETCH_CONTENT_BINARY.args(FILE + 'x'), Err.BXFE_IO);
  }
}
