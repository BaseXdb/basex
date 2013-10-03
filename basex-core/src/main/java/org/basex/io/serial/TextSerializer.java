package org.basex.io.serial;

import java.io.*;

/**
 * This class serializes data as text.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class TextSerializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  TextSerializer(final OutputStream os, final SerializerProp p) throws IOException {
    super(os, p);
  }

  @Override
  protected void attribute(final byte[] n, final byte[] v) throws IOException { }

  @Override
  protected void finishComment(final byte[] n) throws IOException { }

  @Override
  protected void finishPi(final byte[] n, final byte[] v) throws IOException { }

  @Override
  protected void startOpen(final byte[] t) throws IOException { }

  @Override
  protected void finishOpen() throws IOException { }

  @Override
  protected void finishEmpty() throws IOException { }

  @Override
  protected void finishClose() throws IOException { }

  @Override
  protected void encode(final int ch) throws IOException {
    printChar(ch);
  }
}
