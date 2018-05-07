@echo off

set OCT_HOME=C:\Octave\Octave-4.0.3
set "PATH=%OCT_HOME%\bin;%PATH%"

set SCRIPTS_DIR=%~dp0
start /MIN C:\Octave\Octave-4.0.3\bin\octave.exe --eval "cd(getenv('SCRIPTS_DIR')); modelcreator;"

