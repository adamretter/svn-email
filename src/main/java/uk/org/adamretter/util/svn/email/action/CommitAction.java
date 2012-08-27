package uk.org.adamretter.util.svn.email.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.DefaultSVNDiffGenerator;
import org.tmatesoft.svn.core.wc.ISVNDiffGenerator;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffOptions;
import org.tmatesoft.svn.core.wc.SVNRevision;
import uk.org.adamretter.util.svn.email.SmtpMessage;
import uk.org.adamretter.util.tree.TreeException;
import uk.org.adamretter.util.tree.TreeNode;
import uk.org.adamretter.util.tree.TreePrinter;
import uk.org.adamretter.util.tree.TreeUtil;

/**
 * Commit Action Interogation
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class CommitAction extends Action {
    
    private final static byte sep[] = System.getProperty("line.separator").getBytes();
    private int revision;

    
    public CommitAction(final String repository, final String args[]) throws InvalidArgumentsException, IOException {
        super(repository, args);
    }

    @Override
    public ARGS[] getExpectedArgs() {
        return new ARGS[] { Action.ARGS.REPOS, Action.ARGS.REVISION };
    }
    
    @Override
    public void setupArgs(final String[] args) throws InvalidArgumentsException {
        if(args.length < 1) {
            throw new InvalidArgumentsException("For commit, both REPOS and REVISION are required");
        }
        
        final String rev = args[0];
        try {
            this.revision = Integer.parseInt(rev);
        } catch(final NumberFormatException nfe) {
            throw new InvalidArgumentsException("Revision is invalid: " + rev, nfe);
        }
    }
    
    private String getSubject(final Set<String> changedPaths) {   
        try {
            final TreeNode tree = TreeUtil.buildTreeFromPaths(changedPaths);

            //debug
            //TreePrinter.printTree(new PrintWriter(System.out), tree);

            final List<TreeNode> longestCommonBranches = new ArrayList<TreeNode>();
            TreeUtil.findLongestCommonBranches(tree.iterateChildren(), longestCommonBranches);

            final StringWriter commonProjectPaths = new StringWriter();
            for(int i = 0; i < longestCommonBranches.size(); i++) {
                TreePrinter.printNodePath(new PrintWriter(commonProjectPaths), longestCommonBranches.get(i));
                if(longestCommonBranches.size() -1 > i) {
                   commonProjectPaths.write(System.getProperty("line.separator"));
                }
            }
            
            return "[SVN commit] " + String.valueOf(revision) + ": " + commonProjectPaths.toString();
        } catch(final TreeException te) {
            LOG.error(te.getMessage(), te);
            return "[SVN commit] " + String.valueOf(revision) + ": " + svnRepositoryURL.getPath();
        }
    }
    
    @Override
    public void execute(final SmtpMessage message) throws IOException {
        
        try {
            final Collection<SVNLogEntry> logEntries = svnRepository.log(new String[]{""}, null, revision, revision, true, true);
            
            final Iterator<SVNLogEntry> itLogEntry = logEntries.iterator();

            //check we have the log entries
            if(!itLogEntry.hasNext()) {
                throw new IOException("Could not find SVN log entries for revision: " + revision);
            }

            //get the log entry
            final SVNLogEntry logEntry = itLogEntry.next();
            
            final Map<String, SVNLogEntryPath> logEntryPaths = logEntry.getChangedPaths();
        
            final OutputStream os = message.prepareForContent(getSubject(logEntryPaths.keySet()));
            
            //output Header
            outputHeader(revision, logEntry.getAuthor(), logEntry.getDate(), logEntry.getMessage(), os);
            
            //group paths
            final HashMap<Character, ArrayList<String>> typedPaths = new HashMap<Character, ArrayList<String>>();
            for(final String logEntryPathKey : logEntryPaths.keySet()) {
                final SVNLogEntryPath logEntryPath = logEntryPaths.get(logEntryPathKey);
                
                ArrayList<String> typePaths = typedPaths.get(logEntryPath.getType());
                if(typePaths == null) {
                    typePaths = new ArrayList<String>();
                }
                typePaths.add(logEntryPath.getPath());
                typedPaths.put(logEntryPath.getType(), typePaths);
            }
            
            //output paths
            for(final Character type : typedPaths.keySet()) {
                final ArrayList<String> paths = typedPaths.get(type);
                outputPaths(type.charValue(), paths, os);
            }
            
            //output diff
            outputDiff(svnRepositoryURL, revision, os);
        } catch(final SVNException se) {
            throw new IOException("SVN communication error: " + se.getMessage(), se);
        }
    }
    
    /**
     * Writes out some header information about the commit
     * 
     * @param revision The revision number of the commit
     * @param author The author of the commit
     * @param date The date of the commit
     * @param message The commit message
     * @param os The OutputStream to write the header to
     */
    private void outputHeader(int revision, String author, Date date, String message, OutputStream os) throws IOException
    {
        os.write("Revision: ".getBytes());
        os.write(String.valueOf(revision).getBytes());
        os.write(sep);
        
        os.write("Author: ".getBytes());
        os.write(author.getBytes());
        os.write(sep);
        
        os.write("Date: ".getBytes());
        os.write(date.toString().getBytes());
        os.write(sep);
        
        os.write(sep);
        os.write("Commit Message:".getBytes());
        os.write(sep);
        os.write("---------------".getBytes());
        os.write(sep);
        os.write(message.getBytes());
        os.write(sep);
        
        os.write(sep);
        os.flush();
    }
    
    /**
     * Writes out the paths affected by the commit
     * 
     * @param type The type of path change
     * @param paths A string list of affected paths
     * @param os The OutputStream to write the affected paths details to
     */
    private void outputPaths(char type, List<String> paths, OutputStream os) throws IOException
    {
        String header = new String();
        
        if(type == SVNLogEntryPath.TYPE_ADDED)
        {
            header += "Added Paths:";
        }
        else if(type == SVNLogEntryPath.TYPE_DELETED)
        {
            header += "Deleted Paths:";
        }
        else if(type == SVNLogEntryPath.TYPE_MODIFIED)
        {
            header += "Modified Paths:";
        }
        else if(type == SVNLogEntryPath.TYPE_REPLACED)
        {
            header += "Replaced Paths:";
        }
        
        int underlineLength = header.length();
        header += System.getProperty("line.separator");
        
        for(int i = 0; i < underlineLength; i++)
        {
            header+= '-';
        }
        
        header += System.getProperty("line.separator");
        
        os.write(header.getBytes());
        
        for(String path : paths)
        {
            os.write(path.getBytes());
            os.write(sep);
        }
        
        os.write(sep);
        os.flush();
    }
    
    /**
     * Writes out the diff of the commit
     * 
     * @param itemURL The URL of the Subversion Item to diff
     * @param revision The revision number of the commit
     * @param os The OutputStream to write the diff to
     */
    private void outputDiff(SVNURL itemURL, int revision, OutputStream os) throws SVNException, IOException
    {
        SVNDiffClient diffClient = svnClientManager.getDiffClient();
                    
        SVNDiffOptions diffOptions = new SVNDiffOptions();
        ISVNDiffGenerator diffGenerator = diffClient.getDiffGenerator();
        if (diffGenerator instanceof DefaultSVNDiffGenerator) {
            ((DefaultSVNDiffGenerator) diffGenerator).setDiffOptions(diffOptions);
        }

        boolean useAncestry = false;
        boolean recursive = true;

        boolean diffDeleted = false;
        boolean forcedBinaryDiff = false;

        diffGenerator.setDiffDeleted(diffDeleted);
        diffGenerator.setForcedBinaryDiff(forcedBinaryDiff);

        diffClient.doDiff(itemURL, SVNRevision.create(revision-1), itemURL, SVNRevision.create(revision), recursive, useAncestry, os);
        
        os.flush();
    }    
}