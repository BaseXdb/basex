package org.basex.test.stats;

import static org.basex.data.DataText.DATATBL;
import static org.basex.util.Token.string;
import java.io.File;
import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.io.in.DataInput;
import org.basex.util.list.TokenList;

/**
 * This class prints storage statistics.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public class StorageStats extends Statistics {
  /** Storage parameters to analyze. */
  private static final String[] FIELDS = new String[] {
      "Total number of blocks", "Number of used blocks",
      "First PRE of last block", "Empty blocks reused"
  };

  /**
   * Main method of the example class.
   * @param args command-line arguments
   * @throws IOException during reading of the file
   */
  public static void main(final String[] args) throws IOException {
    // tbli.basex provided manually:
    if(args.length == 1) {
      final File f = new File(args[0]);
      if(f.exists()) {
        final TokenList tl = new TokenList();
        analyzeTBLI(f, tl);
        for(int i = 0; i < FIELDS.length; i++)
          System.out.println(FIELDS[i] + ": " + string(tl.get(i)));
        return;
      }
    }
    // analyze existing database:
    new StorageStats(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public StorageStats(final String[] args) {
    if(!init(args)) return;

    run(FIELDS);
  }

  @Override
  void analyze(final TokenList tl) throws BaseXException {
    try {
      analyzeTBLI(ctx.data.meta.dbfile(DATATBL + 'i'), tl);
    } catch(final IOException e) {
      throw new BaseXException(e);
    }
  }

  /**
   * Read meta data from the tbli.basex file.
   * @param file path to the tbli file
   * @param tl output
   * @throws IOException during reading of the file
   */
  private static void analyzeTBLI(final File file, final TokenList tl)
      throws IOException {
    final DataInput in = new DataInput(file);
    try {
      // total number of blocks:
      tl.add(in.readNum());
      // number of used blocks:
      final int blocks = in.readNum();
      tl.add(blocks);
      // block first pres:
      final int[] fpres = in.readNums();
      tl.add(fpres[blocks - 1]);
      // final int[] pages =
      in.readNums();
      // pagemap size:
      final int psize = in.readNum();
      tl.add(psize > 0 ? "yes" : "no");
    } finally {
      in.close();
    }
  }
}
