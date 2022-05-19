package org.basex.gui.layout;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Organizes icons used all over the GUI.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class BaseXImages {
  /** Cached images. */
  private static final HashMap<String, Image> IMAGES = new HashMap<>();
  /** Cached image icons. */
  private static final HashMap<String, ImageIcon> ICONS = new HashMap<>();

  /** File icon cache. */
  private static final HashMap<String, Icon> FILES = new HashMap<>();
  /** System icons. */
  private static final FileSystemView FS = FileSystemView.getFileSystemView();

  /** Icon for XML resources. */
  private static final Icon DB_XML = icon("db_xml");
  /** Icon for binary resources. */
  private static final Icon DB_BIN = icon("db_bin");
  /** Icon for value resources. */
  private static final Icon DB_VAL = icon("db_val");

  /** Icon for closed directories. */
  private static final Icon DIR_CLOSED = icon("dir_closed");
  /** Icon for opened directories. */
  private static final Icon DIR_OPENED = icon("dir_opened");
  /** Icon for textual files. */
  private static final Icon FILE_TEXT = icon("file_text");
  /** Icon for XML/XQuery file types. */
  private static final Icon FILE_XML = icon("file_xml");
  /** Icon for XML/XQuery file types. */
  private static final Icon FILE_XQUERY = icon("file_xquery");

  /** Private constructor. */
  private BaseXImages() { }

  /**
   * Returns the specified image.
   * @param name name of image
   * @return image
   */
  public static Image get(final String name) {
    if(!IMAGES.containsKey(name)) {
      final int n = 5;
      final Image[] images = new Image[n];
      for(int i = 0; i < n; i++) {
        final String path = "/img/" + name + '-' + i + ".png";
        final URL url = BaseXImages.class.getResource(path);
        if(url == null) throw Util.notExpected("Image missing: " + path);
        try {
          images[i] = ImageIO.read(url);
        } catch(IOException ex) {
          throw Util.notExpected(ex);
        }
      }
      IMAGES.put(name, new BaseMultiResolutionImage(images));
    }
    return IMAGES.get(name);
  }

  /**
   * Returns the specified image as icon.
   * @param name name of icon
   * @return icon
   */
  public static ImageIcon icon(final String name) {
    return ICONS.computeIfAbsent(name, n -> new ImageIcon(get(n)));
  }

  /**
   * Returns a directory icon.
   * @param opened expanded state (open/closed)
   * @return icon
   */
  public static Icon dir(final boolean opened) {
    return opened ? DIR_OPENED : DIR_CLOSED;
  }

  /**
   * Returns an icon for the specified text.
   * @param type resource type
   * @return icon
   */
  public static Icon resource(final ResourceType type) {
    return type == ResourceType.XML ? DB_XML : type == ResourceType.BINARY ? DB_BIN : DB_VAL;
  }

  /**
   * Returns an icon for the specified file.
   * @param file file reference
   * @return icon
   */
  public static Icon file(final IOFile file) {
    if(file == null) return FILE_TEXT;

    // fallback code for displaying icons
    final String path = file.path();
    final MediaType type = MediaType.get(path);
    if(type.isXML()) return FILE_XML;
    if(type.isXQuery()) return FILE_XQUERY;

    if(Prop.WIN) {
      // retrieve system icons (only supported on Windows)
      final int p = path.lastIndexOf(path, '.');
      final String suffix = p == -1 ? null : path.substring(p + 1);
      Icon icon = null;
      if(suffix != null) icon = FILES.get(suffix);
      if(icon == null) {
        icon = FS.getSystemIcon(file.file());
        if(suffix != null) FILES.put(suffix, icon);
      }
      return icon;
    }
    // default icon
    return FILE_TEXT;
  }
}
