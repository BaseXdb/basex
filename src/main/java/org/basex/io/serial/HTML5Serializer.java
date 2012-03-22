package org.basex.io.serial;

import static org.basex.data.DataText.*;

import java.io.*;

/**
 * This class serializes data as XHTML.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class HTML5Serializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  HTML5Serializer(final OutputStream os, final SerializerProp p) throws IOException {
    super(os, p);
  }

  @Override
  void doctype(final byte[] dt) throws IOException {
    if(level != 0) return;
    if(sep) indent();
    print(DOCTYPE);
    print(M_HTML);
    print(ELEM_C);
    print(nl);
  }
}
