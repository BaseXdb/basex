package org.basex.io.serial;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;

/**
 * This class serializes items as text.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class TextSerializer extends StandardSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param out print output
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  TextSerializer(final PrintOutput out, final SerializerOptions sopts) throws IOException {
    super(out, sopts);
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    printChars(norm(value));
    sep = false;
  }
}
