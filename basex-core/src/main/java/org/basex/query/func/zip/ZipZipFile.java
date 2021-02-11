package org.basex.query.func.zip;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.zip.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ZipZipFile extends ZipFn {
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
    // check argument
    final ANode elm = toElem(exprs[0], qc);
    if(!elm.qname().eq(Q_FILE)) throw ZIP_UNKNOWN_X.get(info, elm.qname());
    // get file
    final IOFile file = new IOFile(attribute(elm, HREF, true));

    // write zip file
    boolean ok = true;
    try(ZipOutputStream zos = new ZipOutputStream(new BufferOutput(file))) {
      create(zos, elm.childIter(), "", null, qc);
    } catch(final IOException ex) {
      ok = false;
      throw ZIP_FAIL_X.get(info, ex);
    } finally {
      if(!ok) file.delete();
    }
    return Empty.VALUE;
  }

  /**
   * Adds files to the specified zip output, or copies files from the
   * specified file.
   * @param zos output stream
   * @param iter axis iterator
   * @param root root path
   * @param qc query context
   * @param zf original zip file (or {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  final void create(final ZipOutputStream zos, final BasicNodeIter iter, final String root,
      final ZipFile zf, final QueryContext qc) throws QueryException, IOException {

    final byte[] data = new byte[IO.BLOCKSIZE];
    for(ANode node; (node = iter.next()) != null;) {
      qc.checkStop();
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
        create(zos, node.childIter(), root + name, zf, qc);
      } else {
        if(src != null) {
          // write file to zip archive
          if(!new IOFile(src).exists()) throw ZIP_NOTFOUND_X.get(info, src);
          try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src))) {
            for(int c; (c = bis.read(data)) != -1;) zos.write(data, 0, c);
          }
        } else {
          // no source reference: the child nodes are treated as file contents
          final BasicNodeIter ch = node.childIter();
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
              zos.write((hex ? new Hex(bytes) : B64.get(bytes)).toJava());
            } else {
              // serialize new nodes
              final ArrayOutput ao = new ArrayOutput();
              try(Serializer ser = Serializer.get(ao, sopts(node))) {
                do {
                  ser.serialize(DataBuilder.stripNS(n, ZIP_URI, qc.context));
                } while((n = ch.next()) != null);
              } catch(final QueryIOException ex) {
                throw ex.getCause(info);
              }
              zos.write(ao.finish());

            }
          }
        }
        zos.closeEntry();
      }
    }
  }

  /**
   * Returns the value of the specified attribute.
   * @param elm element node
   * @param name attribute to be found
   * @param enforce if set to {@code true}, an exception is thrown if the attribute is not found
   * @return attribute value
   * @throws QueryException query exception
   */
  final String attribute(final ANode elm, final byte[] name, final boolean enforce)
      throws QueryException {

    final byte[] val = elm.attribute(name);
    if(val == null && enforce) throw ZIP_INVALID_X_X.get(info, elm.qname(), name);
    return val == null ? null : string(val);
  }

  /**
   * Returns serialization parameters.
   * @param node node with parameters
   * @return parameters
   * @throws BaseXException database exception
   */
  private static SerializerOptions sopts(final ANode node) throws BaseXException {
    // interpret query parameters
    final SerializerOptions sopts = new SerializerOptions();
    final BasicNodeIter ati = node.attributeIter();
    for(ANode at; (at = ati.next()) != null;) {
      final byte[] name = at.qname().string();
      if(eq(name, NAME, SRC)) continue;
      sopts.assign(string(name), string(at.string()));
    }
    return sopts;
  }
}
