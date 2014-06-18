package org.basex.http.rest;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the embedded REST API and the DELETE method.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RESTDeleteTest extends RESTTest {
  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete1() throws IOException {
    put(NAME, new FileInputStream(FILE));
    // delete database
    assertEquals(delete(NAME).trim(), Util.info(Text.DB_DROPPED_X, NAME));
    try {
      // no database left
      delete(NAME);
      fail("Error expected.");
    } catch(final BaseXException ignored) {
    }
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test
  public void delete2() throws IOException {
    put(NAME, null);
    put(NAME + "/a", new ArrayInput(token("<a/>")));
    put(NAME + "/b", new ArrayInput(token("<b/>")));
    // delete 'a' directory
    assertStartsWith(delete(NAME + "/a"), "1 ");
    // delete 'b' directory
    assertStartsWith(delete(NAME + "/b"), "1 ");
    // no 'b' directory left
    assertStartsWith(delete(NAME + "/b"), "0 ");
    // delete database
    assertEquals(delete(NAME).trim(), Util.info(Text.DB_DROPPED_X, NAME));
    try {
      // no database left
      delete(NAME);
      fail("Error expected.");
    } catch(final BaseXException ignored) {
    }
  }

  /**
   * DELETE Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test
  public void deleteOption() throws IOException {
    put(NAME, null);
    delete(NAME + "/a?" + MainOptions.CHOP.name() + "=true");
    try {
      delete(NAME + "/a?xxx=true");
      fail("Error expected.");
    } catch(final IOException ignored) {
    }
  }

}
