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

#ifndef RESCUE_OUTPUT_H
#define RESCUE_OUTPUT_H

#include "common.h"
#include "property_filter.h"
#include <string>
#include <stdio.h>

namespace Librescue {
  class PropertyFilter;

  class OutputBuffer {
  private:
	Bytes m_bytes;
	int m_index;
	const PropertyFilter* m_filter;

  public:
	OutputBuffer();
	~OutputBuffer();
		
	void clear();
		
	Cursor cursor() const;
	void setCursor(Cursor cursor);

	Cursor writeInt32(INT_32 value);
	Cursor writeInt32(INT_32 value, Cursor position);
	// Write some bytes from a buffer
	Cursor write(int size, const Bytes& bytes);
	Cursor write(int size, Byte* bytes);
	Cursor write(const Bytes& bytes);
	// Write a null-terminated string.
	Cursor writeString(const char* string);
	// Write a string.
	Cursor writeString(std::string string);
	// Write exactly "size" bytes from "string".
	Cursor writeString(const char* string, INT_32 size);

	Cursor writeCommand(const Command* command);
	Cursor writeCommands(const CommandList* commands);

	Cursor writeObject(const RescueObject* object);
	Cursor writeObjects(const ObjectSet& objects);

	Cursor writeProperty(const RescueObject* object, const Property* prop);

	// Fill in the "size" field - this writes the value of "cursor()-base" at location "base", then sets the cursor back to where it was.
	Cursor writeSize(Cursor base);

	const Bytes& buffer() const;
	int size() const;
	void log(FILE* file) const;

	void setPropertyFilter(const PropertyFilter* filter);
	void clearPropertyFilter();
  };

} //namespace Librescue;

#endif // !defined ...
