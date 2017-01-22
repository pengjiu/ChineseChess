package com.pj.chess;

import static com.pj.chess.ChessConstant.*;
import static com.pj.chess.Tools.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import com.pj.chess.chessmove.ChessMovePlay;
import com.pj.chess.chessmove.ChessQuiescMove;
import com.pj.chess.chessmove.MoveNode;
import com.pj.chess.chessparam.ChessParam;
import com.pj.chess.evaluate.EvaluateCompute;
import com.pj.chess.evaluate.EvaluateComputeMiddle;
import com.pj.chess.zobrist.TranspositionTable;
import static com.pj.chess.chessmove.ChessQuiescMove.*;
/**
 * @author pengjiu
 * 棋子棋盘初始化操作
 * 棋子的着法预生成
 */



public class ChessInitialize {
	//马着法预生成数组
	int[][]  knightMove=new int[BOARDSIZE90][8];  
	//马着法拌腿位置预生成数组
	int[][]  horseLeg=new int[BOARDSIZE90][8];  
	//象着法预生成数组
	int[][]  elephantMove=new int[BOARDSIZE90][4];  
	//象着法拌腿位置预生成数组
	int[][]  elephantLeg=new int[BOARDSIZE90][4];
	//兵着法预生成数组
	 int[][][]  soldierMove=new int[2][BOARDSIZE90][3]; 
	//车吃子着法预生成数组
	int[][][]  chariotMoveRowEat=new int[9][512][2];//行(上下)
	int[][][]  chariotMoveColEat=new int[10][1024][2];   //列(左右)
	//车炮不吃子着法预生成数组
	int[][][]  move_chariotGunRowNop=new int[9][512][9];//行(上下)不吃子是有多个情况
	int[][][]  move_chariotGunColNop=new int[10][1024][10];   //列(左右)
	//炮吃子着法预生成数组
	int[][][]  gunMoveRowEat=new int[9][512][2];//行(上下)
	int[][][]  gunMoveColEat=new int[10][1024][2];   //列(左右)
	//炮伪攻击位置
	int[][][]  gunFackAttackRow=new int[9][512][9];//行(上下)
	int[][][]  gunFackAttackCol=new int[10][1024][10];   //列(左右)
	//炮隔多子所能攻击到的位置
	int[][][]  gunMoreRestAttackRow=new int[9][512][9];//行(上下)
	int[][][]  gunMoreRestAttackCol=new int[10][1024][10];   //列(左右)

