package org.basex.io.serial;

import java.io.*;

import org.basex.query.value.item.*;

/**
 * This class serializes items as XML.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XMLSerializer extends MarkupSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  XMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, V10, V11);
  }

  @Override
  protected void doctype(final QNm name) throws IOException {
    if(docsys != null) printDoctype(name.local(), docpub, docsys);
  }
}
