set AGENTDIR=C:\agentats
set DERBY_HOME=%AGENTDIR%\derby
set CLASSPATH=.;%AGENTDIR%\derby\lib;%AGENTDIR%\http;%AGENTDIR%\jre1.6.0_07\lib\
set PATH=%AGENTDIR%\jre1.6.0_07\bin;%SYSTEMROOT%;%SYSTEMROOT%\System32

start javaw -cp lib -jar agentats.jar