	public static ChessParam getGlobalChessParam(int[] boardTemp ){
		int[] board = new int[BOARDSIZE90];
		int[] chesses = new int[48];
		for (int i = 0; i < board.length; i++) {
			board[i] = -1;
		}
		for (int i = 0; i < chesses.length; i++) {
			chesses[i] = -1;
		}

		BitBoard[] chessBitBoardRole = new BitBoard[15];
		for (int i = 0; i < chessBitBoardRole.length; i++) {
			chessBitBoardRole[i] = new BitBoard();
		}
		ChessParam chessParamCont = new ChessParam(board, chesses, new int[2],
				new int[10], new int[9], new int[15], new BitBoard(),
				new BitBoard[] { new BitBoard(), new BitBoard() },
				chessBitBoardRole);
		for(int i=0;i<boardTemp.length;i++){
			if(boardTemp[i]>0){
				int destSite=i;
				int chess=boardTemp[i];
				chessParamCont.board[destSite]=chess;
				chessParamCont.allChess[chess]=destSite;
				int destRow = boardRow[destSite];
				int destCol = boardCol[destSite];
				chessParamCont.boardBitRow[destRow]|=(1<<(8-destCol));
				chessParamCont.boardBitCol[destCol]|=(1<<(9-destRow)); 
			}
		}
		chessParamCont.initChessBaseScoreAndNum();
		TranspositionTable.genStaticZobrist32And64OfBoard(chessParamCont.board);

		return chessParamCont;
	}
	static{
		// 位棋盘的初始
		for (int i = 0; i < MaskChesses.length; i++) {
			MaskChesses[i] = new BitBoard(i);
		}
		new ChessInitialize();
	}
	private ChessInitialize(){
		//马不考虑别腿的位棋盘
		initBitBoard(KnightBitBoards);
		//马别腿的位子
		initBitBoard(KnightLegBitBoards);
		//马象攻击生成
		initBitBoard(KnightBitBoardOfAttackLimit);
		initBitBoard(ElephanBitBoardOfAttackLimit);
		
		cleanEmpty(knightMove);
		cleanEmpty(horseLeg);
		cleanEmpty(elephantMove);
		cleanEmpty(elephantLeg);
		cleanEmpty(soldierMove);
		cleanEmpty(gunFackAttackRow);
		cleanEmpty(gunFackAttackCol);
		cleanEmpty(gunMoreRestAttackRow);
		cleanEmpty(gunMoreRestAttackCol);
		
		//初始马的着法
		initKnightMove();
		//初始象的着法
		initElephantMove();
		//初始卒的着法
		initSoldier();
		//初始不吃子车炮对应所有行着法  
		this.initChariotGunVariedMove(cleanEmpty(move_chariotGunRowNop), 0, 0,false);
		//初始不吃子车炮对应所有列着法
		this.initChariotGunVariedMove(cleanEmpty(move_chariotGunColNop), 1, 0,false); 
		//初始炮吃子对应所有行着法 
		this.initChariotGunVariedMove(cleanEmpty(gunMoveRowEat), 0, 1,true);
		//初始炮吃子对应所有列着法
		this.initChariotGunVariedMove(cleanEmpty(gunMoveColEat), 1, 1,true); 
		//车吃子 (行) 
		this.initChariotGunVariedMove(cleanEmpty(chariotMoveRowEat), 0, 0,true);
		//车吃子 (列)
		this.initChariotGunVariedMove(cleanEmpty(chariotMoveColEat), 1, 0,true); 
		
		/******炮伪攻击位置*******/
		initGunFackEatMove(gunFackAttackRow,0);
		initGunFackEatMove(gunFackAttackCol,1);
		//炮隔多子能攻击到的位置
		this.initChariotGunVariedMove(gunMoreRestAttackRow, 0, 2,true);
		this.initChariotGunVariedMove(gunMoreRestAttackCol, 1, 2,true);
		
		//生成置换表
//		genBoardZobrist();
		//生成基础分 和 棋子数量
 
		//预生成所有位棋盘
		preAllBitBoard();

		
	}

