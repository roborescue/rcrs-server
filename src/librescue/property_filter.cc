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

#include "property_filter.h"
#include <string.h>

namespace Librescue {
  PropertyFilter::PropertyFilter() {
  }

  PropertyFilter::~PropertyFilter() {}

  void PropertyFilter::rewrite(const RescueObject* object, const IntProperty* property, INT_32* oldValue) const {
  }

  void PropertyFilter::rewrite(const RescueObject* object, const ArrayProperty* property, ValueList* values) const {
  }

  SimplePropertyFilter::SimplePropertyFilter() {
	allowAll();
  }

  SimplePropertyFilter::~SimplePropertyFilter() {}

  void SimplePropertyFilter::block(PropertyId id) {
	m_filter[id] = false;
  }

  void SimplePropertyFilter::blockAll() {
	//	for (int i=0;i<PROPERTY_MAX;++i) m_filter[id] = false;
	memset(m_filter,0,sizeof(bool)*PROPERTY_MAX);
  }

  void SimplePropertyFilter::allow(PropertyId id) {
	m_filter[id] = true;
  }

  void SimplePropertyFilter::allowAll() {
	//	for (int i=0;i<PROPERTY_MAX;++i) m_filter[id] = true;
	memset(m_filter,1,sizeof(bool)*PROPERTY_MAX);
  }

  bool SimplePropertyFilter::allowed(const RescueObject*, const Property* prop) const {
	return m_filter[prop->type()];
  }

  TimePropertyFilter::TimePropertyFilter(INT_32 _time) {
	time = _time;
  }

  TimePropertyFilter::~TimePropertyFilter() {
  }

  bool TimePropertyFilter::allowed(const RescueObject* object, const Property* property) const {
	return property->lastUpdate()>=time;
  }

  MultiFilter::MultiFilter() {
  }

  MultiFilter::~MultiFilter() {
	m_filters.clear();
  }

  void MultiFilter::addFilter(const PropertyFilter* filter) {
	m_filters.push_back(filter);
  }

  void MultiFilter::removeFilter(const PropertyFilter* filter) {
	m_filters.remove(filter);
  }

  void MultiFilter::removeAllFilters() {
	m_filters.clear();
  }

  const FilterList MultiFilter::getFilters() const {
	return m_filters;
  }

  bool MultiFilter::allowed(const RescueObject* object, const Property* property) const {
	for (FilterList::const_iterator it = m_filters.begin();it!=m_filters.end();++it) {
	  const PropertyFilter* next = *it;
	  if (!next->allowed(object,property)) return false;
	}
	return true;
  }

  void MultiFilter::rewrite(const RescueObject* object, const IntProperty* property, INT_32* value) const {
	for (FilterList::const_iterator it = m_filters.begin();it!=m_filters.end();++it) {
	  const PropertyFilter* next = *it;
	  next->rewrite(object,property,value);
	}
  }

  void MultiFilter::rewrite(const RescueObject* object, const ArrayProperty* property, ValueList* values) const {
	for (FilterList::const_iterator it = m_filters.begin();it!=m_filters.end();++it) {
	  const PropertyFilter* next = *it;
	  next->rewrite(object,property,values);
	}
  }
}
