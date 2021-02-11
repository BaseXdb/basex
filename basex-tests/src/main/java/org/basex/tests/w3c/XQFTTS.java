package org.basex.tests.w3c;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * XQuery Full Text Test Suite wrapper.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQFTTS extends W3CTS {
  /** Cached stop word files. */
  private final HashMap<String, IO> stop = new HashMap<>();
  /** Cached stop word files. */
  private final HashMap<String, IO> stop2 = new HashMap<>();
  /** Cached stemming dictionaries. */
  private final HashMap<String, IO> stem = new HashMap<>();
  /** Cached thesaurus. */
  private final HashMap<String, IO> thes = new HashMap<>();
  /** Cached thesaurus. */
  private final HashMap<String, IO> thes2 = new HashMap<>();

  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    new XQFTTS(args).run();
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public XQFTTS(final String[] args) {
    super(args, Util.className(XQFTTS.class));
  }

  @Override
  protected void init(final DBNode root) throws QueryException {
    Util.outln("Caching Full-text Structures...");
    for(final Item node : nodes("//*:stopwords", root)) {
      final String val = (path + text("@FileName", node)).replace('\\', '/');
      stop.put(text("@uri", node), IO.get(val));
      stop2.put(text("@ID", node), IO.get(val));
    }
    for(final Item node : nodes("//*:stemming-dictionary", root)) {
      final String val = (path + text("@FileName", node)).replace('\\', '/');
      stem.put(text("@ID", node), IO.get(val));
    }
    for(final Item node : nodes("//*:thesaurus", root)) {
      final String val = (path + text("@FileName", node)).replace('\\', '/');
      thes.put(text("@uri", node), IO.get(val));
      thes2.put(text("@ID", node), IO.get(val));
    }
  }

  @Override
  protected void parse(final QueryProcessor qp, final Item root) throws QueryException {
    final QueryContext qc = qp.qc;
    qc.resources.ftmaps(stop, thes);

    final FTOpt opt = qc.ftOpt();
    for(final String s : aux("stopwords", root)) {
      final IO fn = stop2.get(s);
      if(fn != null) {
        if(opt.sw == null) opt.sw = new StopWords();
        try {
          opt.sw.read(fn, false);
        } catch(final IOException ex) {
          throw new QueryException(ex);
        }
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
  private String[] aux(final String role, final Item root) throws QueryException {
    return Strings.split(text("*:aux-URI[@role = '" + role + "']", root), '/');
  }
}
