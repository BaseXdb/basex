package org.basex.io.serial.json;

import static org.basex.io.serial.SerializerOptions.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.serial.*;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends OutputSerializer {
  /** JSON options. */
  protected final JsonSerialOptions jopts;

  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  protected JsonSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
    opts.set(METHOD, SerialMethod.JSON);
    jopts = opts.get(SerializerOptions.JSON);
    if(jopts.contains(JsonSerialOptions.INDENT)) {
      indent = jopts.get(JsonSerialOptions.INDENT);
    }
  }
}
