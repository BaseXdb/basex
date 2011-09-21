package org.basex.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import org.basex.core.Prop;
import org.basex.io.in.BufferInput;
import org.basex.util.list.ByteList;
import org.xml.sax.InputSource;

/**
 * {@link IO} reference, representing a URL.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class IOUrl extends IO {
  /**
   * Constructor.
   * @param u url
   */
  public IOUrl(final String u) {
    super(u);
  }

  @Override
  public byte[] read() throws IOException {
    final ByteList bl = new ByteList();
    final BufferedInputStream bis = new BufferedInputStream(
        new URL(path).openStream());
    try {
      for(int b; (b = bis.read()) != -1;) bl.add(b);
    } finally {
      try { bis.close(); } catch(final IOException ex) { }
    }
    return bl.toArray();
  }

  @Override
  public InputSource inputSource() {
    return new InputSource(path);
  }

  @Override
  public BufferInput buffer() throws IOException {
    return new BufferInput(new URL(path).openStream());
  }

  @Override
  public long length() {
    return 0;
  }

  @Override
  public IO merge(final String f) {
    return this;
  }

  /**
   * Creates a file path from the specified URL.
   * @param url url to be converted
   * @return file path
   */
  public static String file(final String url) {
    String file = url;
    try {
      if(file.indexOf("%") != -1) file = URLDecoder.decode(file, Prop.ENCODING);
    } catch(final Exception ex) { /* ignored. */ }
    // remove file scheme, duplicate slashes and leading slash in Windows paths
    return file.replaceAll("^" + FILEPREF, "").replaceAll("//+", "/").
        replaceFirst("^/(\\w:)", "$1");
  }
}
