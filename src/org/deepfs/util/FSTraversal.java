package org.deepfs.util;

import java.io.File;
import java.io.IOException;

/**
 * Perform a preorder traversal of a directory hierarchy and fires events.
 * 
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Alexander Holupirek
 */
public abstract class FSTraversal {
  
    /**
     * Fired before the filesystem traversal starts.
     * @param d the directory node the traversal starts from
     */
    abstract void preTraversalVisit(final File d);

    /**
     * Fired once the filesystem traversal finished.
     * @param d the directory node the traversal started from
     */
    abstract void postTraversalVisit(final File d);
    
    /**
     * Visits a directory node in preorder (enter directory). 
     * @param d the directory node
     */
    abstract void preDirectoryVisit(final File d);
    
    /**
     * Visits a directory node in postorder (leaving directory).
     * @param d the directory node  
     */
    abstract void postDirectoryVisit(final File d);

    /** 
     * Visits a regular file.
     * @param f the file  
     */
    abstract void regularFileVisit(final File f);

    /** 
     * Visits a symbolic link.
     * @param f the file  
     */
    abstract void symLinkVisit(final File f);
    
    /** Depth of traversal. */
    protected int level;
    
    /**
     * Determines if the specified file is valid and no symbolic link.
     * @param f file to be tested.
     * @return false for a symbolic link
     */
    public static boolean valid(final File f) {
        try {
            return f.getPath().equals(f.getCanonicalPath());
        } catch (final IOException ex) {
            return false;
        }
    }

    /**
     * Invoked when a directory is visited.
     * @param f file name
     */
    private void visitDirectory(final File f) {
        preDirectoryVisit(f);
        level++;
        traverse(f);
        level--;
        postDirectoryVisit(f);
    }

    /**
     * Invoked when a regular file is visited.
     * @param f file name
     */
    private void visitFile(final File f) {
        regularFileVisit(f);
    }
    
    /**
     * Invoked when a smbolic link is visited.
     * @param f file name
     */
    private void visitSymLink(final File f) {
        symLinkVisit(f);
    }
    
    /**
     * Invoked before the traversal starts.
     * @param d directory the traversal starts from
     */
    private void preTraversal(final File d) {
       preTraversalVisit(d); 
    }
    
    /**
     * Invoked after the traversal has taken place.
     * @param d directory the traversal started from
     */
    private void postTraversal(final File d) {
       postTraversalVisit(d); 
    }
    
    /**
     * Traverses a directory hierarchy.
     * @param d directory traversal starts from.
     */
    private void traverse(final File d) {
        final File[] files = d.listFiles();
        if (files == null) return;

        for (final File f : files) {
            if (!valid(f)) {
                visitSymLink(f);
                continue;
            }
            if (f.isDirectory()) visitDirectory(f); 
            else visitFile(f);
        }
    }
    
    /** 
     * Entry point for a directory traversal.
     * @param d directory traversal should start from.
     */
    public final void startTraversal(final File d) {
        preTraversal(d);
        traverse(d);
        postTraversal(d);
    }
}