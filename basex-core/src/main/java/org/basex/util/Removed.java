package org.basex.util;

import java.util.*;

/**
 * Features that were removed in the current major version, with their replacements.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Removed {
  /** Removed functions (with default prefixes) and replacements. */
  private static final HashMap<String, String> FUNCTIONS = new HashMap<>();
  /** Removed module prefixes and replacements. */
  private static final HashMap<String, String> PREFIXES = new HashMap<>();
  /** Removed options (lower case) and replacements. */
  private static final HashMap<String, String> OPTIONS = new HashMap<>();
  /** Removed option values ({@code option=value}, lower case) and replacements. */
  private static final HashMap<String, String> VALUES = new HashMap<>();

  static {
    FUNCTIONS.put("fn:sequence-join", "fn:insert-separator");
    FUNCTIONS.put("fn:intersperse", "fn:insert-separator");
    FUNCTIONS.put("fn:csv-to-arrays", "fn:parse-csv");
    FUNCTIONS.put("fn:function-identity", "");
    FUNCTIONS.put("fn:subsequence-where", "fn:drop-while and fn:take-while");
    FUNCTIONS.put("map:pairs", "map:entries");
    FUNCTIONS.put("map:of-pairs", "map:build");
    FUNCTIONS.put("map:keys-where", "map:keys or map:filter");
    FUNCTIONS.put("convert:binary-to-string", "bin:decode-string");
    FUNCTIONS.put("convert:string-to-base64", "bin:encode-string");
    FUNCTIONS.put("convert:string-to-hex", "bin:encode-string");
    FUNCTIONS.put("convert:integer-to-dayTime", "fn:seconds");
    FUNCTIONS.put("convert:dayTime-to-integer", "fn:duration-to-seconds");
    FUNCTIONS.put("string:format", "fn:pad-string or string templates");
    FUNCTIONS.put("web:forward", "job:execute");
    PREFIXES.put("hof", "the standard functions");

    OPTIONS.put("mixupdates", "job:execute");
    OPTIONS.put("lserror", "'using fuzzy N errors' or the 'errors' option");
    OPTIONS.put("fairlock", "");
    OPTIONS.put("langkeys", "");
    OPTIONS.put("ignorehostname", "IGNORECERT");
    OPTIONS.put("splitsize", "");
    OPTIONS.put("tailcalls", "");
    OPTIONS.put("addcache", "");
    OPTIONS.put("forcecreate", "db:create");
    OPTIONS.put("createonly", "CLOSE");
    OPTIONS.put("checkstrings", "");
    OPTIONS.put("field-delimiter", "'separator'");
    OPTIONS.put("row-delimiter", "");
    OPTIONS.put("skip-empty", "");
    OPTIONS.put("strict-quoting", "");
    OPTIONS.put("number-parser", "'number-format'");
    OPTIONS.put("entity-expansion-limit", "");
    OPTIONS.put("allow-external-entities", "");
    OPTIONS.put("false-on-error", "");
    OPTIONS.put("indents", "'indent-unit'");
    OPTIONS.put("tabulator", "'indent-unit'");
    OPTIONS.put("newline", "'line-ending'");
    VALUES.put("format=xquery", "'w3'");
    VALUES.put("format=basic", "'w3-xml'");
    VALUES.put("format=w3-arrays", "'w3'");
    VALUES.put("authmethod=digest", "'Basic' via HTTPS");
  }

  /** Private constructor. */
  private Removed() { }

  /**
   * Returns a hint for a removed function.
   * @param name function name with default prefix
   * @return hint or {@code null}
   */
  public static String function(final String name) {
    return hint(FUNCTIONS.get(name));
  }

  /**
   * Returns a hint for a removed module prefix.
   * @param prefix prefix
   * @return hint or {@code null}
   */
  public static String prefix(final String prefix) {
    return hint(PREFIXES.get(prefix));
  }

  /**
   * Returns a hint for a removed option.
   * @param name option name
   * @return hint or {@code null}
   */
  public static String option(final String name) {
    return hint(OPTIONS.get(name.toLowerCase(Locale.ENGLISH)));
  }

  /**
   * Returns a hint for a removed option value.
   * @param name option name
   * @param value value
   * @return hint or {@code null}
   */
  public static String value(final String name, final String value) {
    return hint(VALUES.get((name + '=' + value).toLowerCase(Locale.ENGLISH)));
  }

  /**
   * Returns a hint for a replacement.
   * @param use replacement string (empty if there is none, {@code null} if nothing was removed)
   * @return hint or {@code null}
   */
  private static String hint(final String use) {
    return use == null ? null : "removed" +  (use.isEmpty() ? "" : "; use " + use);
  }
}
