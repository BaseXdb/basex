package org.basex.build.fs.metadata;

import static org.basex.build.fs.FSText.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.io.BufferInput;
import org.basex.util.Token;

/**
 * BMP meta data extractor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BMPExtractor extends AbstractExtractor {
  /** Byte array for GIF header. */
  private final byte[] data = new byte[26];

  @Override
  public void extract(final Builder build, final File f) throws IOException {
    BufferInput.read(f, data);

    // check if the header is valid
    if(!Token.startsWith(data, HEADERBMP)) return;

    // extract image dimensions
    final int w = ((data[21] & 0xFF) << 24) + ((data[20] & 0xFF) << 16) +
      ((data[19] & 0xFF) << 8) + (data[18] & 0xFF);
    final int h = ((data[25] & 0xFF) << 24) + ((data[24] & 0xFF) << 16) +
      ((data[23] & 0xFF) << 8) + (data[22] & 0xFF);

    // open image tag
    build.startElem(IMAGE, atts.set(TYPE, TYPEBMP));
    build.nodeAndText(WIDTH, atts.reset(), Token.token(w));
    build.nodeAndText(HEIGHT, atts, Token.token(h));
    build.endElem(IMAGE);
  }
}
