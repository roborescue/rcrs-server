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

January 2006
   Changed from using macro-defined functions to looking up by name
*/

#include "config.h"
#include "error.h"
#include "handy.h"
#include <string.h>
#include <iostream>
#include <fstream>
#include <stdlib.h>

extern int errno;

namespace Librescue {
  Config::Config() {}

  Config::~Config() {}

  void Config::init(ArgList& args) {
	// Process the arguments
	ArgList::iterator it = args.begin();
	set("mapdir","./");
	while (it!=args.end()) {
	  ArgList::iterator temp = it;
	  const char* next = (*it).c_str();
	  if (strcmp(next,"-c")==0 || strcmp(next,"--config")==0) {
		++it;
		readConfigFile(*it);
		++it;
		args.erase(temp,it);
	  }
	  else if (strcmp(next,"-m")==0 || strcmp(next,"--mapdir")==0) {
		++it;
		std::string dir = *it;
		if (dir.find_last_of('/')!=dir.size()) dir.append("/");
		set("mapdir",dir);
		++it;
		args.erase(temp,it);
	  }
	  else if (strncmp(next,"--",2)==0) {
		std::string key = next+2;
		++it;
		std::string value = *it;
		++it;
		set(key,value);
		LOG_DEBUG("Setting config entry: \"%s\" = \"%s\"",key.c_str(),value.c_str());
		args.erase(temp,it);
	  }
	  else {
		++it;
	  }
	}
	// Set the random seed
	initRandom(getInt("random_seed",-1));
  }

  ConfigError Config::readConfigFile(std::string file) {
	LOG_INFO("Reading config file %s",file.c_str());
	// Read all the data in the file
	std::ifstream in(file.c_str());
	std::string line;
	int lineNo = 0;
	while (in.good() && getline(in,line)) {
	  ++lineNo;
	  trim(line);
	  if (line.size()==0 || line.c_str()[0]=='#') continue;
	  if (line.c_str()[0]=='!') {
		// Command
		if (line.find("!include ")==0) {
		  // Include command
		  std::string target(line,9,line.size());
		  readConfigFile(target);
		}
		else {
		  LOG_WARNING("%s: Unrecognised command at line %d: %s",file.c_str(),lineNo,line.c_str());
		}
	  }
	  else {
		// Key : value
		int index = line.find(':',0);
		if (index<0) {
		  LOG_WARNING("%s: Line %d is malformed: %s",file.c_str(),lineNo,line.c_str());
		  continue;
		}
		std::string key(line,0,index);
		std::string value(line,index+1,line.size());
		// Trim the key and value
		trim(key);
		trim(value);
		if (m_data.find(key)!=m_data.end()) LOG_WARNING("Duplicate entry found: %s was %s, now %s",key.c_str(),m_data[key].c_str(),value.c_str());
		m_data[key] = value;
		//		LOG_DEBUG("%s=%s",key.c_str(),value.c_str());
	  }
	}
	if (in.eof()) return CONFIG_OK;
	return CONFIG_ERROR;
  }

  std::string Config::getString(std::string key, std::string defaultValue) const {
	StringMap::const_iterator it = m_data.find(key);
	if (it==m_data.end()) return defaultValue;
	return (*it).second;
  }

  int Config::getInt(std::string key, int defaultValue) const {
	StringMap::const_iterator it = m_data.find(key);
	if (it==m_data.end()) return defaultValue;
	std::string result = (*it).second;
	return atoi(result.c_str());
  }

  double Config::getDouble(std::string key, double defaultValue) const {
	StringMap::const_iterator it = m_data.find(key);
	if (it==m_data.end()) return defaultValue;
	std::string result = (*it).second;
	return atof(result.c_str());
  }

  bool Config::getBool(std::string key, bool defaultValue) const {
	StringMap::const_iterator it = m_data.find(key);
	if (it==m_data.end()) return defaultValue;
	std::string result = (*it).second;
	return strcasecmp(result.c_str(),"true")==0 || strcasecmp(result.c_str(),"yes")==0;
  }

  void Config::set(std::string key, std::string value) {
	m_data[key] = value;
  }

  void Config::set(std::string key, int value) {
	char buffer[256];
	snprintf(buffer,256,"%d",value);
	std::string result(buffer);
	m_data[key] = result;
  }

  void Config::set(std::string key, bool value) {
	m_data[key] = value?"true":"false";
  }

  void Config::clear(std::string key) {
	m_data.erase(key);
  }

  void Config::trim(std::string& string) {
	// Remove all whitespace
	const char* old = string.c_str();
	int size = string.size();
	char result[size];
	int index = 0;
	for (int i=0;i<size;++i) {
	  switch (old[i]) {
	  case ' ':
	  case '\n':
	  case '\t':
	  case '\r':
		// Ignore
		break;
	  case '#':
		// Comment - ignore rest of line
		result[index] = 0;
		i = size;
		break;
	  default:
		result[index++] = old[i];
		break;
	  }
	}
	result[index] = 0;
	string.assign(result);
  }

  void Config::write(OutputBuffer& out) {
	out.writeInt32(m_data.size());
	for (StringMap::const_iterator it = m_data.begin();it!=m_data.end();++it) {
	  out.writeString((*it).first);
	  out.writeString((*it).second);
	}
  }

  std::string Config::mapdir() const {
	return getString("mapdir");
  }

  std::string Config::mapfile(std::string file) const {
	return mapdir().append(file);
  }
}
