package org.basex.core.cmd;

import java.io.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Evaluates the 'inspect' command: checks if the currently opened database has
 * inconsistent data structures.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Inspect extends Command {
  /**
   * Default constructor.
   */
  public Inspect() {
    super(Perm.READ, true);
  }

  @Override
  protected boolean run() throws IOException {
    final Data data = context.data();
    out.print(inspect(data));
    return info("'%' inspected in %.", data.meta.name, jc().performance);
  }

  @Override
  public void addLocks() {
    jc().locks.reads.add(Locking.CONTEXT);
  }

  /**
   * Inspects the database structures.
   * @param data data
   * @return info string
   */
  private static String inspect(final Data data) {
    final MetaData md = data.meta;
    final Inspection invKind = new Inspection();
    final Inspection parRef = new Inspection();
    final Inspection parChild = new Inspection();
    final Inspection idPre = md.updindex ? new Inspection() : null;
    // loop through all database nodes
    for(int pre = 0; pre < md.size; pre++) {
      // check node kind
      final int kind = data.kind(pre);
      if(kind > 6) invKind.add(pre);
      // check parent reference
      final int par = data.parent(pre, kind);
      if(par >= 0) {
        final int parKind = data.kind(par);
        if(par >= pre) parRef.add(pre);
        // check if parent is no doc and no element, or if node is a descendant
        // of its parent node
        if(parKind != Data.DOC && parKind != Data.ELEM ||
            par + data.size(par, parKind) < pre) parChild.add(pre);
      }
      // check if id/pre mapping is correct
      if(idPre != null && data.pre(data.id(pre)) != pre) idPre.add(pre);
    }

    final TokenBuilder info = new TokenBuilder();
    info.addExt("Checking main table (% nodes):", md.size).add(Prop.NL);
    info.add(invKind.info("invalid node kinds"));
    info.add(parRef.info("invalid parent references"));
    info.add(parChild.info("wrong parent/descendant relationships"));
    if(idPre != null) info.add(idPre.info("wrong id/pre mappings"));
    if(invKind.invalid + parRef.invalid + parChild.invalid == 0) {
      info.add("No inconsistencies found.").add(Prop.NL);
    } else {
      info.add("Warning: Database is inconsistent.").add(Prop.NL);
    }
    return info.toString();
  }

  /** Contains information on single inspection. */
  private static final class Inspection {
    /** Number of invalid. */
    private int invalid;
    /** First invalid hit. */
    private int first = -1;

    /**
     * Adds an entry.
     * @param pre pre value
     */
    private void add(final int pre) {
      invalid++;
      if(first == -1) first = pre;
    }

    /**
     * Prints information.
     * @param info info label
     * @return info string
     */
    private String info(final String info) {
      final StringBuilder sb = new StringBuilder("- % " + info);
      if(invalid > 0) sb.append(" (pre: %,..)");
      return Util.info(sb, invalid, first) + Prop.NL;
    }
  }
}
