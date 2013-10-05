package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;

import java.io.*;

import org.basex.build.*;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends OutputSerializer {
  /** JSON props. */
  protected final JsonProp jprop;

  /**
   * Constructor.
   * @param os output stream reference
   * @param props serialization properties
   * @throws IOException I/O exception
   */
  protected JsonSerializer(final OutputStream os, final SerializerProp props)
      throws IOException {

    super(os, props);
    props.set(S_METHOD, M_JSON);
    jprop = new JsonProp(props.get(SerializerProp.S_JSON));
  }
}
