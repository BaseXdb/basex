package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerOptions.*;

import java.io.*;

import org.basex.build.*;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends OutputSerializer {
  /** JSON options. */
  protected final JsonParserOptions joptions;

  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  protected JsonSerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {

    super(os, opts);
    opts.set(S_METHOD, M_JSON);
    joptions = new JsonParserOptions(opts.get(SerializerOptions.S_JSON));
  }
}
