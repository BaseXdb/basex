package org.basex.build.file;

import java.io.*;

import org.basex.core.*;
import org.basex.query.util.json.JsonParser.Spec;
import org.basex.util.*;

/**
 * This class contains parser properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JsonProp extends AProp {
  /** Option: encoding. */
  public static final Object[] ENCODING = { "encoding", Token.UTF8 };
  /** Option: parser specification. */
  public static final Object[] SPEC = { "spec", Spec.RFC4627.name() };
  /** Option: unescape special characters. */
  public static final Object[] UNESCAPE = { "unescape", true };
  /** Option: JSON format (default, jsonml, plain, map). */
  public static final Object[] FORMAT = { "format", "default" };
  /** Option: lossless naming. */
  public static final Object[] LOSSLESS = { "lossless", true };
  /** Option: root types. */
  public static final Object[] ROOT_TYPES = { "root-types", true };

  /**
   * Default constructor.
   */
  public JsonProp() {
    super();
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string. Properties are separated with commas ({@code ,}),
   * key/values with the equality character ({@code =}).
   * @throws IOException I/O exception
   */
  public JsonProp(final String s) throws IOException {
    parse(s);
  }
}
