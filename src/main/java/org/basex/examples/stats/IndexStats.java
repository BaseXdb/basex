package org.basex.examples.stats;

import static org.basex.data.DataText.*;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.Set;
import org.basex.util.Performance;
import org.basex.util.TokenList;

/**
 * This class prints index statistics.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class IndexStats extends Statistics {
  /**
   * Main method of the example class.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new IndexStats(args);
  }
  
  /**
   * Constructor.
   * @param args command-line arguments
   */
  private IndexStats(final String[] args) {
    if(!init(args)) return;
    run("$path", "$text", "$attr", "$fuzzy", "$trie", "ltext",
        "lattr", "lfuzzy", "ltrie");
  }

  @Override
  void analyze(final TokenList tl) throws BaseXException {
    index(CmdIndex.PATH, tl);
    final String ltxt = index(CmdIndex.TEXT, tl,
        DATATXT + 'l', DATATXT + 'r');
    final String latv = index(CmdIndex.ATTRIBUTE, tl,
        DATAATV + 'l', DATAATV + 'r');

    exec(new Set(Prop.WILDCARDS, false));
    final String lftt = index(CmdIndex.FULLTEXT, tl,
        DATAFTX + 'x', DATAFTX + 'y', DATAFTX + 'z');
    exec(new Set(Prop.WILDCARDS, true));
    final String lftf = index(CmdIndex.FULLTEXT, tl,
        DATAFTX + 'a', DATAFTX + 'b', DATAFTX + 'c');

    tl.add(ltxt);
    tl.add(latv);
    tl.add(lftf);
    tl.add(lftt);
  }

  /**
   * Creates and drops the specified index.
   * @param index index
   * @param tl token list
   * @param dbf database files
   * @return length
   * @throws BaseXException exception
   */
  private String index(final CmdIndex index, final TokenList tl,
      final String... dbf) throws BaseXException {
    p.getTimer();
    exec(new CreateIndex(index));
    tl.add(p.toString().replace(" ms", ""));
    long l = 0;
    for(final String d : dbf) l += ctx.data.meta.file(d).length();
    exec(new DropIndex(index));
    return Performance.format(l);
  }
}
