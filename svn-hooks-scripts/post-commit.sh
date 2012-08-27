@ECHO OFF

REM POST-COMMIT HOOK

REM The post-commit hook is invoked after a commit.  Subversion runs
REM this hook by invoking a program (script, executable, binary, etc.)
REM named 'post-commit' (for which this file is a template) with the 
REM following ordered arguments:

REM   [1] REPOS-PATH   (the path to this repository)
REM   [2] REV          (the number of the revision just committed)

java -jar svn-email-0.9-SNAPSHOT-jar-with-dependencies.jar commit %1 %2