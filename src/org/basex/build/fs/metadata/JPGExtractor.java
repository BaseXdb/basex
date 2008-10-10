package org.basex.build.fs.metadata;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.util.Token;
import static org.basex.build.fs.FSText.*;

/**
 * JPG meta data extractor.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class JPGExtractor extends EXIFExtractor {
  @Override
  public void extract(final Builder listener, final File f) throws IOException {
    //byte[] data = new BufferInput(f, 10).firstBlock();
    final BufferedInputStream in =
      new BufferedInputStream(new FileInputStream(f));

    // check if the header is valid
    for(final int i : HEADERJPG) if(in.read() != i) return;

    // find image dimensions
    while(true) {
      int b = in.read();
      if(b == -1) return;
      if(b != 0xFF) continue;
      b = in.read();
      if(b >= 0xC0 && b <= 0xC3) break;
      int skip = (in.read() << 8) + in.read() - 2;

      try {
        if(b == 0xE1) skip = scanEXIF(in, skip, f.getName());
      } catch(final IOException ex) {
        BaseX.debug(f.toString() + ": " + ex.getMessage());
        exif.clear();
      }
      skip(in, skip);
    }
    skip(in, 3);
    
    // extract image dimensions
    final int h = (in.read() << 8) + in.read();
    final int w = (in.read() << 8) + in.read();

    in.close();

    // open image tag
    listener.startElem(IMAGE, atts.set(TYPE, TYPEJPG));

    listener.nodeAndText(WIDTH, Token.token(w));
    listener.nodeAndText(HEIGHT, Token.token(h));

    if(!exif.isEmpty()) {
      atts.reset();
      listener.startElem(EXIF, atts);
      final Iterator<byte[]> it = exif.iterator();
      while(it.hasNext()) listener.nodeAndText(it.next(), it.next());
      listener.endElem(EXIF);
      exif.clear();
    }
    
    listener.endElem(IMAGE);
  }
}
