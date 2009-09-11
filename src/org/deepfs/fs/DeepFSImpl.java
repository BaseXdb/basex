/*-
 * HelloFS - An example file system for jFUSE.
 * Copyright (C) 2008-2009  Erik Larsson <erik82@kth.se>
 * 
 * Derived from:
 *   FUSE: Filesystem in Userspace (hello.c)
 *   Copyright (C) 2001-2007  Miklos Szeredi <miklos@szeredi.hu>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.deepfs.fs;

import java.nio.ByteBuffer;

import org.catacombae.jfuse.FUSE;
import org.catacombae.jfuse.FUSEFileSystemAdapter;
import org.catacombae.jfuse.types.fuse26.FUSEFileInfo;
import org.catacombae.jfuse.types.fuse26.FUSEFillDir;
import org.catacombae.jfuse.types.system.Stat;
import org.catacombae.jfuse.util.FUSEUtil;

/**
 * Example "Hello world" filesystem.
 *
 * @author Erik Larsson
 */
public class DeepFSImpl extends FUSEFileSystemAdapter {
    /** Content of test file. */
    private final byte[] hello_str;
    /** Path to test file. */
    private final String hello_path = "/hello";
    /** Connection to database storage. */
    private DeepFS dbfs = new DeepFS("hellofs");

    /** Constructor. */
    public DeepFSImpl() {
        /* We set up HelloFS by creating a byte array holding the contents of
         * the filesystem's only file, 'hello'. */
        String hello = "Hello!\n";
        this.hello_str = FUSEUtil.encodeUTF8(hello);
        if(hello_str == null)
            throw new RuntimeException("Couldn't UTF-8 encode the following " +
                    "string: \"" + hello + "\"");   
    }

    @Override
    public int getattr(ByteBuffer path, Stat stbuf) {      
      String pathString = FUSEUtil.decodeUTF8(path);
      dbfs.stat(pathString, stbuf);
      if(pathString == null) // Invalid UTF-8 sequence.
      return -ENOENT;
  
      if(pathString.equals("/")) {
        stbuf.st_mode = S_IFDIR | 0755;
        stbuf.st_nlink = 2;
      } else if(pathString.equals(hello_path)) {
        stbuf.st_mode = S_IFREG | 0444;
        stbuf.st_nlink = 1;
        stbuf.st_size = hello_str.length;
      } else return -ENOENT;
  
      return 0;
    }

    @Override
    public int readdir(ByteBuffer path, FUSEFillDir filler, long offset,
            FUSEFileInfo fi) {
        String pathString = FUSEUtil.decodeUTF8(path);
        if(pathString == null) // Invalid UTF-8 sequence.
            return -ENOENT;
        else if(!pathString.equals("/"))
            return -ENOENT;
        else {
            filler.fill(FUSEUtil.encodeUTF8("."), null, 0);
            filler.fill(FUSEUtil.encodeUTF8(".."), null, 0);
            filler.fill(FUSEUtil.encodeUTF8(hello_path.substring(1)), null, 0);
            return 0;
        }
    }

    @Override
    public int open(ByteBuffer path, FUSEFileInfo fi) {
        String pathString = FUSEUtil.decodeUTF8(path);
        if(pathString == null) // Invalid UTF-8 sequence.
            return -ENOENT;

        if(!pathString.equals(hello_path))
            return -ENOENT;

        if((fi.flags & 3) != O_RDONLY)
            return -EACCES;

        return 0;
    }

    @Override
    public int read(ByteBuffer path, ByteBuffer buf, long offset, FUSEFileInfo fi) {
        String pathString = FUSEUtil.decodeUTF8(path);
        if(pathString == null) { // Invalid UTF-8 sequence.
            return -ENOENT;
        }
        else if(offset < 0 || offset > Integer.MAX_VALUE) {
            return -EINVAL;
        }
        else if(!pathString.equals(hello_path))
            return -ENOENT;
        else {
            int bytesLeftInFile = hello_str.length - (int)(offset);
            if(bytesLeftInFile > 0) {
                int len = Math.min(bytesLeftInFile, buf.remaining());
                buf.put(hello_str, (int)offset, len);
                return len;
            }
            return 0;
        }
    }

    /** Main entry point.
     * 
     * @param args mountpoint is expected as first argument.
     */
    public static void main(String[] args) {
        FUSE.main(args, new DeepFSImpl());
    }
}
