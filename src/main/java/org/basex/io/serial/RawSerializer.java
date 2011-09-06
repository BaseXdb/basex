package org.basex.io.serial;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.query.item.Bin;
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
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  RawSerializer(final OutputStream os, final SerializerProp p)
      throws IOException {
    super(os, p);
  }

  @Override
  public void finishItem(final Item it) throws IOException {
    print(it instanceof Bin ? ((Bin) it).toJava() : atom(it));
  }

  @Override
  protected void code(final int ch) throws IOException {
    print(ch);
  }
}
