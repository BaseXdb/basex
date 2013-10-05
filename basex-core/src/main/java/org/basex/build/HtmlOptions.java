package org.basex.build;

import java.io.*;

import org.basex.util.*;

/**
 * This class contains HTML parsing options for TagSoup.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HtmlOptions extends AOptions {
  /** TagSoup option: html. */
  public static final Option HTML = new Option("html", false);
  /** TagSoup option: omit-xml-declaration. */
  public static final Option OMITXML = new Option("omit-xml-declaration", false);
  /** TagSoup option: method (html). */
  public static final Option METHOD = new Option("method", "xml");
  /** TagSoup option: nons. */
  public static final Option NONS = new Option("nons", true);
  /** TagSoup option: nobogons. */
  public static final Option NOBOGONS = new Option("nobogons", false);
  /** TagSoup option: nodefaults. */
  public static final Option NODEFAULTS = new Option("nodefaults", false);
  /** TagSoup option: nocolons. */
  public static final Option NOCOLONS = new Option("nocolons", false);
  /** TagSoup option: norestart. */
  public static final Option NORESTART = new Option("norestart", false);
  /** TagSoup option: nobogons. */
  public static final Option IGNORABLE = new Option("ignorable", false);
  /** TagSoup option: emptybogons. */
  public static final Option EMPTYBOGONS = new Option("emptybogons", false);
  /** TagSoup option: any. */
  public static final Option ANY = new Option("any", false);
  /** TagSoup option: norootbogons. */
  public static final Option NOROOTBOGONS = new Option("norootbogons", false);
  /** TagSoup option: nocdata. */
  public static final Option NOCDATA = new Option("nocdata", false);
  /** TagSoup option: lexical. */
  public static final Option LEXICAL = new Option("lexical", false);
  /** TagSoup option: doctype-system=systemid. */
  public static final Option DOCTYPESYS = new Option("doctype-system", "");
  /** TagSoup option: doctype-public=publicid. */
  public static final Option DOCTYPEPUB = new Option("doctype-public", "");
  /** TagSoup option: encoding=encoding. */
  public static final Option ENCODING = new Option("encoding", "");

  /**
   * Constructor.
   * @param opts options string
   * @throws IOException exception
   */
  public HtmlOptions(final String opts) throws IOException {
    parse(opts);
  }
}
