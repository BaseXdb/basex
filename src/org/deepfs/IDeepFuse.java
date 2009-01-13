package org.deepfs;

/**
 * Interface to filesystem in userspace framework.
 * 
 * @author Workgroup DBIS, University of Konstanz 2008, ISC License
 * @author Alexander Holupirek
 */
public interface IDeepFuse {

  /**
   * Get file attributes.
   * 
   * @param path to the file the stat information is requested for
   * @return int id of node/inode of the requested file
   */
   int getattr(final String path);

  /**
   * Read the target of a symbolic link.
   * @param path to the link
   * @return String link target
   */
   String readlink(final String path);

  /**
   * Create a file node.
   * 
   * This is called for creation of all non-directory, non-symlink nodes. If the
   * filesystem defines a create() method, then for regular files that will be
   * called instead.
   * @param path name of the file to be created.
   * @param mode specifies both the permissions to use and the type of node to
   *          be created.
   * @param dev If the file type is S_IFCHR or S_IFBLK then dev specifies the
   *          major and minor numbers of the newly created device special file;
   *          otherwise it is ignored.
   * @return zero on success, or -1 if an error occurred (in which case, errno
   *         is set appropriately).
   */
    int mknod(final String path, final int mode, final int dev);

  /**
   * Create a directory.
   * 
   * @param path to directory to be created
   * @param mode permissions for directory
   * @return zero on success, or -1 if an error occurred (in which case, errno
   *         is set appropriately).
   */
    int mkdir(final String path, final int mode);

  /**
   * Remove a file.
   * 
   * @param path to file to be removed
   * @return zero on success, or -1 if an error occurred (in which case, errno
   *         is set appropriately).
   */
    int unlink(final String path);

  /**
   * Remove a directory file.
   * 
   * @param path to file to be removed
   * @return zero on success, or -1 if an error occurred (in which case, errno
   *         is set appropriately).
   */
    int rmdir(final String path);

  /**
   * Make symbolic link to a file.
   * 
   * @param from link source
   * @param to link target
   * @return zero on success, or -1 if an error occurred (in which case, errno
   *         is set appropriately).
   */
    int symlink(final String from, final String to);

  /**
   * Rename a file.
   * 
   * @param from path to file to be renamed
   * @param to new name
   * @return zero on success, or -1 if an error occurred (in which case, errno
   *         is set appropriately).
   */
    int rename(final String from, final String to);

  /**
   * Create a hard link to a file.
   * 
   * @param name1 link source
   * @param name2 link target
   * @return zero on success, or -1 if an error occurred (in which case, errno
   *         is set appropriately).
   */
    int link(final String name1, final String name2);

  /**
   * Change the permission bits of a file.
   * 
   * @param path name of the file
   * @param mode permissions to be set
   * @return zero on success, or -1 if an error occurred
   */
    int chmod(final String path, final int mode);

  /**
   * Change the owner and group of a file.
   * 
   * @param path name of the file
   * @param owner uid
   * @param group gid
   * @return zero on success, or -1 if an error occurred
   */
    int chown(final String path, final int owner, final int group);

  /**
   * Change the size of a file.
   * 
   * @param path name of the file
   * @param off size to be set
   * @return zero on success, or -1 if an error occurred
   */
    int truncate(final String path, final long off);

  /**
   * File open operation.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int open(final String path);

  /**
   * Read data from an open file.
   * 
   * @param path name of the file
   * @param length number of bytes to read
   * @param offset from which to read
   * @return zero on success, or -1 if an error occurred
   */
   byte[] read(final String path, int length, int offset);

  /**
   * Write data to an open file.
   * 
   * @param path name of the file
   * @param length number of bytes to write
   * @param offset from which to write
   * @param data buffer from which to write
   * @return zero on success, or -1 if an error occurred
   */
    int write(final String path, int length, int offset, byte[] data);

  /**
   * Get file system statistics.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int statfs(final String path);

  /**
   * Possibly flush cached data.
   * 
   * BIG NOTE: This is not equivalent to fsync(). It's not a request to sync
   * dirty data.
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int flush(final String path);

  /**
   * Release an open file.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int release(final String path);

  /**
   * Synchronize file contents.
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int fsync(final String path);

  /**
   * Set extended attributes.
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int setxattr(final String path);

  /**
   * Get extended attributes.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int getxattr(final String path);

  /**
   * List extended attributes.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int listxattr(final String path);

  /**
   * Remove extended attributes.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int removexattr(final String path);

  /**
   * Open directory.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int opendir(final String path);

  /**
   * Read directory.
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int readdir(final String path);

  /**
   * Release directory.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int releasedir(final String path);

  /**
   * Synchronize directory contents.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int fsyncdir(final String path);

  /**
   * Initialize filesystem.
   * 
   * @return zero on success, or -1 if an error occurred
   */
    int init();

  /**
   * Clean up filesystem.
   * 
   * Called on filesystem exit.
   * @return zero on success, or -1 if an error occurred
   */
    int destroy();

  /**
   * Check file access permissions.
   * 
   * @param path name of the file
   * @param mode permission to check
   * @return zero on success, or -1 if an error occurred
   */
    int access(final String path, final int mode);

  /**
   * Create and open a file.
   * 
   * @param path for the file to be created
   * @return int id of newly created file or -1 on failure
   */
    int create(final String path);

  /**
   * Change the size of an open file.
   * 
   * @param path name of the file
   * @param off new file size
   * @return zero on success, or -1 if an error occurred
   */
    int ftruncate(final String path, long off);

  /**
   * Get attributes from an open file.
   * 
   * This method is called instead of the getattr() method if the file
   * information is available.
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int fgetattr(final String path);

  /**
   * Perform POSIX file locking operation.
   * 
   * @param path name of the file
   * @param cmd locking command id
   * @return zero on success, or -1 if an error occurred
   */
    int lock(final String path, final int cmd);

  /**
   * Change the access and modification times of a file with nanosecond
   * resolution.
   * 
   * @param path name of the file
   * @return zero on success, or -1 if an error occurred
   */
    int utimens(final String path);

  /**
   * Map block index within file to block index within device.
   * 
   * @param path name of the file
   * @param blocksize block size
   * @param idx block index
   * @return zero on success, or -1 if an error occurred
   */
    int bmap(final String path, final long blocksize, final long idx);
}
