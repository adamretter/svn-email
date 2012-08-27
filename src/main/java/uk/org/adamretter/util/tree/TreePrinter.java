package uk.org.adamretter.util.tree;

import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Utility Class for Printing aspects of a Tree
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class TreePrinter {
    
    /**
     * Prints the entire Tree down from the node provided
     * 
     * @out The output destination for printing the tree
     * @root The start node of the Tree
     */
    public static void printTree(final PrintWriter out, final TreeNode root) {
        for(final Iterator<TreeNode> itChild = root.iterateChildren(); itChild.hasNext();) {
            final TreeNode child = itChild.next();
            recursivePrinter(out, child, 0);
        }
        
        //flush the writer
        out.flush();
    }
    
    /**
     * Used by printTree to recursively print the tree
     * 
     * @param out The output destination for printing the tree
     * @param node The specific node to print
     * @param depth The current depth in the tree
     */
    private static void recursivePrinter(final PrintWriter out, final TreeNode node, final int depth) {
        String indent = new String();
        for(int i = 0; i < depth; i++) {
            indent += '\t';
        }
        
        out.println(indent + node.getName());
        
        for(final Iterator<TreeNode> itChild = node.iterateChildren(); itChild.hasNext();) {
             recursivePrinter(out, itChild.next(), depth+1);
        }
    }
    
    /**
     * Print the path of a Node in the tree
     * 
     * @param out The output destination for printing the path to
     * @param node The node to print the path for
     */
    public static void printNodePath(final PrintWriter out, final TreeNode node) {
        final StringBuilder builder = new StringBuilder(TreeNode.PATH_SEPARATOR + node.getName());
        
        TreeNode p = node;
        while((p = p.getParent()) != null) {
            if(p.getName() != null) {
                builder.insert(0, TreeNode.PATH_SEPARATOR + p.getName());
            }
        }
        
        out.print(builder.toString());
        out.flush();
    }
}
