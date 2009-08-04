/*
 * Copyright (c) 2005, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 January 2006:
   This is a complete re-write of the gis by Cameron Skinner. Elements of the old GIS have been reused.
*/

#include "common.h"
#include "config.h"
#include "connection_manager.h"
#include "input.h"
#include "output.h"
#include "objectpool.h"
#include "objects.h"
#include "error.h"
#include "command.h"
#include "args.h"
#include <set>
#include <vector>
#include <map>
#include <string>
#include <errno.h>
#include <stdlib.h>

using namespace Librescue;

// by koto, for 2004
static bool randomSeedInitialized = false;
static std::set<int> usedIDs;

INT_32 nextID = 0;
ConnectionManager manager;
bool running;
std::map<INT_32,INT_32> idMap;

void addNewObject(Config& config, ObjectPool& pool, RescueObject* newObject);
INT_32 readInt(FILE* fp);
void ChangeDelimiter( char *pBuf, char delimiter);
int main2(int argc, char** argv);
void makeWorld(Config& config, ObjectPool& pool);
void readRoads(Config& config, ObjectPool& pool);
void readNodes(Config& config, ObjectPool& pool);
void readBuildings(Config& config, ObjectPool& pool);
void readGisini(Config& config, ObjectPool& pool);
void fixIDs(ObjectPool& pool);
void replaceBuilding(INT_32 id, Building* replace, ObjectPool& pool);

// By Koto
// Modified by Cameron Skinner
void addNewObject(Config& config, ObjectPool& pool, RescueObject* newObject) {
  INT_32 newId;
  if (!config.getBool("random_IDs")) {
	newId = nextID++;
  }
  else {
	if (!randomSeedInitialized) {
	  srand(time(NULL));
	  randomSeedInitialized = true;
	}
        
	for (;;) {
	  newId = (rand() | (rand() << 16)) & 0x7FFFFFFF;
	  if (newId!=0 && usedIDs.find(newId) == usedIDs.end())
		break;
	}
	usedIDs.insert(newId);
  }
  idMap[newObject->id()] = newId;
  newObject->setId(newId);
  pool.addObject(newObject);
}

// by koto
// Modified by Cameron Skinner
INT_32 readInt(FILE* fp) {
    Byte b1, b2, b3, b4;
    if(fread(&b1, 1, 1, fp) != 1)
	  exit(-1);
    if(fread(&b2, 1, 1, fp) != 1)
	  exit(-1);
    if(fread(&b3, 1, 1, fp) != 1)
	  exit(-1);
    if(fread(&b4, 1, 1, fp) != 1)
	  exit(-1);
    
    return (INT_32)b1 | ((INT_32)b2 << 8) | ((INT_32)b3 << 16) | ((INT_32)b4 << 24);
}

// created by M.H 2001/04/14
void ChangeDelimiter( char *pBuf, char delimiter) {
    while(*pBuf != '\0') {
        if(*pBuf == delimiter) *pBuf = ' ';
        pBuf++;
    }
}

int main(int argc, char** argv) {
    try {
        return main2(argc, argv);
    } catch(std::exception& e) {
        fprintf(stderr, "%s\n", (const char*)e.what());
    } catch(...) {
        fprintf(stderr, "error...\n");
    }
    return 1;
}

