package org.basex.util.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.TokenSet;
import org.basex.util.Util;

/**
 * Simple stop words set for full-text requests.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class StopWords extends TokenSet {
  /**
   * Default constructor.
   */
  public StopWords() { }

  /**
   * Constructor, reading stopword list from disk.
   * And creating database stopword file.
   * @param data data reference
   * @param file stopword list file
   * @throws IOException I/O exception
   */
  public StopWords(final Data data, final String file) throws IOException {
    if(!data.meta.prop.get(Prop.STOPWORDS).isEmpty()) read(IO.get(file), false);
    final DataOutput out = new DataOutput(data.meta.file(DATASWL));
    write(out);
    out.close();
  }

  /**
   * Compiles the stop word list.
   * @param data data reference
   */
  public void comp(final Data data) {
    // stop words have already been defined..
    if(size() != 0) return;
    // try to parse the stop words file of the current database
    try {
      final File file = data.meta.file(DATASWL);
      if(!file.exists()) return;
      final DataInput in = new DataInput(data.meta.file(DATASWL));
      try {
        read(in);
      } finally {
        in.close();
      }
    } catch(final Exception ex) {
      Util.debug(ex);
    }
  }

  /**
   * Reads a stop words file.
   * @param fl file reference
   * @param e except flag
   * @return true if everything went alright
   */
  public boolean read(final IO fl, final boolean e) {
    try {
      final byte[] content = norm(fl.content());
      final int s = contains(content, ' ') ? ' ' : '\n';
      for(final byte[] sl : split(content, s)) {
        if(e) delete(sl);
        else if(id(sl) == 0) add(sl);
      }
      return true;
    } catch(final IOException ex) {
      return false;
    }
  }
}
