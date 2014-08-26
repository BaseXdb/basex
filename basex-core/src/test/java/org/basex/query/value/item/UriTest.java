package org.basex.query.value.item;

import static org.junit.Assert.*;

import org.junit.*;

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
    // [DP] #928
    //assertUriIsAbsolute("//localhost:80", false);

    // absolute URIs don't have fragments
    // [DP] #928
    //assertUriIsAbsolute("http://localhost:80/html#f", false);
  }

  /**
   * Tests {@link Uri#isValid()}.
   */
  @Test
  public void isValid() {
    // [DP] #928
    //assertUriIsValid("x:", true);
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
    assertEquals("Uri validation failed for '" + uri + '\'' + uri, expected,
        Uri.uri(uri).isValid());
  }

  /**
   * Tests if a URI is absolute.
   * @param uri uri
   * @param expected expected value
   */
  private static void assertUriIsAbsolute(final String uri, final boolean expected) {
    assertEquals("Uri absolute check failed for '" + uri + '\'', expected,
        Uri.uri(uri).isAbsolute());
  }
}
