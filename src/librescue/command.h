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
 Mohammad Mehdi Saboorian / Channel based communication
*/

#ifndef RESCUE_COMMAND_H
#define RESCUE_COMMAND_H

#include "output.h"
#include "input.h"
#include "common.h"
#include "property_filter.h"
#include <vector>

namespace Librescue {
  // Decode a Command. This will return a pointer to a new Command object.
  Command* decodeCommand(Header header, INT_32 size, Bytes& in);
  Command* decodeCommand(Header header, INT_32 size, InputBuffer& in);
  void decodeCommands(CommandList& result, Bytes& in);
  void decodeCommands(CommandList& result, InputBuffer& in);

  class Command {
  private:
	// No copying allowed
	Command(const Command& rhs);
	Command& operator=(const Command& rhs);

  protected:
	Command();

  public:
	virtual ~Command();
	virtual Header getType() const = 0;
	// Write the contents of this command to an OutputBuffer. Do not write the command type or size.
	virtual void encode(OutputBuffer& out) const;
	// Decode a command.
	virtual void decode(InputBuffer& out);

	virtual Command* clone() const = 0;
  };

  class AgentConnect : public Command {
  private:
	AgentType m_type;
	Id m_tempId;
	INT_32 m_version;

  public:
	AgentConnect(AgentType type, Id tempId, INT_32 version);
	AgentConnect(InputBuffer& in);
	virtual ~AgentConnect();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	AgentType getAgentType() const;
	Id getTempId() const;
	INT_32 getVersion() const;
  };

  class AgentAcknowledge : public Command {
  private:
	Id m_id;

  public:
	AgentAcknowledge(Id id);
	AgentAcknowledge(InputBuffer& in);
	virtual ~AgentAcknowledge();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	Id getId() const;
  };

  class AgentConnectOK : public Command {
  private:
	Id m_tempId;
	Id m_id;
	ObjectSet m_objects;
	RescueObject* m_self;

	bool m_delete;
	void deleteObjects();

  public:
	AgentConnectOK(Id tempId, Id id, const ObjectSet& staticObjects, RescueObject& self);
	AgentConnectOK(InputBuffer& in);
	virtual ~AgentConnectOK();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	Id getTempId() const;
	Id getId() const;
	const ObjectSet& getObjects() const;
	RescueObject* getSelf() const;
  };

  class AgentConnectError : public Command {
  private:
	Id m_id;
	std::string m_reason;

  public:
	AgentConnectError(Id id, std::string reason);
	AgentConnectError(InputBuffer& in);
	virtual ~AgentConnectError();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const std::string& getReason() const;
  };

  class AgentSense : public Command {
  private:
	Id m_id;
	INT_32 m_time;
	ObjectSet m_objects;

	bool m_delete;
	void deleteObjects();

  public:
	AgentSense(Id id, INT_32 time, const ObjectSet& objects);
	AgentSense(InputBuffer& in);
	virtual ~AgentSense();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	Id getId() const;
	INT_32 getTime() const;
	const ObjectSet& getObjects() const;
  };

  class AgentHear : public Command {
  private:
	Header m_type;
	Id m_to;
	Id m_from;
	Byte m_channel;
	Bytes m_data;

  public:
	AgentHear(Header type, Id to, Id from, Byte channel, const Bytes& data);
	AgentHear(Header type, InputBuffer& in);
	virtual ~AgentHear();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	Id getFrom() const;
	Id getTo() const;
	Byte getChannel() const;
	const Bytes& getData() const;
  };

  class AgentCommand : public Command {
  protected:
	AgentCommand(Id agent);
	AgentCommand(InputBuffer& in);

	Id m_agentId;

  public:
	virtual ~AgentCommand();

	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	Id getAgentId() const;
  };

  class VoiceCommand : public AgentCommand {
  protected:
	Bytes m_data;
	Byte m_channel;

	VoiceCommand(Id agent, const Byte* message, int size, const Byte channel = CHANNEL_NONE);
	VoiceCommand(Id agent, const Bytes& message, const Byte channel = CHANNEL_NONE);
	VoiceCommand(InputBuffer& in);

  public:
	virtual ~VoiceCommand();
	
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	const Bytes& getData() const;
	const Byte getChannel() const;
  };

