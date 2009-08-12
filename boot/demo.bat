start kernel.bat
# Nasty hack. Why doesn't windoze have a sleep command by default?
ping -n 4 127.0.0.1 > nul
start traffic.bat
start fire.bat
start viewer.bat
ping -n 4 127.0.0.1 > nul
start sampleagents.bat