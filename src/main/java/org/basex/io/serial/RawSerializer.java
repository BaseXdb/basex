package org.basex.io.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.basex.io.out.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.item.Item;

/**
 * This class serializes data in its internal format: no indentation and entity
 * coding takes place, binary data is directly output as raw data,
 * and all nodes except for text nodes are skipped.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class RawSerializer extends TextSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sp serialization properties
   * @throws IOException I/O exception
   */
  RawSerializer(final OutputStream os, final SerializerProp sp)
      throws IOException {
    super(os, sp);
  }

  @Override
  public void finishItem(final Item it) throws IOException {
    try {
      final InputStream is = it.input(null);
      try {
        final PrintOutput po = out;
        for(int i; (i = is.read()) != -1;) po.write(i);
      } finally {
        is.close();
      }
    } catch(final QueryException ex) {
      throw new SerializerException(ex.err());
    }
  }

  @Override
  protected void code(final int ch) throws IOException {
    printChar(ch);
  }
}
