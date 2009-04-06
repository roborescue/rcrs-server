                  Morimoto Traffic Simulator ver.0.21
        for RoboCupRescue Prototype Simulation System ver.0.39

                            May. 11, 2002


This traffic simulator is developed for RoboCupRescue Simulation
League on RoboCup 2002 in Fukuoka/Busan. We hope that agent developers
will not be much affected by changing from the Atsumi Traffic
Simulator.


I. Installation

To make this simulator, you simply type the following command:

    % make

    NOTE: JDK 1.3.0 is required


II. Usage

Synopsis is as follows:

    java traffic.Main [ hostname [ port ] ]

    hostname : kernel host name (default: localhost)
    port     : kernel port (default: 6000)


III. Required Environment

In 1/10 Model of Kobe-Map, it is necessary to execute the simulation
system by using at least three PCs, each of which is responsible to:

  - Platoon agents (ambulance team, fire brigade, and police force)
  - Civilian agents and a fire simulator
  - The kernel and other simulators (including this traffic simulator)

We checked the operations by using three PCs of the following spec:

    CPU : Pentium III 930MHz
    MEM : 256MB
    OS  : Linux (Vine 2.1.5)
    JDK : 1.3.0


IV. Minimum Information for Agent Developers

(1) This program simulates AK-Commands: AK_MOVE, AK_LOAD and
AK_UNLOAD.  The definition of AK-Commands are same as before.

(1-1) Move (moving object only)

Header: AK_MOVE
Body:   routePlan : an array of IDs  (32 bit * number of route objects)
        0         : a sentinel of array (32 bit)

An agent moves along the given routePlan.  The route plan is a
statement acceptable by the following automaton, which consists of the
current position as the origin of a route and IDs of MotionlessObject.

             n          n
          ------>    <------ OrgBldg
      Road       Node
          <------    ------> DestBldg
             r          b

       Initial state    || Road | Node | OrgBldg
      ------------------++------+------+----------
       Current position || Road | Node | Building

       input   || n                  | r                | b
      ---------++--------------------+------------------+----------------------
       meaning || adjacent Node's ID | adjcnt Road's ID | adjcnt Building's ID

      End State : all states

(1-2) Load (ambulance team only)

Header: AK_LOAD
Body:   target : an ID (32 bit)

An ambulance team agent loads the ambulance car with the given injured
target.  The target must be at the same position as the ambulance
team.  Note, however, that a road and its endpoint nodes are
considered as the same position so that the ambulance team on a road
can loads its car with an injured person on an endpoint node of the
road.

(1-3) Unload (ambulance team only)

Header: AK_UNLOAD
Body:   Nothing

An ambulance team agent unloads the ambulance car with the injured
person.  The unloaded person will stand initially at the same position
as the ambulance team.

(2) Passability Criteria

An agent cannot pass a blockade or a traffic jam.

(2-1) Blockade

No car or civilian can pass a blockade.  The definition of a blockade
is the same as that of the Atsumi Traffic Simulator:

    road.lineWidth        := width / (linesToHead + linesToTail)
    road.blockedlines     := floor(road.block / road.lineWidth / 2 + 0.5)
    road.aliveLinesTo...  := max(0, road.linesTo... - road.blockedLines)
    road.isPassableToHead :- road.aliveLinesToHead >= 1
                          or movingObject.positionExtra < road.length / 2
    road.isPassableToTail :- road.aliveLinesToTail >= 1
                          or movingObject.positionExtra > road.length / 2

A blockade is assumed to be located at the midpoint of a road.  And on
a road on which there are more than one lane, the lanes are blocked
from outside to inside.  If the value isPassableTo...  is true while
there is a blockade on the road, it means several inside lanes are not
blocked or at least an agent has already bypassed the blockade.

(2-2) Traffic Jam

This program moves each agent keeping a safe distance to its forward
MovingObject, so a traffic jam may occur on roads where many
MovingObjects concentrate.  The minimum safe distance to a forward car
from a MovingObject is MIN_SAFE_DISTANCE_BETWEEN_CARS.  The minimum
safe distance to a forward civilian from a civilian is
MIN_SAFE_DISTANCE_BETWEEN_CIVILIAN (cf. Constants.java). Cars for
rescue are, however, prioritized in the traffic, so that these cars
ignore civilians even if they are on the front, assuming that
civilians are attentive to cars and give way to them.  If there is a
stopped car on the front, an agent changes to a lane of the same
direction, and then the agent can pass.  In addition, a civilian agent
changes its lane if there is a stopped civilian on the front.

