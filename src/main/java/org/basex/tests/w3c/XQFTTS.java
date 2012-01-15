package org.basex.tests.w3c;

import java.util.HashMap;

import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.QueryProcessor;
import org.basex.query.ft.ThesQuery;
import org.basex.query.ft.Thesaurus;
import org.basex.util.Util;
import org.basex.util.ft.StemDir;
import org.basex.util.ft.StopWords;

/**
 * XQuery Full Text Test Suite wrapper.
 *
 * @author BaseX Team 2005-12, BSD License
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
    new XQFTTS().run(args);
  }

  /**
   * Constructor.
   */
  public XQFTTS() {
    super(Util.name(XQFTTS.class));
    //context.prop.set(Prop.FTINDEX, true);
    //context.prop.set(Prop.FORCECREATE, true);
  }

  @Override
  protected void init(final Nodes root) throws Exception {
    Util.outln("Caching Full-text Structures...");
    for(final int s : nodes("//*:stopwords", root).list) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      stop.put(text("@uri", srcRoot), val);
      stop2.put(text("@ID", srcRoot), val);
    }
    for(final int s : nodes("//*:stemming-dictionary", root).list) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      stem.put(text("@ID", srcRoot), val);
    }
    for(final int s : nodes("//*:thesaurus", root).list) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      thes.put(text("@uri", srcRoot), val);
      thes2.put(text("@ID", srcRoot), val);
    }
  }

  @Override
  protected void parse(final QueryProcessor qp, final Nodes root)
      throws Exception {

    final QueryContext ctx = qp.ctx;
    ctx.stop = stop;
    ctx.thes = thes;

    for(final String s : aux("stopwords", root)) {
      final String fn = stop2.get(s);
      if(fn != null) {
        if(ctx.ftopt.sw == null) ctx.ftopt.sw = new StopWords();
        ctx.ftopt.sw.read(IO.get(fn), false);
      }
    }

    for(final String s : aux("stemming-dictionary", root)) {
      final String fn = stem.get(s);
      if(fn != null) {
        if(ctx.ftopt.sd == null) ctx.ftopt.sd = new StemDir();
        ctx.ftopt.sd.read(IO.get(fn));
      }
    }

    for(final String s : aux("thesaurus", root)) {
      final String fn = thes2.get(s);
      if(fn != null) {
        if(ctx.ftopt.th == null) ctx.ftopt.th = new ThesQuery();
        ctx.ftopt.th.add(new Thesaurus(IO.get(fn), context));
      }
    }
  }
}
