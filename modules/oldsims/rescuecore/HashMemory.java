/*
 * Last change: $Date: 2004/05/04 03:09:38 $
 * $Revision: 1.7 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * This is an implementation of Memory that stores the data in a hashtable
 */
public class HashMemory extends Memory {
  protected Map data;

  /**
   * Construct a new empty memory
   */
  public HashMemory() {
    data = new HashMap();
  }

  public Memory copy() {
    HashMemory m = new HashMemory();
    Iterator it = data.values().iterator();
    while (it.hasNext()) {
      RescueObject o = (RescueObject) it.next();
      m.add(o.copy(), 0, RescueConstants.SOURCE_UNKNOWN);
    }
    return m;
  }

  public RescueObject lookup(int id) {
    return (RescueObject) data.get(Integer.valueOf(id));
  }

  public Collection<RescueObject> getAllObjects() {
    return new HashSet<RescueObject>(data.values());
  }

  public void getObjectsOfType(Collection<RescueObject> result, int... types) {
    for (Iterator it = data.values().iterator(); it.hasNext();) {
      RescueObject next = (RescueObject) it.next();
      int type = next.getType();
      for (int nextType : types) {
        if (type == nextType) {
          result.add(next);
          break;
        }
      }
    }
  }

  /*
   * public RescueObject[] getObjectsOfInternalType(int type) { List result = new
   * ArrayList(); for (Iterator it = data.values().iterator();it.hasNext();) {
   * RescueObject next = (RescueObject)it.next(); if (next!=null &&
   * (next.getInternalType() & type)!=0) result.add(next); } return
   * (RescueObject[])result.toArray(new RescueObject[0]); }
   */

  public void add(RescueObject o, int timestamp, Object source) {
    data.put(Integer.valueOf(o.getID()), o);
    super.add(o, timestamp, source);
  }

  public void remove(RescueObject o) {
    data.remove(Integer.valueOf(o.getID()));
    super.remove(o);
  }
}