	private void preAllBitBoard() {
		// 马 考虑别腿所能攻击到的位置
		preBitBoardAttack(knightMove, horseLeg, KnightBitBoardOfAttackLimit, 0);
		// 象 考虑别腿所能攻击到的位置
		preBitBoardAttack(elephantMove, elephantLeg,
				ElephanBitBoardOfAttackLimit, 1);
		// 车炮不吃子的情况 行
		preGunAndChariotBitBoardAttack(move_chariotGunRowNop,
				MoveChariotOrGunBitBoardRow, 0);
		// 车炮不吃子的情况 列
		preGunAndChariotBitBoardAttack(move_chariotGunColNop,
				MoveChariotOrGunBitBoardCol, 1);
		// 车吃子情况 行
		preGunAndChariotBitBoardAttack(chariotMoveRowEat,
				ChariotBitBoardOfAttackRow, 0);
		// 车吃子情况 列
		preGunAndChariotBitBoardAttack(chariotMoveColEat,
				ChariotBitBoardOfAttackCol, 1);
		// 炮吃子情况 行
		preGunAndChariotBitBoardAttack(gunMoveRowEat, GunBitBoardOfAttackRow, 0);
		// 炮吃子情况 列
		preGunAndChariotBitBoardAttack(gunMoveColEat, GunBitBoardOfAttackCol, 1);
		// 生成将的位棋盘
		preBitBoardKingMove(KingBitBoard);
		// 生成士的位棋盘
		preBitBoardGuardMove(GuardBitBoard);
		// 卒将军的棋盘
		preKingCheckedSoldierBitBoards(KingCheckedSoldierBitBoards);
		//炮伪攻击位置
		preGunAndChariotBitBoardAttack(gunFackAttackRow, GunBitBoardOfFakeAttackRow, 0);
		preGunAndChariotBitBoardAttack(gunFackAttackCol, GunBitBoardOfFakeAttackCol, 1);
		//车炮的机动性
		preGunAndChariotMobility(MoveChariotOrGunBitBoardRow,ChariotAndGunMobilityRow);
		preGunAndChariotMobility(MoveChariotOrGunBitBoardCol,ChariotAndGunMobilityCol);
		// 炮隔多子吃子情况 行
		preGunAndChariotBitBoardAttack(gunMoreRestAttackRow, GunBitBoardOfMoreRestAttackRow, 0);
		// 炮隔多子吃子情况 列
		preGunAndChariotBitBoardAttack(gunMoreRestAttackCol, GunBitBoardOfMoreRestAttackCol, 1);
		//加载开局库
//		loadBook();
	}
	private void preKingCheckedSoldierBitBoards(BitBoard[] bitBoard){
		for(int i=0;i<bitBoard.length;i++){
			bitBoard[i]=new BitBoard(KnightLegBitBoards[i]);
			if(bitBoard[i].Count()==4){
				if(i<45){
					bitBoard[i].assignXor(new BitBoard(i-9));
				}else{
					bitBoard[i].assignXor(new BitBoard(i+9));
				}
			}
		}
	}
	/**
	 * 初始马的着法预生成数组
	 */
	private  void initKnightMove(){
		int[] cnKnightMoveTab=new int[]{-0x21, -0x1f, -0x12, -0x0e, +0x0e, +0x12, +0x1f, +0x21}; 
		int[] cnHorseLegTab=new int[]{-0x10, -0x10, -0x01, +0x01, -0x01, +0x01, +0x10, +0x10};
		for(int site=0;site<255;site++){ 
			if(isBoardTo255(site)){ 
				int z=0;
				 for(int j=0;j<cnKnightMoveTab.length;j++){
					 int _tKnight=site+cnKnightMoveTab[j];
					 int _tHorseLeg=site+cnHorseLegTab[j];
					 if(isBoardTo255(_tKnight) && isBoardTo255(_tHorseLeg)){ 
						 int siteTo90=boardMap[site];
						 int _tKnightTo90=boardMap[_tKnight];
						 int _tHorseLegTo90=boardMap[_tHorseLeg];
						 knightMove[siteTo90][z]=_tKnightTo90;
						 horseLeg[siteTo90][z]=_tHorseLegTo90;						  
						 z++;
						 if(KnightBitBoards[siteTo90]==null){
							 KnightBitBoards[siteTo90]=new BitBoard();
						 }
						 if(KnightLegBitBoards[siteTo90]==null){
							 KnightLegBitBoards[siteTo90]=new BitBoard();
						 }
						 //马的位棋盘
						 KnightBitBoards[siteTo90].assignOr(new BitBoard(_tKnightTo90));
						 KnightLegBitBoards[siteTo90].assignOr(new BitBoard(_tHorseLegTo90));
						 
					 }
				 }
			}
		}  
		
	}
	/**
	 * 初始象的着法预生成数组
	 */
	public void initElephantMove(){
		int[] cnElephantMoveTab=new int[]{-0x22, -0x1e,+0x1e, +0x22}; 
		int[] cnElephantLegTab=new int[]{-0x11, -0xf,+0xf, +0x11};
		for(int site=0;site<255;site++){ 
			if(isBoardTo255(site)){ 
				int z=0;
				int[] _tElephantMoveTab=cnElephantMoveTab;
				int[] _tElephantLegTab=cnElephantLegTab;
				//黑象到达楚汉边界重置其着法表不允许地河
				if(site/16==7 || site/16==6){  
					 _tElephantMoveTab=new int[]{-0x22, -0x1e}; 
					 _tElephantLegTab=new int[]{-0x11, -0xf};
				 //红象到达楚汉边界重置其着法表不允许地河					 
				 }else if(site/16==8 || site/16==9){
					 _tElephantMoveTab=new int[]{+0x1e, +0x22}; 
					 _tElephantLegTab=new int[]{+0xf, +0x11};
				 }
				 for(int j=0;j<_tElephantMoveTab.length;j++){
					 
					 int _tElephant=site+_tElephantMoveTab[j];
					 int _tElephantLeg=site+_tElephantLegTab[j];
					 
					 if(isBoardTo255(_tElephant) && isBoardTo255(_tElephantLeg)){
						 
						 int siteTo90=boardMap[site];
						 int _tElephant_90=boardMap[_tElephant];
						 int _tElephantLeg_90=boardMap[_tElephantLeg];
						 
						 elephantMove[siteTo90][z]=_tElephant_90;
						 elephantLeg[siteTo90][z]=_tElephantLeg_90;
						 
						 if(ElephanLegBitBoards[siteTo90]==null){
							 ElephanLegBitBoards[siteTo90]=new BitBoard();
						 }
						 //象被塞眼的位棋盘
						 ElephanLegBitBoards[siteTo90].assignXor(MaskChesses[_tElephantLeg_90]);
						 z++;
					 }
				 }
			}
		}  
	}
	/**
	 * 初始兵的着法预生成数组
	 */
	private void initSoldier() { 
		int[] _tSoldierMoveTab = null;
		for (int i = 0; i < soldierMove.length; i++)
			for (int site = 0; site < 255; site++) {
				if (isBoardTo255(site)) {
					int z = 0;
					if(i==BLACKPLAYSIGN){
						//黑方兵以经过界
						if (site/16 >7) {
							_tSoldierMoveTab=new int[]{+0x10,-0x01,+0x01};
						}else{
							_tSoldierMoveTab=new int[]{+0x10};
						}
					}else if(i==REDPLAYSIGN){
						//红方兵以经过界						
						if(site/16<8){
							_tSoldierMoveTab=new int[]{-0x10,-0x01,+0x01};
						}else{
							_tSoldierMoveTab=new int[]{-0x10};
						}
					}
					for (int j = 0; j < _tSoldierMoveTab.length; j++) {
						int _tSoldier = site + _tSoldierMoveTab[j]; 
						if (isBoardTo255(_tSoldier)) { 
							 int siteTo90=boardMap[site];
							 int _tSoldier90=boardMap[_tSoldier];
							soldierMove[i][siteTo90][z] =_tSoldier90;
							if(SoldiersBitBoard[i][siteTo90]==null){
								SoldiersBitBoard[i][siteTo90]=new BitBoard();
							}
							//兵所能攻击到的位棋盘
							SoldiersBitBoard[i][siteTo90].assignOr(MaskChesses[_tSoldier90]);
							z++;
						}
					}
				}
			}
	}
	 
	 
	/**
	 *@author pengjiu 
	 *@date:Sep 23, 2011 1:44:24 PM
	 * 功能：车炮吃子与不吃子
	 *@param moveEat 数组
	 *@param direction 方向
	 *@param handicapNum 中间间隔棋子数
	 *@param isEat 是否吃子 true false
	*/
	private  void initChariotGunVariedMove(int [][][] moveEat,int direction,int handicapNum,boolean isEat){
		int num=moveEat.length-1;
		int sort=moveEat[0].length;
		 for(int i=0;i<=num;i++){
			 int site=1<<i;//所在位置
			 for(int j=0;j<=sort;j++){
				 if(((j & site)>0)){
					 if(isEat && j==site){ //吃子自己不能算进去
						 continue;
					 }
					 int isHandicap=0;
					 int eatIndex=0;
					 //向左(上)取值
					 for(int n=i+1;n<=num;n++){
						int _tSite=1<<n;
						if (isEat) {
							if((j & _tSite) > 0){ 
								if (isHandicap <handicapNum) {
									isHandicap ++;
								} else {
									if (direction == 0) {
										moveEat[num - i][j][eatIndex++] = (num - n);
									} else {
										moveEat[num - i][j][eatIndex++] = (num - n) * 9;
									}
									break;
								}
							}
						}else{
							if((j & _tSite)==0 ){
								 if(direction==0){
									 moveEat[num-i][j][eatIndex++]=(num-n);
								 }else{
									 moveEat[num-i][j][eatIndex++]=(num-n)*9;
								 }
							}else if((j & _tSite)>0 ){
								break;
							}
						 }
					 } 
					 isHandicap=0; 
					 //向右(下)取值
					 for(int n=i-1;n>=0;n--){
						 int _tSite=1<<n;
						 if (isEat) {
							 if((j & _tSite)>0 ){ 
								 if(isHandicap<handicapNum){
									 isHandicap++;
								 }else{
									 if(direction==0){
										 moveEat[num-i][j][eatIndex++]=(num-n);
									 }else{
										 moveEat[num-i][j][eatIndex++]=(num-n)*9;	 
									 }
									 break;
								 }
								 
							 } 
						 }else{
							if((j & _tSite)==0 ){
								 if(direction==0){
									 moveEat[num-i][j][eatIndex++]=(num-n);
								 }else{
									 moveEat[num-i][j][eatIndex++]=(num-n)*9;
								 }
							}else if((j & _tSite)>0 ){
								break;
							}
						 }
					 }
				 }
			 }
			 
		 }
	}
	/*
	 * 生成32位64位 唯一随机数
	 */
//	private void genBoardZobrist(){
//		Random random = new Random();
//		for(int i=0;i<ChessZobristList64.length;i++){
//			for(int j=0;j<ChessZobristList64[i].length;j++){
//					ChessZobristList64[i][j]=Math.abs((random.nextLong()<<15)^(random.nextLong()<<30)^(random.nextLong()<<45)^(random.nextLong()<<60));
//					ChessZobristList32[i][j]= Math.abs((random.nextInt()<<15)^(random.nextInt()<<30));
//			}
//		}
//	}
 
	
	private void initChessBaseScoreAndNum(ChessParam chessParamCont){
 
		int[] allChess = chessParamCont.allChess;
		int[] board = chessParamCont.board;
		
		for (int i = 16; i < allChess.length; i++) {
			if(allChess[i]!=NOTHING){
				int site=allChess[i];
				int chessRole = chessRoles[board[allChess[i]]];
				int play=i < 32?BLACKPLAYSIGN:REDPLAYSIGN;
				chessParamCont.increaseChessesNum(chessRole);
//				chessParamCont.baseScore[play]+=EvaluateComputeMiddle.chessBaseScore[i];
//				chessParamCont.baseScore[play]+=new EvaluateComputeMiddle(chessParamCont).chessAttachScore(chessRole,allChess[i]);
				chessParamCont.maskBoardChesses.assignXor(MaskChesses[site]);
				chessParamCont.maskBoardPersonalChesses[play].assignXor(MaskChesses[site]);
				chessParamCont.maskBoardPersonalRoleChesses[chessRole].assignXor(MaskChesses[site]);
			}
		} 
		
	}
	/**
	 *@author pengjiu 
	 *@date:Aug 26, 2011 5:09:07 PM
	 * 功能： 生成马 and 象 不别腿所能攻击到的位置
	 *@param attackBoard  预生成攻击位置
	 *@param leg  攻击时的别腿位置
	 *@param attackBoardBit 最终返回的数据
	 *@param type 0 为马  1为象
	*/
	private  void preBitBoardAttack(int[][] attackBoard,int[][]leg,BitBoard[][] attackBoardBit,int type){ 
		for(int i=0;i<ChessConstant.BOARDSIZE90;i++){ 
			//所有别腿去重复后的数组
			int[] legSite=removeRepeatArray(leg[i]);
			//得到此别腿的所有组合情况 KnightBitBoardOfAttackLimit
			int[][] legSiteComb=getAllLegCombByLeg(legSite);
			for(int k=0;k<legSiteComb.length;k++){
				 if(legSiteComb[k][0]==ChessConstant.NOTHING){
					 break;
				 }
				 BitBoard siteAttBit=new BitBoard();
				 BitBoard siteLegBit=new BitBoard();
				 int[] legTemp=leg[i];
				 for(int n=0;n<legTemp.length && legTemp[n]!=ChessConstant.NOTHING;n++){
					 boolean isExists=false;
					 for(int j=0;j<legSiteComb[k].length&&legSiteComb[k][j]!=ChessConstant.NOTHING;j++){
						 //把组合中在legTemp存在的
						  if(legTemp[n]==legSiteComb[k][j]){
							  isExists=true;break;
						  }
					 }
					 //设置别腿
					 if(!isExists){ 
						 if(attackBoard[i][n]!=ChessConstant.NOTHING){
							 siteLegBit.assignOr(MaskChesses[legTemp[n]]); 
						 }
					 }
					 //设置不别腿能走到的位置
					 if(isExists){//在组合中不存在 写入位棋盘
						 if(attackBoard[i][n]!=ChessConstant.NOTHING){
							 siteAttBit.assignXor(MaskChesses[attackBoard[i][n]]);
						 }
					 }
				 }
				 if(type==0){ //马
//					 if(!attackBoardBit[i][siteLegBit.checkSumOfKnight()].isEmpty()){
//						 System.out.println("==========================马===========================");
//						 System.out.println(attackBoardBit[i][siteLegBit.checkSumOfKnight()]+" \n  原位置"+i+attackBoardBit[i][siteLegBit.checkSumOfKnight()].checkSumOfKnight()+" 棋子在->"+i);
//						 System.out.println(siteLegBit+" \n  现在位置"+i+siteLegBit.checkSumOfKnight()+" 棋子在->"+i);
//						 System.out.println("=====================================================");
//					 }else {
						 attackBoardBit[i][siteLegBit.checkSumOfKnight()]=siteAttBit;
						 //马的机动性
						 KnightMobility[i][siteLegBit.checkSumOfKnight()]=siteAttBit.Count();
//					 }
				 }else{  //象
//					 if(!attackBoardBit[i][siteLegBit.checkSumOfElephant()].isEmpty()){
//						 System.out.println("========================象=============================");
//						 System.out.println(attackBoardBit[i][siteLegBit.checkSumOfElephant()]+" \n  原位置"+i+attackBoardBit[i][siteLegBit.checkSumOfElephant()].checkSumOfElephant()+" 棋子在->"+i);
//						 System.out.println(siteLegBit+" \n  现在位置"+i+siteLegBit.checkSumOfElephant()+" 棋子在->"+i);
//						 System.out.println("=====================================================");
//					 }else {
						 attackBoardBit[i][siteLegBit.checkSumOfElephant()]=siteAttBit;
//					 }
				 }
				 
			}
		}
	}	
	//数组中去除重复数据
	private  int[] removeRepeatArray(int[] array){
		int[] duplicate=new int[array.length];
		for(int k=0;k<duplicate.length;k++){
			duplicate[k]=-1;
		}
		for(int i=0;i<array.length;i++){
			if(array[i]==ChessConstant.NOTHING){
				break;
			}
			for(int z=0;z<duplicate.length;z++){
				if(duplicate[z]==array[i]){
					break;
				}else if(duplicate[z]==ChessConstant.NOTHING){
					duplicate[z]=array[i];
					break;
				}
			}
		}
		return duplicate;
	}
	/*
	 * 所有别腿的组合
	 */
	private  int[][] getAllLegCombByLeg(int legs[]){
		int r[][]=new int[20][4];
		for(int i=0;i<r.length;i++ ){
			for(int j=0;j<r[i].length;j++ ){
				r[i][j]=ChessConstant.NOTHING;
			}
		}
		for(int i=0,b=0;i<legs.length&&legs[i]!=ChessConstant.NOTHING;i++){
			String[] result=computCombination(0,legs,i+1);
			for(int j=0;j<result.length && result[j]!=null;j++){
				String[] siteStr=result[j].split(",");
				for(int m=0;m<siteStr.length;m++){
					r[b][m]=Integer.valueOf(siteStr[m]);
				}
				if(siteStr.length>0){
					b++;
				}
			}
		}
		return r;
	}
	/**
	 *@author pengjiu 
	 * index : 数组起始位置
	 * a  : 数组
	 * num : 从数组后面拿取几位
	 * 一直拿取到只为0为止
	 */
	private  String[]  computCombination(int index,int[] a,int num){
		String[] value=new String[10];
		int b=0;
		for(int i=index;i<a.length&&a[i]!=ChessConstant.NOTHING;i++){
			if(num==1){
				value[b++]=a[i]+"";
			}else{
				String[] r=computCombination(i+1,a,num-1);
				for(int j=0;j<r.length&&r[j]!=null;j++){
					value[b++]=a[i]+","+r[j];
				}
			}
		}
		return value;
	}
	/**
	 *@author pengjiu 
	 *@date:Sep 1, 2011 12:43:05 PM
	 * 功能： 车炮机动性能预生成
	 *@param moveSite
	 *@param mobility
	*/
	private void preGunAndChariotMobility(BitBoard[][] moveSite,int[][] mobility){
		for(int i=0;i<moveSite.length;i++){
			for(int j=0;j<moveSite[i].length;j++){
				if(moveSite[i][j]!=null){
//					System.out.println(moveSite[i][j].Count());
					mobility[i][j]=moveSite[i][j].Count();
				}
					
			}
		}
	}
	/** @author pengjiu 
	 * @param moveSite 需要从之前预生成着法中遍历所有可行着法用来生成位棋盘
	 * @param bitBoard 生成后赋值的位棋盘
	 * @param type 0行  1 列
	 */
	private void preGunAndChariotBitBoardAttack(int[][][] moveSite,BitBoard[][] bitBoard,int type){
		
		for(int i=0;i<BOARDSIZE90;i++){
			int row = boardRow[i];
			int col = boardCol[i];
			//moveSite[rowOrCol][行or列的二进制状态][能移动到的位置]
			int rowOrCol=0; 
			int[][] moveSiteTemp=null;
			if(type==0){ //行
				rowOrCol=row;
				//因为一行上面要知道他所在的位置所以放入当前棋子所在的一行中的哪一列
				moveSiteTemp=moveSite[col];
			}else{  //列
				rowOrCol=col;
				moveSiteTemp=moveSite[row];
			}
			for(int j=0;j<moveSiteTemp.length;j++){
				bitBoard[i][j]=new BitBoard();
				for(int k=0;k<moveSiteTemp[j].length &&moveSiteTemp[j][k]!=NOTHING ;k++){
					int site=0;
					if(type==0){ //行
						site=moveSiteTemp[j][k]+rowOrCol*9;
					}else{  //列
						site=moveSiteTemp[j][k]+rowOrCol;
					}
					bitBoard[i][j].assignXor(MaskChesses[site]);
				}	
			}	
		}
		
	}
	/**
	 *@author pengjiu 
	 *@date:Aug 29, 2011 12:17:14 PM
	 * 功能： 将的位棋盘生成
	 *@param bitBoard
	*/
	public void preBitBoardKingMove(BitBoard[] bitBoard){
		for(int i=0;i<BOARDSIZE90;i++){
			int[] _tMove=null;
			int srcSite=i;
			switch (srcSite) { 
			case 3:
				_tMove=new int[]{4,12};
				break;			
			case 4:
				_tMove=new int[]{3,13,5};
				break; 
			case 5:
				_tMove=new int[]{4,14};
				break; 
			case 12:
				_tMove=new int[]{3,13,21};
				break;
			case 13:
				_tMove=new int[]{12,4,14,22};
				break; 
			case 14:
				_tMove=new int[]{5,13,23};
				break;
			case 21:
				_tMove=new int[]{12,22};
				break;	
			case 22:
				_tMove=new int[]{21,13,23};
				break;
			case 23:
				_tMove=new int[]{22,14};
				break;  
			case 84:
				_tMove=new int[]{75,85};
				break;			
			case 85:
				_tMove=new int[]{84,76,86};
				break; 
			case 86:
				_tMove=new int[]{85,77};
				break;
			case 75:
				_tMove=new int[]{66,84,76};
				break; 
			case 76:
				_tMove=new int[]{85,75,77,67};
				break;
			case 77:
				_tMove=new int[]{76,68,86};
				break;	
			case 66:
				_tMove=new int[]{75,67};
				break;
			case 67:
				_tMove=new int[]{66,76,68};
				break;
			case 68:
				_tMove=new int[]{67,77};
				break; 				
			}  
			if(_tMove!=null){
				bitBoard[i]=new BitBoard();
				for(int j=0;j<_tMove.length;j++){
					bitBoard[i].assignXor(MaskChesses[_tMove[j]]);
			    }
		    }
		}
	}
	/**
	 *@author pengjiu 
	 *@date:Aug 29, 2011 12:25:50 PM
	 * 功能：士位棋盘
	 *@param bitBoard
	*/
	public void preBitBoardGuardMove(BitBoard[] bitBoard){
		for(int i=0;i<bitBoard.length;i++){
			int[] _tMove=null;
			int srcSite=i;
			switch (srcSite) { 
			case 3:
				_tMove=new int[]{13}; 
				break;
			case 5:
				_tMove=new int[]{13};
				break;
			case 13:
				_tMove=new int[]{3,5,21,23}; 
				break;
			case 21:
				_tMove=new int[]{13}; 
				break; 
			case 23:
				_tMove=new int[]{13}; 
				break; 
			case 84:
				_tMove=new int[]{76}; 
				break;
			case 86:
				_tMove=new int[]{76};
				break;
			case 76:
				_tMove=new int[]{66,68,84,86}; 
				break;
			case 66:
				_tMove=new int[]{76};
				break;
			case 68:
				_tMove=new int[]{76};
				break; 			
			}  
			if(_tMove!=null){
				bitBoard[i]=new BitBoard();
				for(int j=0;j<_tMove.length;j++){
					bitBoard[i].assignXor(MaskChesses[_tMove[j]]);
			    }
		    }
		}
	}
	
