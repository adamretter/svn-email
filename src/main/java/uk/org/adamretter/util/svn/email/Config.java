package uk.org.adamretter.util.svn.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXB;
import uk.org.adamretter.util.svn.email.config.Authentication;
import uk.org.adamretter.util.svn.email.config.Mapping;
import uk.org.adamretter.util.svn.email.config.Mappings;
import uk.org.adamretter.util.svn.email.config.Repository;
import uk.org.adamretter.util.svn.email.config.Svn;
import uk.org.adamretter.util.svn.email.config.SvnEmailConfig;

/**
 *
 * @author Adam Retter <adam.retter@googlemail.com>
 * @version 0.9
 */
public class Config {
    
    private final static String CONF_XML_FILE = "svnemail.conf.xml";
    
    private static Config instance = null;
    
    private final SvnEmailConfig svnEmailConfig;
    
    private Config() throws IOException {
        InputStream isConf = null;
        try {
            isConf = getClass().getClassLoader().getResourceAsStream(CONF_XML_FILE);
            final SvnEmailConfig conf = JAXB.unmarshal(isConf, SvnEmailConfig.class);
            
            this.svnEmailConfig = conf;
            
        } finally {
            if(isConf != null) {
                isConf.close();
            }
        }
    }
    
    /**
     * Gets an instance of the Config
     * 
     * @return A Config instance
     */
    public final static synchronized Config getInstance() throws IOException {
        if(instance == null) {
            instance = new Config();
        }
            
        return instance;
    }
    
    private SvnEmailConfig getSvnEmailConfig() {
        return svnEmailConfig;
    }
    
    /**
     * Gets the SMTP Server hostname
     * 
     * @return Hostname of the SMTP Server
     */
    public String getSMTPServer() {
        return getSvnEmailConfig().getEmail().getServer().getHostname();
    }
    
    /**
     * Gets the SMTP Server Port
     * 
     * @return Port of the SMTP Server
     */
    public int getSMTPServerPort() {
        return getSvnEmailConfig().getEmail().getServer().getPort().intValue();
    }
    
    /**
     * Gets the email address to send mail from
     * 
     * @return From email address
     */
    public String getMailFromAddress() {
        return getSvnEmailConfig().getEmail().getAddresses().getSender().getAddress();
    }
    
    /**
     * Gets the email addresses to send mail to
     * 
     * @return To email addresses
     */
    public List<String> getMailToAddresses() {
        return getSvnEmailConfig().getEmail().getAddresses().getRecipients().getAddress();
    }
    
    /**
     * Returns a mapping from this repository URI to another repository URI
     * null if no mapping exists
     * 
     * @param reposUri The source repository URI
     * @return The destination repository URI
     */
    public String getSVNRepositoryMapping(final String reposUri) {
        
        final Mapping mapping = getRepoMapping(reposUri);
        if(mapping != null) {
            return mapping.getTo().getUri();
        }
        
        return null;
    }
    
    /**
     * Gets the Subversion username
     * 
     * @param Subversion username
     */
    public String getSVNUsername(final String reposUri) {
        final Mapping mapping = getRepoMapping(reposUri);
        if(mapping != null) {
            final Authentication auth = mapping.getTo().getAuthentication();
            if(auth != null) {
                return auth.getUsername();
            }
        }
        
        return null;
    }
    
    /**
     * Gets the Subversion password
     * 
     * @param Subversion password
     */
    public String getSVNPassword(final String reposUri) {
        final Mapping mapping = getRepoMapping(reposUri);
        if(mapping != null) {
            final Authentication auth = mapping.getTo().getAuthentication();
            if(auth != null) {
                return auth.getPassword();
            }
        }
        
        return null;
    }
    
    private Mapping getRepoMapping(final String fromUri) {
        final Svn svn = getSvnEmailConfig().getSvn();
        if(svn != null) {
            final Repository repo = svn.getRepository();
            if(repo != null) {
                final Mappings mappings = repo.getMappings();
                if(mappings != null) {
                    for(final Mapping mapping : mappings.getMapping()) {
                        if(mapping.getFrom().getUri().equals(fromUri)) {
                            return mapping;
                        }
                    }
                }
            }
        }
        return null;
    }
}
