package uk.org.adamretter.util.svn.email.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import uk.org.adamretter.util.svn.email.SmtpMessage;
import uk.org.adamretter.util.tree.TreeException;
import uk.org.adamretter.util.tree.TreeNode;
import uk.org.adamretter.util.tree.TreePrinter;
import uk.org.adamretter.util.tree.TreeUtil;

/**
 * Lock and Unlock Action Interrogations
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class LockUnlockAction extends Action {
    
    private final static byte sep[] = System.getProperty("line.separator").getBytes();
    
    protected enum Type{
        lock,
        unlock
    };
    
    final Type type;
    final List<String> paths;
    
    String author;
    
    
    public LockUnlockAction(final String repository, final Type type, final String args[], final List<String>paths) throws InvalidArgumentsException, IOException {
        super(repository, args);
        this.type = type;
        this.paths = paths;
    }
    
    @Override
    public ARGS[] getExpectedArgs() {
        return new ARGS[] { Action.ARGS.REPOS, Action.ARGS.AUTHOR };
    }
    
    @Override
    public void setupArgs(final String[] args) throws InvalidArgumentsException {
        if(args.length != 1) {
            throw new InvalidArgumentsException("For commit, both REPOS and AUTHOR are required");
        }
        
        this.author = args[0];
        if(author == null || author.length() == 0) {
            throw new InvalidArgumentsException("Author is invalid");
        }
    }

    private String getSubject() {
        try {
            final TreeNode tree = TreeUtil.buildTreeFromPaths(paths);
            
            final List<TreeNode> longestCommonBranches = new ArrayList<TreeNode>();
            TreeUtil.findLongestCommonBranches(tree.iterateChildren(), longestCommonBranches);

            final StringWriter commonProjectPaths = new StringWriter();
            for(int i = 0; i < longestCommonBranches.size(); i++) {
                TreePrinter.printNodePath(new PrintWriter(commonProjectPaths), longestCommonBranches.get(i));
                if(longestCommonBranches.size() -1 > i) {
                   commonProjectPaths.write(System.getProperty("line.separator"));
                }
            }
            
            return "[SVN " + type + "] " + author + ": " + commonProjectPaths.toString();
        } catch(final TreeException te ) {
            LOG.error(te.getMessage(), te);
            return "[SVN " + type + "] " + author + ": " + svnRepositoryURL.getPath();
        }
    }
    
    @Override
    public void execute(final SmtpMessage message) throws IOException {
        final OutputStream os = message.prepareForContent(getSubject());
        
        outputHeader(os);
        
        try {
            outputPaths(os);
        } catch(final SVNException se) {
            throw new IOException("SVN communication error: " + se.getMessage(), se);
        }
    }
    
    /**
     * Writes out some header information about the lock/unlock
     * 
     * @param os The OutputStream to write the header to
     */
    private void outputHeader(final OutputStream os) throws IOException {   
        os.write("Author: ".getBytes());
        os.write(author.getBytes());
        os.write(sep);
        
        os.write("Date: ".getBytes());
        os.write(Calendar.getInstance().getTime().toString().getBytes());
        os.write(sep);
        
        os.write(sep);
        os.flush();
    }
    
    /**
     * Writes out the paths affected by the lock/unlock
     * 
     * @param os The OutputStream to write the affected paths details to
     */
    private void outputPaths(final OutputStream os) throws SVNException, IOException {
        final StringBuilder builder = new StringBuilder();
        
        if(type == Type.lock)
        {
            builder.append("Locked Paths:");
        }
        else if(type == Type.unlock)
        {
            builder.append("Unlocked Paths:");
        }
        
        final int underlineLength = builder.length();
        builder.append(System.getProperty("line.separator"));
        
        for(int i = 0; i < underlineLength; i++) {
            builder.append('-');
        }
        
        builder.append(System.getProperty("line.separator"));
        
        os.write(builder.toString().getBytes());
        
        for(final String path : paths) {
            os.write(path.getBytes());
            os.write(sep);
            
            if(type == Type.lock) {
                final SVNLock lock = svnRepository.getLock(path);
                final String comment = lock.getComment();
                os.write("Comment: ".getBytes());
                os.write(comment.getBytes());
                os.write(sep);
                os.write(sep);
            }
        }
        
        os.write(sep);
        os.flush();
    }
}
