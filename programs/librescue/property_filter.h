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
*/

#ifndef RESCUE_PROPERTY_FILTER_H
#define RESCUE_PROPERTY_FILTER_H

#include "common.h"
#include "objects.h"

namespace Librescue {
  class IntProperty;
  class ArrayProperty;

  class PropertyFilter {
  protected:
	PropertyFilter();
	
  public:
	virtual ~PropertyFilter();

	virtual bool allowed(const RescueObject* object, const Property* property) const = 0;
	virtual void rewrite(const RescueObject* object, const IntProperty* property, INT_32* value) const;
	virtual void rewrite(const RescueObject* object, const ArrayProperty* property, ValueList* values) const;
  };

  class SimplePropertyFilter : public PropertyFilter {
  private:
	bool m_filter[PROPERTY_MAX];

  public:
	SimplePropertyFilter();
	virtual ~SimplePropertyFilter();

	void block(PropertyId id);
	void blockAll();

	void allow(PropertyId id);
	void allowAll();

	virtual bool allowed(const RescueObject* object, const Property* property) const;
  };

  class TimePropertyFilter : public PropertyFilter {
  private:
	INT_32 time;

  public:
	TimePropertyFilter(INT_32 time);
	virtual ~TimePropertyFilter();

	virtual bool allowed(const RescueObject* object, const Property* property) const;	
  };

  typedef std::list<const PropertyFilter*> FilterList;

  class MultiFilter : public PropertyFilter {
  private:
	FilterList m_filters;

	// No copying
	MultiFilter(const MultiFilter& rhs);
	MultiFilter& operator=(const MultiFilter& rhs);

  public:
	MultiFilter();
	virtual ~MultiFilter();

	void addFilter(const PropertyFilter* filter);
	void removeFilter(const PropertyFilter* filter);
	void removeAllFilters();
	const FilterList getFilters() const;

	virtual bool allowed(const RescueObject* object, const Property* property) const;
	virtual void rewrite(const RescueObject* object, const IntProperty* property, INT_32* old) const;
	virtual void rewrite(const RescueObject* object, const ArrayProperty* property, ValueList* values) const;	
  };
}

#endif
