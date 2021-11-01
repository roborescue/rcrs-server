# `rcrs-server` Robocup Rescue Simulation Server

(Linux) Instructions to download, build and run the RoboCup Rescue Simulator (RCRS)

## 1. Software Pre-Requisites

- Git
- Gradle
- OpenJDK Java 11

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

$ cd scripts

$ ./start.sh -m ../maps/test/map -c ../maps/test/config
```

Open another terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ cd scripts

$ ./sampleagent.sh
```

## 5. Tools

### 5.1 Map Editor

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew gmlEditor --args='<map file path>'
```
where ```--args=<map file path>``` is optional.

#### Example
```
./gradlew gmlEditor --args='maps/test/map/map.gml'
```

### 5.2 Scenario Editor

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew scenarioEditor --args='<scenario path>'
```
where ```--args=<scenario path>``` is optional.

#### Example
```
./gradlew scenarioEditor --args='maps/test/map'
```

### 5.3 Random Scenario

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew randomScenario --args="'<map path>' <options>"
```
where ```<map path>``` is the path to the map directory and ```<options>``` are the minimum and maximum value for each entity in the scenario. The ```<options>``` are optional.

#### Example
```
./gradlew randomScenario --args="'../maps/test/map' -civ 0 100 -fb 0 10 -fs 0 1 -pf 0 10 -po 0 1 -at 0 5 -ac 0 0 -refuge 0 2 -fire 0 0"
```

### 5.4 Log Viewer

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew logViewer --args="'-c' '<config file>' '<log path>'"
```
where ```<config file>``` is the path to the log viewer configuration file, default ```config/logviewer.cfg```. The ```<log path>``` is the rescue log file path, default ```logs/log/rescue.log```.

#### Example
```
./gradlew logViewer --args="'-c' 'config/logviewer.cfg' 'logs/log/rescue.log'"
```

### 5.5 `osm2gml`

Open a terminal window, navigate to the ```rcrs-server``` root directory and execute

```bash

$ ./gradlew osm2gml --args="'<osm map path>' '<gml map path>'"
```
The ```<osm map path>``` is the path to the OSM map file and the ```<gml map path>``` is the destination GML map path.

### Example
```
./gradlew osm2gml --args="'/home/user/newmap.osm' '/home/user/newmap.gml'"
```

## 6. Support

To report a bug, suggest improvements or request support, please open an issue at GitHub <https://github.com/roborescue/rcrs-server/issues>.
