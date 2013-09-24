package org.basex.io.serial;

import static org.basex.io.serial.SerializerProp.*;

import java.io.*;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends OutputSerializer {
  /**
   * Constructor.
   * @param os output stream reference
   * @param props serialization properties
   * @throws IOException I/O exception
   */
  protected JsonSerializer(final OutputStream os, final SerializerProp props)
      throws IOException {
    super(os, props);
  }

  /**
   * Returns a specific serializer.
   * @param os output stream reference
   * @param props serialization properties (can be {@code null})
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os, final SerializerProp props)
      throws IOException {

    return props.get(S_JSON_FORMAT).equals("json") ?
      new JsonCGSerializer(os, props) : new JsonMLSerializer(os, props);
  }
}
