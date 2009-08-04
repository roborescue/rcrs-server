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

 Contributors and list of changes:

 Cameron Skinner
 Arash Rahimi
*/

#include "blockade.h"
#include "error.h"
#include "objects.h"
#include <fstream>
#include <vector>
#include <algorithm>
#include <stdlib.h>

using namespace Librescue;

struct RoadSortFunction {
  ObjectPool& m_pool;
	
  RoadSortFunction(ObjectPool& pool) : m_pool(pool) {}

  bool operator()(Road *e1, Road *e2) const {
	Node *h1, *t1, *h2, *t2;
	h1 = dynamic_cast<Node*>(m_pool.getObject(e1->getHead()));
	t1 = dynamic_cast<Node*>(m_pool.getObject(e1->getTail()));
	h2 = dynamic_cast<Node*>(m_pool.getObject(e2->getHead()));
	t2 = dynamic_cast<Node*>(m_pool.getObject(e2->getTail()));
	
	int x1 = (h1->getX() + t1->getX()) / 2;
	int y1 = (h1->getY() + t1->getY()) / 2;		
	int x2 = (h2->getX() + t2->getX()) / 2;
	int y2 = (h2->getY() + t2->getY()) / 2;		
	
	return x1 < x2 || (x1 == x2 && y1 < y2);		
  }
};

int BlockadeSimulator::init(Config* config, ArgList& args) {
  Simulator::init(config,args);
  m_blockadeFile = "blockades.lst";
  return 0;
}

int BlockadeSimulator::step(INT_32 time, const AgentCommandList& commands, ObjectSet& changed) {
  if (time==1) {
	// Get a list of all roads, sorted by x then y
	std::vector<Road*> roads;
	for (ObjectSet::iterator it = m_pool.objects().begin();it!=m_pool.objects().end();++it) {
	  RescueObject* next = *it;
	  Road* r = dynamic_cast<Road*>(next);
	  if (r) roads.push_back(r);
	}
	// Sort them
	std::sort(roads.begin(),roads.end(),RoadSortFunction(m_pool));
	// Read from the blockade file
	std::ifstream inFile(m_config->mapfile(m_blockadeFile).c_str());
	char line[256];
	Road* next;
	double rate = m_config->getDouble("road_clear_rate",20000000);
	for (std::vector<Road*>::iterator it = roads.begin();it!=roads.end();++it) {
	  next = *it;
	  while (inFile.good()) {
		inFile.getline(line,256);
		if (line[0]!='#') {
		  int roadBlock = atoi(line);
		  if (roadBlock > next->getWidth()) {
			LOG_WARNING("WARNING: Tried to set a block of %d on a road of width %d",roadBlock,next->getWidth());
			roadBlock = next->getWidth();
		  }
		  int cost = 0;
		  if (roadBlock > 0) {
			cost = (int)((roadBlock * next->getLength() + rate-1)/rate);
		  }
		  next->setBlock(roadBlock, time);
		  next->setRepairCost(cost,time);
		  LOG_DEBUG("Setting road %d block to %d, repair cost = %d",next->id(),roadBlock,cost);
		  changed.insert(next);
		  break;
		}
	  }
	}
  }
  return 0;
}
