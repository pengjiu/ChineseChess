package com.pj.chess;

 

import com.pj.chess.chessparam.ChessParam; 

public class ChessConstant {
	public static final int KING=7;    //王
	public static final int CHARIOT=6; //车
	public static final int KNIGHT=5; //马
	public static final int GUN=4; //炮
	public static final int ELEPHANT=3; //象
	public static final int GUARD=2; //士
	public static final int SOLDIER=1; //兵
	/*****************红******************/
	public static final int REDKING=KING+0;    //王
	public static final int REDCHARIOT=CHARIOT+0; //车
	public static final int REDKNIGHT=KNIGHT+0; //马
	public static final int REDGUN=GUN+0; //炮
	public static final int REDELEPHANT=ELEPHANT+0; //象
	public static final int REDGUARD=GUARD+0; //士
	public static final int REDSOLDIER=SOLDIER+0; //兵

	/*****************黑******************/
	public static final int BLACKKING=KING+7;    //王
	public static final int BLACKCHARIOT=CHARIOT+7; //车
	public static final int BLACKKNIGHT=KNIGHT+7; //马
	public static final int BLACKGUN=GUN+7; //炮
	public static final int BLACKELEPHANT=ELEPHANT+7; //象
	public static final int BLACKGUARD=GUARD+7; //士
	public static final int BLACKSOLDIER=SOLDIER+7; //兵
  
 
    //每个棋子对应的角色(与allChess下标对应)
	public static  final int[] chessRoles=new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		BLACKKING,BLACKCHARIOT,BLACKCHARIOT,BLACKKNIGHT,BLACKKNIGHT,BLACKGUN,BLACKGUN,BLACKELEPHANT,BLACKELEPHANT,BLACKGUARD,BLACKGUARD,BLACKSOLDIER,BLACKSOLDIER,BLACKSOLDIER,BLACKSOLDIER,BLACKSOLDIER,
		REDKING,REDCHARIOT,REDCHARIOT,REDKNIGHT,REDKNIGHT,REDGUN,REDGUN,REDELEPHANT,REDELEPHANT,REDGUARD,REDGUARD,REDSOLDIER,REDSOLDIER,REDSOLDIER,REDSOLDIER,REDSOLDIER
	};
	
	public static  final int[] chessRoles_eight=new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		KING,CHARIOT,CHARIOT,KNIGHT,KNIGHT,GUN,GUN,ELEPHANT,ELEPHANT,GUARD,GUARD,SOLDIER,SOLDIER,SOLDIER,SOLDIER,SOLDIER,
		KING,CHARIOT,CHARIOT,KNIGHT,KNIGHT,GUN,GUN,ELEPHANT,ELEPHANT,GUARD,GUARD,SOLDIER,SOLDIER,SOLDIER,SOLDIER,SOLDIER
	};
	//int BLACKKINGIindex=16,BLACKCHARIOTIndex1=17,BLACKCHARIOTIndex2=18,BLACKKNIGHTIndex1=19,BLACKKNIGHTIndex2=20,BLACKGUNIndex1=21,BLACKGUNIndex2=22,BLACKELEPHANTIndex1=23,;
	public  static  final int maxScore=9999;
	//红方标志
	public static final int REDPLAYSIGN  =1;
	//黑方标志
	public static final int BLACKPLAYSIGN  =0;
	//棋盘大小
