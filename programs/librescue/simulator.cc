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

#include "simulator.h"
#include "common.h"
#include "connection_manager.h"
#include "connection.h"
#include "objectpool.h"
#include "objects.h"
#include "command.h"
#include <stdlib.h>
#include <string.h>

namespace Librescue {
  Simulator::Simulator() {}

  Simulator::~Simulator() {}

  int Simulator::init(Config* config, ArgList& args) {
	m_config = config;
	return 0;
  }

  void Simulator::cleanup() {}

  void Simulator::update(INT_32 time, const ObjectSet& changed) {
	m_pool.update(changed);
  }

  void Simulator::printOptions(FILE* file) {
	fprintf(file,"[(-h | --host) <host>] ");
	fprintf(file,"[(-p | --port) <port>] ");
	fprintf(file,"[(-c | --config) <file>] ");
	fprintf(file,"[--help] ");
  }

  void Simulator::printHelp(FILE* file) {
	fprintf(file,"-h\t--host\t<file>\tSpecify the host that the kernel is running on\n");
	fprintf(file,"-p\t--port\t<file>\tSpecify the port that the kernel is listening to\n");
	fprintf(file,"-c\t--config\t<file>\tSpecify the config file to read\n");
	fprintf(file,"\t--help\t<file>\tPrint this help message\n");
  }
}
