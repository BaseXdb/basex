package org.basex.query.func.zip;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNZip extends StandardFunc {
  /** Module prefix. */
  private static final String PREFIX = "zip";
  /** QName. */
  private static final QNm Q_FILE = QNm.get(PREFIX, "file", ZIPURI);
  /** QName. */
  private static final QNm Q_DIR = QNm.get(PREFIX, "dir", ZIPURI);
  /** QName. */
  private static final QNm Q_ENTRY = QNm.get(PREFIX, "entry", ZIPURI);

  /** Attribute: href. */
  private static final byte[] HREF = token("href");
  /** Attribute: name. */
  private static final byte[] NAME = token("name");
  /** Attribute: src. */
  private static final byte[] SRC = token("src");
  /** Attribute: method. */
  private static final byte[] METHOD = token("method");
  /** Method "base64". */
  private static final String M_BASE64 = "base64";
  /** Method "hex". */
  private static final String M_HEX = "hex";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkCreate(qc);
    switch(func) {
      case _ZIP_BINARY_ENTRY:     return binaryEntry(qc);
      case _ZIP_TEXT_ENTRY:       return textEntry(qc);
      case _ZIP_HTML_ENTRY:       return xmlEntry(qc, true);
      case _ZIP_XML_ENTRY:        return xmlEntry(qc, false);
      case _ZIP_ENTRIES:          return entries(qc);
      case _ZIP_ZIP_FILE:         return zipFile(qc);
      case _ZIP_UPDATE_ENTRIES:   return updateEntries(qc);
      default:                    return super.item(qc, ii);
    }
  }

  /**
   * Returns a xs:base64Binary item, created from a binary file.
   * Returns a binary entry.
   * @param qc query context
   * @return binary result
   * @throws QueryException query exception
   */
  private B64 binaryEntry(final QueryContext qc) throws QueryException {
    return new B64(entry(qc));
  }

  /**
   * Returns a string, created from a text file.
   * @param qc query context
   * @return binary result
   * @throws QueryException query exception
   */
  private Str textEntry(final QueryContext qc) throws QueryException {
    final String enc = exprs.length < 3 ? null : string(toToken(exprs[2], qc));
    final IO io = new IOContent(entry(qc));
    final boolean val = qc.context.options.get(MainOptions.CHECKSTRINGS);
    try {
      return Str.get(new NewlineInput(io).encoding(enc).validate(val).content());
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }

  /**
   * Returns a document node, created from an XML or HTML file.
   * @param qc query context
   * @param html html flag
   * @return binary result
   * @throws QueryException query exception
   */
  private ANode xmlEntry(final QueryContext qc, final boolean html) throws QueryException {
    final MainOptions opts = qc.context.options;
    final IO io = new IOContent(entry(qc));
    try {
      return new DBNode(html ? new HtmlParser(io, opts) : Parser.xmlParser(io));
    } catch(final IOException ex) {
      throw SAXERR_X.get(info, ex);
    }
  }

  /**
   * Returns a zip archive description.
   * @param qc query context
   * @return binary result
   * @throws QueryException query exception
   */
  private ANode entries(final QueryContext qc) throws QueryException {
    final String file = string(toToken(exprs[0], qc));

    // check file path
    final IOFile path = new IOFile(file);
    if(!path.exists()) throw ZIP_NOTFOUND_X.get(info, file);
    // loop through file
    try(final ZipFile zf = new ZipFile(file)) {
      // create result node
      final FElem root = new FElem(Q_FILE).declareNS().add(HREF, path.path());
      createEntries(paths(zf).iterator(), root, "");
      return root;
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }

  /**
   * Creates the zip archive nodes in a recursive manner.
   * @param it iterator
   * @param par parent node
   * @param pref directory prefix
   * @return current prefix
   */
  private static String createEntries(final Iterator<String> it, final FElem par,
      final String pref) {

    String path = null;
    boolean curr = false;
    while(curr || it.hasNext()) {
      if(!curr) {
        path = it.next();
        curr = true;
      }
      if(path == null) break;
      // current entry is located in a higher/other directory
      if(!path.startsWith(pref)) return path;

      // current file starts with new directory
      final int i = path.lastIndexOf('/');
      final String dir = i == -1 ? path : path.substring(0, i);
      final String name = path.substring(i + 1);

      if(name.isEmpty()) {
        // path ends with slash: create directory
        path = createEntries(it, createDir(par, dir), dir);
      } else {
        // create file
        createFile(par, name);
        curr = false;
      }
    }
    return null;
  }

  /**
   * Creates a directory element.
   * @param par parent node
   * @param name name of directory
   * @return element
   */
  private static FElem createDir(final FElem par, final String name) {
    final FElem e = new FElem(Q_DIR).add(NAME, name);
    par.add(e);
    return e;
  }

  /**
   * Creates a file element.
   * @param par parent node
   * @param name name of directory
   */
  private static void createFile(final FElem par, final String name) {
    par.add(new FElem(Q_ENTRY).add(NAME, name));
  }

  /**
   * Creates a new zip file.
   * @param qc query context
   * @return binary result
   * @throws QueryException query exception
   */
  private Item zipFile(final QueryContext qc) throws QueryException {
    // check argument
    final ANode elm = toElem(exprs[0], qc);
    if(!elm.qname().eq(Q_FILE)) throw ZIP_UNKNOWN_X.get(info, elm.qname());
    // get file
    final String file = attribute(elm, HREF, true);

    // write zip file
    boolean ok = true;
    try(final FileOutputStream fos = new FileOutputStream(file);
        final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
      create(zos, elm.children(), "", null, qc);
    } catch(final IOException ex) {
      ok = false;
      throw ZIP_FAIL_X.get(info, ex);
    } finally {
      if(!ok) new IOFile(file).delete();
    }
    return null;
  }

  /**
   * Adds files to the specified zip output, or copies files from the
   * specified file.
   * @param zos output stream
   * @param ai axis iterator
   * @param root root path
   * @param qc query context
   * @param zf original zip file (or {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void create(final ZipOutputStream zos, final AxisIter ai, final String root,
      final ZipFile zf, final QueryContext qc) throws QueryException, IOException {

    final byte[] data = new byte[IO.BLOCKSIZE];
    for(ANode node; (node = ai.next()) != null;) {
      // get entry type
      final QNm mode = node.qname();
      final boolean dir = mode.eq(Q_DIR);
      if(!dir && !mode.eq(Q_ENTRY)) throw ZIP_UNKNOWN_X.get(info, mode);

      // file path: if null, the zip base name is used
      String name = attribute(node, NAME, false);
      // source: if null, the node's children are serialized
      String src = attribute(node, SRC, false);
      if(src != null) src = src.replaceAll("\\\\", "/");

      if(name == null) {
        // throw exception if both attributes are null
        if(src == null) throw ZIP_INVALID_X_X.get(info, node.qname(), SRC);
        name = src;
      }
      name = name.replaceAll(".*/", "");

      // add slash to directories
      if(dir) name += '/';
      zos.putNextEntry(new ZipEntry(root + name));

      if(dir) {
        create(zos, node.children(), root + name, zf, qc);
      } else {
        if(src != null) {
          // write file to zip archive
          if(!new IOFile(src).exists()) throw ZIP_NOTFOUND_X.get(info, src);

          BufferedInputStream bis = null;
          try {
            bis = new BufferedInputStream(new FileInputStream(src));
            for(int c; (c = bis.read(data)) != -1;) zos.write(data, 0, c);
          } finally {
            if(bis != null) try { bis.close(); } catch(final IOException ignored) { }
          }
        } else {
          // no source reference: the child nodes are treated as file contents
          final AxisIter ch = node.children();
          final String m = attribute(node, METHOD, false);
          // retrieve first child (might be null)
          ANode n = ch.next();

          // access original zip file if available, and if no children exist
          ZipEntry ze = null;
          if(zf != null && n == null) ze = zf.getEntry(root + name);

          if(ze != null) {
            // add old zip entry
            final InputStream zis = zf.getInputStream(ze);
            for(int c; (c = zis.read(data)) != -1;) zos.write(data, 0, c);
          } else if(n != null) {
            // write new binary content to archive
            final boolean hex = M_HEX.equals(m);
            if(hex || M_BASE64.equals(m)) {
              // treat children as base64/hex
              final ByteList bl = new ByteList();
              do bl.add(n.string()); while((n = ch.next()) != null);
              final byte[] bytes = bl.finish();
              zos.write((hex ? new Hex(bytes) : new B64(bytes)).toJava());
            } else {
              // serialize new nodes
              try {
                final Serializer ser = Serializer.get(zos, serPar(node));
                do {
                  ser.serialize(DataBuilder.stripNS(n, ZIPURI, qc.context));
                } while((n = ch.next()) != null);
                ser.close();
              } catch(final QueryIOException ex) {
                throw ex.getCause(info);
              }
            }
          }
        }
        zos.closeEntry();
      }
    }
  }

  /**
   * Returns serialization parameters.
   * @param node node with parameters
   * @return parameters
   * @throws BaseXException database exception
   */
  private static SerializerOptions serPar(final ANode node) throws BaseXException {
    // interpret query parameters
    final SerializerOptions sopts = new SerializerOptions();
    final AxisIter ati = node.attributes();
    for(ANode at; (at = ati.next()) != null;) {
      final byte[] name = at.qname().string();
      if(eq(name, NAME, SRC)) continue;
      sopts.assign(string(name), string(at.string()));
    }
    return sopts;
  }

  /**
   * Updates a zip archive.
   * @param qc query context
   * @return empty result
   * @throws QueryException query exception
   */
  private Item updateEntries(final QueryContext qc) throws QueryException {
    // check argument
    final ANode elm = toElem(exprs[0], qc);
    if(!elm.qname().eq(Q_FILE)) throw ZIP_UNKNOWN_X.get(info, elm.qname());

    // sorted paths in original file
    final String in = attribute(elm, HREF, true);

    // target and temporary output file
    final IOFile target = new IOFile(string(toToken(exprs[1], qc)));
    IOFile out;
    do {
      out = new IOFile(target.path() + new Random().nextInt(0x7FFFFFFF));
    } while(out.exists());

    // open zip file
    if(!new IOFile(in).exists()) throw ZIP_NOTFOUND_X.get(info, in);
    boolean ok = true;
    try(final ZipFile zf = new ZipFile(in)) {
      // write zip file
      try(final FileOutputStream fos = new FileOutputStream(out.path());
          final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
        // fill new zip file with entries from old file and description
        create(zos, elm.children(), "", zf, qc);
      } catch(final IOException ex) {
        ok = false;
        throw ZIP_FAIL_X.get(info, ex);
      }
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    } finally {
      if(ok) {
        // rename temporary file to final target
        target.delete();
        out.rename(target);
      } else {
        // remove temporary file
        out.delete();
      }
    }
    return null;
  }

  /**
   * Returns a list of all file paths.
   * @param zf zip file file to be parsed
   * @return binary result
   */
  private static StringList paths(final ZipFile zf) {
    // traverse all zip entries and create intermediate map,
    // as zip entries are not sorted
    //final StringList paths = new StringList();
    final TreeSet<String> paths = new TreeSet<>();

    final Enumeration<? extends ZipEntry> en = zf.entries();
    // loop through all files
    while(en.hasMoreElements()) {
      final ZipEntry ze = en.nextElement();
      final String name = ze.getName();
      final int i = name.lastIndexOf('/');
      // add directory
      if(i > -1 && i + 1 < name.length()) paths.add(name.substring(0, i + 1));
      paths.add(name);
    }
    final StringList sl = new StringList();
    for(final String path : paths) sl.add(path);
    return sl;
  }

  /**
   * Returns the value of the specified attribute.
   * @param elm element node
   * @param name attribute to be found
   * @param force if set to {@code true}, an exception is thrown if the
   * attribute is not found
   * @return attribute value
   * @throws QueryException query exception
   */
  private String attribute(final ANode elm, final byte[] name, final boolean force)
      throws QueryException {

    final byte[] val = elm.attribute(name);
    if(val == null && force) throw ZIP_INVALID_X_X.get(info, elm.qname(), name);
    return val == null ? null : string(val);
  }

  /**
   * Returns an entry from a zip file.
   * @param qc query context
   * @return binary result
   * @throws QueryException query exception
   */
  private byte[] entry(final QueryContext qc) throws QueryException {
    final IOFile file = new IOFile(string(toToken(exprs[0], qc)));
    final String path = string(toToken(exprs[1], qc));
    if(!file.exists()) throw ZIP_NOTFOUND_X.get(info, file);

    try {
      return new Zip(file).read(path);
    } catch(final FileNotFoundException ex) {
      throw ZIP_NOTFOUND_X.get(info, file + "/" + path);
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }
}
