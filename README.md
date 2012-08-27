svn-email
=========

[![Build Status](https://secure.travis-ci.org/adamretter/svn-email.png)](http://travis-ci.org/adamretter/svn-email)

This is a simple took that can be used as a commit hook with Subversion server.

It produces and sends an email complete with commit message and diff, to a list
of configured recipients each time a commit is made to Subversion.

Requirements
============
*Maven 3 to build
*JRE 6 to run


Installation
============
1) Edit the file "src/main/resources/svnemail.conf.xml" with the settings for
your environment.
Optionally also edit "src/main/resources/log4j.xml" if you have specific logging
requirements.

2) Run "mvn clean assembly:assembly"

3) Take the resultant file
"target/svn-email-0.9-SNAPSHOT-jar-with-dependencies.jar" and deploy to your
"hooks" folder on your Subversion server.

4) Edit the "post-commit.bat", "post-lock.bat" and "post-unlock.bat" scripts
that are present in your Subversion severs "hooks" folders to call svn-email.
Examples are provided in "svn-hooks-scripts".

** If you are on Unix/Linux then use the files ending with ".sh" and not ".bat",
and make sure they have the execute bit set for the Subversion server account.

** If the 'java' executable is not available on the PATH, then you will need to
edit the scripts above appropriately.
