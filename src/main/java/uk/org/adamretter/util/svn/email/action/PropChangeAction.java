package uk.org.adamretter.util.svn.email.action;

import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import uk.org.adamretter.util.svn.email.SmtpMessage;

/**
 * Property Change Interrogation
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class PropChangeAction extends Action {
    
    private final static Logger LOG = Logger.getLogger(PropChangeAction.class);
    
    private int revision;
    private String author;
    private String propName;
    private char propAction;
    private final List<String> oldPropValue;
    
    public PropChangeAction(final String repository, final String args[], final List<String> oldPropValue) throws InvalidArgumentsException, IOException {
        super(repository, args);
        this.oldPropValue = oldPropValue;
    }

    @Override
    public ARGS[] getExpectedArgs() {
        return new ARGS[] { Action.ARGS.REPOS, Action.ARGS.REVISION, Action.ARGS.AUTHOR, Action.ARGS.PROPNAME, Action.ARGS.PROPACTION  };
    }
    
    
    @Override
    public void setupArgs(final String[] args) throws InvalidArgumentsException {
        if(args.length < 4) {
            throw new InvalidArgumentsException("For commit, both REPOS and REVISION are required");
        }
        
        final String rev = args[0];
        try {
            this.revision = Integer.parseInt(rev);
        } catch(final NumberFormatException nfe) {
            throw new InvalidArgumentsException("Revision is invalid: " + rev, nfe);
        }
        
        this.author = args[1];
        if(author == null || author.length() == 0) {
            throw new InvalidArgumentsException("Author is invalid");
        }
        
        this.propName = args[2];
        if(propName == null || propName.length() == 0) {
            throw new InvalidArgumentsException("Propname is invalid");
        }
        
        if(args[3] == null || args[3].length() != 1) {
            throw new InvalidArgumentsException("Propaction is invalid");
        }
        this.propAction = args[3].charAt(0);
    }

    /*
    @Override
    public String getSubject()
    {
        return "[SVN propchange] " + String.valueOf(revision) + ": " + svnRepositoryURL.getPath();   
    }
    */

    @Override
    public void execute(final SmtpMessage message) throws IOException {
        final SVNWCClient svnWCClient = svnClientManager.getWCClient();
        
        try {
            final SVNPropertyData svnPropData = svnWCClient.doGetProperty(svnRepositoryURL, propName, SVNRevision.create(revision), SVNRevision.create(revision));
            
            LOG.info("PROPCHANGE: getName()=" + svnPropData.getName() + " getValue()=" + svnPropData.getValue());
            System.out.println(svnPropData.getName() + ":" + svnPropData.getValue());
            
            //TODO write the prop change to the OutputStream
            
            //TODO dont forget old prop value
        } catch(final SVNException se) {
            throw new IOException("SVN communication error: " + se.getMessage(), se);
        }
    }   
}
