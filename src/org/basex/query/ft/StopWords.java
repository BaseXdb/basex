package org.basex.query.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.util.TokenSet;

/**
 * Simple stop words set for full-text requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class StopWords extends TokenSet {
  /**
   * Default Constructor.
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
    if(!data.meta.prop.get(Prop.STOPWORDS).equals(""))
      read(IO.get(file), false);
    final DataOutput out = new DataOutput(data.meta.file(DATASWL));
    write(out);
    out.close();
  }

  /**
   * Compiles the full-text options.
   * @param ctx query context
   */
  public void comp(final QueryContext ctx) {
    // stop words have already been defined..
    if(size() != 0 || !(ctx.item instanceof DBNode)) return;
    // try to parse the stop words file of the current database
    try {
      final Data data = ((DBNode) ctx.item).data;
      final File file = data.meta.file(DATASWL);
      if(!file.exists()) return;
      final DataInput in = new DataInput(data.meta.file(DATASWL));
      read(in);
      in.close();
    } catch(final Exception ex) {
      Main.debug(ex);
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
