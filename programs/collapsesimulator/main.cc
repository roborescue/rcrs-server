/*
 Modified by Cameron Skinner
 May 2005: Converted to librescue
 November 2005: Code cleaned up and converted to Librescue::Simulator subclass
*/

#include "collapse.h"

// Librescue includes
#include "container.h"

using namespace Librescue;

int main(int argc, char** argv)
{
  Container container(argc,argv);
  CollapseSimulator sim;
  container.addSimulator(&sim);
  container.run();
  return 0;
}

