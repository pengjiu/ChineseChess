package com.pj.chess.evaluate;

import static com.pj.chess.ChessConstant.*;

import com.pj.chess.BitBoard;
import com.pj.chess.ChessConstant;
import com.pj.chess.Tools;
import com.pj.chess.chessmove.ChessQuiescMove;
import com.pj.chess.chessparam.ChessParam;

public class EvaluateComputeOther extends EvaluateCompute {
	
    //每个棋子机动性惩罚
	public   final int[] chessMobilityRewards  =new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	    //将车   马     炮
		13,2,2,8,8,1,1,0,0,0,0,0,0,0,0,0,
		13,2,2,8,8,1,1,0,0,0,0,0,0,0,0,0
	};
	//每个棋子最低机动性要求(低于此值要罚)
	public static  final int[] chessMinMobility  =new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  //将车   马     炮
		1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,
		1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0
	}; 
	
	public EvaluateComputeOther(ChessParam chessParam){
		this.chessParam=chessParam;
		init();
	}
	//保护分 主攻 主防 控点分
	int mainProtect=20,defenseProtect=9,controlPoint=2;
	int exposedGun=150; //空头炮罚分
	int incompleteScore=60;//防首方不全奖励
	int palacePointScore=20;//9宫格控制得分
	int score[]=new int[2];
	BitBoard[] bitBoard=new BitBoard[2];
	BitBoard[] bitBoardAttack=new BitBoard[2];
	public int evaluate(int play){
		score[REDPLAYSIGN]=chessParam.baseScore[REDPLAYSIGN];
		score[BLACKPLAYSIGN]=chessParam.baseScore[BLACKPLAYSIGN];
		if(true){
			return score[play]-score[1-play];
		}
		bitBoard[REDPLAYSIGN]= new BitBoard(chessParam.maskBoardPersonalChesses[REDPLAYSIGN]);
		bitBoard[BLACKPLAYSIGN]= new BitBoard(chessParam.maskBoardPersonalChesses[BLACKPLAYSIGN]); 
		bitBoardAttack =new BitBoard[]{new BitBoard(),new BitBoard()}; 
		int currplay=0;
		int[] kingMoveExtend=new int[2];
		int[] attackPalacePoint=new int[2];
		int[] attackPalaceChessNum=new int[2];
		BitBoard[] kingBitBoard = new BitBoard[2];
		kingBitBoard[REDPLAYSIGN]=new BitBoard(KingBitBoard[chessParam.allChess[chessPlay[REDPLAYSIGN]]]);
		kingBitBoard[BLACKPLAYSIGN]=new BitBoard(KingBitBoard[chessParam.allChess[chessPlay[BLACKPLAYSIGN]]]);
		int[] kingGetAttack=new int[90];
		
		//得到所有子力图
		for(int chess=16;chess<48;chess++){
			if(chessParam.allChess[chess]!=NOTHING){
				if(chess<32){currplay=BLACKPLAYSIGN;}else{currplay=REDPLAYSIGN;}
				BitBoard bAttack=chessAllMove(chessRoles[chess],chessParam.allChess[chess], currplay);
				bitBoardAttack[currplay].assignOr(bAttack);
				//有机动性参数
				if(chessMinMobility[chess]>0){ 
					int mobility = this.chessMobility(chessRoles[chess], chessParam.allChess[chess],bitBoard[currplay]);
					score[currplay]+=mobility*chessMobilityRewards[chess];
					if(chessRoles[chess]==REDKING){
						kingMoveExtend[REDPLAYSIGN]=mobility;
					 }else if(chessRoles[chess]==BLACKKING){
						kingMoveExtend[BLACKPLAYSIGN]=mobility;
					 }
					//攻击到9宫格
					BitBoard kingattack = BitBoard.assignAndToNew(kingBitBoard[1-currplay],bAttack);
					int attackSite=0;
					while((attackSite=kingattack.MSB(currplay))!=-1){
						kingGetAttack[attackSite]++;
//						attackPalacePoint[currplay]+=attackCount;
//						attackPalaceChessNum[currplay]+=1;
						kingattack.assignXor(MaskChesses[attackSite]);
					}
					
				}
			}
		} 
		int oppPlayTemp,oppKingSite,attackOnePalaceScore; 
		BitBoard mainAttackBitB =null,defenseChessBitB=null;
		for(int i=0;i<2;i++){
			oppPlayTemp=1-i;
			attackOnePalaceScore=0;
			oppKingSite = chessParam.allChess[chessPlay[oppPlayTemp]]; 
			int oppKingSiteRow=chessParam.boardBitRow[boardRow[oppKingSite]];
			int oppKingSiteCol=chessParam.boardBitCol[boardCol[oppKingSite]]; 
			mainAttackBitB = this.getMainAttackChessesBitBoad(i);
			defenseChessBitB = this.getDefenseChessesBitBoad(i);
			//保护
			score[i]+=BitBoard.assignAndToNew(mainAttackBitB,bitBoardAttack[i]).Count()*16;
			score[i]+=BitBoard.assignAndToNew(defenseChessBitB,bitBoardAttack[i]).Count()*8;
			//被攻击			
			score[i]-=BitBoard.assignAndToNew(mainAttackBitB,bitBoardAttack[oppPlayTemp]).Count()*18;
			score[i]-=BitBoard.assignAndToNew(defenseChessBitB,bitBoardAttack[oppPlayTemp]).Count()*9;
//			if(attackPalaceChessNum[i]>2){
				//对方将机动力
				switch(kingMoveExtend[oppPlayTemp]){
					case 0:
						attackOnePalaceScore=20;
					break;
					default:
						attackOnePalaceScore=12;
				}
//				attackPalacePoint[currplay]=BitBoard.assignAndToNew(AttackPalaceBitboard[i],bitBoardAttack[i]).Count();
//				attackPalacePoint[currplay]=BitBoard.assignAndToNew(kingBitBoard[oppPlayTemp],bitBoardAttack[i]).Count();
				
				if(this.exposedCannon(i, oppKingSite, oppKingSiteRow, oppKingSiteCol)!=-1){
					attackOnePalaceScore+=2;
				}
				if(kingMoveExtend[oppPlayTemp]==0 && this.bottomCannon(i, oppKingSite, oppKingSiteRow, oppKingSiteCol)!=-1){
					attackOnePalaceScore+=2;
				}
				if(kingMoveExtend[oppPlayTemp]==0  && this.restChariot(i, oppKingSite, oppKingSiteRow, oppKingSiteCol)!=-1){
					attackOnePalaceScore+=2;
				}
				int attackSite=0;
				while((attackSite=kingBitBoard[oppPlayTemp].MSB(oppPlayTemp))!=-1){
					if(kingGetAttack[attackSite]>0){
						score[i]+=kingGetAttack[attackSite]*attackOnePalaceScore;
					}
					kingBitBoard[oppPlayTemp].assignXor(MaskChesses[attackSite]);
				}
//				score[i]+=attackPalacePoint[i]*(attackPalaceChessNum[i]*attackOnePalaceScore);
//			}
			 
		}
		
		return score[play]-score[1-play];
	}
	//马
	public  final int blackKnightAttach[]={ 
				 -60 ,-35 ,-20,-40,-40,-40,-20,-35,-60,
				 -30 ,+20 ,+20,+30,-75,+30,+20,+20,-30,
				 -30 ,+20 ,+35,+25,+20,+25,+35,+20,-30,
				 -30 ,+30 ,+40,+45,+60,+45,+40,+30,-30,
				 -35 ,+60 ,+60,+60,+70,+60,+60,+60,-35,
				 
				 -30 ,+50,+65,+70,+80,+70,+65,+50,-30,
				 -40 ,+60,+70,+75,+80,+75,+70,+60,-40,
				 -40 ,+60,+75,+90,+95,+90,+75,+60,-40,
				 -40 ,+60,+90,+80,+40,+80,+90,+60,-40,
				 -50 ,+10,+40,+40,+10,+40,+40,+10,-50
		};
	//炮
	public  final  int blackGunAttach[]={
		 -50 ,-20,-20 ,0  ,0 ,0,-20,-20,-50,
		 -10 ,+30,+40,+60,+40,+60,+40,+30,-10, 
		 -10 ,+40,+40,+60,+75,+60,+40,+40,-10,
		 -10 ,+40,+30,+45,+65,+45,+30,+40,-10,
		 +20 ,+40,+40,+45,+60,+45,+40,+40,+20,
		 
		 -10 ,+30,+30,+40,+60,+40,+30,+30,-10,
		 -10 ,+30,+30,+40,+50,+40,+30,+30,-10,
		 -10 ,+60,+60,+30,0  ,+30,+60,+60,-10,
		 -10 ,+60,+60,+30,0  ,+30,+60,+60,-10,
		 +30 ,+70,+60,+30,-10,+30,+60,+70,+30
	};
	//车
	public  final  int blackChariotAttach[]={ 
			 -60,-10,-10,-10,-10,-10,-10,-10,-60,
			 -10,+10,+10,+30,-40,+30,+10,+10,-10,
			 -20,+15,+15,+30,+10,+30,+15,+15,-20,
			 -20,+30,+30,+30,+50,+30,+30,+30,-20,
			 -20,+70,+70,+85,+80,+85,+70,+70,-20,
			 
			 -20,+70,+70,+85,+80,+85,+70,+70,-20 ,
			 -20,+40,+40,+50,+70,+50,+40,+40,-20 ,
			 +20,+60,+60,+60,+70,+60,+60,+60,+20 ,
			 +20,+60,+60,+60,+100,+60,+60,+60,+20 ,
			 +20,+60,+60,+60,+60,+60,+60,+60,+10
	};
	   //卒
			public  final  int blackSoldierAttach[]={ 
				
				  0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,+10,0  ,0  ,0  ,0    
				 ,+20,0  ,+45,0  ,+35,0  ,+45,0  ,+20
				 ,+80,+100,+100 ,+100,+100,+100,+120,+100,+80
				 ,+100,+120,+150 ,+170,+170,+170,+150,+120,+100
				 ,+100,+150,+200,+240,+250,+240,+200,+150,+100
				 ,+100,+150,+200,+250,+300,+250,+200,+150,+100
				 ,+100,+100,+100,+100,+100,+100,+100,+100,+100
		};
			//象
		public  final  int ElephantAttch[]={ 
				 
				  0  ,0  ,15 ,0  ,0  ,0  ,15 ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,-10,0  ,0  ,0  ,40 ,0  ,0  ,0  ,-10
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,-10,0  ,0  ,0  ,-10,0  ,0  
				 
				 ,0  ,0  ,-10,0  ,0  ,0  ,-10,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,-10,0  ,0  ,0  ,40 ,0  ,0  ,0  ,-10
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,15 ,0  ,0  ,0  ,15 ,0  ,0  
				 
		};	
		//士
		public  final  int GuardAttach[]={  
			 
			  0  ,0  ,0  ,10  ,0  ,10  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,5 ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,10 ,0  ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,10 ,0  ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,5 ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,10  ,0  ,10  ,0  ,0  ,0    
	};	
		//王
		public  final  int kingAttach[]={ 
			  0  ,0  ,0  ,10 ,20 ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
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

 
	private  final BitBoard[]  AttackPalaceBitboard=new BitBoard[2];
//	private static final int attackControlPointScore=20;//,defenseControlPointScore=2;
	private void init(){
		int attackRedPalacePoint[]={ 
				  0  ,0  ,0  ,1  ,1  ,1  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,1  ,1  ,1  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,1  ,1  ,1  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
		   }; 
		int[] attackBlackPalacePoint=Tools.exchange(attackRedPalacePoint); 
	   //9宫格
	   AttackPalaceBitboard[REDPLAYSIGN]=new BitBoard(attackRedPalacePoint);
	   AttackPalaceBitboard[BLACKPLAYSIGN]=new BitBoard(attackBlackPalacePoint); 
	   
	}
} 
 


