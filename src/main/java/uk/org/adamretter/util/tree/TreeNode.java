package uk.org.adamretter.util.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple Tree implementation
 * 
 * Should be memory and processing efficient
 * each node keeps a link to its parent and its children
 * each node only stores its name
 * 
 * Each child must have a unique name
 * 
 * The nodes name is null when it is the root node
 * The nodes parent is null when it is the root node
 * 
 * The trees root is virtual
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class TreeNode {
    protected final static char PATH_SEPARATOR = '/';
    
    private final String name;
    private final TreeNode parent;
    private final Map<String, TreeNode> children = new HashMap<String, TreeNode>();
    
    /**
     * Creates a new Tree and returns the root node
     * 
     * @return The virtual root node of the new Tree
     */
    public final static TreeNode newTree() {
        return new TreeNode();
    }
    
    /**
     * Constructor for a new Tree
     */
    private TreeNode() {
        name = null;
        parent = null;
    }
    
    /**
     * Constructor for a Child node in the Tree
     * 
     * @param name The name of the node
     * @param parent The parent of this node in the tree
     */
    private TreeNode(final String name, final TreeNode parent) throws TreeException {
        if(name == null) {
            throw new TreeException("A node must have a name");
        }
        
        if(parent == null) {
            throw new TreeException("A child node must have a parent");
        }
        
        this.name = name;
        this.parent = parent;
    }
  
    /**
     * Gets a child node by its name
     * 
     * @param name The name of the child node
     * @return the child node or null if no such child exists
     */
    public TreeNode getChild(final String name) {
        return children.get(name);
    }

    /**
     * Gets the parent node of this node
     * 
     * @return the parent node or null if this is the root node
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Gets the name of this node
     * 
     * @return the name of this node
     */
    public String getName() {
        return name;
    }
    
    /**
     * Counts how many child nodes belong to this node
     * 
     * @return The number of child nodes of this node
     */
    public int countChildren() {
        return children.size();
    }

    /**
     * Does this node have a sibling node?
     * 
     * @return true if this node has a sibling node, false otherwise
     */
    public boolean hasSibling() {
        if(parent == null) {
            return false;
        }

        return (parent.children.size() > 1);
    }

    /**
     * Creates a new Child node
     * 
     * If a child of the same name already exists it is replaced by this new node
     * 
     * @param name The name for the new child node
     * @return the new child node of this node
     * 
     */
    public TreeNode createChild(final String name) throws TreeException {
        final TreeNode t = new TreeNode(name, this);
        children.put(name, t);
        return t;
    }

    /**
     * Iterator over the children of this node
     * 
     * @return Iterator for the children of this node
     */
    public Iterator<TreeNode> iterateChildren() {
        return children.values().iterator();
    }
}
