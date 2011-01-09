package org.basex.examples.create;

import static org.basex.util.Token.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.basex.build.FileParser;
import org.basex.io.BufferInput;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.deepfs.fs.DeepFS;

/**
 * This class parses files in the LST format
 * and sends events to the specified database builder.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class LSTParser extends FileParser {
  /** Date format. */
  private static final SimpleDateFormat DATE =
    new SimpleDateFormat("yyyy.MM.dd hh:mm.ss");

  /**
   * Constructor.
   * @param path file path
   */
  public LSTParser(final String path) {
    super(path);
  }

  @Override
  public void parse() throws IOException {
    builder.startElem(DeepFS.FSML, atts.reset());

    final BufferInput bi = new BufferInput(file.path());

    //new FileReader(file.path()));
    final TokenBuilder tb = new TokenBuilder();
    bi.readLine(tb);
    atts.add(DeepFS.BACKINGSTORE, replace(tb.finish(), '\\', '/'));
    builder.startElem(DeepFS.DEEPFS, atts);

    byte[][] old = {};
    while(true) {
      bi.readLine(tb);
      if(tb.size() == 0) break;

      final byte[][] entries = split(replace(tb.finish(), '\\', '/'), '\t');
      byte[] name = entries[0];

      byte[] mtime = {};
      try {
        final String time = string(entries[2]) + ' ' + string(entries[3]);
        mtime = token(DATE.parse(time).getTime());
      } catch(final ParseException ex) {
        Util.debug(ex);
      }

      if(indexOf(name, '/') != -1) {
        // Directory
        name = substring(name, 0, name.length - 1);
        final byte[][] path = split(name, '/');

        int i = -1;
        while(++i < Math.min(old.length, path.length)) {
          if(!eq(old[i], path[i])) break;
        }
        for(int j = i; j < old.length; ++j) {
          builder.endElem(DeepFS.DIR);
        }
        for(int j = i; j < path.length; ++j) {
          atts.reset();
          atts.add(DeepFS.NAME, path[i]);
          atts.add(DeepFS.MTIME, mtime);
          builder.startElem(DeepFS.DIR, atts);
        }
        old = path;
      } else {
        // File
        atts.reset();
        atts.add(DeepFS.NAME, name);
        atts.add(DeepFS.SIZE, entries[1]);
        atts.add(DeepFS.MTIME, mtime);
        builder.emptyElem(DeepFS.FILE, atts);
      }
    }
    bi.close();
    for(int j = old.length; j > 0; j--) builder.endElem(DeepFS.DIR);

    builder.endElem(DeepFS.DEEPFS);
    builder.endElem(DeepFS.FSML);
    builder.meta.deepfs = true;
  }
}