(3) Others

(3-1) The maximum speed of a car is MAX_VELOCITY_PER_SEC [mm/sec]
(cf. Constants.java), so it is MAX_VELOCITY_PER_SEC * 60 [mm] that the
car can move in 1 cycle at most.

(3-2) Signal lights and restrictions on left or right turn are not
considered.  All signal lights are turned off in the disaster.  In the
Prototype Simulation System, cars in the map are only for polices,
ambulances and fire brigades.  No civilian agent drives a car.  So
agent needs not consider these restrictions.

(3-3) Central strip of a road, a side walk, number of cars passed and
number of humans passed are not considered.  The Atsumi Traffic
Simulator also ignores them.


V. For Advanced Agent Development

(1) Outline of the Simulation

One of the key concepts of this program is that this treats lanes
explicitly.  Each agent is on some lane, except when it is in a
building.  The traffic simulator deals with the following actions:

  - move on lanes
  - enter a building
  - load/unload an ambulance car with an injured person
  - pass a crosspoint
  - avoid a blockade
  - overtake a stopped car/civilian
  - turn
  - move to the outermost lane at the destination
  - get out from a building

The latter six actions cause a lane change.  An agent cannot know
which lane agents are on including itself, because the Prototype
Simulation System has not the lane property.  It is important,
however, that agents should be aware of lanes to avoid a traffic jam.

(2) Moving on Lanes

In the simulation, agents move on lanes and change lanes occasionally.
An agent moves on some fixed lane, but if there is an obstruction in
front of it, such as car, civilian or a blockade, it changes the lane
inwards if possible.

An agent keeps a velocity at which the agent will be able to stop if
it finds an obstruction in front of it.  The traffic simulator
calculates the velocity of an agent by solving the following equation
(1) about the acceleration a:

    dx  : distance to a forward object
    ma  : maximum acceleration
    msd : minimum safe distance to a forward object
    v   : velocity
    a   : acceleration
    safeDistance = (v + a)^2 / (2 * ma) + msd
    dx - safeDistance = v + a                  ... (1)

    NOTE: t = (v + a) / ma : a time which the agent needs to stop
          (v + a) / 2 * t  : a distance which the agent needs to stop

The acceleration of a car is limited, so it takes at least
ACCELERATING_SEC [sec] (cf. Constants.java) to accelerate from 0
[mm/sec] to MAX_VELOCITY_PER_SEC [mm/sec], and vice versa, that is,
for braking.  The acceleration of a civilian is not limited, because
civilians walk slowly.

An agent can stop immediately in a cycle.  That is, this program tries
to simulate the traffic at every second as smoothly as possible, but
it cannot cope with a sudden agent decision to stop at a location at
the beginning of the next cycle, so that an agent running at its top
speed till 59th second seems to be able to stop at 60th second
suddenly.  However, from the viewpoint of a minute-level resolution
observer, it can be understood that an agent which finds something
urgent makes full braking in a violent but physically admissible
manner.  This phenomenon rises from the resolution discrepancy between
the traffic simulator and the whole of the Prototype Simulation
System.

(3) Changing Lanes

An agent can change the lane if the target lane is safe.  A lane is
safe to change, if there is neither forward obstruction nor backward
moving agent that is nearer than the safe distance on the target lane.
An agent will wait until some lane becomes safe.

A car will stop at the moment when it is about to enter a crosspoint
whose crossing road is wider than or equal to the road on which the
car is now moving.  (If there are more than one car at a crosspoint of
roads of same width, which car starts first is randomly selected.)

(4) Route Plan and Actual Movement

To clarify the simulation, we illustrate the actual movement of an
agent according to its route plan.  Route plans are represented by the
following characters:

   B        | N    | R
  ----------+------+------
   Building | Node | Road

