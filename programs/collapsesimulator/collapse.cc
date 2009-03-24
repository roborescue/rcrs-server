/*
 Modified by Cameron Skinner
 May 2005: Converted to librescue
 November 2005: Code cleaned up and converted to Librescue::Simulator subclass
*/

#include "collapse.h"
#include "error.h"
#include <math.h>

CollapseSimulator::CollapseSimulator() {
}

CollapseSimulator::~CollapseSimulator() {
}

int CollapseSimulator::init(Config* config, ArgList& args) {
  Simulator::init(config,args);
  srand(config->getInt("misc_random_seed"));
  LOG_INFO("Misc random seed: %d",config->getInt("misc_random_seed"));
  m_galpolydata = config->mapfile("galpolydata.dat");
  return 0;
}

int CollapseSimulator::step(INT_32 time, const AgentCommandList& commands, ObjectSet& changed) {
  if (time==1) {
	Building* bPtr;
	long int accel = 0;
	long int answer = 0; //ÆÀ¤é¤ì¤¿ÅÝ²õÅÙ

	long int polyNum = 0;
	long int pointNum = 0;
	coordinates* polyPtr = NULL;
	polyPtr = getPoly(&polyNum, &pointNum, polyPtr);
	
	RescueObject* objPtr;
	ObjectSet::const_iterator oit = m_pool.objects().begin();
	for( ; oit != m_pool.objects().end(); oit++){
	  objPtr = *oit;
	  TypeId objectType = objPtr->type();
	  if (objectType==TYPE_BUILDING) {
		bPtr = dynamic_cast<Building*>(objPtr);
		accel = getAccel(bPtr->getX(), bPtr->getY(), polyNum, polyPtr);
		answer = collapse_check(bPtr, accel);
		bPtr->setBrokenness(answer,time);
		changed.insert(bPtr);
	  }
	}
  }
  return 0;
}
  

coordinates* CollapseSimulator::getPoly(long int *plyNum, long int *pntNum, coordinates *polyPtr){
  long int val1;  //¥Õ¥¡¥¤¥ë¤«¤éÆÉ¤ß¤³¤ó¤ÇÆþ¤ì¤ëÊÑ¿ô
  long int val2;
  FILE *fp;

  ///////////////////////////////////////
  //¥Õ¥¡¥¤¥ë¤¬¤Ê¤«¤Ã¤¿¤é¥¨¥é¡¼¥á¥Ã¥»¡¼¥¸
  const char* filename = m_galpolydata.c_str();
  if( (fp = fopen(filename, "r") ) == NULL){
    printf("cannot find file --- %s\n", filename);
    exit(1);
  }

  ////////////////////////////////////////////////////////////
  //¥Õ¥¡¥¤¥ë¤ÎºÇ½é¤Ëµ­½Ò¤µ¤ì¤Æ¤¤¤ë¡ÉÁ´ÅÀ¿ô¡É¤È¡ÉÁ´¥Ý¥ê¥´¥ó¿ô¡É
  //¤òÆÉ¤ß¹þ¤à
  fscanf(fp, "%ld,%ld", &val1, &val2);
  *pntNum = val1;
  *plyNum = val2;

  polyPtr = new coordinates[*pntNum];  //¥á¥â¥ê¤ÎÆ°Åª³ÎÊÝ
  coordinates *vfpolyPtr; //very first poly pointer
  vfpolyPtr = polyPtr;

  ///////////////////////////
  //¥Ç¡¼¥¿¤ò¹½Â¤ÂÎ¤ËÎ®¤·¹þ¤à

  int cnt = 0;
  for(cnt=1; cnt<=*pntNum; cnt++, ++polyPtr){ 
    fscanf(fp, "%ld, %ld", &val1, &val2);
    polyPtr->polyx=val1;
    polyPtr->polyy=val2;
  }

  polyPtr=vfpolyPtr; //polyPtr¤Î°ÌÃÖ¤òÌá¤·¤Æ¤ª¤¯


  fclose(fp);

  return (polyPtr);
}

