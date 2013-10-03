package org.basex.io.serial;

import java.io.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This class serializes data in its internal format: no indentation and entity
 * coding takes place, binary data is directly output as raw data,
 * and all nodes except for text nodes are skipped.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RawSerializer extends TextSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sp serialization properties
   * @throws IOException I/O exception
   */
  RawSerializer(final OutputStream os, final SerializerProp sp) throws IOException {
    super(os, sp);
  }

  @Override
  protected void atomic(final Item it) throws IOException {
    try {
      final InputStream is = it.input(null);
      try {
        final PrintOutput po = out;
        for(int i; (i = is.read()) != -1;) po.write(i);
      } finally {
        is.close();
      }
    } catch(final QueryException ex) {
      throw new SerializerException(ex);
    }
  }

  @Override
  protected void encode(final int ch) throws IOException {
    print(ch);
  }
}
