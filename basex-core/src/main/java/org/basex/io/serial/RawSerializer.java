package org.basex.io.serial;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This class serializes items in their internal format: no indentation and entity
 * encoding takes place, binary data is directly output as raw data,
 * and all nodes except for text nodes are skipped.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RawSerializer extends StandardSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param out print output
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  RawSerializer(final PrintOutput out, final SerializerOptions sopts) throws IOException {
    super(out, sopts);
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    printChars(value);
  }

  @Override
  protected void atomic(final Item it) throws IOException {
    try(final InputStream is = it.input(null)) {
      final PrintOutput po = out;
      for(int b; (b = is.read()) != -1;) po.write(b);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }
}
