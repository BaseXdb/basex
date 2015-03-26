package org.basex.util.http;

import java.util.*;

import org.basex.util.*;

/**
 * Single Internet media type.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class MediaType {
  /** Text/plain. */
  public static final MediaType TEXT_PLAIN = new MediaType("text/plain");

  /** Original string. */
  private final String string;
  /** Main type. */
  private final String main;
  /** Sub type. */
  private final String sub;
  /** Parameters. */
  private final HashMap<String, String> params = new HashMap<>();

  /**
   * Constructor.
   * @param string media type string
   */
  public MediaType(final String string) {
    this.string = string;

    final int p = string.indexOf(';');
    final String type = p == -1 ? string : string.substring(0, p);

    final int s = type.indexOf('/');
    main = s == -1 ? type : type.substring(0, s);
    sub  = s == -1 ? ""   : type.substring(s + 1).trim();

    if(p != -1) {
      for(final String param : Strings.split(string.substring(p + 1), ';')) {
        final String[] kv = Strings.split(param, '=', 2);
        params.put(kv[0].trim(), kv.length < 1 ? "" : kv[1].trim());
      }
    }
  }

  /**
   * Returns the media type, composed from the main and sub type.
   * @return type
   */
  public String type() {
    return main.isEmpty() ? "" : new StringBuilder(main).append('/').append(sub).toString();
  }

  /**
   * Returns the parameters.
   * @return parameters
   */
  public HashMap<String, String> parameters() {
    return params;
  }

  @Override
  public String toString() {
    return string;
  }
}
