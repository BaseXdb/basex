package org.basex.query.xpath;

import java.util.ArrayList;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Skeleton;
import org.basex.data.Skeleton.Node;
import org.basex.query.QueryParser;
import org.basex.query.xpath.locpath.Axis;
import org.basex.query.xpath.locpath.Preds;
import org.basex.query.xpath.locpath.Test;
import org.basex.query.xpath.locpath.TestName;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class analyzes the current path and gives suggestions for code
 * completions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XPSuggest extends QueryParser {
  /** Node reference. */
  Nodes curr;
  /** Current skeleton nodes. */
  ArrayList<Node> ctx;
  /** Skeleton reference. */
  Skeleton skel;

  /**
   * Constructor, specifying a node set.
   * @param n context nodes
   */
  public XPSuggest(final Nodes n) {
    curr = n;
    skel = curr.data.skel;
  }

  /**
   * Parses an AbsoluteLocationPath.
   */
  void absLocPath() {
    ctx = new ArrayList<Node>();
    ctx.add(skel.root());
    ctx = skel.child(ctx, 0, false);
  }

  /**
   * Parses a LocationStep.
   * @param axis axis
   * @param test node test
   * @param preds predicates
   */
  void step(final Axis axis, final Test test, final Preds preds) {
    final byte[] n = test instanceof TestName ? ((TestName) test).name : null;
    final int t = n != null ? curr.data.tags.id(n) : 0;
    if(preds.size() != 0) {
      ctx = new ArrayList<Node>();
    } else if(axis == Axis.CHILD) {
      ctx = skel.child(ctx, t, false);
    } else if(axis == Axis.DESC || axis == Axis.DESCORSELF) {
      ctx = skel.child(ctx, t, true);
    } else {
      ctx = new ArrayList<Node>();
    }
  }
  
  /**
   * Returns the code completions.
   * @return completions
   */
  StringList complete() {
    final StringList sl = new StringList();
    if(ctx == null) return sl;
    
    for(final Node r : ctx) {
      String name = null;
      if(r.kind == Data.ATTR) {
        name = "@" + Token.string(curr.data.atts.key(r.name));
      } else if(r.kind == Data.ELEM) {
        name = Token.string(curr.data.tags.key(r.name));
      } else if(r.kind == Data.TEXT) {
        name = "text()";
      } else if(r.kind == Data.COMM) {
        name = "comment()";
      } else if(r.kind == Data.PI) {
        name = "processing-instruction()";
      }
      if(name != null && !sl.contains(name)) sl.add(name);
    }
    sl.sort();
    return sl;
  }
}
