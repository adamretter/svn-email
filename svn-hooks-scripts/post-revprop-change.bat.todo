@ECHO OFF

REM POST-REVPROP-CHANGE HOOK

REM The post-revprop-change hook is invoked after a revision property
REM has been added, modified or deleted.  Subversion runs this hook by
REM invoking a program (script, executable, binary, etc.) named
REM 'post-revprop-change' (for which this file is a template), with the
REM following ordered arguments:

REM   [1] REPOS-PATH   (the path to this repository)
REM   [2] REV          (the revision that was tweaked)
REM   [3] USER         (the username of the person tweaking the property)
REM   [4] PROPNAME     (the property that was changed)
REM   [5] ACTION       (the property was 'A'dded, 'M'odified, or 'D'eleted)

REM   [STDIN] PROPVAL  ** the old property value is passed via STDIN.

java -jar svn-email-0.9-SNAPSHOT-jar-with-dependencies.jar propchange %1 %2 %3 %4 %5
