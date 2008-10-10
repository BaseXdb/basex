package org.basex.build.fs.metadata;

import static org.basex.build.fs.FSText.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.util.Token;

/**
 * BMP meta data extractor.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BMPExtractor extends AbstractExtractor {
  /** Byte array for GIF header. */
  private final byte[] data = new byte[26];

  @Override
  public void extract(final Builder listener, final File f) throws IOException {
    final FileInputStream in = new FileInputStream(f);
    in.read(data);
    in.close();
    
    // check if the header is valid
    if(!Token.startsWith(data, HEADERBMP)) return;

    // extract image dimensions
    final int w = ((data[21] & 0xFF) << 24) + ((data[20] & 0xFF) << 16) +
      ((data[19] & 0xFF) << 8) + (data[18] & 0xFF);
    final int h = ((data[25] & 0xFF) << 24) + ((data[24] & 0xFF) << 16) +
      ((data[23] & 0xFF) << 8) + (data[22] & 0xFF);

    // open image tag
    listener.startElem(IMAGE, atts.set(TYPE, TYPEBMP));
    listener.nodeAndText(WIDTH, Token.token(w));
    listener.nodeAndText(HEIGHT, Token.token(h));
    listener.endElem(IMAGE);
  }
}