int main2(int argc, char** argv) {
  Config config;

  ArgList args = generateArgList(argc,argv);
  config.init(args);

  manager.listenTCP(config.getInt("gis_port",7001));
  manager.start();

  Bytes input;
  OutputBuffer output;
  Address to;

  LOG_INFO("start");

  ObjectPool pool;
  makeWorld(config, pool);
  running = true;
  while (running) {
	try {
	  if (manager.receive(input,&to,1000)) {
		InputBuffer buffer(input);
		CommandList commands;
		try {
		  decodeCommands(commands,buffer);
		}
		catch (Overrun& o) {
		  LOG_WARNING("Overrun detected while decoding commands from %s: %s",to.toString(),o.why().c_str());
		  LOG_WARNING("The error was encountered at index %d",buffer.cursor());
		  buffer.setCursor(0);
		  dumpBytes(Librescue::LOG_LEVEL_WARNING,buffer);
		}
		for (CommandList::iterator it = commands.begin();it!=commands.end();++it) {
		  Command* next = *it;
		  switch(next->getType()) {
		  case KG_CONNECT:
			LOG_INFO("KG_CONNECT");
			{
			  GISConnectOK reply(pool.objects());
			  output.clear();
			  output.writeCommand(&reply);
			  output.writeInt32(HEADER_NULL);
			  if (!manager.send(output.buffer(),to)) {
				LOG_WARNING("Error sending reply to %s",to.toString());
			  }
			}
			break;
		  case KG_ACKNOWLEDGE:
			LOG_INFO("KG_ACKNOWLEDGE");
			break;
		  default:
			// Ignore
			break;
		  }
		  delete next;
		}
	  }
	}
	catch(std::exception& e) {
	  LOG_ERROR("Error: %s", (const char*)e.what());
	}
	catch(...) {
	  LOG_ERROR("error...");
	}
  }
  return 0;
}

void makeWorld(Config& config, ObjectPool& pool) {
  // Read all the roads, nodes and buildings first
  readRoads(config,pool);
  readNodes(config,pool);
  readBuildings(config,pool);
  fixIDs(pool);
  readGisini(config,pool);
}

void readRoads(Config& config, ObjectPool& pool) {
 std::string roadFile = config.mapfile("road.bin");
 FILE* file = fopen(roadFile.c_str(),"r");
 if (!file) {
   LOG_ERROR("Couldn't open %s: %s",roadFile.c_str(),strerror(errno));
   exit(1);
 }
 // Discard three ints
 readInt(file);
 readInt(file);
 readInt(file);
 INT_32 numRoads = readInt(file);
 for (int i=0;i<numRoads;++i) {
   readInt(file); // Skip the size
   Road* road = new Road();
   road->setId(readInt(file));
   road->setHead(readInt(file),0);
   road->setTail(readInt(file),0);
   road->setLength(readInt(file),0);
   road->setRoadKind(readInt(file),0);
   road->setCarsPassToHead(readInt(file),0);
   road->setCarsPassToTail(readInt(file),0);
   road->setHumansPassToHead(readInt(file),0);
   road->setHumansPassToTail(readInt(file),0);
   road->setWidth(readInt(file),0);
   road->setBlock(readInt(file),0);
   road->setRepairCost(readInt(file),0);
   road->setMedianStrip(readInt(file),0);
   road->setLinesToHead(readInt(file),0);
   road->setLinesToTail(readInt(file),0);
   road->setWidthForWalkers(readInt(file),0);
   addNewObject(config,pool,road);
 }
 fclose(file);
}

void readNodes(Config& config, ObjectPool& pool) {
  std::string nodeFile = config.mapfile("node.bin");
  FILE* file = fopen(nodeFile.c_str(),"r");
  if (!file) {
	LOG_ERROR("Couldn't open %s: %s",nodeFile.c_str(),strerror(errno));
	exit(1);
  }
  // Discard three ints
  readInt(file);
  readInt(file);
  readInt(file);
  INT_32 numNodes = readInt(file);
  for (int i=0;i<numNodes;++i) {
	readInt(file); // Skip the size
	Node* node = new Node();
	node->setId(readInt(file));
	node->setX(readInt(file),0);
	node->setY(readInt(file),0);
	int numEdges = readInt(file);
	ValueList edges;
	ValueList shortcut;
	ValueList pocket;
	ValueList timing;
	edges.reserve(numEdges);
	shortcut.reserve(numEdges);
	pocket.reserve(numEdges*2);
	timing.reserve(numEdges*3);
	for (int j=0;j<numEdges;++j) {
	  edges.push_back(readInt(file));
	}
	node->setEdges(edges,0);
	node->setSignals(readInt(file),0);
	for (int j=0;j<numEdges;++j) {
	  shortcut.push_back(readInt(file));
	}
	for (int j=0;j<numEdges;++j) {
	  pocket.push_back(readInt(file));
	  pocket.push_back(readInt(file));
	}
	for (int j=0;j<numEdges;++j) {
	  timing.push_back(readInt(file));
	  timing.push_back(readInt(file));
	  timing.push_back(readInt(file));
	}
	node->setShortcutToTurn(shortcut,0);
	node->setPocketToTurnAcross(pocket,0);
	node->setSignalTiming(timing,0);
	addNewObject(config,pool,node);
  }
  fclose(file);
}