and, an actual movement is represented by the following characters:

   [ ]      | :    | ---> | +        | s          | m
  ----------+------+------+----------+------------+--------------------
   Building | Node | Lane | Blockade | self agent | other MovingObject

  case 1:
    route plan: {B}
      # The agent does nothing
      [s]        [s]
      before     after

  case 2:
    route plan: {... R}
    Or the agent did not submit an AK_MOVE command, but it is on a road
      # The agent moves to the outermost lane not to prevent other agents
      # from moving
      ------->       ---s--->
      ---s--->       ------->
      <-------       <-------
      <-------       <-------
       before         after

  case 3:
    route plan: {... N}
    Or the agent did not submit an AK_MOVE command, but it is on a node
      # The agent moves to the outermost lane not to prevent other agents
      # from moving
      ------->:       ------->s
      ------->s       ------->:
      <-------:       <-------:
      <-------:       <-------:
       before           after

  case 4:
    route plan: {B N}
      # The agent enters a lane at N
             [s]                   [ ]
              |                     |
      ------->:------->      ------->s------->
      <-------:<-------      <-------:<-------
           before                  after

  case 5:
    route plan: {B N B}
      # The agent changes to a lane at N, and then it enters
      # the destination B
             [s]                    [ ]                    [ ]
              |                      |                      |
      ------->:------->      ------->s------->      ------->:-------->
      <-------:<-------      <-------:<-------      <-------:<--------
              |                      |                      |
             [ ]                    [ ]                    [s]
           before                  after                more after

  case 6:
    route plan: {B N R} ({B R} is similar)
      # The agent changes to a lane at N on R
      [s]             [ ]
       |               |
       :------->       :s------>
       :<-------       :<-------
         before           after

  case 7:
    route plan: {... N B}
      # The agent enters B
             [ ]            [s]
              |              |
      ------->s      ------->:
      <-------:      <-------:
        before         after

  case 8:
    route plan: {... N R ...}
      case 8-1: ... -> N -> R -> ...
        case 8-1-1: The agent can go on straight from N to R
          # The agent goes on straight
          s------->       :---s--->
          before           after

        case 8-1-2: N is the crosspoint on R
          # The agent changes to a lane at N on R, and then it goes on
          # straight
           :------->       :s------>       :---s--->
          s:               :               :
            before           after         more after

        case 8-2:      ->    -> N  -> ...
                  ... <-  R <-    <-
        # The agent changes to an opposite lane at N on R, and then it
        # turns and goes on straight
        ------->s------->       ------->:------->       ------->:------->
        <-------:               <------s:               <---s---:
             before                    after                more after

  case 9:
    route plan: {... R N ...}
      case 9-1: ... -> R -> N -> ...
        case 9-1-1: The agent can go on straight from R to N
        # The agent goes on straight
        ---s--->:       ------->s
         before           after

        case 9-1-2: R is blocked
          case 9-1-2-1: The agent has already bypassed the blockade
          # The agent goes on straight
          ---+s-->:       ---+--->s:
           before           after

          case 9-1-2-2: The agent is on the front of a blockade
          # The agent changes to a lane it can proceed, and then it
          # goes on straight (The agent waits until some lane becomes
          # safe)
          --s+--->:       ---+--->:       ---+---> :
          ------->:       --s---->:       ------->s:
           before           after         more after

        case 9-1-3: The agent is on the front of a moving object
        # The agent changes to a lane it can proceed, and then it goes
        # on straight (The agent waits until some lane becomes safe)
        -s---m->:       -----m->:       -----m-> :
        ------->:       -s----->:       ------->s:
         before           after         more after

      case 9-2:      ->    -> R  -> ...
                ... <-  N <-    <-
      # The agent changes to an opposite lane at N, and then it turns
      # and goes on straight
      :-------s->       :--------->       : --------->
      :<---------       :<------s--       :s<---------
         before            after           more after

(5) Priorities of AK_MOVE, AK_LOAD and AK_UNLOAD

The traffic simulator deals with AK_MOVE commands at first, and then
it deals with AK_LOAD/UNLOAD commands if the target injured agent did
not submit an AK_MOVE command.

Before the Prototype Simulation System Ver.0.31, the misc simulator
deals with AK_LOAD/UNLOAD commands.  After the Ver.0.36, the traffic
simulator deals with these commands in order not to make the position,
the positionExtra and the positionHistory properties among
sub-simulators conflict.


VI. For Development and Maintenance

(1) The traffic simulator assumes that the properties concerning the
position of an agent:

  MovingObject.position
  MovingObject.positionExtra
  MovingObject.positionHistory

are determined only by the traffic simulator.

