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

#ifndef RESCUE_SIMULATOR_H
#define RESCUE_SIMULATOR_H

#include "objectpool.h"
#include "common.h"
#include "output.h"
#include "config.h"
#include "connection_manager.h"
#include "command.h"
#include "args.h"
#include <stdio.h>

namespace Librescue {
  class Simulator {
  protected:
	Simulator();

	Config* m_config;
	ObjectPool m_pool;

  public:
	virtual ~Simulator();

	// Initialise the simulator. Return zero on success.
	virtual int init(Config* config, ArgList& args);
	// Do any cleanup required at the end of the simulation
	virtual void cleanup();

	// Print a string describing a summary of available options. Do not print a newline character
	// An example implementation would be "fprintf(file,"--foo <bar>");"
	virtual void printOptions(FILE* file);

	// Print a longer description of all available options, one per line
	// An example implementation would be "fprintf(file,"--foo <bar>\tSet foo to <bar>\n");"
	virtual void printHelp(FILE* file);

	// Process one timestep of this simulation.
	// time: The current timestep.
	// commands: A list of commands sent from agents.
	// changed: This should be filled with pointers to all objects that have changed as a result of this simulator step.
	// return: zero on success, non-zero on error.
	virtual int step(INT_32 time, const AgentCommandList& commands, ObjectSet& changed) = 0;
	virtual void update(INT_32 time, const ObjectSet& changed);
  };
}

#endif
