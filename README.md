# `rcrs-server` Robocup Rescue Simulation Server

(Linux) Instructions to download, build and run the RoboCup Rescue Simulator (RCRS)

## 1. Software Pre-Requisites

- Git
- Gradle
- OpenJDK Java 8+

## 2. Download project from GitHub

```bash

$ git clone git@github.com:roborescue/rcrs-server.git
```

## 3. Compile the project

```bash

$ ./gradlew clean

$ ./gradlew completeBuild
```

## 4. Execute the Rescue Simulation Server

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ cd boot

$ ./start.sh -m ../maps/gml/test/map -c ../maps/gml/test/config
```

Open another terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ cd boot

$ ./sampleagent.sh
```

## 5. Tools

### 5.1 Map Editor

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew gmlEditor --args=<map file path>
```
where ```--args=<map file path>``` is optional. The default map file path is ```maps/gml/test/map/map.gml```.

### 5.2 Scenario Editor

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew scenarioEditor --args=<scenario path>
```
where ```--args=<scenario path>``` is optional. The default scenario path is ```maps/gml/test/map```.

### 5.3 Random Scenario

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew randomScenario --args=<map path>
```
where ```--args=<map path>``` is optional. The default map path is ```maps/gml/test/map```.

### 5.4 Log Viewer

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew logViewer --args='-c config/logviewer.cfg <log path>'
```
where ```--args='-c config/logviewer.cfg <log path>'``` is optional and ```<log path>``` defines the log file path. The default log file path is ```logs/rescue.log```.

## 6. Support

To report a bug, suggest improvements or request support, please open an issue at GitHub <https://github.com/roborescue/rcrs-server/issues>.
