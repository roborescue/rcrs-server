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
   Converted original Robocup Rescue software into librescue
*/

#include "objectpool.h"
#include "error.h"
#include <math.h>
#include <algorithm>

namespace Librescue {
  ObjectPool::ObjectPool() : data(), allData() {
  }

  ObjectPool::~ObjectPool() {
    for (ObjectSet::iterator it = allData.begin();it!=allData.end();++it) {
      RescueObject* next = *it;
      delete next;
    }
  }

  void ObjectPool::addObject(RescueObject* o) {
    RescueObject* old = data[o->id()];
    if (old) {
      allData.erase(old);
      delete old;
    }
    data[o->id()] = o;
    allData.insert(o);
  }

  void ObjectPool::removeObject(RescueObject* o) {
    data[o->id()] = NULL;
    allData.erase(o);
  }

  RescueObject* ObjectPool::getObject(Id id) {
    return data[id];
  }

  const RescueObject* ObjectPool::getObject(Id id) const {
    IdToRescueObject::const_iterator it = data.find(id);
    if (it == data.end()) {
      return 0;
    }
    RescueObject* result = (*it).second;
    return result;
  }

  ObjectSet ObjectPool::getNeighbours(Id id) {
    const RescueObject* object = getObject(id);
    if (object) return getNeighbours(object);
    else {
      ObjectSet o;
      return o;
    }
  }

  ObjectSet ObjectPool::getNeighbours(const RescueObject* object) {
    ObjectSet result;
    const Building* b = dynamic_cast<const Building*>(object);
    if (b) {
      const ValueList entrances = b->getEntrances();
      for (ValueList::const_iterator it = entrances.begin();it!=entrances.end();++it) {
	RescueObject* o = getObject(*it);
	if (o) result.insert(o);
      }
    }
    const Vertex* v = dynamic_cast<const Vertex*>(object);
    if (v) {
      const ValueList edges = v->getEdges();
      for (ValueList::const_iterator it = edges.begin();it!=edges.end();++it) {
	RescueObject* o = getObject(*it);
	if (o) result.insert(o);
      }
    }
    const Edge* e = dynamic_cast<const Edge*>(object);
    if (e) {
      RescueObject* head = getObject(e->getHead());
      RescueObject* tail = getObject(e->getTail());
      if (head) result.insert(head);
      if (tail) result.insert(tail);
    }
    return result;
  }

  const ObjectSet& ObjectPool::objects() const {
    return allData;
  }

  int ObjectPool::getObjectsInRange(int x, int y, int maxRange, ObjectSet& result) const {
    int count = 0;
    for (IdToRescueObject::const_iterator it = data.begin();it!=data.end();++it) {
      int x1,y1;
      RescueObject* o = it->second;
      if (locate(o,&x1,&y1)) {
	if (range(x,y,x1,y1) <= maxRange) {
	  result.insert(o);
	  ++count;
	}
      }
    }
    return count;
  }

  int ObjectPool::preProcessData(int maxX,int maxY,int minX,int minY,int visionRange,int fireRange)
  {
    int x,y,i,count=0;

    xr=((maxX-minX)/visionRange)+1;
    yr=((maxY-minY)/visionRange)+1;
    bxr=((maxX-minX)/fireRange)+1;
    byr=((maxY-minY)/fireRange)+1;

    preData.resize(xr);
    for(i=0;i<xr;i++){
      preData[i].resize(yr);
    }
    preBData.resize(bxr);
    for(i=0;i<bxr;i++){
      preBData[i].resize(byr);
    }
    for (IdToRescueObject::const_iterator it = data.begin();it!=data.end();++it)
      {
	RescueObject* o = it->second;

	if (locate(o,&x,&y)) {

	  preData[(x/visionRange)][(y/visionRange)].insert(o);

	  Building* b = dynamic_cast<Building*>(o);
		
	  if (b && b->getFieryness()!=0) {
	    preBData[(x/fireRange)][(y/fireRange)].insert(b);
	    ++count;
	  }

	}
      }
    return count;

  }


  int ObjectPool::postProcessData(int visionRange,int fireRange){
    int count=0,x,y;
    for (IdToRescueObject::const_iterator it = data.begin();it!=data.end();++it) {
      RescueObject* o = it->second;
      if (locate(o,&x,&y)) {
	preData[(x/visionRange)][(y/visionRange)].clear();
	Building* b = dynamic_cast<Building*>(o);
	if (b && b->getFieryness()!=0) {
	  preBData[(x/fireRange)][(y/fireRange)].clear();
	  ++count;
	    
	}
      }
	
    }
    return count;
  }

  int ObjectPool::range(int x1, int y1, int x2, int y2) const {
    double dx = x2-x1;
    double dy = y2-y1;
    return (int)sqrt(dx*dx + dy*dy);
  }

  int ObjectPool::range(const RescueObject* o1, const RescueObject* o2) const {
    int x1, x2, y1, y2;
    if (!locate(o1,&x1,&y1)) return -1;
    if (!locate(o2,&x2,&y2)) return -1;
    return range(x1,y1,x2,y2);
  }

