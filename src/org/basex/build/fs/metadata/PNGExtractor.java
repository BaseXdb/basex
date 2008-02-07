package org.basex.build.fs.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.util.Token;
import static org.basex.build.fs.FSText.*;

/**
 * PNG meta data extractor.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PNGExtractor extends AbstractExtractor {
  /** Byte array for PNG header. */
  private final byte[] data = new byte[24];
  
  @Override
  public void extract(final Builder listener, final File f) throws IOException {

    final FileInputStream in = new FileInputStream(f);
    in.read(data);
    in.close();
    
    // check if the header is valid
    if(!Token.startsWith(data, HEADERPNG)) return;

    // open image tag
    listener.startNode(IMAGE, new byte[][] { TYPE, TYPEPNG });

    // extract image dimensions
    final int w = ((data[16] & 0xFF) << 24) + ((data[17] & 0xFF) << 16) +
      ((data[18] & 0xFF) << 8) + (data[19] & 0xFF);
    final int h = ((data[20] & 0xFF) << 24) + ((data[21] & 0xFF) << 16) +
      ((data[22] & 0xFF) << 8) + (data[23] & 0xFF);

    listener.nodeAndText(WIDTH, Token.token(w));
    listener.nodeAndText(HEIGHT, Token.token(h));
    
    listener.endNode(IMAGE);
  }
}
