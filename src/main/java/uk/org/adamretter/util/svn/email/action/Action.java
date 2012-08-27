package uk.org.adamretter.util.svn.email.action;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import uk.org.adamretter.util.svn.email.Config;
import uk.org.adamretter.util.svn.email.SmtpMessage;

/**
 * Subversion Action abstraction
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public abstract class Action {
    
    protected final static Logger LOG = Logger.getLogger(Action.class);
    
    protected final String args[];
    
    protected enum ARGS{
      REPOS,
      REVISION,
      AUTHOR,
      PROPNAME,
      PROPACTION;
    };
    
    protected SVNURL svnRepositoryURL = null;
    protected SVNClientManager svnClientManager = null;
    protected SVNRepository svnRepository = null;
    
    /**
     * @param repository The URI of the Subversion Repository
     * @param args Arguments for the Subversion Action
     */
    public Action(String repository, final String[] args) throws InvalidArgumentsException, IOException {
        this.args = args;
        
        final Config config = Config.getInstance();
        
        final String dstRepository = config.getSVNRepositoryMapping(repository);
        if(dstRepository != null) {
            LOG.info("Mapping Repository URI: " + repository + " to: " + dstRepository);
            repository = dstRepository;
        }
        
        try {   
            if(repository.startsWith("http://") || repository.startsWith("https://")) {
                //WebDAV protocol
                svnRepositoryURL = SVNURL.parseURIDecoded(repository);
                DAVRepositoryFactory.setup();
                svnRepository = DAVRepositoryFactory.create(svnRepositoryURL);
            } else if(repository.startsWith("svn://") || repository.startsWith("svn+ssh://")) {
                //SVN protocol
                svnRepositoryURL = SVNURL.parseURIDecoded(repository);
                SVNRepositoryFactoryImpl.setup();
                svnRepository = SVNRepositoryFactoryImpl.create(svnRepositoryURL);
            } else {
                //Filesystem
                svnRepositoryURL = SVNURL.fromFile(new File(repository));
                FSRepositoryFactory.setup();
                svnRepository = FSRepositoryFactory.create(svnRepositoryURL);
            }
            
            final ISVNOptions svnOptions = SVNWCUtil.createDefaultOptions(true);
            
            //do we need authentication
            if(config.getSVNUsername(repository) != null) {
                final ISVNAuthenticationManager svnAuthManager = SVNWCUtil.createDefaultAuthenticationManager(config.getSVNUsername(repository), config.getSVNPassword(repository));
                svnClientManager = SVNClientManager.newInstance(svnOptions, svnAuthManager);
                svnRepository.setAuthenticationManager(svnAuthManager);
            } else {
                svnClientManager = SVNClientManager.newInstance(svnOptions);
            }
        } catch(final SVNException se) {
            final InvalidArgumentsException iae = new InvalidArgumentsException("Invalid repository URL: " + repository + ". Error: " + se.getMessage(), se);
            iae.setExpectedArgs(getExpectedArgs());
            throw iae;
        }
        
        try {   
            setupArgs(args);
        } catch(final InvalidArgumentsException iae) {
            //anotate with expected args
            iae.setExpectedArgs(getExpectedArgs());
            throw iae;
        }
    }
    
    /**
     * Sets up the arguments for this action
     *
     * @param args An array of arguments for this action
     * @throws InvalidArgumentsException
     */
    public abstract void setupArgs(String args[]) throws InvalidArgumentsException;
    
    /**
     * Callback to get the arguments expected by this Action
     * 
     * @return An array of the expected arguments
     */
    public abstract ARGS[] getExpectedArgs();
    
    /**
     * Executes this Action interogation
     * 
     * @param os An OutputStream where results of this Action interogation may be written
     */
    public abstract void execute(SmtpMessage message) throws IOException;
}
