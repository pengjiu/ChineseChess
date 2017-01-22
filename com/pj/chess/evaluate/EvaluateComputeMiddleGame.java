package com.pj.chess.evaluate;

import static com.pj.chess.ChessConstant.*;

import com.pj.chess.BitBoard;
import com.pj.chess.ChessConstant;
import com.pj.chess.Tools;
import com.pj.chess.chessmove.ChessQuiescMove;
import com.pj.chess.chessparam.ChessParam;

public class EvaluateComputeMiddleGame extends EvaluateCompute {
	//每个棋子机动性惩罚
	public   final int[] chessMobilityRewards  =new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	    //将车   马     炮
		50,5,5,12,12,2,2,0,0,0,0,0,0,0,0,0,
		50,5,5,12,12,2,2,0,0,0,0,0,0,0,0,0
	};
	//每个棋子最低机动性要求(低于此值要罚)
	public static  final int[] chessMinMobility  =new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  //将车   马     炮
		1,19,19,8,8,19,19,0,0,0,0,0,0,0,0,0,
		1,19,19,8,8,19,19,0,0,0,0,0,0,0,0,0
	}; 
	
	public EvaluateComputeMiddleGame(ChessParam chessParam){
		this.chessParam=chessParam; 
	} 
	int score[]=new int[2];
	BitBoard[] bitBoard=new BitBoard[2];
	BitBoard[] bitBoardMove=new BitBoard[2];
	int[][] attackPartition=new int[2][3];
	int[][] defensePartition=new int[2][3];
	public int evaluate(int play){ 
		score[REDPLAYSIGN]=chessParam.baseScore[REDPLAYSIGN];
		score[BLACKPLAYSIGN]=chessParam.baseScore[BLACKPLAYSIGN];
		bitBoard[REDPLAYSIGN]= new BitBoard(chessParam.maskBoardPersonalChesses[REDPLAYSIGN]);
		bitBoard[BLACKPLAYSIGN]= new BitBoard(chessParam.maskBoardPersonalChesses[BLACKPLAYSIGN]); 
		bitBoardMove[REDPLAYSIGN]=new BitBoard();bitBoardMove[BLACKPLAYSIGN]=new BitBoard();
		int currplay=0;
		int[][] partitionScore=new int[2][7]; //按区域计分
		boolean[] kingUnMove=new boolean[]{false,false}; 
		//根据目前存活棋子决定其区域分数
		this.dynamicCMPChessPartitionScore();
		//得到所有子力图
		for(int chess=16;chess<48;chess++){
			if(chessParam.allChess[chess]!=NOTHING){
				if(chess<32){
					currplay=BLACKPLAYSIGN;
				}else{
					currplay=REDPLAYSIGN;
				}

				
				BitBoard bAttack=chessAllMove(chessRoles[chess],chessParam.allChess[chess], currplay);
//				if(!BitBoard.assignAndToNew(bAttack, bb).isEmpty()){
					//区域分数运算
					compPartitionScore(currplay,chessParam.allChess[chess], chess, partitionScore[currplay]);					
//				}
				bitBoardMove[currplay].assignOr(bAttack);
				//有机动性参数
				if(chessMinMobility[chess]>0){ 
					int mobility = this.chessMobility(chessRoles[chess], chessParam.allChess[chess],bitBoard[currplay]);
					if(mobility<chessMinMobility[chess]){ 
						score[currplay]-=(chessMinMobility[chess]-mobility)*chessMobilityRewards[chess];
						 if(chessRoles[chess]==REDKING){
							 kingUnMove[REDPLAYSIGN]=true;
						 }else if(chessRoles[chess]==BLACKKING){
							 kingUnMove[BLACKPLAYSIGN]=true;
						 }
					}
				}
			}
		} 
		//格式化区域
		trimPartitionScore(partitionScore, attackPartition, defensePartition);
		
		int oppPlayTemp,oppKingSite,proMainS,proDefenseS,attMains,attDefenseS;
		BitBoard attacChessShape =null,defenseChessShape=null,oppAttackChessShape,oppDefeneseChessShape;
		proMainS=10;proDefenseS=6;
		attMains=18;attDefenseS=9;
		for(int i=0;i<2;i++){
			oppPlayTemp=1-i;
			attacChessShape = this.getMainAttackChessesBitBoad(i);
			defenseChessShape = this.getDefenseChessesBitBoad(i);
			oppAttackChessShape = this.getMainAttackChessesBitBoad(oppPlayTemp);
			oppDefeneseChessShape = this.getDefenseChessesBitBoad(oppPlayTemp);
 
			//保护
			score[i]+=BitBoard.assignAndToNew(bitBoardMove[i],attacChessShape).Count()*proMainS;
			score[i]+=BitBoard.assignAndToNew(bitBoardMove[i],defenseChessShape).Count()*proDefenseS;
			//攻击			
			score[i]+=BitBoard.assignAndToNew(bitBoardMove[i],oppAttackChessShape).Count()*attMains;
			score[i]+=BitBoard.assignAndToNew(bitBoardMove[i],oppDefeneseChessShape).Count()*attDefenseS;
			
			int gunNum = chessParam.getChessesNum(i,ChessConstant.GUN);
			//对方将所在位置			
			oppKingSite = chessParam.allChess[chessPlay[oppPlayTemp]]; 
			int row=chessParam.boardBitRow[boardRow[oppKingSite]];
			int col=chessParam.boardBitCol[boardCol[oppKingSite]]; 
			boolean weakness=false;
			//对手棋子数量
			int oppAllChessNum=chessParam.getAttackChessesNum(oppPlayTemp)+chessParam.getDefenseChessesNum(oppPlayTemp)-1;
			
			if(gunNum>0){
				//空头炮
				int gunSite ;
				if(oppAllChessNum>5 && (gunSite = this.exposedCannon(i, oppKingSite, row, col))!=-1){ 
					int extend=boardRow[oppKingSite]-boardRow[gunSite];
					extend+=boardCol[oppKingSite]-boardCol[gunSite];
					extend=(extend < 0) ? -extend : extend;
					score[i]+=extend*45;
					weakness=true;
				}
				//沉底炮
				gunSite=this.bottomCannon(i, oppKingSite, row, col);
				if(gunSite!=-1){
					int extend=boardRow[oppKingSite]-boardRow[gunSite];
					extend+=boardCol[oppKingSite]-boardCol[gunSite];
					extend=(extend < 0) ? -extend : extend;
					if(extend<=3){
						score[i]+=100;
						weakness=true;
					}
					
				}
			}
			//隔子车
			if(this.restChariot(i, oppKingSite, row, col)!=1){
				score[i]+=30; 
			}
			//对方将不能动并有底炮或隔子车消弱一圈护甲
			if(weakness){ 
				defensePartition[oppPlayTemp][RIGHTSITE]-=1;
				defensePartition[oppPlayTemp][LEFTSITE]-=1;
				defensePartition[oppPlayTemp][MIDSITE]-=1;
			}
			if(kingUnMove[oppPlayTemp]){
				int v=1;
				if(weakness){
					v=2;
				}
				defensePartition[oppPlayTemp][RIGHTSITE]-=v;
				defensePartition[oppPlayTemp][LEFTSITE]-=v;
				defensePartition[oppPlayTemp][MIDSITE]-=v;
			}
			
			//将不在中线
			switch(boardCol[oppKingSite]){
				case 3: //向左偏
					defensePartition[oppPlayTemp][LEFTSITE]-=1;
					break;
				case 5: //向右偏
					defensePartition[oppPlayTemp][RIGHTSITE]-=1;
					break;
				case 4 :
					//将不在底线
					switch(boardRow[oppKingSite]){
						case 1: //高
							defensePartition[oppPlayTemp][MIDSITE]-=1;
							break;
						case 2: //高
							defensePartition[oppPlayTemp][MIDSITE]-=1;
							break;
						case 8: //高
							defensePartition[oppPlayTemp][MIDSITE]-=1;
							break;
						case 7: //高
							defensePartition[oppPlayTemp][MIDSITE]-=1;
							break;
					}
			}
			
			
			
			//区域分数计算			
			if(attackPartition[i][LEFTSITE]>defensePartition[oppPlayTemp][LEFTSITE]){
				score[i]+=(attackPartition[i][LEFTSITE]-defensePartition[oppPlayTemp][LEFTSITE])*30;
			}
			if(attackPartition[i][RIGHTSITE]>defensePartition[oppPlayTemp][RIGHTSITE]){
				score[i]+=(attackPartition[i][RIGHTSITE]-defensePartition[oppPlayTemp][RIGHTSITE])*30;
			}
			if(attackPartition[i][MIDSITE]>defensePartition[oppPlayTemp][MIDSITE]){
				score[i]+=(attackPartition[i][MIDSITE]-defensePartition[oppPlayTemp][MIDSITE])*30;
			}
			 
			//数量
			int chariotNum = chessParam.getChessesNum(i,ChessConstant.CHARIOT);
			int knightNum = chessParam.getChessesNum(i,ChessConstant.KNIGHT);  
			//对手
			int opponentElephantNum = chessParam.getChessesNum(oppPlayTemp,ChessConstant.ELEPHANT);
			int opponentGuardNum = chessParam.getChessesNum(oppPlayTemp,ChessConstant.GUARD); 
			if(opponentElephantNum<2){
				 if(opponentGuardNum>=2 && gunNum>0){//缺相士全怕炮
					 score[i]+=60;
				 }
			}
			if(opponentGuardNum<2){
				 if(knightNum>0){//缺士怕马
					 score[i]+=60;
				 }
			}
			if(chariotNum>0 ){ //多兵种加分
				score[i]+=100;
			}
			if(knightNum>0){ //多兵种加分
				score[i]+=100;
			}
			if(gunNum>0){ //多兵种加分
				score[i]+=100;
			}
			 
		}
		
//		return score[play]-(score[1-play]+30);
		return score[play]-(score[1-play]);
	}
	//马
	public  final int blackKnightAttach[]={ 
				 -60 ,-36 ,-20,-20,-20,-20,-20,-36,-60,
				 -20 ,0   ,+15,+15,-70,+15,+15,0  ,-20,
				 -20 ,0   ,+25,+20,+20,+20,+25,0  ,-20,
				 -20 ,0   ,+20,+20,+56,+20,+20,0  ,-20,
				 -20 ,+45 ,+45,+50,+60,+50,+45,+45,-20,
				 
				 -20 ,+45,+60,+70,+70,+70,+60,+45,-20,
				 -20 ,+50,+60,+75,+75,+75,+60,+50,-20,
				 -20 ,+50,+75,+85,+85,+85,+75,+50,-20,
				 -20 ,+50,+80,+75,+40,+75,+80,+50,-20,
				 -60 ,+10,+20,+20,-20,+20,+20,+10,-60
		};
	//炮
	public  final  int blackGunAttach[]={
		 -30 ,0  ,+30,+40,+20,+40,+30,0  ,-30,
		 -20 ,+30,+40,+50,+40,+50,+40,+30,-20, 
		 -20 ,+30,+30,+65,+75,+65,+30,+30,-20,
		 -20 ,+30,+30,+40,+60,+40,+30,+30,-20,
		 -20 ,+30,+35,+45,+60,+45,+35,+30,-20,
		 
		 -20 ,+20,+20,+20,+51,+20,+20,+20,-20,
		 -20 ,+20,+20,+10,+50,+10,+20,+20,-20,
		 -20 ,+20,+20,0  ,0  ,0  ,+20,+20,-20,
		 -20 ,+20,+20,0  ,0  ,0  ,+20,+20,-20,
		 -30 ,+40,+30,+10,-10,+10,+30,+40,-30
	};
	//车
	public  final  int blackChariotAttach[]={ 
			 -60,-10 , 0 ,+20,-10,+20, 0 ,-10,-60,
			 -10,+10,+10,+30,-40,+30,+10,+10,-10,
			 -20,+15,+15,+30,+10,+30,+15,+15,-20,
			 -20,+30,+30,+40,+45,+40,+30,+30,-20,
			 -10,+65,+65,+85,+75,+85,+65,+65,-10,
			 
			 -10,+60,+60,+80,+70,+80,+60,+60,-10 ,
			 -20,+40,+40,+50,+50,+50,+40,+40,-20 ,
			 -20,+40,+40,+50,+50,+50,+40,+40,-20 ,
			 -20,+40,+40,+60,+90,+60,+40,+40,-20 ,
			 -30,+20,+20,+20,+20,+20,+20,+20,-30
	};
	   //卒
			public  final  int blackSoldierAttach[]={ 
				
				  0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,+10,0  ,0  ,0  ,0    
				 ,+20,0  ,+45,0  ,+35,0  ,+45,0  ,+20
				 ,+80,+100,+110 ,+110,+110,+110,+110,+100,+80
				 ,+100,+120,+140 ,+160,+160,+160,+140,+120,+100
				 ,+100,+150,+190,+220,+220,+220,+190,+150,+100
				 ,+100,+150,+200,+250,+280,+250,+200,+150,+100
				 ,+100,+100,+100,+100,+100,+100,+100,+100,+100
		};
			//象
		public  final  int ElephantAttch[]={ 
				 
				  0  ,0  ,10 ,0  ,0  ,0  ,10 ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,-10,0  ,0  ,0  ,30 ,0  ,0  ,0  ,-10
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,-10,0  ,0  ,0  ,30 ,0  ,0  ,0  ,-10
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,10 ,0  ,0  ,0  ,10 ,0  ,0  
				 
		};	
		//士
		public  final  int GuardAttach[]={  
			 
			  0  ,0  ,0  ,0  ,0  ,0 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,25 ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,10 ,0  ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,10 ,0  ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,25 ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
	};	
		//王
		public  final  int kingAttach[]={ 
			  0  ,0  ,0  ,10 ,20 ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,-45,-50,-45,0  ,0  ,0  
			 ,0  ,0  ,0  ,-80,-90,-80,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,-80,-90,-80,0  ,0  ,0      
			 ,0  ,0  ,0  ,-45,-50,-45,0  ,0  ,0      
			 ,0  ,0  ,0  ,10 ,20 ,10 ,0  ,0  ,0  
	};	
	public   final int[] redSoldierAttach=Tools.exchange(blackSoldierAttach);
	public   final int[] redKnightAttach=Tools.exchange(blackKnightAttach);
	public   final int[] redChariotAttach=Tools.exchange(blackChariotAttach);
	public   final int[] redGunAttach=Tools.exchange(blackGunAttach); 
	
	public  final int[][] knightAttach=new int[][]{blackKnightAttach,redKnightAttach};
	public  final int[][] chariotAttach=new int[][]{blackChariotAttach,redChariotAttach};
	public  final int[][] soldierAttach=new int[][]{blackSoldierAttach,redSoldierAttach};
	public  final int[][] gunAttach=new int[][]{blackGunAttach,redGunAttach};
	public  final int[][] kingsAttach=new int[][]{kingAttach,kingAttach};
	public  final int[][] ElephantsAttch=new int[][]{ElephantAttch,ElephantAttch};
	public  final int[][] GuardsAttach=new int[][]{GuardAttach,GuardAttach};
	
	 
	public  final int[][] chessSiteScoreByRole=new int[][]{{},
		    redSoldierAttach,GuardAttach,ElephantAttch,redGunAttach,redKnightAttach,redChariotAttach,kingAttach,
		    blackSoldierAttach,GuardAttach,ElephantAttch,blackGunAttach,blackKnightAttach,blackChariotAttach,kingAttach
	};
	
	/*
	 *附加分 
	 */ 
	public  int chessAttachScore(int chessRole, int chessSite) {
		return chessSiteScoreByRole[chessRole][chessSite];
	}	
} 
 