//	public  static final int BOARDSIZE=255;
	
	public  static final int BOARDSIZE90=90;

	//用来判断是哪一方棋子
	public static  int[]  chessPlay=new int[]{16,32}; 
	
	public static final int LONGCHECKSCORE=8888; //长将重复着法
	
	public static final int drawScore=0; //和棋着法
	
	//,,,,BLACKHORSE,BLACKGUN,BLACKGUN,BLACKELEPHANT,BLACKELEPHANT,BLACKGUARD,BLACKGUARD,BLACKSOLDIER,BLACKSOLDIER,BLACKSOLDIER,BLACKSOLDIER,BLACKSOLDIER,

	
	
	public static final int boardRow[]={ 
		0,0,0,0,0,0,0,0,0,
		1,1,1,1,1,1,1,1,1,
		2,2,2,2,2,2,2,2,2,
		3,3,3,3,3,3,3,3,3,
		4,4,4,4,4,4,4,4,4,
		5,5,5,5,5,5,5,5,5,
		6,6,6,6,6,6,6,6,6,
		7,7,7,7,7,7,7,7,7,
		8,8,8,8,8,8,8,8,8,
		9,9,9,9,9,9,9,9,9,
		
};
	public static final int boardCol[]={ 

		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,
		0,1,2,3,4,5,6,7,8,

	};
	
	//没有棋子
	public final static int NOTHING=-1;
	
	public  static int MAXDEPTH=6;
	
	public final static long ChessZobristList64[][]=new long[BOARDSIZE90][15];
	public final static int ChessZobristList32[][]=new int[BOARDSIZE90][15];
	
	
	public static BitBoard[] KnightBitBoards=new BitBoard[BOARDSIZE90];	//马不考虑别腿所能攻击到的位置
	public static BitBoard[] KnightLegBitBoards=new BitBoard[BOARDSIZE90]; //马别腿的位置
	public static BitBoard[] KingCheckedSoldierBitBoards=new BitBoard[BOARDSIZE90]; //兵将军的位置
	public static BitBoard[][] KnightBitBoardOfAttackLimit=new BitBoard[BOARDSIZE90][200];//[棋子位置][不别马腿的BitBoard.checkSum()]
	public static BitBoard[] ElephanLegBitBoards=new BitBoard[BOARDSIZE90]; //象塞眼的位置
	public static BitBoard[][] ElephanBitBoardOfAttackLimit=new BitBoard[BOARDSIZE90][200];//[棋子位置][不别象腿的BitBoard.checkSum()]
	
	public static BitBoard[][] ChariotBitBoardOfAttackRow=new BitBoard[BOARDSIZE90][512]; //车行(一行上面有9个位置)所能攻击的位棋盘
	public static BitBoard[][] ChariotBitBoardOfAttackCol=new BitBoard[BOARDSIZE90][1024];  //车列(一列上面有10个位置)所能攻击的位棋盘
	public static BitBoard[][] MoveChariotOrGunBitBoardRow=new BitBoard[BOARDSIZE90][512];  //车and炮行(指的一行，是9个子所以为512)所能移动的位棋盘
	public static BitBoard[][] MoveChariotOrGunBitBoardCol=new BitBoard[BOARDSIZE90][1024];  //车and炮列所能移动的位棋盘
	public static BitBoard[][] GunBitBoardOfAttackRow=new BitBoard[BOARDSIZE90][512]; //炮行所能攻击的位棋盘
	public static BitBoard[][] GunBitBoardOfAttackCol=new BitBoard[BOARDSIZE90][1024];  //炮列所能攻击的位棋盘
	public static BitBoard[] KingBitBoard=new BitBoard[BOARDSIZE90]; //将的位棋盘
	public static BitBoard[] GuardBitBoard=new BitBoard[BOARDSIZE90]; //士的位棋盘
	public static BitBoard[][] SoldiersBitBoard=new BitBoard[2][BOARDSIZE90]; //兵的位棋盘[玩家][位置]
	public static BitBoard[][] GunBitBoardOfFakeAttackRow=new BitBoard[BOARDSIZE90][512]; //炮所能攻击到的位子(指压致力)
	public static BitBoard[][] GunBitBoardOfFakeAttackCol=new BitBoard[BOARDSIZE90][1024];  //炮列所能攻击到的位子(指压致力)
	public static BitBoard[][] GunBitBoardOfMoreRestAttackRow=new BitBoard[BOARDSIZE90][512]; //炮隔多子所能攻击到的位子
	public static BitBoard[][] GunBitBoardOfMoreRestAttackCol=new BitBoard[BOARDSIZE90][1024];  //炮隔多子列所能攻击到的位子
	public static BitBoard[] MaskChesses=new BitBoard[BOARDSIZE90]; //用位来表示一个棋子在某个位置
	//机动性
	public static int[][] ChariotAndGunMobilityRow=new int[BOARDSIZE90][512];
	public static int[][] ChariotAndGunMobilityCol=new int[BOARDSIZE90][1024];
	public static int[][] KnightMobility=new int[BOARDSIZE90][200];
	
	public static final int boardMap[]={ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,1,2,3,4,5,6,7,8,0,0,0,0,
		0,0,0,9,10,11,12,13,14,15,16,17,0,0,0,0,
		0,0,0,18,19,20,21,22,23,24,25,26,0,0,0,0,
		0,0,0,27,28,29,30,31,32,33,34,35,0,0,0,0,
		0,0,0,36,37,38,39,40,41,42,43,44,0,0,0,0,
		0,0,0,45,46,47,48,49,50,51,52,53,0,0,0,0,
		0,0,0,54,55,56,57,58,59,60,61,62,0,0,0,0,
		0,0,0,63,64,65,66,67,68,69,70,71,0,0,0,0,
		0,0,0,72,73,74,75,76,77,78,79,80,0,0,0,0,
		0,0,0,81,82,83,84,85,86,87,88,89,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	};
	
	

	
	
}









