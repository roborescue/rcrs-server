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

#ifndef RESCUE_INPUT_H
#define RESCUE_INPUT_H

#include "common.h"
#include <string>
#include <exception>

namespace Librescue
{
	class Overrun : public std::exception {
	public:
	  Overrun(std::string reason = "");
	  virtual ~Overrun() throw();

	  std::string why() const;

	private:
	  std::string m_reason;
	};

	class InputBuffer {
	protected:
		Bytes m_bytes;
		int m_index;

	public:
		InputBuffer(const Bytes& source);
		~InputBuffer();
			
		// Get the total size of this buffer
		int size() const;

		Cursor cursor() const;
		void setCursor(Cursor cursor);
		void skip(INT_32 numBytes);

		INT_32 readInt32(std::string reason = "");
		INT_32 peekInt32(std::string reason = "");
		// Read some number of bytes into a buffer
		void read(int size, Bytes& buffer,std::string reason = "");
		void read(int size, Byte* buffer,std::string reason = "");
		// Read a string into a char array. The size will be read first.
		void readString(char* result,std::string reason = ""); // Better hope that result is long enough!
		// Read a string of a particular size
		void readString(char* result, int size,std::string reason = "");
		// Read a string and return a new string object. The size will be read first.
		std::string readString(std::string reason = "");

		// Read all RescueObjects in the stream and store the result in an Objects buffer. This will fill "result" with new RescueObjects - don't forget to delete them later!
		void readObjects(int time, ObjectSet& result);
		// Allocate and populate a new RescueObject. This function will return a new RescueObject. Don't forget to delete it later!
		RescueObject* readObject(int time);
		void readProperty(Property* p, int time);
	};

} //namespace rescue;

#endif // !defined ...
