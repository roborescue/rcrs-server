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

#include "command.h"
#include "error.h"

namespace Librescue {
  void decodeCommands(CommandList& result, Bytes& in) {
	InputBuffer buffer(in);
	decodeCommands(result,buffer);
  }

  void decodeCommands(CommandList& result, InputBuffer& in) {
	Header h;
	do {
	  h = (Header)in.readInt32("Reading command header");
	  if (h!=HEADER_NULL) {
		INT_32 size = in.readInt32("Reading command size");
		Command* c = decodeCommand(h,size,in);
		if (c) result.push_back(c);
	  }
	} while (h!=HEADER_NULL);
  }

  Command* decodeCommand(Header type, INT_32 size, Bytes& in) {
	InputBuffer buffer(in);
	return decodeCommand(type,size,buffer);
  }

  Command* decodeCommand(Header type, INT_32 size, InputBuffer& in) {
	Command* result;
	//	LOG_DEBUG("Command 0x%x is size %d",type,size);
	switch (type) {
	case AK_SAY:
	  result = new SayCommand(in);
	  break;
	case AK_TELL:
	  result = new TellCommand(in);
	  break;
	case AK_SPEAK:
	  result = new SpeakCommand(in);
	  break;
	case AK_CHANNEL:
	  result = new ChannelCommand(in);
	  break;
	case AK_MOVE:
	  result = new MoveCommand(in);
	  break;
	case AK_EXTINGUISH:
	  result = new ExtinguishCommand(in);
	  break;
	case AK_LOAD:
	  result = new LoadCommand(in);
	  break;
	case AK_UNLOAD:
	  result = new UnloadCommand(in);
	  break;
	case AK_REST:
	  result = new RestCommand(in);
	  break;
	case AK_RESCUE:
	  result = new RescueCommand(in);
	  break;
	case AK_CLEAR:
	  result = new ClearCommand(in);
	  break;
	case AK_CONNECT:
	  result = new AgentConnect(in);
	  break;
	case AK_ACKNOWLEDGE:
	  result = new AgentAcknowledge(in);
	  break;
	case KA_CONNECT_OK:
	  result = new AgentConnectOK(in);
	  break;
	case KA_CONNECT_ERROR:
	  result = new AgentConnectError(in);
	  break;
	case KA_SENSE:
	  result = new AgentSense(in);
	  break;
	case KA_HEAR:
	  result = new AgentHear(KA_HEAR,in);
	  break;
	case KA_HEAR_SAY:
	  result = new AgentHear(KA_HEAR_SAY,in);
	  break;
	case KA_HEAR_TELL:
	  result = new AgentHear(KA_HEAR_TELL,in);
	  break;

	case SK_CONNECT:
	  result = new SimulatorConnect(in);
	  break;
	case SK_ACKNOWLEDGE:
	  result = new SimulatorAcknowledge(in);
	  break;
	case SK_UPDATE:
	  result = new KernelUpdate(in);
	  break;
	case KS_CONNECT_OK:
	  result = new SimulatorConnectOK(in);
	  break;
	case KS_CONNECT_ERROR:
	  result = new SimulatorConnectError(in);
	  break;
	case COMMANDS:
	  result = new Commands(in);
	  break;
	case UPDATE:
	  result = new Update(in);
	  break;

	case VK_CONNECT:
	  result = new ViewerConnect(in);
	  break;
	case VK_ACKNOWLEDGE:
	  result = new ViewerAcknowledge(in);
	  break;
	case KV_CONNECT_OK:
	  result = new ViewerConnectOK(in);
	  break;
	case KV_CONNECT_ERROR:
	  result = new ViewerConnectError(in);
	  break;
	  //	case KV_UPDATE:
	  //	  result = new ViewerUpdate(in);
	  //	  break;

	case GK_CONNECT_OK:
	  result = new GISConnectOK(in);
	  break;
	case GK_CONNECT_ERROR:
	  result = new GISConnectError(in);
	  break;
	case KG_CONNECT:
	  result = new GISConnect(in);
	  break;
	case KG_ACKNOWLEDGE:
	  result = new GISAcknowledge(in);
	  break;
	  //	case KG_UPDATE:
	default:
	  LOG_ERROR("Unrecognised command: %d",type);
	  result = NULL;
	  in.skip(size);
	  break;
	}
	return result;
  }

  Command::Command() {
  }

  Command::~Command() {
  }

  void Command::encode(OutputBuffer& out) const {
  }

  void Command::decode(InputBuffer& in) {
  }
}