  class SayCommand : public VoiceCommand {
  public:
	SayCommand(Id agent, const Byte* message, int size);
	SayCommand(Id agent, const Bytes& message);
	SayCommand(InputBuffer& in);
	virtual ~SayCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class TellCommand : public VoiceCommand {
  public:
	TellCommand(Id agent, const Byte* message, int size, const Byte channel = CHANNEL_NONE);
	TellCommand(Id agent, const Bytes& message, const Byte channel = CHANNEL_NONE);
	TellCommand(InputBuffer& in);
	virtual ~TellCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class ChannelCommand : public AgentCommand
  {
  private:
	  Bytes m_channels;

  public:
	  ChannelCommand (Id agent);
	  ChannelCommand (Id agent, const Bytes& channels);
	  ChannelCommand (InputBuffer& in);
	  virtual ~ChannelCommand();

	  void add (Byte channel);

	  virtual void encode(OutputBuffer& out) const;
	  virtual void decode(InputBuffer& in);
	  const Bytes& getChannels() const;
	  virtual Header getType() const;
	  virtual Command* clone() const;
  };

  class MoveCommand : public AgentCommand {
  private:
	IdList m_path;

  public:
	MoveCommand(Id agent);
	MoveCommand(Id agent, const IdList& path);
	MoveCommand(InputBuffer& in);
	virtual ~MoveCommand();

	void add(Id id);

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const IdList& getPath() const;
  };

  typedef struct Nozzle {
	Id target;
	INT_32 direction;
	INT_32 x;
	INT_32 y;
	INT_32 amount;

	Nozzle(Id t, INT_32 d, INT_32 x_, INT_32 y_, INT_32 a) : target(t), direction(d), x(x_), y(y_), amount(a) {}
  } Nozzle;

  typedef std::vector<Nozzle> Nozzles;

  class ExtinguishCommand : public AgentCommand {
  private:
	Nozzles m_nozzles;

  public:
	ExtinguishCommand(Id agent);
	ExtinguishCommand(Id agent, Id target, INT_32 direction, INT_32 x, INT_32 y, INT_32 amount);
	ExtinguishCommand(InputBuffer& in);
	virtual ~ExtinguishCommand();

	void addNozzle(Id target, INT_32 direction, INT_32 x, INT_32 y, INT_32 amount);

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const Nozzles& getNozzles() const;
  };

  class TargetCommand : public AgentCommand {
  protected:
	Id m_target;
	TargetCommand(Id agent, Id target);
	TargetCommand(InputBuffer& in);

  public:
	virtual ~TargetCommand();

	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	Id getTarget() const;
  };

  class LoadCommand : public TargetCommand {
  public:
	LoadCommand(Id agent, Id target);
	LoadCommand(InputBuffer& in);
	virtual ~LoadCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class UnloadCommand : public AgentCommand {
  public:
	UnloadCommand(Id agent);
	UnloadCommand(InputBuffer& in);
	virtual ~UnloadCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class RestCommand : public AgentCommand {
  public:
	RestCommand(Id agent);
	RestCommand(InputBuffer& in);
	virtual ~RestCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class RescueCommand : public TargetCommand {
  public:
	RescueCommand(Id agent, Id target);
	RescueCommand(InputBuffer& in);
	virtual ~RescueCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class ClearCommand : public TargetCommand {
  public:
	ClearCommand(Id agent, Id target);
	ClearCommand(InputBuffer& in);
	virtual ~ClearCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class RepairCommand : public TargetCommand {
  public:
	RepairCommand(Id agent, Id target);
	RepairCommand(InputBuffer& in);
	virtual ~RepairCommand();

	virtual Header getType() const;

	virtual Command* clone() const;
  };

  class SimulatorConnect : public Command {
  private:
	INT_32 m_version;
	
  public:
	SimulatorConnect(INT_32 version);
	SimulatorConnect(InputBuffer& in);
	virtual ~SimulatorConnect();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	INT_32 getVersion() const;
  };

  class SimulatorConnectOK : public Command {
  private:
	Id m_id;
	ObjectSet m_objects;

	bool m_delete;
	void deleteObjects();
	
  public:
	SimulatorConnectOK(Id id, const ObjectSet& objects);
	SimulatorConnectOK(InputBuffer& in);
	virtual ~SimulatorConnectOK();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	Id getId() const;
	const ObjectSet& getObjects() const;
  };

  class SimulatorConnectError : public Command {
  private:
	std::string m_reason;
	
  public:
	SimulatorConnectError(std::string reason);
	SimulatorConnectError(InputBuffer& in);
	virtual ~SimulatorConnectError();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const std::string& getReason() const;
  };

  class SimulatorAcknowledge : public Command {
  private:
	Id m_id;
	
  public:
	SimulatorAcknowledge(Id id);
	SimulatorAcknowledge(InputBuffer& in);
	virtual ~SimulatorAcknowledge();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	Id getId() const;
  };

  class Commands : public Command {
  private:
	INT_32 m_time;
	AgentCommandList m_commands;

	bool m_delete;
	void deleteObjects();
	
  public:
	Commands(INT_32 time, const AgentCommandList& commands);
	Commands(InputBuffer& in);
	virtual ~Commands();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	INT_32 getTime() const;
	const AgentCommandList& getCommands() const;

	virtual Command* clone() const;
  };

  class Update : public Command {
  private:
	INT_32 m_time;
	ObjectSet m_objects;

	bool m_delete;
	void deleteObjects();
	
  public:
	Update(INT_32 time, const ObjectSet& objects);
	Update(InputBuffer& in);
	virtual ~Update();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	INT_32 getTime() const;
	const ObjectSet& getObjects() const;

	virtual Command* clone() const;
  };

  class KernelUpdate : public Command { // This gets sent to the kernel
  private:
	Id m_id;
	INT_32 m_time;
	ObjectSet m_objects;
	
	bool m_delete;
	void deleteObjects();

  public:
	KernelUpdate(Id id, INT_32 time, const ObjectSet& objects);
	KernelUpdate(InputBuffer& in);
	virtual ~KernelUpdate();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	Id getId() const;
	INT_32 getTime() const;
	const ObjectSet& getObjects() const;
  };

  class ViewerConnect : public Command {
  private:
	INT_32 m_version;
	
  public:
	ViewerConnect(INT_32 version);
	ViewerConnect(InputBuffer& in);
	virtual ~ViewerConnect();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	INT_32 getVersion() const;
  };

  class ViewerConnectOK : public Command {
  private:
	ObjectSet m_objects;
	
	bool m_delete;
	void deleteObjects();

  public:
	ViewerConnectOK(const ObjectSet& objects);
	ViewerConnectOK(InputBuffer& in);
	virtual ~ViewerConnectOK();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const ObjectSet& getObjects() const;
  };

  class ViewerConnectError : public Command {
  private:
	std::string m_reason;
	
  public:
	ViewerConnectError(std::string reason);
	ViewerConnectError(InputBuffer& in);
	virtual ~ViewerConnectError();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const std::string& getReason() const;
  };

  class ViewerAcknowledge : public Command {
  private:
	
  public:
	ViewerAcknowledge();
	ViewerAcknowledge(InputBuffer& in);
	virtual ~ViewerAcknowledge();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;
  };

  class GISConnect : public Command {
  private:
	INT_32 m_version;
	
  public:
	GISConnect(INT_32 version);
	GISConnect(InputBuffer& in);
	virtual ~GISConnect();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	INT_32 getVersion() const;
  };

  class GISConnectOK : public Command {
  private:
	ObjectSet m_objects;
	
	bool m_delete;
	void deleteObjects();

  public:
	GISConnectOK(const ObjectSet& objects);
	GISConnectOK(InputBuffer& in);
	virtual ~GISConnectOK();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const ObjectSet& getObjects() const;
  };

  class GISConnectError : public Command {
  private:
	std::string m_reason;
	
  public:
	GISConnectError(std::string reason);
	GISConnectError(InputBuffer& in);
	virtual ~GISConnectError();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;

	const std::string& getReason() const;
  };

  class GISAcknowledge : public Command {
  private:
	
  public:
	GISAcknowledge();
	GISAcknowledge(InputBuffer& in);
	virtual ~GISAcknowledge();

	virtual Header getType() const;
	virtual void encode(OutputBuffer& out) const;
	virtual void decode(InputBuffer& in);

	virtual Command* clone() const;
  };
}

#endif