void readBuildings(Config& config, ObjectPool& pool) {
 std::string buildingFile = config.mapfile("building.bin");
 FILE* file = fopen(buildingFile.c_str(),"r");
 if (!file) {
   LOG_ERROR("Couldn't open %s: %s",buildingFile.c_str(),strerror(errno));
   exit(1);
 }
 // Discard three ints
 readInt(file);
 readInt(file);
 readInt(file);
 INT_32 numBuildings = readInt(file);
 for (int i=0;i<numBuildings;++i) {
   readInt(file); // Skip the size
   Building* building = new Building();
   building->setId(readInt(file));
   building->setX(readInt(file),0);
   building->setY(readInt(file),0);
   building->setFloors(readInt(file),0);
   building->setAttributes(readInt(file),0);
   building->setIgnition(readInt(file),0);
   building->setFieryness(readInt(file),0);
   building->setBrokenness(readInt(file),0);
   int numEntrances = readInt(file);
   ValueList entrances;
   entrances.reserve(numEntrances);
   for (int j=0;j<numEntrances;++j) entrances.push_back(readInt(file));
   building->setEntrances(entrances,0);
   readInt(file); // Discard the shape ID
   //   building->setShapeId(readInt(file),0);
   building->setGroundArea(readInt(file),0);
   building->setTotalArea(readInt(file),0);
   building->setBuildingCode(readInt(file),0);
   int numApexes = readInt(file);
   ValueList apexes;
   apexes.reserve(numApexes*2);
   for (int j=0;j<numApexes;++j) {
	 apexes.push_back(readInt(file));
	 apexes.push_back(readInt(file));
   }
   building->setApexes(apexes,0);
   building->setImportance(1,0);
   addNewObject(config,pool,building);
 }
 fclose(file);
}

void fixIDs(ObjectPool& pool) {
  for (ObjectSet::iterator it = pool.objects().begin();it!=pool.objects().end();++it) {
	RescueObject* next = *it;
	Building* b = dynamic_cast<Building*>(next);
	Road* r = dynamic_cast<Road*>(next);
	Node* n = dynamic_cast<Node*>(next);
	if (b) {
	  // Fix the entrances
	  ValueList entrances = b->getEntrances();
	  b->clearEntrances(0);
	  for (ValueList::iterator ix = entrances.begin();ix!=entrances.end();++ix) {
		int oldId = *ix;
		int newId = idMap[oldId];
		b->appendEntrance(newId,0);
	  }
	}
	if (r) {
	  // Fix head and tail
	  r->setHead(idMap[r->getHead()],0);
	  r->setTail(idMap[r->getTail()],0);
	}
	if (n) {
	  // Fix edges
	  ValueList edges = n->getEdges();
	  n->clearEdges(0);
	  for (ValueList::iterator ix = edges.begin();ix!=edges.end();++ix) {
		int oldId = *ix;
		int newId = idMap[oldId];
		n->appendEdge(newId,0);
	  }	  
	}
  }
}

