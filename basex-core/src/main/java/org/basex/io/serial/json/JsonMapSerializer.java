package org.basex.io.serial.json;

import java.io.*;

import org.basex.io.parse.json.*;
import org.basex.io.serial.*;

/**
 * This class serializes map data as JSON. The input must conform to the rules
 * defined in the {@link JsonMapConverter} class.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class JsonMapSerializer extends JsonSerializer {
  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonMapSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
  }
}
