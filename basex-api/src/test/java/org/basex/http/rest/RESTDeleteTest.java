package org.basex.http.rest;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the embedded REST API and the DELETE method.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class RESTDeleteTest extends RESTTest {
  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test public void delete1() throws IOException {
    put(NAME, new FileInputStream(FILE));
    // delete database
    assertEquals(delete(NAME, 200).trim(), Util.info(Text.DB_DROPPED_X, NAME));
    // no database left
    delete(NAME, 404);
  }

  /**
   * DELETE Test.
   * @throws IOException I/O exception
   */
  @Test public void delete2() throws IOException {
    put(NAME, null);
    put(NAME + "/a", new ArrayInput(token("<a/>")));
    put(NAME + "/b", new ArrayInput(token("<b/>")));
    // delete 'a' directory
    assertStartsWith(delete(NAME + "/a", 200), "1 ");
    // delete 'b' directory
    assertStartsWith(delete(NAME + "/b", 200), "1 ");
    // no 'b' directory left
    assertStartsWith(delete(NAME + "/b", 200), "0 ");
    // delete database
    assertEquals(delete(NAME, 200).trim(), Util.info(Text.DB_DROPPED_X, NAME));
    // no database left
    delete(NAME, 404);
  }

  /**
   * DELETE Test: specify an option.
   * @throws IOException I/O exception
   */
  @Test public void deleteOption() throws IOException {
    put(NAME, null);
    delete(NAME + "/a?" + MainOptions.STRIPWS.name() + "=true", 200);
    // unknown option
    delete(NAME + "/a?xxx=true", 400);
  }
}