The structure of positionHistory property is the same as the Atsumi
Traffic Simulator; it is a list of node IDs.

(2) The user can change the setting of the traffic simulator by
editing the Constants.java file.  Constant variables in the file are
as follows.

  UNIT_SEC                        the simulation unit time

  MAX_VELOCITY_PER_SEC            the velocity of a car/civilian
  MAX_CIV_VELOCITY_PER_SEC

  ACCELERATING_SEC                the accelerating time up to maximum velocity

  MIN_SAFE_DISTANCE_BETWEEN_CARS  the safe distance between car/civilian
  MIN_SAFE_DISTANCE_BETWEEN_CIVILIANS

  DRIVING_DIRECTION_IS_LEFT       the driving direction

  CALCULATING_LIMIT_MILLI_SEC     the time limit for foreclosing calculation
                                  (cf. VI (3-3))

(3) Procession of the Simulation

The traffic simulator initializes all agent states by
  traffic.Simulator.Simulator(InetAddress kernelAddress, int kernelPort)
and then it repeats
  traffic.Simulator.simulate()

(3-1) The traffic.Simulator.simulate() method executes the following steps repeatedly.

  It receives AK-Commands               by traffic.io.receiveCommands()
  It deals with AK_MOVEs                by traffic.Simulator.move()
  It deals with AK_LOAD/UNLOADs         by traffic.loadUnload()
  It submits the simulation result      by traffic.io.sendUpdate()
  It receives other simulation results  by traffic.io.receiveUpdate()

(3-2) AK-Command Inspection

The traffic simulator inspects AK-Commands by
  traffic.WorldModel.parseCommands(int[] data)
If an AK-Command is invalid, it regards the agent did not submit an
AK-Command.

(3-3) Disposal of AK_MOVE

At the beginning of each cycle, the traffic simulator initializes each
agent's route plan and so on by
  traffic.object.MovingObject.initializeEveryCycle()
and then it simulates the movement of all moving agents at every
UNIT_SEC (cf. VI (3-2)) interval.  The simulation at every unit time
proceeds as follows.

  1. It decides whether it forecloses the simulation of the current
     cycle
    cf. Constants.CALCULATING_LIMIT_MILLI_SEC

  2. It sets obstructions and moving lanes of every moving object
    cf. traffic.object.MovingObject.setMotionlessObstructionAndMovingLaneList()

  3. It sets forward cars/civilians of every moving object
    cf. traffic.object.MovingObject.setMovingObstruction()

  4. It sorts agents by their obstruction
    cf. traffic.Simulator.sortByObstruction()

  5. It moves agents which have an obstructing forward car/civilian
    cf. traffic.Simulator.m_waitingNoChangeList
        traffic.Simulator.moveBeforeForwardMvObj(MovingObject MovingObject mv)

  6. It moves agents which encounters a blockade, changing their lanes
     or directions accordingly
    cf. traffic.Simulator.m_waitingMap
        traffic.Simulator.dispatch(ArrayList follows)

  7. It moves agents which are at their destination or an entrance of
     the destination building
    cf. traffic.Simulator.m_noWaitingList
        traffic.object.MovingObject.move()

  8. It moves agents which do not have an obstruction
    cf. traffic.Simulator.m_noWaitingNoChangeList
        traffic.object.MovingObject.move()

The first step is an unavoidable measure in order to send surely the
simulation result to the kernel every cycle.  Sometimes the traffic
simulator uses this measure when many agents move along long distance.

The traffic.Simulator.dispatch(ArrayList follows) method in the 6th
step restricts the number of agents that change lanes to only one
agent in a unit time.


VII. License of the Morimoto Traffic Simulator

(1) Neither the RoboCupRescue committee nor development staffs of this
program provide warranty.  Use the software at your own risk.

(2) Copyright of all program code and documentation included in source
or binary package of this program belongs to Takeshi Morimoto.

(3) You can use this program for research and/or education purpose
only, commercial use is not allowed.


VIII. Author

Takeshi MORIMOTO
Ikuo Takeuchi Laboratory
Department of Computer Science
The University of Electro-Communications

Additional information can be found in:
    http://ne.cs.uec.ac.jp/~morimoto/rescue/traffic/

Mail bug reports and suggestions to:
    morimoto@takopen.cs.uec.ac.jp
