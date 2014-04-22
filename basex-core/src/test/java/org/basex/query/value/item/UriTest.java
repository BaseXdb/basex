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
  /**
   * Tests {@link Uri#isAbsolute()}.
   */
  @Test
  public void isAbsolute() {
    assertUriIsAbsolute("x:", true);

    // absolute URIs always have schema
    assertUriIsAbsolute("x", false);
    assertUriIsAbsolute("", false);
    assertUriIsAbsolute("//localhost:80", false);

    // absolute URIs don't have fragments
    assertUriIsAbsolute("http://localhost:80/html#f", false);
  }

  /**
   * Tests {@link Uri#isValid()}.
   */
  @Test
  public void isValid() {
    assertUriIsValid("x:", true);
    assertUriIsValid("x", true);
    assertUriIsValid("", true);
    assertUriIsValid("//localhost:80", true);
  }

  /**
   * Tests if a URI is valid.
   * @param uri uri
   * @param expected expected value
   */
  private static void assertUriIsValid(final String uri, final boolean expected) {
    assertEquals("Uri validation failed for '" + uri + "'" + uri, expected, Uri.uri(uri).isValid());
  }

  /**
   * Tests if a URI is absolute.
   * @param uri uri
   * @param expected expected value
   */
  private static void assertUriIsAbsolute(final String uri, final boolean expected) {
    assertEquals("Uri absolute check failed for '" + uri + "'", expected, Uri.uri(uri).isAbsolute());
  }
}
