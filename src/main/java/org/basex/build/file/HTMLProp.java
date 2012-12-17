package org.basex.build.file;

import java.io.*;

import org.basex.core.*;

/**
 * This class contains HTML parsing properties for TagSoup.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class HTMLProp extends AProp {
  /** TagSoup option: html. */
  public static final Object[] HTML = { "html", false };
  /** TagSoup option: omit-xml-declaration. */
  public static final Object[] OMITXML = { "omit-xml-declaration", false };
  /** TagSoup option: method=html. */
  public static final Object[] METHOD = { "method", "xml" };
  /** TagSoup option: nons. */
  public static final Object[] NONS = { "nons", false };
  /** TagSoup option: nobogons. */
  public static final Object[] NOBOGONS = { "nobogons", false };
  /** TagSoup option: nodefaults. */
  public static final Object[] NODEFAULTS = { "nodefaults", false };
  /** TagSoup option: nocolons. */
  public static final Object[] NOCOLONS = { "nocolons", false };
  /** TagSoup option: norestart. */
  public static final Object[] NORESTART = { "norestart", false };
  /** TagSoup option: nobogons. */
  public static final Object[] IGNORABLE = { "ignorable", false };
  /** TagSoup option: emptybogons. */
  public static final Object[] EMPTYBOGONS = { "emptybogons", false };
  /** TagSoup option: any. */
  public static final Object[] ANY = { "any", false };
  /** TagSoup option: norootbogons. */
  public static final Object[] NOROOTBOGONS = { "norootbogons", false };
  /** TagSoup option: nocdata. */
  public static final Object[] NOCDATA = { "nocdata", false };
  /** TagSoup option: lexical. */
  public static final Object[] LEXICAL = { "lexical", false };
  /** TagSoup option: doctype-system=systemid. */
  public static final Object[] DOCTYPESYS = { "doctype-system", "" };
  /** TagSoup option: doctype-public=publicid. */
  public static final Object[] DOCTYPEPUB = { "doctype-public", "" };
  /** TagSoup option: encoding=encoding. */
  public static final Object[] ENCODING = { "encoding", "" };

  /**
   * Constructor.
   * @param s properties string
   * @throws IOException exception
   */
  public HTMLProp(final String s) throws IOException {
    parse(s);
  }
}
