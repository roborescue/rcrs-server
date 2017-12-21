# `rcrs-server` Robocup Rescue Simulation Server

(Linux) Instructions to download, compile and install the RoboCup Rescue Simulator (RCRS)

## 1. Software Pre-Requisites

- Git
- Ant
- Oracle Java JDK 8

## 2. Download project from GitHub

```bash

$ git clone git@github.com:roborescue/rcrs-server.git
```

## 3. Compile

```bash

$ ant clean

$ ant
```

## 4. Execute

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ cd boot

$ ./start.sh ../maps/gml/test
```

Open another terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ cd boot

$ ./sampleagent.sh
```

## 5. Support

To report a bug, suggest improvements or request support, please open an issue at GitHub https://github.com/roborescue/rcrs-server/issues.