  int ObjectPool::range(Id o1, Id o2) const {
    return range(getObject(o1),getObject(o2));
  }

  int ObjectPool::range(const RescueObject* o1, Id o2) const {
    return range(o1,getObject(o2));
  }

  int ObjectPool::range(Id o1, const RescueObject* o2) const {
    return range(getObject(o1),o2);
  }

  bool ObjectPool::locate(Id id, int* x, int* y) const {
    const RescueObject* object = getObject(id);
    if (object) return locate(object,x,y);
    else return false;
  }

  bool ObjectPool::locate(const RescueObject* o, int* x, int* y) const {
    const Vertex* vertex = dynamic_cast<const Vertex*>(o);
    if (vertex) {
      // Just get the x and y values out of the vertex
      *x = vertex->getX();
      *y = vertex->getY();
      return true;
    }
    const Building* b = dynamic_cast<const Building*>(o);
    if (b) {
      // Just get the x and y values out of the building
      *x = b->getX();
      *y = b->getY();
      return true;
    }
    const Edge* edge = dynamic_cast<const Edge*>(o);
    if (edge) {
      const RescueObject* head = getObject(edge->getHead());
      const RescueObject* tail = getObject(edge->getTail());
      int headX, headY, tailX, tailY;
      if (locate(head,&headX,&headY) && locate(tail,&tailX,&tailY)) {
	*x = (headX+tailX)/2;
	*y = (headY+tailY)/2;
	return true;
      }
      return false;
    }
    const MovingObject* moving = dynamic_cast<const MovingObject*>(o);
    if (moving) {
      // Find it's position
      const RescueObject* positionObject = getObject(moving->getPosition());
      //	  LOG_DEBUG("Locating a moving object: %d's position is %d (%p)",moving->id(),moving->getPosition(),positionObject);
      if (positionObject) {
	// Check whether we are on an Edge (road or river)
	const Edge* edge = dynamic_cast<const Edge*>(positionObject);
	if (edge) {
	  // First find the edge's head and tail locations
	  const Node* head = dynamic_cast<const Node*>(getObject(edge->getHead()));
	  const Node* tail = dynamic_cast<const Node*>(getObject(edge->getTail()));
	  int headX, headY, tailX, tailY;
	  if (!locate(head,&headX,&headY)) return false;
	  if (!locate(tail,&tailX,&tailY)) return false;
	  double positionExtra = moving->getPositionExtra(); // Store positionExtra as a double so we can calculate the proportion correctly (look down two lines).
	  // Now work out what our real position is
	  double proportion = positionExtra/edge->getLength();
	  int dx = tailX-headX;
	  int dy = tailY-headY;
	  *x = headX + (int)(proportion*dx);
	  *y = headY + (int)(proportion*dy);
	  return true;
	}
	else {
	  // Just find the positionObjects location
	  return locate(positionObject,x,y);
	}
      }
    }
    return false;
  }

  /*
    void ObjectPool::write(Output& out) const {
    for (ObjectSet::const_iterator it = allData.begin();it!=allData.end();++it) {
    const RescueObject* next = *it;
    next->write(out);
    }
    out.writeInt32(TYPE_NULL);
    }

    void ObjectPool::write(Output& out, int time) const {
    //	snprintf(errorBuffer,ERROR_BUFFER_LENGTH,"Sending update at time %d",time);
    //	logDebug(errorBuffer);
    for (ObjectSet::const_iterator it = allData.begin();it!=allData.end();++it) {
    RescueObject* next = *it;
    next->write(out,time);
    }
    out.writeInt32(TYPE_NULL);
    }
  */

  void ObjectPool::update(const ObjectSet& changed) {
    for (ObjectSet::const_iterator it = changed.begin();it!=changed.end();++it) {
      const RescueObject* next = *it;
      // Do we already know about this object?
      RescueObject* existing = getObject(next->id());
      if (existing) {
	// Yes. Merge the new one into the old one.
        //        LOG_DEBUG("Merging object %d",next->id());
	existing->merge(next);
      }
      else {
	// No. Add a clone.
        //        LOG_DEBUG("Cloning object %d",next->id());
	addObject(next->clone());
      }
    }
  }

  /*
    bool ObjectPool::update(Input& buffer, int time) {
    TypeId type;
    //	logDebug("Updating objectpool");
    do {
    type = (TypeId)buffer.readInt32();
    if (type!=TYPE_NULL) {
    Id id = (Id)buffer.peekInt32();
    // Look up the object
    RescueObject* object = getObject(id);
    bool add = false;
    if (!object) {
    //		  logDebug("New object");
    object = newRescueObject(type);
    add = true;
    if (!object) {
    LOG_DEBUG("Couldn't read update message!");
    return false;
    }
    }
    LOG_DEBUG("Updating object %d",id);
    object->read(buffer,time);
    if (add) addObject(object);
    }
    } while (type!=TYPE_NULL);
    return true;
    }
  */
}