int CollapseSimulator::collapse_check(Building* bPtr, int accel){
  double pga = (accel+1)*100 + 50;
  double lambda_a; //collapse
  double lambda_ha; //moderate¡Ácollapse
  double lambda_mp; //slight¡Ácollapse
  double zeta_a; //collapse
  double zeta_ha; //moderate¡Ácollapse
  double zeta_mp; //slight¡Ácollapse
  float m = ((float)rand()/RAND_MAX); //random number 0¡Á1

  if (bPtr->getAttributes() == WOOD){ //ÌÚÂ¤¤Î¾ì¹ç
    lambda_a = 6.883;
    lambda_ha = 5.683;
    lambda_mp = 4.045;
    zeta_a = 0.636;
    zeta_ha = 0.910;
    zeta_mp = 1.441;
    
    if (m <=  P(pga, lambda_a, zeta_a))
      return COLLAPSE;
    else if (m <= P(pga, lambda_ha, zeta_ha))
      return MODERATE;
    else if (m <= P(pga, lambda_mp, zeta_mp))
      return SLIGHT;
    else
      return NODAMAGE;
  }

  else if (bPtr->getAttributes() == S || bPtr->getAttributes() == RC){ //RC, S ¤Î¾ì¹ç
    lambda_a = 8.523;
    lambda_ha = 7.377;
    lambda_mp = 7.862;
    zeta_a = 1.067;
    zeta_ha = 1.314;
    zeta_mp = 6.897;
    
    if (m <=  P(pga, lambda_a, zeta_a))
      return COLLAPSE;
    else if (m <= P(pga, lambda_ha, zeta_ha))
      return MODERATE;
    else if (m <= P(pga, lambda_mp, zeta_mp))
      return SLIGHT;
    else
      return NODAMAGE;
  }
  return 0;
}

int CollapseSimulator::getAccel(long int tx, long int ty, long int polyNum, coordinates* polyPtr){
  int in_or_out; //Æâ³°È½Äê¤Î·ë²Ì
  long int j;  //¥Ý¥ê¥´¥ó¤ò¤Þ¤ï¤¹index
  long int polySize;  //¥Ý¥ê¥´¥ó¤Î¹½À®ÅÀ¿ô
  int gal; //¥Ý¥ê¥´¥ó¤Î»ý¤Ä²ÃÂ®ÅÙ
  int tgal = 0; //ÂÐ¾ÝÅÀ¤Î»ý¤Ä²ÃÂ®ÅÙ
  coordinates* fpolyPtr; //first poly pointer
 
  tx = tx;
  ty = ty;

  for(j=1;j<=polyNum;j++){
    
    fpolyPtr=polyPtr; //fpolyPtr¤ËvirtualÅÀ¤Î¥¢¥É¥ì¥¹¤ò°ì»þÅª¤ËÊÝÂ¸
    gal=fpolyPtr->polyx;  //virtualÅÀ¤«¤égal¤òÆÀ¤ë
    polySize=fpolyPtr->polyy; //virtualÅÀ¤«¤é¹½À®ÅÀ¿ô¤òÆÀ¤ë
    ++polyPtr; //polyPtr¤òvirtualÅÀ¤«¤éºÇ½é¤Î¹½À®ÅÀ¤Ø¤º¤é¤¹

    in_or_out=polygonIn(tx, ty, polyPtr, polySize); //Æâ³°È½Äê

    switch(in_or_out){
    case 1:
      tgal=gal; //¤Á¤Ê¤ß¤Ë¡¢ÂÐ¾ÎÅÀ¤¬¶­³¦Àþ¤Ë¾è¤Ã¤Æ¤¤¤ë¤È¤­¤Ï²ÃÂ®ÅÙ¤Ï¾å½ñ¤­¤µ¤ì¤ë
      break;
    case 0:
      break;
    } //switch

    polyPtr=fpolyPtr+polySize+1; //polyPtr¤Ë¼¡¤Î¥Ý¥ê¥´¥ó¤ÎvirtualÅÀ¤Î¥¢¥É¥ì¥¹¤òÀßÄê

  } //for

  return(tgal);
}

double CollapseSimulator::P(double pga, double lambda, double zeta){
  return ( Phi((log(pga)-lambda)/zeta) );
}

#define  SLICES  100  //number of slices for integral

double CollapseSimulator::Phi(double x){
  double z = 0; //the sigma part
  double y = 0; //the answer
  long int n = 0; //count up to SLICES
  
  for (n=0;n<=SLICES;n++){
    z += psi((double)n*x/(double)SLICES) + psi((double)(n+1)*x/(double)SLICES);
  }
  
  y = 0.5 + x*z/(2*(double)SLICES);
  return (y);
}

double CollapseSimulator::psi(double x){
  //printf ("psi = %f\n", exp(-pow(x, 2)/2)/sqrt(2*M_PI) );
  const double pi = 3.14159265358979323846;
  return ( exp(-1*pow(x, 2)/2)/sqrt(2*pi) );
}

