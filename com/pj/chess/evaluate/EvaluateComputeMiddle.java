package com.pj.chess.evaluate;

import static com.pj.chess.ChessConstant.*;

import com.pj.chess.BitBoard;
import com.pj.chess.ChessConstant;
import com.pj.chess.Tools;
import com.pj.chess.chessmove.ChessQuiescMove;
import com.pj.chess.chessparam.ChessParam;

public class EvaluateComputeMiddle extends EvaluateCompute {
	
    //每个棋子机动性惩罚
	public static  final int[] chessMobilityRewards  =new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	    //将车   马     炮
		50,10,10,20,20,10,10,0,0,0,0,0,0,0,0,0,
		50,10,10,20,20,10,10,0,0,0,0,0,0,0,0,0
	};
	//每个棋子最低机动性要求(低于此值要罚)
	public static  final int[] chessMinMobility  =new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	  //将车   马     炮
		1,6,6,4,4,5,5,0,0,0,0,0,0,0,0,0,
		1,6,6,4,4,5,5,0,0,0,0,0,0,0,0,0
	}; 
	
	public EvaluateComputeMiddle(ChessParam chessParam){
		this.chessParam=chessParam;
		init();
	}
	//保护分 主攻 主防 控点分
	int mainProtect=16,defenseProtect=8,controlPoint=2;
	int exposedGun=80; //空头炮罚分
	int incompleteScore=50;//防首方不全奖励
	int palacePointScore=20;//9宫格控制得分
	int score[]=new int[2];
	BitBoard[] bitBoard=new BitBoard[2];
	BitBoard[] bitBoardAttack=new BitBoard[2];
	public int evaluate(int play){
		score[REDPLAYSIGN]=chessParam.baseScore[REDPLAYSIGN];
		score[BLACKPLAYSIGN]=chessParam.baseScore[BLACKPLAYSIGN];
//		if(true){
//			return score[play]-score[1-play];
//		}
		int opponentPlay = 1-play;
		bitBoard[REDPLAYSIGN]= new BitBoard(chessParam.maskBoardPersonalChesses[REDPLAYSIGN]);
		bitBoard[BLACKPLAYSIGN]= new BitBoard(chessParam.maskBoardPersonalChesses[BLACKPLAYSIGN]); 
		chessParam.getRoleIndexByPlayRole(REDPLAYSIGN,CHARIOT);
		chessParam.getRoleIndexByPlayRole(REDPLAYSIGN,CHARIOT);
		bitBoardAttack[0]=new BitBoard();bitBoardAttack[1]=new BitBoard();
		//得到所有子力图
		for(int chess=16;chess<48;chess++){
			if(chessParam.allChess[chess]!=NOTHING){
				if(chess<32){
					BitBoard bAttack=chessAllMove(chessRoles[chess],chessParam.allChess[chess], BLACKPLAYSIGN);
					bitBoardAttack[BLACKPLAYSIGN].assignOr(bAttack);
					//有机动性参数
					if(chessMinMobility[chess]>0){ 
						int mobility = this.chessMobility(chessRoles[chess], chessParam.allChess[chess],bitBoard[BLACKPLAYSIGN]);
						//<最低机动性要罚分
						if(mobility<chessMinMobility[chess]){
							score[BLACKPLAYSIGN]-=(chessMinMobility[chess]-mobility)*chessMobilityRewards[chess];
						}
					}
				}else{
					BitBoard bAttack=chessAllMove(chessRoles[chess],chessParam.allChess[chess], REDPLAYSIGN);
					bitBoardAttack[REDPLAYSIGN].assignOr(bAttack);
					//有机动性参数
					if(chessMinMobility[chess]>0){
						int mobility = this.chessMobility(chessRoles[chess], chessParam.allChess[chess],bitBoard[REDPLAYSIGN]);
						//<最低机动性要罚分
						if(mobility<chessMinMobility[chess]){
							score[REDPLAYSIGN]-=(chessMinMobility[chess]-mobility)*chessMobilityRewards[chess];
						}
					}
				}
			}
		} 
		if(true){
		return score[play]-score[1-play];
	}
		int space,oppPlayTemp,oppKingSite,playBoundNum,oppPlayBoundNum;;
		int kingSite;
		BitBoard mainAttackBitB =null,defenseChessBitB=null,kingMoveBitBoad=null,oppMainAttackBitB;
		for(int i=0;i<2;i++){
			oppPlayTemp=1-i;
			mainAttackBitB = this.getMainAttackChessesBitBoad(i);
			defenseChessBitB = this.getDefenseChessesBitBoad(i);
			oppMainAttackBitB = this.getMainAttackChessesBitBoad(oppPlayTemp); 
			//保护
			score[i]+=BitBoard.assignAndToNew(mainAttackBitB,bitBoardAttack[i]).Count()*(mainProtect-(mainProtect/3));
			score[i]+=BitBoard.assignAndToNew(defenseChessBitB,bitBoardAttack[i]).Count()*(defenseProtect-(mainProtect/3));
			//被攻击			
			score[i]-=BitBoard.assignAndToNew(mainAttackBitB,bitBoardAttack[1-i]).Count()*mainProtect;
			score[i]-=BitBoard.assignAndToNew(defenseChessBitB,bitBoardAttack[1-i]).Count()*defenseProtect;
			//控点
//			score[i]+=bitBoardAttack[i].Count()*controlPoint;
			score[i]+=BitBoard.assignAndToNew(AttackCenterControlPoint[i],bitBoardAttack[i]).Count()*controlPoint;
			
			//左
			playBoundNum=BitBoard.assignAndToNew(AttackDirectionControlPoint[i][0],mainAttackBitB).Count();
			if(playBoundNum>=3){
				oppPlayBoundNum=BitBoard.assignAndToNew(AttackDirectionControlPoint[i][0],oppMainAttackBitB).Count();
				score[i]+=(playBoundNum-oppPlayBoundNum)*70;
			}
			//右
			playBoundNum=BitBoard.assignAndToNew(AttackDirectionControlPoint[i][1],mainAttackBitB).Count();
			if(playBoundNum>=3){
				oppPlayBoundNum=BitBoard.assignAndToNew(AttackDirectionControlPoint[i][1],oppMainAttackBitB).Count();
				score[i]+=(playBoundNum-oppPlayBoundNum)*70;
			}
			 
			//数量
			int chariotNum = chessParam.getChessesNum(i,ChessConstant.CHARIOT);
			int knightNum = chessParam.getChessesNum(i,ChessConstant.KNIGHT);
			int gunNum = chessParam.getChessesNum(i,ChessConstant.GUN);
			int elephantNum = chessParam.getChessesNum(i,ChessConstant.ELEPHANT);
			int guardNum = chessParam.getChessesNum(i,ChessConstant.GUARD);
			int soldierNum = chessParam.getChessesNum(i,ChessConstant.SOLDIER);
			//对手
			int opponentElephantNum = chessParam.getChessesNum(oppPlayTemp,ChessConstant.ELEPHANT);
			int opponentGuardNum = chessParam.getChessesNum(oppPlayTemp,ChessConstant.GUARD); 
			

			
			if(gunNum>0){
//				特殊情况处理(当头炮)
				kingSite = chessParam.allChess[chessPlay[i]]; 
				int row=chessParam.boardBitRow[boardRow[kingSite]];
				int col=chessParam.boardBitCol[boardCol[kingSite]]; 
				BitBoard bitBoard = BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[kingSite][row], ChariotBitBoardOfAttackCol[kingSite][col]);
				bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.GUN));
				if(!bitBoard.isEmpty()){ 
					score[i]-=exposedGun;
				}
			 }
			if(opponentElephantNum<2){
				 if(opponentGuardNum>=2 && gunNum>0){//缺相士全怕炮
					 score[i]+=incompleteScore;
				 }
			}
			if(opponentGuardNum<2){
				 if(knightNum>0){//缺士怕马
					 score[i]+=incompleteScore;
				 }else if(chariotNum==2){ //缺士怕双车
					 score[i]+=incompleteScore;
				 }
			}
			if(chariotNum>0 ){ //多兵种加分
				score[i]+=incompleteScore;
			}
			if(knightNum>0){ //多兵种加分
				score[i]+=incompleteScore;
			}
			if(gunNum>0){ //多兵种加分
				score[i]+=incompleteScore;
			}
		}
 
		return score[play]-score[1-play];
	}
	//马
	public  final int blackKnightAttach[]={ 
				 -60 ,-35 ,-20,-20,-20,-20,-20,-35,-60,
				 -35 ,0   ,+20,+20,-70,+20,+20,0  ,-35,
				 -35 ,0   ,+20,+20,+20,+20,+20,0  ,-35,
				 -35 ,0   ,+20,+20,+56,+20,+20,0  ,-35,
				 -35 ,+40 ,+40,+50,+60,+50,+40,+40,-35,
				 
				 -30 ,+45,+60,+70,+70,+70,+60,+45,-30,
				 -30 ,+50,+60,+75,+75,+75,+60,+50,-30,
				 -30 ,+50,+80,+90,+90,+90,+80,+50,-30,
				 -30 ,+50,+90,+80,+40,+80,+90,+50,-30,
				 -60 ,+10,+20,+20,-20,+20,+20,+10,-60
		};
	//炮
	public  final  int blackGunAttach[]={
		 -50 ,-20,-20,-20,-20,-20,-20,-20,-50,
		 -20 ,+30,+40,+50,+30,+50,+40,+30,-20, 
		 -20 ,+30,+40,+50,+60,+50,+40,+30,-20,
		 -20 ,+30,+40,+40,+60,+40,+40,+30,-20,
		 -20 ,+30,+45,+45,+60,+45,+45,+30,-20,
		 
		 -20 ,+20,+20,+20,+51,+20,+20,+20,-20,
		 -20 ,+20,+20,+10,+50,+10,+20,+20,-20,
		 -20 ,+20,+20,0  ,0  ,0  ,+20,+20,-20,
		 -20 ,+20,+20,0  ,0  ,0  ,+20,+20,-20,
		 -30 ,+50,+30,+10,-10,+10,+30,+50,-30
	};
	//车
	public  final  int blackChariotAttach[]={ 
			 -60,-20,-20,-20,-20,-20,-20,-20,-60,
			 -20,+10,+10,+30,-40,+30,+10,+10,-20,
			 -20,+15,+15,+30,+10,+30,+15,+15,-20,
			 -20,+30,+30,+30,+40,+30,+30,+30,-20,
			 -20,+50,+50,+80,+60,+80,+50,+50,-20,
			 
			 -20,+50,+50,+80,+60,+80,+50,+50,-20 ,
			 -20,+40,+40,+50,+50,+50,+40,+40,-20 ,
			 -20,+40,+40,+50,+50,+50,+40,+40,-20 ,
			 -20,+40,+40,+60,+60,+60,+40,+40,-20 ,
			 -30,+20,+20,+20,+20,+20,+20,+20,-30
	};
	   //卒
			public  final  int blackSoldierAttach[]={ 
				
				  0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0    
				 ,0  ,0  ,0  ,0  ,+15,0  ,0  ,0  ,0    
				 ,+20,0  ,+45,0  ,+35,0  ,+45,0  ,+20
				 ,+80,+100,+120 ,+120,+120,+120,+120,+100,+80
				 ,+100,+120,+150 ,+180,+180,+180,+150,+120,+100
				 ,+100,+150,+200,+250,+250,+250,+200,+150,+100
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
			 
			  0  ,0  ,0  ,5  ,0  ,5  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,5 ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,10 ,0  ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,10 ,0  ,10 ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,5 ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,5  ,0  ,5  ,0  ,0  ,0    
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

 
	private  final BitBoard[]  AttackPalaceControlPoint=new BitBoard[2];
	private  final BitBoard[]  AttackCenterControlPoint=new BitBoard[2];
	private  final BitBoard[][]  AttackDirectionControlPoint=new BitBoard[2][2]; 
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
		//棋子的控制点得分(9宫)
		 int blackDefenseInt[]={ 
			  0  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,0    
			 ,0  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,0  
			 ,0  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,0  
			 ,0  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
	   };
			//棋子的控制点得分(9宫)
		 int redLeftAttackPoint[]={ 
			  0  ,1  ,1  ,1  ,1  ,0  ,0  ,0  ,0    
			 ,0  ,1  ,1  ,1  ,1  ,0  ,0  ,0  ,0  
			 ,0  ,1  ,1  ,1  ,1  ,0  ,0  ,0  ,0  
			 ,0  ,1  ,1  ,1  ,1  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
	   };
			//棋子的控制点得分(9宫)
		 int redRightAttackPoint[]={ 
			  0  ,0  ,0  ,0  ,1  ,1  ,1  ,1  ,0   
			 ,0  ,0  ,0  ,0  ,1  ,1  ,1  ,1  ,0 
			 ,0  ,0  ,0  ,0  ,1  ,1  ,1  ,1  ,0 
			 ,0  ,0  ,0  ,0  ,1  ,1  ,1  ,1  ,0 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0 
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
	   };
	   int[] attackBlackPalacePoint=Tools.exchange(attackRedPalacePoint);
	   int[] redDefenseInt=Tools.exchange(blackDefenseInt);
	   int[] blackLeftAttackPoint=Tools.exchange(redLeftAttackPoint);
	   int[] blackRightAttackPoint=Tools.exchange(redRightAttackPoint); 
 
	   //9宫格
	   AttackPalaceControlPoint[REDPLAYSIGN]=new BitBoard(attackRedPalacePoint);
	   AttackPalaceControlPoint[BLACKPLAYSIGN]=new BitBoard(attackBlackPalacePoint);
	   
	   
	   //中间
	   AttackCenterControlPoint[REDPLAYSIGN]=new BitBoard(blackDefenseInt);
	   AttackCenterControlPoint[BLACKPLAYSIGN]=new BitBoard(redDefenseInt);
	   //左右位置攻击
	   AttackDirectionControlPoint[REDPLAYSIGN][0]=new BitBoard(redLeftAttackPoint);
	   AttackDirectionControlPoint[REDPLAYSIGN][1]=new BitBoard(redRightAttackPoint);
	   AttackDirectionControlPoint[BLACKPLAYSIGN][0]=new BitBoard(blackLeftAttackPoint);
	   AttackDirectionControlPoint[BLACKPLAYSIGN][1]=new BitBoard(blackRightAttackPoint);
	}
} 
 


