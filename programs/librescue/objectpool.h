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

#ifndef RESCUE_OBJECTPOOL_H
#define RESCUE_OBJECTPOOL_H

#include "objects.h"
#include "common.h"
#include "output.h"
#include <vector>
#include <map>

namespace Librescue {
  class ObjectPool {
  private:
	typedef std::map<Id,RescueObject*> IdToRescueObject;
	IdToRescueObject data;
	ObjectSet allData;

  public:
	ObjectPool();
	// This will delete all RescueObjects that we know about.
	virtual ~ObjectPool();
        int xr,yr,bxr,byr;
	std::vector<std::vector<ObjectSet> > preData;
	std::vector<std::vector<ObjectSet> > preBData;

	int preProcessData(int mmaxX,int mmaxY,int mminX,int mminY,int visionRange,int fireRange);
	int postProcessData(int visionRange,int fireRange);

	// Add an object to the pool
	void addObject(RescueObject* object);
	// Remove an object from the pool
	void removeObject(RescueObject* object);
	// Find an object by ID
	RescueObject* getObject(Id id);
	const RescueObject* getObject(Id id) const;
	// Get the neighbours of an object
	ObjectSet getNeighbours(Id object);
	ObjectSet getNeighbours(const RescueObject* object);
	// Get all objects
	const ObjectSet& objects() const;
	// Find all objects with a certain radius of a point and store them in "result". Returns the number of objects found.
	int getObjectsInRange(int x, int y, int distance, ObjectSet& result) const;
	// Find the distance between two objects. Returns -1 if the range could not be calculated.
	int range(const RescueObject* o1, const RescueObject* o2) const;
	int range(Id o1, Id o2) const;
	int range(const RescueObject* o1, Id o2) const;
	int range(Id o1, const RescueObject* o2) const;
	int range(int x1, int y1, int x2, int y2) const;
	// Find the coordinates of an object and store the result in x and y. Returns true iff the object could be located.
	bool locate(const RescueObject*, int* x, int* y) const;
	bool locate(Id id, int* x, int* y) const;
	// Write all objects to an Output
	//	void write(Output& out) const;
	// Write all objects that have changed after a particular time to an Output
	//	void write(Output& out, int time) const;

	void update(const ObjectSet& changed);

	// Update from a buffer. Returns false if the update failed. The position of the buffer's cursor will be undefined in this case.
	//	bool update(Input& buffer, int time);
  };
}

#endif
