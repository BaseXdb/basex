package org.basex.util;

import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * <p>This class provides convenience operations for strings.</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Strings {
  /** UTF8 encoding string. */
  public static final String UTF8 = "UTF-8";
  /** UTF16 encoding string. */
  public static final String UTF16 = "UTF-16";
  /** UTF16BE encoding string. */
  public static final String UTF16BE = "UTF-16BE";
  /** UTF16 encoding string. */
  public static final String UTF16LE = "UTF-16LE";
  /** UTF16 encoding string. */
  public static final String UTF32 = "UTF-32";
  /** ISO-8859-1 encoding string. */
  public static final String ISO88591 = "ISO-8859-1";

  /** UTF8 encoding strings. */
  private static final String[] ALL_UTF8 = { UTF8, "UTF8" };
  /** UTF16-LE encoding strings. */
  private static final String[] ALL_UTF16 = { UTF16, "UTF16" };
  /** UTF32 encoding strings. */
  private static final String[] ALL_UTF32 = { UTF32, "UTF32" };

  /** Available encodings. */
  private static String[] encodings;

  /** Hidden constructor. */
  private Strings() { }

  /**
   * Converts the specified string into an long value.
   * {@link Long#MIN_VALUE} is returned if the input is invalid.
   * @param string string to be converted
   * @return resulting long value
   */
  public static long toLong(final String string) {
    return Token.toLong(Token.token(string));
  }

  /**
   * Converts the specified string into an integer value.
   * {@link Integer#MIN_VALUE} is returned if the input is invalid.
   * @param string string to be converted
   * @return resulting integer value
   */
  public static int toInt(final String string) {
    return Token.toInt(Token.token(string));
  }

  /**
   * Checks if the specified string is "yes", "true", "on" or "1".
   * @param string string to be checked
   * @return result of check
   */
  public static boolean toBoolean(final String string) {
    return eqic(string, "1", Text.TRUE, Text.YES, Text.ON);
  }

  /**
   * Compares two strings for equality. The arguments may be {@code null}.
   * @param string1 first string
   * @param string2 strings to be compared
   * @return true if one test is successful
   */
  public static boolean eq(final String string1, final String string2) {
    return Objects.equals(string1, string2);
  }

  /**
   * Compares several strings for equality. The arguments may be {@code null}.
   * @param string first string
   * @param strings strings to be compared
   * @return true if one test is successful
   */
  public static boolean eq(final String string, final String... strings) {
    for(final String str : strings) {
      if(Objects.equals(string, str)) return true;
    }
    return false;
  }

  /**
   * Compares several strings for equality, ignoring the case.
   * @param string first string
   * @param strings strings to be compared
   * @return true if one test is successful
   */
  public static boolean eqic(final String string, final String... strings) {
    for(final String str : strings) {
      if(string == null ? str == null : string.equalsIgnoreCase(str)) return true;
    }
    return false;
  }

  /**
   * Splits a string around matches of the given separator.
   * @param string string to be split
   * @param separator separation character
   * @return resulting strings
   */
  public static String[] split(final String string, final char separator) {
    return split(string, separator, -1);
  }

  /**
   * Splits a string around matches of the given separator.
   * @param string string to be split
   * @param separator separation character
   * @param limit maximum number of strings (ignored if {@code -1})
   * @return resulting strings
   */
  public static String[] split(final String string, final char separator, final int limit) {
    final StringList sl = new StringList(Array.initialCapacity(limit));
    final int tl = string.length();
    int s = 0, c = 1;
    final int l = limit >= 0 ? limit : Integer.MAX_VALUE;
    for(int p = 0; p < tl && c < l; p++) {
      if(string.charAt(p) == separator) {
        sl.add(string.substring(s, p));
        s = p + 1;
        c++;
      }
    }
    return sl.add(string.substring(s, tl)).finish();
  }

  /**
   * Deletes a character from a string.
   * @param string string
   * @param ch character to be removed
   * @return resulting token
   */
  public static String delete(final String string, final char ch) {
    if(!contains(string, ch)) return string;

    final int tl = string.length();
    final StringBuilder sb = new StringBuilder(tl - 1);
    for(int p = 0; p < tl; p++) {
      final char c = string.charAt(p);
      if(c != ch) sb.append(c);
    }
    return sb.toString();
  }

  /**
   * Checks if a string contains a character.
   * @param string string
   * @param ch character to search for
   * @return result of check
   */
  public static boolean contains(final String string, final char ch) {
    return string.indexOf(ch) != -1;
  }

  /**
   * Returns an MD5 hash in lower case.
   * @param string string to be hashed
   * @return md5 hash
   */
  public static String md5(final String string) {
    return hash(string, "MD5");
  }

  /**
   * Returns an SHA256 hash in lower case.
   * @param string string to be hashed
   * @return sha256 hash
   */
  public static String sha256(final String string) {
    return hash(string, "SHA-256");
  }

  /**
   * Returns a hash in lower case.
   * @param string string to be hashed
   * @param algo hashing algorithm
   * @return hash
   */
  private static String hash(final String string, final String algo) {
    try {
      final MessageDigest md = MessageDigest.getInstance(algo);
      return Token.string(Token.hex(md.digest(Token.token(string)), false));
    } catch(final Exception ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns a unified representation of the specified encoding.
   * @param encoding input encoding (UTF-8 is returned for a {@code null} reference)
   * @return encoding
   */
  public static String normEncoding(final String encoding) {
    return normEncoding(encoding, false);
  }

  /**
   * Returns a unified representation of the specified encoding.
   * @param encoding input encoding (UTF-8 is returned for a {@code null} reference)
   * @param utf16 normalize UTF-16 encoding
   * @return encoding
   */
  public static String normEncoding(final String encoding, final boolean utf16) {
    if(encoding == null) return UTF8;
    final String e = encoding.toUpperCase(Locale.ENGLISH);
    if(eq(e, ALL_UTF8)) return UTF8;
    if(e.equals(UTF16LE)) return UTF16LE;
    if(e.equals(UTF16BE)) return UTF16BE;
    if(eq(e, ALL_UTF16))  return utf16 ? UTF16BE : UTF16;
    if(eq(e, ALL_UTF32))  return UTF32;
    return encoding;
  }

  /**
   * Checks if the specified encoding is supported.
   * @param encoding encoding
   * @return result of check
   */
  public static boolean supported(final String encoding) {
    try {
      return Charset.isSupported(encoding);
    } catch(final IllegalArgumentException ex) {
      return false;
    }
  }

  /**
   * Capitalizes the first letter of a string.
   * @param string input string
   * @return capitalized string
   */
  public static String capitalize(final String string) {
    final StringBuilder sb = new StringBuilder();
    if(!string.isEmpty())
      sb.append(Character.toUpperCase(string.charAt(0))).append(string.substring(1));
    return sb.toString();
  }

  /**
   * Converts the given string to a Java class name. Slashes will be replaced with dots, and
   * the last package segment will be capitalized and camel-cased.
   * @param string string to convert
   * @return class name
   */
  public static String className(final String string) {
    final String s = string.replace('/', '.');
    final int c = s.lastIndexOf('.') + 1;
    return s.substring(0, c) + capitalize(camelCase(s.substring(c)));
  }

  /**
   * Converts the given string to camel case.
   * @param string string to convert
   * @return resulting string
   */
  public static String camelCase(final String string) {
    final StringBuilder sb = new StringBuilder();
    boolean upper = false;
    final int sl = string.length();
    for(int s = 0; s < sl; s++) {
      final char ch = string.charAt(s);
      if(ch == '-') {
        upper = true;
      } else if(upper) {
        sb.append(Character.toUpperCase(ch));
        upper = false;
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /**
   * Checks if the specified string is "no", "false", "off" or "0".
   * @param string string to be checked
   * @return result of check
   */
  public static boolean no(final String string) {
    return eqic(string, Text.FALSE, Text.NO, Text.OFF, "0");
  }

  /**
   * Converts a URI to a directory path.
   * See https://docs.basex.org/wiki/Repository#URI_Rewriting for details.
   * @param uri namespace uri
   * @return converted path
   */
  public static String uri2path(final String uri) {
    String path = uri;
    try {
      final URI u = new URI(uri);
      final TokenBuilder tb = new TokenBuilder();
      if(u.isOpaque()) {
        tb.add(u.getScheme()).add('/').add(u.getSchemeSpecificPart().replace(':', '/'));
      } else {
        final String auth = u.getAuthority();
        if(auth != null) {
          // reverse authority, replace dots by slashes. example: basex.org  ->  org/basex
          final String[] comp = split(auth, '.');
          for(int c = comp.length - 1; c >= 0; c--) tb.add('/').add(comp[c]);
        }
        // add remaining path
        final String p = u.getPath();
        tb.add(p == null || p.isEmpty() ? "/" : p.replace('.', '/'));
      }
      path = tb.toString();
    } catch(final URISyntaxException ignore) { }

    // replace special characters with dashes; remove multiple slashes
    path = path.replaceAll("[^\\w.-/]+", "-").replaceAll("//+", "/");
    // add "index" string
    if(Strings.endsWith(path, '/')) path += "index";
    // remove heading slash
    if(Strings.startsWith(path, '/')) path = path.substring(1);
    return path;
  }

  /**
   * Checks if a string starts with the specified character.
   * @param string string
   * @param ch character to be found
   * @return result of check
   */
  public static boolean startsWith(final String string, final char ch) {
    return string.indexOf(ch) == 0;
  }

  /**
   * Checks if a string ends with the specified character.
   * @param string string
   * @param ch character to be found
   * @return result of check
   */
  public static boolean endsWith(final String string, final char ch) {
    final int sl = string.length();
    return sl > 0 && string.charAt(sl - 1) == ch;
  }

  /**
   * Concatenates multiple objects.
   * @param objects objects
   * @return resulting string
   */
  public static String concat(final Object... objects) {
    return Token.string(Token.concat(objects));
  }

  /**
   * Returns a string array with all supported encodings.
   * @return encodings
   */
  public static String[] encodings() {
    if(encodings == null) encodings = Charset.availableCharsets().keySet().toArray(new String[0]);
    return encodings;
  }
}
