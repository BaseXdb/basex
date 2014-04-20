package org.basex.query.value.item;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * URI tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public class UriTest {

  @Test
  public void isAbsolute() throws Exception {
    assertUriIsAbsolute("x:", true);

    // absolute URIs always have schema
    assertUriIsAbsolute("x", false);
    assertUriIsAbsolute("", false);
    assertUriIsAbsolute("//localhost:80", false);

    // absolute URIs don't have fragments
    assertUriIsAbsolute("http://localhost:80/html#f", false);
  }

  @Test
  public void isValid() throws Exception {
    assertUriIsValid("x:", true);
    assertUriIsValid("x", true);
    assertUriIsValid("", true);
    assertUriIsValid("//localhost:80", true);
  }

  private static void assertUriIsValid(final String uri, final boolean expected) {
    assertEquals("Uri validation failed for '" + uri + "'" + uri, expected, Uri.uri(uri).isValid());
  }

  private static void assertUriIsAbsolute(final String uri, final boolean expected) {
    assertEquals("Uri absolute check failed for '" + uri + "'", expected, Uri.uri(uri).isAbsolute());
  }
}
