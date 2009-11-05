package org.deepfs.fsml.extractors;

import java.io.File;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.io.BufferInput;
import org.basex.util.Token;
import static org.basex.build.fs.FSText.*;

/**
 * PNG meta data extractor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class PNGExtractor extends AbstractExtractor {
  /** Byte array for PNG header. */
  private final byte[] data = new byte[24];

  @Override
  public void extract(final Builder build, final File f) throws IOException {
    BufferInput.read(f, data);

    // check if the header is valid
    if(!Token.startsWith(data, HEADERPNG)) return;

    // open image tag
    build.startElem(IMAGE, atts.set(TYPE, TYPEPNG));

    // extract image dimensions
    final int w = ((data[16] & 0xFF) << 24) + ((data[17] & 0xFF) << 16) +
      ((data[18] & 0xFF) << 8) + (data[19] & 0xFF);
    final int h = ((data[20] & 0xFF) << 24) + ((data[21] & 0xFF) << 16) +
      ((data[22] & 0xFF) << 8) + (data[23] & 0xFF);

    build.nodeAndText(WIDTH, atts.reset(), Token.token(w));
    build.nodeAndText(HEIGHT, atts, Token.token(h));
    build.endElem(IMAGE);
  }
}
