package org.basex.query.value.item;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * URI tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public class UriTest {
  /**
   * Sample URIs.
   * @return object parameters
   */
  public static Collection<Object[]> sampleUris() {
    return Arrays.asList(new Object[][] {
      { "x:", true, true },
      { "x", true, false },
      { "a string", true, false },
      { "ç.html", true, false },
      { "http:\\\\example.com\\\\examples", true, true },
      { "〜", true, false },
      { " http://example.com/ foo.xml ", true, true },
      { "http://www.w3.org/TestModules/<=>@/test", true, true },
      { "", true, false },
      { "//test.org:80", true, false },
      { "//[fe80::216:ceff:fe86:3e33]", true, false },
      { "x+y://a:b@[fe80::216:ceff:fe86:3e33]:80/p/b/c?q=1&q=2#test?123", true, true },
      { "x+y://a:b@254.254.254.254:80/p/b254/c?q=1&q=2#test?123", true, true },
      { "http://!$&'()*+,;=/", true, true },
      { "http://%0Ad%E2%9C%90%F0%98%9A%A0/", true, true },
      { "odd-scheme+1.://www.example.org/", true, true },
      { "http://www.example.org/%0Ad%E2%9C%90%F0%98%9A%A0/", true, true },
      { "http://www.example.org/!/$/&/'/(/)/*/+/,/;/=/", true, true },
      { "http://www.example.org/:/@/", true, true },
      { "http://www.example.org/?%0Ad%E2%9C%90%F0%98%9A%A0/", true, true },
      { "http://www.example.org/?!$&'()*+,;=", true, true },
      { "http://www.example.org/?:@", true, true },
      { "http://www.example.org/#%0Ad%E2%9C%90%F0%98%9A%A0/", true, true },
      { "http://www.example.org/#!$&'()*+,;=", true, true },
      { "http://www.example.org/#:@", true, true },
      { "ldap://[2001:db8::7]/c=GB?objectClass?one", true, true },
      { "mailto:John.Doe@example.com", true, true },
      { "news:comp.infosystems.www.servers.unix", true, true },
      { "tel:+1-816-555-1212", true, true },
      { "telnet://192.0.2.16:80/", true, true },
      { "urn:oasis:names:specification:docbook:dtd:xml:4.1.2", true, true },

      { "1:", false, false },
      { "invalidURI%gg", false, false },
      { "ttp:\\\\invalidURI\\someURI%gg", false, false },
      { ":/", false, false },
      { "##invalid", false, false },
      { "%gg", false, false },
      { "%", false, false },
      { "http://www.example.com/file%GF.html", false, false },
      { ":/cut.jpg", false, false },
      { ":/images/cut.png", false, false },
      { "http:\\\\invalid%>URI\\someURI", false, false },
      { "//z:1%40", false, false },
      { "//x//x", true, false },
      { "/a/../b/./c/d/.x//x", true, false },
    });
  }

  /**
   * Tests for {@link Uri#isAbsolute()}.
   * @param uri current test URI.
   * @param valid current valid flag.
   * @param absolute current absolute flag.
   */
  @ParameterizedTest
  @MethodSource("sampleUris")
  public void isAbsolute(final String uri, @SuppressWarnings("unused") final boolean valid,
      final boolean absolute) {
    assertEquals(absolute, Uri.uri(uri).isAbsolute(), "Uri absolute check failed");
  }

  /**
   * Tests for {@link Uri#isValid()}.
   * @param uri current test URI.
   * @param valid current valid flag.
   * @param absolute current absolute flag.
   */
  @ParameterizedTest
  @MethodSource("sampleUris")
  public void isValid(final String uri, final boolean valid,
      @SuppressWarnings("unused") final boolean absolute) {
    assertEquals(valid, Uri.uri(uri).isValid(), "Uri validation failed");
  }
}
