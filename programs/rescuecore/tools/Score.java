/*
 * Last change: $Date: 2005/02/18 03:34:34 $
 * $Revision: 1.5 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore.tools;

import java.io.*;
import java.util.*;
import rescuecore.*;
import rescuecore.objects.*;
import rescuecore.log.*;

/**
   This class will calculate the score of a particular log file
 */
public class Score {
    private final static String HEADER = "RoboCup-Rescue Prototype Log 00\0";

    private final static String SILENT_FLAG = "--silent";
	private final static String JUST_SCORE_FLAG = "--just-score";
	private final static String KILL_FLAG = "--no-kill-civilians";

    private static boolean silent;
	private static boolean justScore;
	private static boolean killCiviliansAtEnd;

    private final static int VERBOSITY_NONE = 0;
    private final static int VERBOSITY_SOME = 1;
    private final static int VERBOSITY_LOTS = 2;

    private final static int MODE_MAXIMUM = 0;
    private final static int MODE_MINIMUM = 1;
    private final static int MODE_AVERAGE = 2;
    private final static int MODE_VARIANCE = 3;
    private final static int MODE_STANDARD_DEVIATION = 4;

    public static void main(String[] args) {
		silent = false;
		justScore = false;
		killCiviliansAtEnd = true;
		if (args.length==0) {
			printUsage();
			return;
		}
		List<String> fileNames = new ArrayList<String>();
		for (int i=0;i<args.length;++i) {
			if (args[i].equalsIgnoreCase(SILENT_FLAG)) {
				silent = true;
			}
			else if (args[i].equalsIgnoreCase(JUST_SCORE_FLAG)) {
				justScore = true;
			}
			else if (args[i].equalsIgnoreCase(KILL_FLAG)) {
				killCiviliansAtEnd = false;
			}
			else {
				fileNames.add(args[i]);
			}
		}
		if (fileNames.size()==0) {
			printUsage();
			return;
		}
		List<LogScore> allScores = new ArrayList<LogScore>();
		for (String next : fileNames) {
			try {
				if (!silent) System.err.println("Reading log "+next);
				LogScore score = calculateScore(next);
				if (score!=null) allScores.add(score);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		LogScore[] scores = new LogScore[allScores.size()];
		allScores.toArray(scores);
		if (justScore || scores.length==1) {
			for (int i=0;i<scores.length;++i) System.out.println(scores[i].score);
		}
		else {
			LogScore max = new LogScore("Highest",scores,MODE_MAXIMUM);
			LogScore min = new LogScore("Lowest",scores,MODE_MINIMUM);
			LogScore average = new LogScore("Average",scores,MODE_AVERAGE);
			LogScore variance = new LogScore("Variance",scores,MODE_VARIANCE);
			LogScore sd = new LogScore("Standard deviation",scores,MODE_STANDARD_DEVIATION);
			System.out.println("Run\tBuilding area left\tBuilding area total\tHP left\tHP total\tCivilians alive\tTotal civilians\tAgents alive\tTotal agents\tScore from HP\tScore from buildings\tOverall score");
			for (int i=0;i<scores.length;++i) {
				scores[i].write();
			}
			System.out.println();
			if (scores.length>1) {
				System.out.println("\tBuilding area left\tBuilding area total\tHP left\tHP total\tCivilians alive\tTotal civilians\tAgents alive\tTotal agents\tScore from HP\tScore from buildings\tOverall score");
				max.write();
				min.write();
				average.write();
				variance.write();
				sd.write();
			}
		}
    }

    private static LogScore calculateScore(String filename) throws IOException, InvalidLogException {
		Log log = Log.generateLog(filename);
		return new LogScore(filename,log.getMemory(log.getMaxTimestep()),killCiviliansAtEnd);
    }

    private static void printUsage() {
		System.out.println("Usage: Score [options] <log files>");
		System.out.println("Options");
		System.out.println("=======");
		System.out.println(SILENT_FLAG+"\tSilent mode. No progress indicators will be emitted.");
		System.out.println(JUST_SCORE_FLAG+"\tDon't calculate any statistics about the scores.");
		System.out.println(KILL_FLAG+"\tDon't consider buried or damaged civilians to be dead.");
    }

    private static double calculate(double[] values, int mode) {
		double result, average;
		switch (mode) {
		case MODE_MAXIMUM:
			result = values[0];
			for (int i=0;i<values.length;++i) result = Math.max(result,values[i]);
			return result;
		case MODE_MINIMUM:
			result = values[0];
			for (int i=0;i<values.length;++i) result = Math.min(result,values[i]);
			return result;
		case MODE_AVERAGE:
			result = 0;
			for (int i=0;i<values.length;++i) result += values[i];
			return result/values.length;
		case MODE_VARIANCE:
			average = 0;
			for (int i=0;i<values.length;++i) average += values[i];
			average/=values.length;
			result = 0;
			for (int i=0;i<values.length;++i) result += (values[i]-average)*(values[i]-average);
			return result/(values.length-1);
		case MODE_STANDARD_DEVIATION:
			average = 0;
			for (int i=0;i<values.length;++i) average += values[i];
			average/=values.length;
			result = 0;
			for (int i=0;i<values.length;++i) result += (values[i]-average)*(values[i]-average);
			return Math.sqrt(result/(values.length-1));
		}
		throw new RuntimeException("Unknown mode: "+mode);
    }

    private static class LogScore {
		String name;
		double score, HPscore, buildingScore, areaMax, areaLeft, hpMax, hpLeft, civilians, civiliansAlive, agents, agentsAlive;

		public LogScore(String name, LogScore[] scores, int mode) {
			this.name = name;
			double[][] values = new double[11][scores.length];
			for (int i=0;i<scores.length;++i) {
				values[0][i] = scores[i].score;
				values[1][i] = scores[i].HPscore;
				values[2][i] = scores[i].buildingScore;
				values[3][i] = scores[i].areaMax;
				values[4][i] = scores[i].areaLeft;
				values[5][i] = scores[i].hpMax;
				values[6][i] = scores[i].hpLeft;
				values[7][i] = scores[i].civilians;
				values[8][i] = scores[i].civiliansAlive;
				values[9][i] = scores[i].agents;
				values[10][i] = scores[i].agentsAlive;
			}
			score = calculate(values[0],mode);
			HPscore = calculate(values[1],mode);
			buildingScore = calculate(values[2],mode);
			areaMax = calculate(values[3],mode);
			areaLeft = calculate(values[4],mode);
			hpMax = calculate(values[5],mode);
			hpLeft = calculate(values[6],mode);
			civilians = calculate(values[7],mode);
			civiliansAlive = calculate(values[8],mode);
			agents = calculate(values[9],mode);
			agentsAlive = calculate(values[10],mode);
		}

		public LogScore(String name, Memory state, boolean killCivilians) {
			this.name = name;
			Collection<RescueObject> allBuildings = state.getObjectsOfType(RescueConstants.TYPE_BUILDING,RescueConstants.TYPE_REFUGE,RescueConstants.TYPE_FIRE_STATION,RescueConstants.TYPE_POLICE_OFFICE,RescueConstants.TYPE_AMBULANCE_CENTER);
			Collection<RescueObject> allAgents = state.getObjectsOfType(RescueConstants.TYPE_CIVILIAN,RescueConstants.TYPE_FIRE_BRIGADE,RescueConstants.TYPE_POLICE_FORCE,RescueConstants.TYPE_AMBULANCE_TEAM);
			areaMax = 0;
			areaLeft = 0;
			hpMax = 0;
			hpLeft = 0;
			civilians = 0;
			civiliansAlive = 0;
			agents = allAgents.size();
			agentsAlive = 0;
			//	    System.out.println(allBuildings.length+" buildings");
			for (RescueObject b : allBuildings) {
				Building next = (Building)b;
				double area = next.getTotalArea();
				areaMax += area;
				//		System.out.println(next.toLongString());
				//		System.out.println("Next building area, fieryness: "+area+", "+next.getFieryness());
				switch (next.getFieryness()) {
				case RescueConstants.FIERYNESS_NOT_BURNT:
					areaLeft += area;
					break;
				case RescueConstants.FIERYNESS_HEATING:
				case RescueConstants.FIERYNESS_SLIGHTLY_BURNT:
				case RescueConstants.FIERYNESS_WATER_DAMAGE:
					areaLeft += area*2.0/3.0;
					break;
				case RescueConstants.FIERYNESS_BURNING:
				case RescueConstants.FIERYNESS_MODERATELY_BURNT:
					areaLeft += area/3.0;

				case RescueConstants.FIERYNESS_VERY_BURNT:
				case RescueConstants.FIERYNESS_INFERNO:
				case RescueConstants.FIERYNESS_BURNT_OUT:
					break;
				}
				//		System.out.println("New total area, area left: "+areaMax+", "+areaLeft);
			}
			for (RescueObject a : allAgents) {
				Humanoid next = (Humanoid)a;
				if (killCivilians) {
					if (next.isCivilian() && (next.isDamaged() || next.isBuried())) next.setHP(0,3000,null);
				}
				if (next.isAlive()) ++agentsAlive;
				if (next.isCivilian()) {
					++civilians;
					if (next.isAlive()) ++civiliansAlive;
				}
				hpMax += RescueConstants.MAX_HP;
				hpLeft += next.getHP();
			}
			HPscore = agentsAlive + (hpLeft/hpMax);
			buildingScore = Math.sqrt(areaLeft/areaMax);
			score = HPscore * buildingScore;
		}

		public void write() {
			System.out.print(name);
			System.out.print("\t");
			System.out.print(areaLeft);
			System.out.print("\t");
			System.out.print(areaMax);
			System.out.print("\t");
			System.out.print(hpLeft);
			System.out.print("\t");
			System.out.print(hpMax);
			System.out.print("\t");
			System.out.print(civiliansAlive);
			System.out.print("\t");
			System.out.print(civilians);
			System.out.print("\t");
			System.out.print(agentsAlive);
			System.out.print("\t");
			System.out.print(agents);
			System.out.print("\t");
			System.out.print(HPscore);
			System.out.print("\t");
			System.out.print(buildingScore);
			System.out.print("\t");
			System.out.println(score);
		}
    }
}
