@ECHO OFF

REM POST-LOCK HOOK

REM The post-lock hook is run after a path is locked.  Subversion runs
REM this hook by invoking a program (script, executable, binary, etc.)
REM named 'post-lock' (for which this file is a template) with the 
REM following ordered arguments:

REM   [1] REPOS-PATH   (the path to this repository)
REM   [2] USER         (the user who created the lock)

REM The paths that were just locked are passed to the hook via STDIN (as
REM of Subversion 1.2, only one path is passed per invocation, but the
REM plan is to pass all locked paths at once, so the hook program
REM should be written accordingly).

java -jar svn-email-0.9-SNAPSHOT-jar-with-dependencies.jar lock %1 %2