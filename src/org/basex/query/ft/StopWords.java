package org.basex.query.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataInput;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.Token;
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
   * Constructor, reading an existing database stopword file.
   * @param dat data reference
   * @throws IOException IOExcetion
   */
  public StopWords(final Data dat) throws IOException {
    read(new DataInput(getFile(dat)));
  }

  /**
   * Constructor, reading stopword list from disk.
   * And creating database stopword file.
   * @param dat data reference
   * @param file stopword list file
   * @throws IOException I/O exception
   */
  public StopWords(final Data dat, final String file) throws IOException {
    if(!dat.meta.prop.get(Prop.FTSTOPW).equals("")) read(IO.get(file), false);
    write(new DataOutput(getFile(dat)));
  }

  /**
   * Returns the database stopword list file.
   * @param dat data reference
   * @return database stopword list file
   */
  private File getFile(final Data dat) {
    return dat.meta.prop.dbfile(dat.meta.name, DATASWL);
  }

  /**
   * Reads the database stopword list from disk.
   * @param in input stream
   * @throws IOException I/O exception
   */
  private void read(final DataInput in) throws IOException {
    keys = in.readBytesArray();
    next = in.readNums();
    bucket = in.readNums();
    size = in.readNum();
    in.close();
  }

  /**
   * Writes the database stopword list to disk.
   * @param out output stream
   * @throws IOException I/O exception
   */
  private void write(final DataOutput out) throws IOException {
    out.writeBytesArray(keys);
    out.writeNums(next);
    out.writeNums(bucket);
    out.writeNum(size);
    out.close();
  }

  /**
   * Checks if a token is contained in the stopword list.
   * @param tok token looking for
   * @return result
   */
  public boolean contains(final byte[] tok) {
    return size != 0 && id(tok) > 0;
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
      final int s = Token.contains(content, ' ') ? ' ' : '\n';

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
