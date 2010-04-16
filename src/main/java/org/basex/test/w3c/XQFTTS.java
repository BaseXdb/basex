package org.basex.test.w3c;

import java.util.HashMap;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.ft.StemDir;
import org.basex.query.ft.StopWords;
import org.basex.query.ft.ThesQuery;
import org.basex.query.ft.Thesaurus;

/**
 * XQuery Full Text Test Suite wrapper.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class XQFTTS extends W3CTS {
  /** Cached stop word files. */
  private final HashMap<String, String> stop = new HashMap<String, String>();
  /** Cached stop word files. */
  private final HashMap<String, String> stop2 = new HashMap<String, String>();
  /** Cached stemming dictionaries. */
  private final HashMap<String, String> stem = new HashMap<String, String>();
  /** Cached thesaurus. */
  private final HashMap<String, String> thes = new HashMap<String, String>();
  /** Cached thesaurus. */
  private final HashMap<String, String> thes2 = new HashMap<String, String>();

  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQFTTS().init(args);
  }

  /**
   * Constructor.
   */
  public XQFTTS() {
    super("XQFTTS");
    context.prop.set(Prop.FTINDEX, true);
  }

  @Override
  void init(final Nodes root) throws Exception {
    Main.outln("Caching Full-text Structures...");
    for(final int s : nodes("//*:stopwords", root).nodes) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      stop.put(text("@uri", srcRoot), val);
      stop2.put(text("@ID", srcRoot), val);
    }
    for(final int s : nodes("//*:stemming-dictionary", root).nodes) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      stem.put(text("@ID", srcRoot), val);
    }
    for(final int s : nodes("//*:thesaurus", root).nodes) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      thes.put(text("@uri", srcRoot), val);
      thes2.put(text("@ID", srcRoot), val);
    }
  }

  @Override
  void parse(final QueryContext qctx, final Nodes root) throws Exception {
    qctx.stop = stop;
    qctx.thes = thes;

    for(final String s : aux("stopwords", root)) {
      final String fn = stop2.get(s);
      if(fn != null) {
        if(qctx.ftopt.sw == null) qctx.ftopt.sw = new StopWords();
        qctx.ftopt.sw.read(IO.get(fn), false);
      }
    }

    for(final String s : aux("stemming-dictionary", root)) {
      final String fn = stem.get(s);
      if(fn != null) {
        if(qctx.ftopt.sd == null) qctx.ftopt.sd = new StemDir();
        qctx.ftopt.sd.read(IO.get(fn));
      }
    }

    for(final String s : aux("thesaurus", root)) {
      final String fn = thes2.get(s);
      if(fn != null) {
        if(qctx.ftopt.th == null) qctx.ftopt.th = new ThesQuery();
        qctx.ftopt.th.add(new Thesaurus(IO.get(fn), context));
      }
    }
  }
}
