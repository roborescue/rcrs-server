/*
 Modified by Cameron Skinner
 May 2005: Converted to librescue
 November 2005: Code cleaned up and converted to Librescue::Simulator subclass
*/

#ifndef COLLAPSE_H
#define COLLAPSE_H

#include <vector>
#include "objects.h"
#include "simulator.h"
#include "args.h"

using namespace Librescue;

#define COLLAPSE 100
#define MODERATE 50
#define SLIGHT 25
#define NODAMAGE 0

enum bAttributes{
  WOOD,
  S,
  RC
};

enum accellerations{
  AC100,
  AC200,
  AC300,
  AC400,
  AC500,
  AC600,
  AC700,
  AC800,
  AC900
};

typedef std::vector<Building*> Buildings;
  
////////////////
//polygon stuff
struct coordinates{
  long int polyx;
  long int polyy;
};

enum judgePolygon{
  OUT_SIDE,
  IN_SIDE
};

class CollapseSimulator : public Simulator {
 private:
  coordinates* getPoly(long int *plyNum, long int *pntNum, coordinates *polyPtr);
  int collapse_check(Building* bPtr, int accel);
  int getAccel(long int tx, long int ty, long int polyNum, coordinates* polyPtr);
  double P(double pga, double lambda, double zeta);
  double Phi(double x);
  double psi(double x);
  judgePolygon polygonIn(long int ptTx, long int ptTy, coordinates* polyPtr, long int polySize);

  std::string m_galpolydata;

 public:
  CollapseSimulator();
  virtual ~CollapseSimulator();

  virtual int init(Config* config, ArgList& args);
  virtual int step(INT_32 time, const AgentCommandList& commands, ObjectSet& changed);
};

#endif
