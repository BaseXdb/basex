package org.basex.tests.w3c;

import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ft.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * XQuery Full Text Test Suite wrapper.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class XQFTTS extends W3CTS {
  /** Cached stop word files. */
  private final HashMap<String, IO> stop = new HashMap<String, IO>();
  /** Cached stop word files. */
  private final HashMap<String, IO> stop2 = new HashMap<String, IO>();
  /** Cached stemming dictionaries. */
  private final HashMap<String, IO> stem = new HashMap<String, IO>();
  /** Cached thesaurus. */
  private final HashMap<String, IO> thes = new HashMap<String, IO>();
  /** Cached thesaurus. */
  private final HashMap<String, IO> thes2 = new HashMap<String, IO>();

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
  protected void init(final Nodes root) throws QueryException {
    Util.outln("Caching Full-text Structures...");
    for(final int s : nodes("//*:stopwords", root).list) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      stop.put(text("@uri", srcRoot), IO.get(val));
      stop2.put(text("@ID", srcRoot), IO.get(val));
    }
    for(final int s : nodes("//*:stemming-dictionary", root).list) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      stem.put(text("@ID", srcRoot), IO.get(val));
    }
    for(final int s : nodes("//*:thesaurus", root).list) {
      final Nodes srcRoot = new Nodes(s, data);
      final String val = (path + text("@FileName", srcRoot)).replace('\\', '/');
      thes.put(text("@uri", srcRoot), IO.get(val));
      thes2.put(text("@ID", srcRoot), IO.get(val));
    }
  }

  @Override
  protected void parse(final QueryProcessor qp, final Nodes root) throws QueryException {
    final QueryContext ctx = qp.ctx;
    ctx.stop = stop;
    ctx.thes = thes;

    final FTOpt opt = ctx.ftOpt();
    for(final String s : aux("stopwords", root)) {
      final IO fn = stop2.get(s);
      if(fn != null) {
        if(opt.sw == null) opt.sw = new StopWords();
        opt.sw.read(fn, false);
      }
    }

    for(final String s : aux("stemming-dictionary", root)) {
      final IO fn = stem.get(s);
      if(fn != null) {
        if(opt.sd == null) opt.sd = new StemDir();
        opt.sd.read(fn);
      }
    }

    for(final String s : aux("thesaurus", root)) {
      final IO fn = thes2.get(s);
      if(fn != null) {
        if(opt.th == null) opt.th = new ThesQuery();
        opt.th.add(new Thesaurus(fn, context));
      }
    }
  }

  /**
   * Returns the resulting auxiliary uri in multiple strings.
   * @param role role
   * @param root root node
   * @return attribute value
   * @throws QueryException query exception
   */
  private String[] aux(final String role, final Nodes root) throws QueryException {
    return text("*:aux-URI[@role = '" + role + "']", root).split("/");
  }
}
