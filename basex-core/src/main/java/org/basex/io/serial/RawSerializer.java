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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RawSerializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  RawSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException { }

  @Override
  protected void comment(final byte[] value) throws IOException { }

  @Override
  protected void pi(final byte[] name, final byte[] value) throws IOException { }

  @Override
  protected void startOpen(final byte[] name) throws IOException { }

  @Override
  protected void finishOpen() throws IOException { }

  @Override
  protected void finishEmpty() throws IOException { }

  @Override
  protected void finishClose() throws IOException { }

  @Override
  protected void atomic(final Item it, final boolean iter) throws IOException {
    try(final InputStream is = it.input(null)) {
      final PrintOutput po = out;
      for(int i; (i = is.read()) != -1;) po.write(i);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  protected void encode(final int cp) throws IOException {
    print(cp);
  }
}
