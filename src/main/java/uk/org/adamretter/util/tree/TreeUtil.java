package uk.org.adamretter.util.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for working with Trees
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class TreeUtil {   
    
    /**
     * Finds the longest common branches of a tree
     * 
     * e.g. Provided with the nodes a and m for these branches
     * 
     * /a/b/c/d/e
     * /a/b/c/1/2
     * /a/b/c/x/y
     * /m/n/o/p
     * /m/n/o/u
     * 
     * The nodes c and o are returned
     * 
     * 
     * @param nodes The root nodes of the branches to search for commonalities
     * @param longestCommonBranches (return) The nodes that are the end-points of the common branches
     */
    public static void findLongestCommonBranches(final Iterator<TreeNode> nodes, final List<TreeNode> longestCommonBranches) {
        
        while(nodes.hasNext()){
            final TreeNode child = nodes.next();
            
            if(child.countChildren() == 0 || child.countChildren() > 1) {
                //this child
                longestCommonBranches.add(child);
            } else {
                //look at nodes of this child
                findLongestCommonBranches(child.iterateChildren(), longestCommonBranches);
            }
        }
    }
    
    /**
     * Builds a Tree from a Collection of Paths
     * Each segment in a path is a node in the tree
     * 
     * @param paths Collection of paths to form nodes in the tree
     * @return The root node of the tree
     */
    public static TreeNode buildTreeFromPaths(final Collection<String> paths) throws TreeException {
        final TreeNode root = TreeNode.newTree();
        TreeNode n = root;
        
        for(final String path : paths) {
            for(final String changedPathSeg : path.split(String.valueOf(TreeNode.PATH_SEPARATOR))) {
                if(changedPathSeg.length() != 0) {
                    TreeNode c = n.getChild(changedPathSeg);
                    if(c == null) {
                        c = n.createChild(changedPathSeg);
                    }
                    n = c;
                }
            }
            n = root;
        }
        return root;
    }
}
