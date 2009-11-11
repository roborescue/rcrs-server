start kernel.bat
# Nasty hack. Why doesn't windoze have a sleep command by default?
ping -n 40 127.0.0.1 > nul
start traffic.bat
start fire.bat
start viewer.bat
start misc.bat
start collapse.bat
start blockade.bat
ping -n 4 127.0.0.1 > nul
start sampleagents.bat