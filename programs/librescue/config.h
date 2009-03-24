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

#ifndef RESCUE_CONFIG_H
#define RESCUE_CONFIG_H

#include <string>
#include <map>
#include "args.h"
#include "output.h"

namespace Librescue {
  enum ConfigError {
	CONFIG_OK,
	CONFIG_ERROR
  };

  class Config {
  public:
	Config();
	virtual ~Config();

	void init(ArgList& args);

	ConfigError readConfigFile(std::string filename);

	std::string getString(std::string key, std::string defaultValue = "") const;
	int getInt(std::string key, int defaultValue = 0) const;
	double getDouble(std::string key, double defaultValue = 0) const;
	bool getBool(std::string key, bool defaultValue = false) const;

	void set(std::string key, std::string value);
	void set(std::string key, int value);
	void set(std::string key, bool value);
	void clear(std::string key);

	void write(OutputBuffer& out);

	// Common config entries
	// Get the map directory
	std::string mapdir() const;
	// Get the full path to a file that lives in the map directory. This is equivalent to the expression "mapdir()+file"
	std::string mapfile(std::string file) const;

  private:
	typedef std::map<std::string,std::string> StringMap;
	StringMap m_data;

	void trim(std::string& string);
  };
}

#endif