void readGisini(Config& config, ObjectPool& pool) {
  std::string gisiniFile = config.mapfile("gisini.txt");
  FILE* file = fopen(gisiniFile.c_str(),"r");
  if (!file) {
	LOG_ERROR("Couldn't open %s: %s",gisiniFile.c_str(),strerror(errno));
	exit(1);
  }
  char buffer[1024];
  int id, extra;
  while (fgets(buffer,1024,file)!=0) {
	if (buffer[0]=='#') continue;
	Humanoid* h = 0;
	extra = 0;
	if (sscanf(buffer," FireStation = %d",&id)==1) {
	  //	  LOG_DEBUG("Found fire station %d in building %d (%d)",index,id,idMap[id]);
	  replaceBuilding(idMap[id],new FireStation(),pool);
	}
	if (sscanf(buffer," PoliceOffice = %d",&id)==1) {
	  //	  LOG_DEBUG("Found police office %d in building %d (%d)",index,id,idMap[id]);
	  replaceBuilding(idMap[id],new PoliceOffice(),pool);
	}
	if (sscanf(buffer," AmbulanceCenter = %d",&id)==1) {
	  //	  LOG_DEBUG("Found ambulance center %d in building %d (%d)",index,id,idMap[id]);
	  replaceBuilding(idMap[id],new AmbulanceCenter(),pool);
	}
	if (sscanf(buffer," Refuge = %d",&id)==1) {
	  //	  LOG_DEBUG("Found refuge %d in building %d (%d)",index,id,idMap[id]);
	  replaceBuilding(idMap[id],new Refuge(),pool);
	}
	if (sscanf(buffer," FireBrigade = %d , %d",&id,&extra)>0) {
	  //	  LOG_DEBUG("Found fire brigade %d at location %d (%d)",index,id,idMap[id]);
	  FireBrigade* f = new FireBrigade();
	  h = f;
	  f->setWater(config.getInt("tank_quantity_maximum",15000),0);
	}
	if (sscanf(buffer," AmbulanceTeam = %d , %d",&id,&extra)>0) {
	  //	  LOG_DEBUG("Found ambulance team %d at location %d (%d)",index,id,idMap[id]);
	  h = new AmbulanceTeam();
	}
	if (sscanf(buffer," PoliceForce = %d , %d",&id,&extra)>0) {
	  //	  LOG_DEBUG("Found police force %d at location %d (%d)",index,id,idMap[id]);
	  h = new PoliceForce();
	}
	if (sscanf(buffer," Civilian = %d , %d",&id,&extra)>0) {
	  //	  LOG_DEBUG("Found civilian %d at location %d (%d)",index,id,idMap[id]);
	  h = new Civilian();
	}
	if (h) {
	  h->setId(0);
	  h->setPosition(idMap[id],0);
	  h->setPositionExtra(extra<0?0:extra,0);
	  h->setStamina(10000,0);
	  h->setHP(10000,0);
	  h->setDamage(0,0);
	  h->setBuriedness(0,0);
	  h->setDirection(0,0);
	  h->clearPositionHistory(0);
	  addNewObject(config,pool,h);
	}
	if (sscanf(buffer," FirePoint = %d",&id)==1) {
	  //	  LOG_DEBUG("Found fire %d in building %d (%d)",index,id,idMap[id]);
	  Building* b = dynamic_cast<Building*>(pool.getObject(idMap[id]));
	  if (!b) {
		LOG_ERROR("Could not find building id %d",idMap[id]);
		exit(2);
	  }
	  b->setIgnition(1,0);
	}
	if (sscanf(buffer," ImportantBuilding %d = %d ",&id,&extra)==2) {
	  Building* b = dynamic_cast<Building*>(pool.getObject(idMap[id]));
	  if (!b) {
		LOG_ERROR("Could not find building id %d",idMap[id]);
		exit(2);
	  }
	  b->setImportance(extra,0);
	}
  }
  fclose(file);
}

void replaceBuilding(INT_32 id, Building* replace, ObjectPool& pool) {
  Building* old = dynamic_cast<Building*>(pool.getObject(id));
  if (!old) {
	LOG_ERROR("Could not find building id %d",id);
	exit(2);
  }
  pool.removeObject(old);
  replace->setX(old->getX(),0);
  replace->setY(old->getY(),0);
  replace->setFloors(old->getFloors(),0);
  replace->setAttributes(old->getAttributes(),0);
  replace->setIgnition(old->getIgnition(),0);
  replace->setFieryness(old->getFieryness(),0);
  replace->setBrokenness(old->getBrokenness(),0);
  replace->setEntrances(old->getEntrances(),0);
  replace->setBuildingCode(old->getBuildingCode(),0);
  replace->setGroundArea(old->getGroundArea(),0);
  replace->setTotalArea(old->getTotalArea(),0);
  replace->setApexes(old->getApexes(),0);
  replace->setImportance(old->getImportance(),0);
  replace->setId(id);
  pool.addObject(replace);
  delete old;
}
