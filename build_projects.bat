:: -----------------------------------------------------------------------------------------
:: This batch file will build JSeatProjects for all project names found in project_list.txt
:: 			The file should have one project name per line
::
:: It is intended to be run from the root JSeat directory as it looks in the bin directory
:: for the appropriate files. Similarly, project_list.txt should also be in the root directory.
::
::
:: The JVM has been setup to use up to 400mb of ram. This should be sufficient in most cases.
:: If however you experience problems, consider tweaking this value.
:: The paths in this file have been hardcoded and should be updated if your environment
:: is setup differently.
:: -----------------------------------------------------------------------------------------
cd bin
@echo off
FOR /F %%p IN (../project_list.txt) DO java -Xmx400m -cp .;../lib/asm-all-3.0.jar;../lib/colt.jar;../lib/concurrent.jar; "metric/JSeatExtractor" -i b:/workspace/builds/%%p/%%p.ver -o "D:/builds/workspace/jseat_data/%%p/%%p.jpf" -t 4
cd ..