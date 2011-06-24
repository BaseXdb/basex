package org.basex.examples.stats;

import static org.basex.data.DataText.*;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.util.TokenList;

/**
 * This class prints meta statistics.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class MetaStats extends Statistics {
  /**
   * Main method of the example class.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new MetaStats(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  private MetaStats(final String[] args) {
    if(!init(args)) return;

    ctx.prop.set(Prop.INTPARSE, true);
    ctx.prop.set(Prop.TEXTINDEX, false);
    ctx.prop.set(Prop.ATTRINDEX, false);
    ctx.prop.set(Prop.PATHINDEX, false);

    run("Length", "Pre", "Attsize", "Name(e)", "Name(a)", "URI",
        "Content(t)", "Content(a)", "DBSize", "Depth", "Docs");
  }

  @Override
  void analyze(final TokenList tl) {
    final Data data = ctx.data;
    // file size
    tl.add(format(data.meta.filesize));
    // number of nodes
    tl.add(data.meta.size);
    // maximum number of attributes per element
    //add(tl, "max(for $d in //* return count($d/@*))");
    tl.add("xxx");
    // total number of element names
    tl.add(data.tagindex.size());
    //add(tl, "count(distinct-values(for $d in //* return name($d)))");
    // total number of attribute names
    tl.add(data.atnindex.size());
    //add(tl, "count(distinct-values(for $d in //@* return name($d)))");
    // total number of namespace URIs
    tl.add(data.ns.size());
    // total string length of text nodes
    tl.add(ctx.data.meta.file(DATATXT).length());
    //add(tl, "sum(for $d in //text() return string-length($d) + 1)");
    // total string length of attribute values
    tl.add(ctx.data.meta.file(DATAATV).length());
    //add(tl, "sum(for $d in //@* return string-length($d) + 1)");
    // database size
    tl.add(format(data.meta.dbsize()));
    // number of documents
    tl.add(data.doc().length);
  }
}
