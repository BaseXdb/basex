package org.basex.build.fs.parser;

import static org.basex.util.Token.*;
import java.io.EOFException;
import java.io.IOException;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.MetaElem;
import org.basex.build.fs.util.MetaStore.MetaType;
import org.basex.build.fs.util.MetaStore.MimeType;

/**
 * Parser for GIF files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public final class GIFParser extends AbstractParser {

  /** GIF87a header info. */
  private static final byte[] HEADERGIF87 = token("GIF87a");
  /** GIF89a header info. */
  private static final byte[] HEADERGIF89 = token("GIF89a");

  static {
    NewFSParser.register("gif", GIFParser.class);
  }

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    final int len = HEADERGIF87.length;
    if(f.size() < len) return false;
    final byte[] header = f.get(new byte[len]);
    return eq(header, HEADERGIF87) || eq(header, HEADERGIF89);
  }

  @Override
  protected void meta(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    try {
      f.buffer(10);
    } catch(final EOFException e) {
      return;
    }
    if(!check(f)) return;

    meta.setType(MetaType.PICTURE);
    meta.setFormat(MimeType.GIF);

    // extract image dimensions
    meta.add(MetaElem.PIXEL_WIDTH, f.get() + (f.get() << 8));
    meta.add(MetaElem.PIXEL_HEIGHT, f.get() + (f.get() << 8));
  }

  @Override
  protected void content(final BufferedFileChannel bfc, //
      final NewFSParser parser) {
  // no content to read...
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) {
    return false;
  }
}
