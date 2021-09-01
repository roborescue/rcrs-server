# `rcrs-server` Robocup Rescue Simulation Server

(Linux) Instructions to download, build and run the RoboCup Rescue Simulator (RCRS)

## 1. Software Pre-Requisites

- Git
- Gradle
- OpenJDK Java 11+

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

$ ./gradlew gmlEditor --args='<map file path>'
```
where ```--args=<map file path>``` is optional. The ```<map path>``` is the path to the map's Geography Markup Language file, such as, ```--args='maps/gml/test/map/map.gml'```.

### 5.2 Scenario Editor

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew scenarioEditor --args='<map path>'
```
where ```--args=<scenario path>``` is mandatory and refers to the path of the scenario directory to edit, such as, ```--args='maps/gml/test/map'```.

### 5.3 Random Scenario

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew randomScenario --args='<map path>'
```
where ```--args=<map path>``` is mandatory and refers to the path of the map for creating the random scenario. For example, ```--args='maps/gml/test/map'```.

### 5.4 Log Viewer

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew logViewer --args='-c config/logviewer.cfg <log path>'
```
where ```--args='-c config/logviewer.cfg <log path>'``` is optional and the ```<log path>``` refers to the log file path. The default log file path is ```logs/rescue.log```.

### 5.5 `osm2gml`

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew osm2gml --args='<osm map path> <gml map path>'
```
The ```<osm map path>``` is the path to the OSM map file and the ```<gml map path>``` is the destination GML map path. Both parameters are mandatory.

## 6. Support

To report a bug, suggest improvements or request support, please open an issue at GitHub <https://github.com/roborescue/rcrs-server/issues>.
