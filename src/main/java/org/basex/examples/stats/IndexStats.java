package org.basex.examples.stats;

import static org.basex.data.DataText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdIndex;
import org.basex.core.proc.CreateIndex;
import org.basex.core.proc.DropIndex;
import org.basex.core.proc.InfoIndex;
import org.basex.core.proc.Set;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * This class prints index statistics.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class IndexStats extends Statistics {
  /** Index pattern. */
  private static final Pattern INDEX = Pattern.compile("Entries: (\\d+)");

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
    run("$pth", "$txt", "$atv", "$ftf", "$ftt", "ltext",
        "lattr", "lfuzzy", "ltrie", "#txt", "#atv", "#ftf", "#ftt");
  }

  @Override
  void analyze(final TokenList tl) throws BaseXException {
    index(CmdIndex.PATH, tl);

    final String[] ltxt = index(CmdIndex.TEXT, tl,
        DATATXT + 'l', DATATXT + 'r');

    final String[] latv = index(CmdIndex.ATTRIBUTE, tl,
        DATAATV + 'l', DATAATV + 'r');

    exec(new Set(Prop.WILDCARDS, false));
    final String[] lftt = index(CmdIndex.FULLTEXT, tl,
        DATAFTX + 'x', DATAFTX + 'y', DATAFTX + 'z');

    exec(new Set(Prop.WILDCARDS, true));
    final String[] lftf = index(CmdIndex.FULLTEXT, tl,
        DATAFTX + 'a', DATAFTX + 'b', DATAFTX + 'c');

    // index size
    tl.add(ltxt[0]);
    tl.add(latv[0]);
    tl.add(lftf[0]);
    tl.add(lftt[0]);
    // number of entries
    tl.add(ltxt[1]);
    tl.add(latv[1]);
    tl.add(lftf[1]);
    tl.add(lftt[1]);
  }

  /**
   * Creates and drops the specified index.
   * @param index index
   * @param tl token list
   * @param dbf database files
   * @return length
   * @throws BaseXException exception
   */
  private String[] index(final CmdIndex index, final TokenList tl,
      final String... dbf) throws BaseXException {

    p.getTimer();
    exec(new CreateIndex(index));
    final double time = Token.toInt(p.toString().replaceAll("\\..* ms", ""));
    tl.add(Double.toString(time / 1000.0).replace('.', ','));
    long l = 0;
    for(final String d : dbf) l += ctx.data.meta.file(d).length();

    String inf = exec(new InfoIndex(index));
    final Matcher m = INDEX.matcher(inf);
    String entries = m.find() ? m.group(1) : "xxx";

    exec(new DropIndex(index));
    return new String[] { format(l), entries };
  }
}