	/**初始炮能攻击到的位置
	 * @param moveEat
	 * @param direction
	 */
	private  void initGunFackEatMove(int [][][] moveEat,int direction){
		int num=moveEat.length-1;
		int sort=moveEat[0].length;
		 for(int i=0;i<=num;i++){
			 int site=1<<i;//所在位置
			 for(int j=0;j<=sort;j++){
				 if(((j & site)>0) && j!=site){
					 boolean isEat=false;
					 boolean isHandicap=false;
					 int eatIndex=0;
					 //向左(上)取值
					 for(int n=i+1;n<=num;n++){
						 int _tSite=1<<n;
						 if(isEat){
							 break;
						 }
						if ((j & _tSite) > 0) {
							if (isHandicap == false) {
								isHandicap = true;
							} else {
								isEat = true;
								
							}
						} else if (isHandicap) {
							if (direction == 0) {
								moveEat[num - i][j][eatIndex++] = (num - n);
							} else {
								moveEat[num - i][j][eatIndex++] = (num - n) * 9;
							}
						}
					 } 
					 isHandicap=false;
					 isEat=false; 
					 //向右(下)取值
					 for(int n=i-1;n>=0;n--){
						 int _tSite=1<<n;
						 if(isEat){
							 break;
						 }
						 if((j & _tSite)>0 ){ 
							 if(isHandicap==false){
								 isHandicap=true;
							 }else{
								 isEat=true;
							 }
							 
						 } else if (isHandicap) {
							 if(direction==0){
								 moveEat[num-i][j][eatIndex++]=(num-n);
							 }else{
								 moveEat[num-i][j][eatIndex++]=(num-n)*9;	 
							 }
						 }
					 }
				 }
			 }
			 
		 }
	}
	public void preInCheck(){
		
	}
	
	private int[][][] cleanEmpty(int[][][] array){
		for(int i=0;i<array.length;i++){
			for(int j=0;j<array[i].length;j++){
				for(int k=0;k<array[i][j].length;k++){
					array[i][j][k]=-1;
				}
			}
		}
		return array;
	}
	public int[][] cleanEmpty(int[][] array){
		for(int i=0;i<array.length;i++){
			for(int j=0;j<array[i].length;j++){
					array[i][j]=-1;
			}
		}
		return array;
	}
	public int[] cleanEmpty(int[] array){
			for (int j = 0; j < array.length; j++) {
			array[j] = -1;
		}
		return array;
	}
	public BitBoard[][] initBitBoard(BitBoard[][] array){
		for(int i=0;i<array.length;i++){
			for(int j=0;j<array[i].length;j++){
					array[i][j]=new BitBoard();
			}
		}
		return array;
	}
	public BitBoard[] initBitBoard(BitBoard[] array){
		for(int i=0;i<array.length;i++){
				array[i]=new BitBoard();
		}
		return array;
	}

	public static void main(String[] args) {
	}
} 









