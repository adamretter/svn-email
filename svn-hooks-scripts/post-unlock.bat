@ECHO OFF

REM POST-UNLOCK HOOK

REM The post-unlock hook runs after a path is unlocked.  Subversion runs
REM this hook by invoking a program (script, executable, binary, etc.)
REM named 'post-unlock' (for which this file is a template) with the 
REM following ordered arguments:

REM   [1] REPOS-PATH   (the path to this repository)
REM   [2] USER         (the user who destroyed the lock)

REM The paths that were just unlocked are passed to the hook via STDIN
REM (as of Subversion 1.2, only one path is passed per invocation, but
REM the plan is to pass all unlocked paths at once, so the hook program
REM should be written accordingly).

java -jar svn-email-0.9-SNAPSHOT-jar-with-dependencies.jar unlock %1 %2