package org.basex.gui.view.map;

import static org.basex.core.Text.*;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.basex.data.Data;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This is an cache for images which have been retrieved from the file system.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class MapFSImages extends Thread {
  /** Maximum number of cached images. */
  private static final int MAXNR = 5000;
  /** Memory error. */
  private static int err;

  /** Reference to the treemap panel. */
  private final MapView view;
  /** Image cache. */
  private final BufferedImage[] imgs = new BufferedImage[MAXNR];
  /** Maximum image size. */
  private final boolean[] imgmax = new boolean[MAXNR];
  /** Image id cache. */
  private final int[] imgid = new int[MAXNR];
  /** Pointer to next cached image. */
  private int imgc;

  /** Image id cache. */
  private final int[] idCache = new int[MAXNR];
  /** Image width cache. */
  private final int[] wCache = new int[MAXNR];
  /** Image height cache. */
  private final int[] hCache = new int[MAXNR];
  /** Cache counter. */
  private int loaderC;

  /** Thread reference. */
  private Thread thread;

  /**
   * Default constructor.
   * @param panel reference to the treemap
   */
  MapFSImages(final MapView panel) {
    view = panel;
  }

  /**
   * Returns image with specified id or {@code null} if none was found.
   * @param id cache to be found
   * @return cached image
   */
  Image get(final int id) {
    for(int i = 0; i < MAXNR; ++i) if(imgid[i] == id) return imgs[i];
    return null;
  }

  /**
   * Adds an image to be loaded into the cache with the specified size.
   * @param id picture id
   * @param w width
   * @param h height
   */
  void add(final int id, final int w, final int h) {
    // add image to the download stack
    if(loaderC < 0) loaderC = 0;
    else if(loaderC == MAXNR) --loaderC;

    idCache[loaderC] = id;
    wCache[loaderC] = w;
    hCache[loaderC++] = h;
  }

  /**
   * Loads the currently cached images.
   */
  void load() {
    if(thread != null || loaderC == 0) return;
    thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    while(loaderC > 0 && view.visible()) {
      final int id = idCache[--loaderC];
      int ww = wCache[loaderC];
      int hh = hCache[loaderC];

      // find new image id in cached images
      int ic = -1;
      while(++ic < MAXNR && imgid[ic] != id);

      // check if the image exists already, or if the existing version
      // is bigger than the new one
      if(ic != MAXNR && (imgmax[ic] || imgs[ic].getWidth() + 1 >= ww ||
          imgs[ic].getHeight() + 1 >= hh)) continue;

      try {
        // database closed - quit
        final Data data = view.gui.context.data;
        if(data == null) {
          loaderC = 0;
          break;
        }

        // load image and wait until it's done
        final File f = new File(Token.string(data.fs.path(id, false)));
        BufferedImage image = null;
        try {
          image = ImageIO.read(f);
        } catch(final Throwable ex) {
          if(ex instanceof OutOfMemoryError) {
            Performance.gc(3);
            if(err++ == 0) Dialog.error(view.gui, PROCMEM);
          }
          image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_BGR);
        }

        // calculate optimal image size
        final double iw = image.getWidth();
        final double ih = image.getHeight();
        final double min = Math.min(ww / iw, hh / ih);

        // create scaled image instance
        if(min < 1) {
          ww = (int) (iw * min);
          hh = (int) (ih * min);

          final BufferedImage bi = new BufferedImage(ww, hh,
              BufferedImage.TYPE_INT_BGR);
          bi.createGraphics().drawImage(image, 0, 0, ww, hh, null);
          image = bi;
        }

        // cache image
        if(ic == MAXNR) {
          ic = imgc;
          imgid[ic] = id;
          imgc = (ic + 1) % MAXNR;
        }
        imgs[ic] = image;
        imgmax[ic] = min >= 1;

        if((loaderC & 5) == 0 && !view.gui.painting) paint();
      } catch(final Exception ex) {
        // catch and ignore any kind of exception
        Util.debug(ex);
      }
    }
    paint();
    loaderC = 0;
    thread = null;
  }

  /**
   * Refreshes the map.
   */
  private void paint() {
    // determine maximum cache size, depending on window size
    final Dimension size = view.gui.getSize();
    final Runtime rt = Runtime.getRuntime();
    final long max = Math.max(size.width * size.height * 10L,
        rt.maxMemory() / 5);

    // remove images if pixel limit is reached
    int imgSize = 0;
    for(int j = (imgc == 0 ? MAXNR : imgc) - 1; j != imgc;) {
      if(imgs[j] != null) {
        if(imgSize < max) {
          imgSize += imgs[j].getWidth() * imgs[j].getHeight();
        } else {
          imgid[j] = 0;
          imgs[j] = null;
        }
      }
      j = (j == 0 ? MAXNR : j) - 1;
    }
    // (might get slow for large maps)
    view.refreshLayout();
  }

  /**
   * Stops the loading thread.
   */
  void reset() {
    loaderC = 0;
  }

  /**
   * Closes the image cache.
   */
  void close() {
    reset();
    /*for(int i = 0; i < MAXNR; ++i) {
      imgs[i] = null;
      imgid[i] = 0;
    }*/
  }
}
