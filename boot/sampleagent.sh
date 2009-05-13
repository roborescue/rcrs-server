#! /bin/sh
DIR=`dirname $0`/../src/
java -Xmx256m -cp $DIR/ rescuecore.Launch localhost 7000 "0 rescuecore.sample.SampleFireBrigade" "0 rescuecore.sample.SamplePoliceForce" "0 rescuecore.sample.SampleAmbulanceTeam" "0 rescuecore.DummyAgent"