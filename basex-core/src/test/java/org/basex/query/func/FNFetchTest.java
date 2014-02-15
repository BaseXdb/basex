package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Fetch Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNFetchTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /** Test method. */
  @Test
  public void text() {
    query(_FETCH_TEXT.args(FILE));
    error(_FETCH_TEXT.args(FILE + 'x'), Err.BXFE_IO);
    error(_FETCH_TEXT.args(FILE, "xxx"), Err.BXFE_ENCODING);
  }

  /** Test method. */
  @Test
  public void binary() {
    query(_FETCH_BINARY.args(FILE));
    error(_FETCH_BINARY.args(FILE + 'x'), Err.BXFE_IO);
  }

  /** Test method. */
  @Test
  public void contentType() {
    query(_FETCH_CONTENT_TYPE.args(FILE));
    error(_FETCH_CONTENT_TYPE.args(FILE + 'x'), Err.BXFE_IO);
  }
}