judgePolygon CollapseSimulator::polygonIn(long int ptTx, long int ptTy, coordinates* polyPtr, long int polySize){
//»ØÄêÅÀ(ÂÐ¾Ýx,ÂÐ¾Ýy), ¸½ºß¤Î¥Ý¥ê¥´¥ó¤ÎºÇ½é¤ÎÅÀ¤Ø¤Î¥Ý¥¤¥ó¥¿, polygon¹½À®ÅÀ¿ô

  int xCnt; //crossCount È¾Ä¾Àþ¤È¤Î¸òÅÀ¿ô
  double dVal;  //¸òÅÀ¤ÎyºÂÉ¸
  long int a; //forÊ¸¤ò²ó¤¹Ê¸»ú
  long int ptB1x;  //ÀþÊ¬ÀèÃ¼xºÂÉ¸
  long int ptB1y;  //ÀþÊ¬ÀèÃ¼yºÂÉ¸
  long int ptB2x;  //ÀþÊ¬½ªÃ¼xºÂÉ¸
  long int ptB2y;  //ÀþÊ¬½ªÃ¼yºÂÉ¸

  ///////////
  //È½Äê¤¹¤ë
  for(a=0,xCnt=0;a<(polySize-1);a++){
    
    //ÂÐ¾ÝºÂÉ¸±ôÄ¾²¼¸þ¤ËÈ¾Ä¾ÀþÁÛÄê¤·¡¤ÀþÊ¬¤È¤Î¸òÅÀ¿ô¤ò¿ô¤¨¤ë¡¥
    ptB1x = polyPtr->polyx; // - 21950000;
    ptB1y = polyPtr->polyy; // - 2950000;
    ptB2x = (polyPtr+1)->polyx; // - 21950000;
    ptB2y = (polyPtr+1)->polyy; // - 2950000;
    
    ++polyPtr; //¹½À®ÅÀ¤Ø¤Î¥Ý¥¤¥ó¥¿¤Î¥¤¥ó¥¯¥ê¥á¥ó¥È
    
    //¸òº¹¤Î²ÄÇ½À­¤¬¤Ê¤¤»þ¤Ï¡¤skip
    if ( ptTy < ptB1y && ptTy < ptB2y ) {
      continue;
    }
    if ( (ptTx-ptB1x<0)&&(ptTx-ptB2x<0) || (ptTx-ptB1x>0)&&(ptTx-ptB2x>0) ){
      continue;
    }
    
    //Y¼´¤ËÊ¿¹Ô¤Ê¾ì¹ç¡¤Æ±°ìÅÀ¤Î½èÍý: ONLINE¢ª½ªÎ»¡¤¤½¤ì°Ê³°¢ªskip
    if ( (ptTx == ptB1x) && (ptTx == ptB2x) ){
      if ( (ptTy-ptB1y<=0)&&(ptTy-ptB2y>=0) || (ptTy-ptB1y>=0)&&(ptTy-ptB2y<=0) )
	return(IN_SIDE);  //ÀþÊ¬¾å¤ËÂÐ¾ÝºÂÉ¸Í­
      else{
	continue;
      }
    };
    
    //¸òÅÀ¤ÎxºÂÉ¸¤¬ÂÐ¾ÝÀþÊ¬¤Îº¸Ã¼¤ÎxºÂÉ¸¤Ë°ìÃ×¤¹¤ë»þ¤Ï¡¤skip
	long smaller = ptB1x < ptB2x? ptB1x : ptB2x;
    if ( ptTx == smaller ){
      continue;
    }
    
    //Í¿ÅÀ¤ÈÀþÊ¬¤Î¸òÅÀ¤ÎYºÂÉ¸·×»»
    dVal = (double)ptB1y + 
      (double)(( ptB2y - ptB1y )*( ptTx - ptB1x )) / (double)( ptB2x - ptB1x );
    if ( (double)ptTy < dVal ){
      continue;
    }
    else if ( (double)ptTy == dVal )
      return(IN_SIDE);  //ÀþÊ¬¾å¤ËÂÐ¾ÝºÂÉ¸Í­
    else
      xCnt++;  //¸òÅÀ¿ô¥¤¥ó¥¯¥ê¥á¥ó¥È
    
  } //for
  
  return((xCnt % 2 == 0) ? OUT_SIDE : IN_SIDE);	//¸òÅÀ¿ô¶ö¿ô=³°Â¦,´ñ¿ô=ÆâÂ¦
  
}; //polygonIn
