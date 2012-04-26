package org.basex.io.serial;

import static org.basex.data.DataText.*;

import java.io.*;

/**
 * This class serializes data as XML.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class XMLSerializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  XMLSerializer(final OutputStream os, final SerializerProp p) throws IOException {
    super(os, p, V10, V11);
  }
}
