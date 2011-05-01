package org.basex.query.util;

import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.io.IOFile;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.QNm;
import org.basex.query.iter.NodeMore;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.TokenMap;
import org.basex.util.TokenObjMap;

/**
 * Repository.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class Repo {

  /** Package descriptor. */
  private static final String PKGDESC = "expath-pkg.xml";
  /** Platform dependent file separator. */
  private static final String SEP = System.getProperty("file.separator");
  /** Package name. */
  private static final byte[] NAME = token("name");
  /** Element <xquery/>. */
  private static final byte[] XQUERY = token("xquery");
  /** Element <namespace/>. */
  private static final byte[] NMSPC = token("namespace");
  /** Element <abbrev/>. */
  private static final byte[] ABBREV = token("abbrev");
  /** Package version. */
  private static final byte[] VERSION = token("version");
  /**
   * Map containing namespaces available in the repository and the packages in
   * which they are found.
   */
  public final TokenObjMap<TokenList> nsDict;
  /** Map containing installed packages and their directories. */
  public final TokenMap pkgDict;
  /** Context properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param p context properties
   */
  public Repo(final Prop p) {
    nsDict = new TokenObjMap<TokenList>();
    pkgDict = new TokenMap();
    this.prop = p;
    try {
      readRepo();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Reads the contents of the package repository.
   * @throws IOException IO exception
   */
  private void readRepo() throws IOException {
    final File repoDir = new File(prop.get(Prop.REPOPATH));
    if(repoDir.exists()) {
      final File[] pkgDirs = repoDir.listFiles();
      for(int i = 0; i < pkgDirs.length; i++) {
        if(pkgDirs[i].isDirectory()) {
          final File pkgDesc = new File(pkgDirs[i].getPath() + SEP + PKGDESC);
          if(!pkgDesc.exists()) {
            // TODO: Error: Missing package descriptor.
          } else {
            readPkg(pkgDesc);
          }
        }
      }
    }
  }

  /**
   * Reads a package descriptor.
   * @param pkgDesc package descriptor
   * @throws IOException IO Exception
   */
  private void readPkg(final File pkgDesc) throws IOException {
    final IOFile io = new IOFile(pkgDesc);
    final Parser p = Parser.xmlParser(io, prop, "");
    final ANode pkg = new DBNode(MemBuilder.build(p, prop, ""), 0).children().next();
    final ANode xqueryComp = getChild(pkg, XQUERY);
    if(xqueryComp != null) {
      final ANode ns = getChild(xqueryComp, NMSPC);
      if(ns != null) {
        if(nsDict.get(ns.atom()) != null) {
          nsDict.get(ns.atom()).add(pkg.attribute(new QNm(NAME)));
        } else {
          final TokenList vals = new TokenList();
          vals.add(pkg.attribute(new QNm(NAME)));
          nsDict.add(ns.atom(), vals);
        }
        // Build unique package name: package uri + package version
        final TokenBuilder pkgNameBld = new TokenBuilder();
        pkgNameBld.add(pkg.attribute(new QNm(NAME))).add('-').add(
            pkg.attribute(new QNm(VERSION)));
        // Build path to package directory
        final TokenBuilder pkgPathBld = new TokenBuilder();
        pkgPathBld.add(token(prop.get(Prop.REPOPATH))).add(token(SEP)).add(
            pkg.attribute(new QNm(ABBREV)));
        pkgDict.add(pkgNameBld.finish(), pkgPathBld.finish());
      }
    }
  }

  /**
   * Returns the child node with the specified name.
   * @param node node
   * @param name child name
   * @return child node
   */
  private static ANode getChild(final ANode node, final byte[] name) {
    final NodeMore ch = node.children();
    while(true) {
      final ANode next = ch.next();
      if(next == null) return null;
      if(eq(name, next.nname())) return next;
    }
  }
}
