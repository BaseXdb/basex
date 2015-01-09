package org.basex.io.serial;

import java.io.*;

/**
 * This class serializes data as text.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class TextSerializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  TextSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
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
  protected void encode(final int cp) throws IOException {
    printChar(cp);
  }
}
