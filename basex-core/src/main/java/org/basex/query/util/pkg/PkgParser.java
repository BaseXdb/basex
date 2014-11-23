package org.basex.query.util.pkg;

import static org.basex.query.QueryError.*;
import static org.basex.query.util.pkg.PkgText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.pkg.Package.Component;
import org.basex.query.util.pkg.Package.Dependency;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Parses the package descriptors and performs schema checks.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class PkgParser {
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param info input info
   */
  public PkgParser(final InputInfo info) {
    this.info = info;
  }

  /**
   * Parses package descriptor.
   * @param io XML input
   * @return package container
   * @throws QueryException query exception
   */
  public Package parse(final IO io) throws QueryException {
    final Package pkg = new Package();
    try {
      // checks root node
      final ANode node = childElements(new DBNode(new IOContent(io.read()))).next();
      if(!eqNS(PACKAGE, node.qname()))
        throw BXRE_DESC_X.get(info, Util.info(WHICHELEM, node.qname()));

      parseAttributes(node, pkg, PACKAGE);
      parseChildren(node, pkg);
      return pkg;
    } catch(final IOException ex) {
      throw BXRE_PARSE_X_X.get(info, io.name(), ex);
    }
  }

  /**
   * Parses the attributes of <package/> or <module/>.
   * @param node package node
   * @param p package container
   * @param root root node
   * @throws QueryException query exception
   */
  private void parseAttributes(final ANode node, final Package p, final byte[] root)
      throws QueryException {

    final AxisIter atts = node.attributes();
    for(ANode next; (next = atts.next()) != null;) {
      final byte[] name = next.name();
      if(eq(A_NAME, name))         p.name = next.string();
      else if(eq(A_ABBREV, name))  p.abbrev = next.string();
      else if(eq(A_VERSION, name)) p.version = next.string();
      else if(eq(A_SPEC, name))    p.spec = next.string();
      else throw BXRE_DESC_X.get(info, Util.info(WHICHATTR, name));
    }

    // check mandatory attributes
    if(p.name == null)
      throw BXRE_DESC_X.get(info, Util.info(MISSATTR, A_NAME, root));
    if(p.version == null)
      throw BXRE_DESC_X.get(info, Util.info(MISSATTR, A_VERSION, root));
    if(p.abbrev == null)
      throw BXRE_DESC_X.get(info, Util.info(MISSATTR, A_ABBREV, root));
    if(p.spec == null)
      throw BXRE_DESC_X.get(info, Util.info(MISSATTR, A_SPEC, root));
  }

  /**
   * Parses the children of <package/>.
   * @param node package node
   * @param p package container
   * @throws QueryException query exception
   */
  private void parseChildren(final ANode node, final Package p) throws QueryException {
    final AxisIter ch = childElements(node);
    for(ANode next; (next = ch.next()) != null;) {
      final QNm name = next.qname();
      if(eqNS(DEPEND, name)) p.dep.add(parseDependency(next));
      else if(eqNS(XQUERY, name)) p.comps.add(parseComp(next));
    }
  }

  /**
   * Parses <dependency/>.
   * @param node node <dependency/> to be parsed
   * @return dependency container
   * @throws QueryException query exception
   */
  private Dependency parseDependency(final ANode node) throws QueryException {
    final AxisIter atts = node.attributes();
    final Dependency d = new Dependency();
    for(ANode next; (next = atts.next()) != null;) {
      final byte[] name = next.name();
      if(eq(A_PACKAGE, name))            d.pkg = next.string();
      else if(eq(A_PROCESSOR, name))      d.processor = next.string();
      else if(eq(A_VERSIONS, name))      d.versions = next.string();
      else if(eq(A_SEMVER, name))    d.semver = next.string();
      else if(eq(A_SEMVER_MIN, name)) d.semverMin = next.string();
      else if(eq(A_SEMVER_MAX, name)) d.semverMax = next.string();
      else throw BXRE_DESC_X.get(info, Util.info(WHICHATTR, name));
    }
    return d;
  }

  /**
   * Parses <xquery/>.
   * @param node xquery component
   * @return component container
   * @throws QueryException query exception
   */
  private Component parseComp(final ANode node) throws QueryException {
    final AxisIter ch = childElements(node);
    final Component c = new Component();
    for(ANode next; (next = ch.next()) != null;) {
      final QNm name = next.qname();
      if(eqNS(A_NAMESPACE, name)) c.uri = next.string();
      else if(eqNS(A_FILE, name)) c.file = next.string();
      else throw BXRE_DESC_X.get(info, Util.info(WHICHELEM, name));
    }

    // check mandatory children
    if(c.uri == null) throw BXRE_DESC_X.get(info, Util.info(MISSCOMP, A_NAMESPACE));
    if(c.file == null) throw BXRE_DESC_X.get(info, Util.info(MISSCOMP, A_FILE));
    return c;
  }

  /**
   * Returns an iterator on all child elements
   * (text and other nodes will be skipped).
   * @param node root node
   * @return child element iterator
   */
  private static AxisIter childElements(final ANode node) {
    return new AxisIter() {
      /** Child iterator. */
      final AxisIter ch = node.children();

      @Override
      public ANode next() {
        while(true) {
          final ANode n = ch.next();
          if(n == null || n.type == NodeType.ELM) return n;
        }
      }
    };
  }

  /**
   * Checks if the specified name equals the qname and if it uses the packaging
   * namespace.
   * @param cmp input
   * @param name name to be compared
   * @return result of check
   */
  private static boolean eqNS(final byte[] cmp, final QNm name) {
    return name.eq(new QNm(cmp, QueryText.PKG_URI));
  }
}
