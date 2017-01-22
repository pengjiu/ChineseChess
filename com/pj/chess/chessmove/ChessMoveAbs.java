package com.pj.chess.chessmove;

import static com.pj.chess.ChessConstant.*;

import java.util.ArrayList;
import java.util.List;

import com.pj.chess.BitBoard;
import com.pj.chess.ChessConstant;
import com.pj.chess.Tools;
import com.pj.chess.chessparam.ChessParam;
import com.pj.chess.evaluate.EvaluateCompute;
import com.pj.chess.movelist.MoveNodeList;
import com.pj.chess.zobrist.TranspositionTable;

/**
 * @author Administrator
 *
 */
public abstract class ChessMoveAbs {

 

//	public int[]  board;
//	public int[] allChess;
//	public int[] chessParam.boardBitRow;
//	public int[] chessParam.boardBitCol;
	public int tranHit,killerHit;
	
	protected MoveNodeList generalMoveList;
	
	protected MoveNodeList goodMoveList;
	
	protected MoveNodeList  repeatMoveList;
	
	protected BitBoard oppAttackSite;
//	public int play;
//	boolean isEatKing=false;
	protected ChessParam chessParam ;

	protected TranspositionTable tranTable;
	
	protected int[] board;
	
	protected int[] allChess;
	
	protected EvaluateCompute evaluateCompute;
	
	public ChessMoveAbs(){
	
	}
	public ChessMoveAbs(ChessParam chessParam,TranspositionTable tranTable,EvaluateCompute evaluateCompute){
		this.tranTable=tranTable;
		this.chessParam=chessParam;
		this.board=this.chessParam.board;
		this.allChess=this.chessParam.allChess;
		this.evaluateCompute=evaluateCompute;
	}
	
	/**棋子移动改变棋盘和位棋盘
	 * @param moveNode
	 */
	public void moveOperate(MoveNode moveNode){

		int srcSite=moveNode.srcSite;
		int destSite=moveNode.destSite;
		int srcChess = moveNode.srcChess;
		int destChess = moveNode.destChess;
		if(board[srcSite]==-1){
			return ;
		}
		
		/*******棋子每步分数预加*******/
		int srcChessRole = chessRoles[board[srcSite]];
		int srcPlay;
		if((chessPlay[BLACKPLAYSIGN] & srcChess)!=0){ //原棋子为黑方		
			srcPlay=BLACKPLAYSIGN;
		}else{ //原棋子为红方
			srcPlay=REDPLAYSIGN;
		}
		//基础分数和位置分数预加
		chessParam.baseScore[srcPlay]-=evaluateCompute.chessAttachScore(srcChessRole, srcSite);
		chessParam.baseScore[srcPlay]+=evaluateCompute.chessAttachScore(srcChessRole, destSite);
		//改全局变位棋盘
		chessParam.maskBoardChesses.assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardChesses.assignOr(MaskChesses[destSite]);
		//修改着子方位棋盘
		chessParam.maskBoardPersonalChesses[srcPlay].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalChesses[srcPlay].assignXor(MaskChesses[destSite]);
		//修改此角色的位棋盘
		chessParam.maskBoardPersonalRoleChesses[srcChessRole].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalRoleChesses[srcChessRole].assignXor(MaskChesses[destSite]);
		
		//有吃子
		if(destChess!=NOTHING){
			int destPlay=1-srcPlay;
			int destChessRole = chessRoles[board[destSite]];
			chessParam.baseScore[destPlay]-=evaluateCompute.chessBaseScore[destChess];
			chessParam.baseScore[destPlay]-=evaluateCompute.chessAttachScore(destChessRole, destSite);
			//减少棋子数量
			chessParam.reduceChessesNum(destChessRole);
			//修改被吃方的位棋盘
			chessParam.maskBoardPersonalChesses[destPlay].assignXor(MaskChesses[destSite]);
			//修改被吃方的角色位棋盘
			chessParam.maskBoardPersonalRoleChesses[destChessRole].assignXor(MaskChesses[destSite]);
		}
		setBoard(srcSite,NOTHING);
		setBoard(destSite,srcChess);
		setChess(srcChess,destSite);
		setChess(destChess,NOTHING);	
			
		
		int srcRow = boardRow[srcSite];
		int srcCol = boardCol[srcSite];
		int destRow = boardRow[destSite];
		int destCol = boardCol[destSite];
//		System.out.println("原位置:第"+srcRow+"行"+srcCol+"列");
//		System.out.println("目标位置:第"+destRow+"行"+destCol+"列");
//		System.out.println("修改之前row关系\t"+Integer.toBinaryString(boardBitRow[destRow]));
		chessParam.boardBitRow[destRow]|=(1<<(8-destCol));
//		System.out.println("修改之后row关系\t"+Integer.toBinaryString(chessParam.boardBitRow[destRow]));
		chessParam.boardBitCol[destCol]|=(1<<(9-destRow));
		
//		System.out.println("修改之前row关系\t"+Integer.toBinaryString(chessParam.boardBitRow[srcRow]));
		chessParam.boardBitRow[srcRow]^=(1<<(8-srcCol));
//		System.out.println("修改之后row关系\t"+Integer.toBinaryString(chessParam.boardBitRow[srcRow]));
		chessParam.boardBitCol[srcCol]^=(1<<(9-srcRow));
		
//		System.out.println("移动棋子前Zobrist："+TranspositionTable.boardZobrist);
		tranTable.moveOperate(moveNode);
//		System.out.println("移动棋子后Zobrist："+TranspositionTable.boardZobrist);
		
	}
	/**撤销棋子移动改变棋盘和位棋盘
	 * @param moveNode 
	 */
	public void unMoveOperate(MoveNode moveNode){
		int srcSite=moveNode.destSite;
		int srcChess =moveNode.destChess;
		int destSite=moveNode.srcSite;
		int destChess = moveNode.srcChess;
		//关系还原 
		setBoard(srcSite,srcChess);
		setBoard(destSite,destChess);
		setChess(srcChess,srcSite);
		setChess(destChess,destSite);		
		
		int destPlay; 
		int destChessRole = chessRoles[board[destSite]];
		if((chessPlay[BLACKPLAYSIGN] & destChess)!=0){ //原棋子为黑方			
			destPlay=BLACKPLAYSIGN;
		}else{ //原棋子为红方			
			destPlay=REDPLAYSIGN; 
		}
		//棋子分数预加
		chessParam.baseScore[destPlay]-=evaluateCompute.chessAttachScore(destChessRole, srcSite);
		chessParam.baseScore[destPlay]+=evaluateCompute.chessAttachScore(destChessRole, destSite);
		//改全局变位棋盘(将目标位置还原)
		chessParam.maskBoardChesses.assignXor(MaskChesses[destSite]);
		//修改着子方位棋盘
		chessParam.maskBoardPersonalChesses[destPlay].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalChesses[destPlay].assignXor(MaskChesses[destSite]);
		//修改着子方角色位棋盘
		chessParam.maskBoardPersonalRoleChesses[destChessRole].assignXor(MaskChesses[srcSite]);
		chessParam.maskBoardPersonalRoleChesses[destChessRole].assignXor(MaskChesses[destSite]);
		
		//这一步发生过吃子
		if(srcChess!=NOTHING){
			int srcChessRole = chessRoles[board[srcSite]];
			int srcPlay=1-destPlay;
			chessParam.baseScore[srcPlay] += evaluateCompute.chessBaseScore[srcChess];
			chessParam.baseScore[srcPlay] += evaluateCompute.chessAttachScore(srcChessRole, srcSite);
			//还原棋子数量
			chessParam.increaseChessesNum(srcChessRole);
			//将被吃子一方的棋子在位棋盘上还原
			chessParam.maskBoardPersonalChesses[srcPlay].assignXor(MaskChesses[srcSite]);
			//将被吃子一方的棋子在角色位棋盘上还原
			chessParam.maskBoardPersonalRoleChesses[srcChessRole].assignXor(MaskChesses[srcSite]);
		}else{
			//没有棋子修改全局位棋盘  因为要还原所以要清楚那里的棋子
			chessParam.maskBoardChesses.assignXor(MaskChesses[srcSite]);
		}
		 
		
		
		int srcRow = boardRow[srcSite];
		int srcCol = boardCol[srcSite];
		int destRow = boardRow[destSite];
		int destCol = boardCol[destSite];
		
		chessParam.boardBitRow[destRow]|=(1<<(8-destCol));
		chessParam.boardBitCol[destCol]|=(1<<(9-destRow)); 
		if(srcChess==NOTHING){
			chessParam.boardBitRow[srcRow]^=(1<<(8-srcCol)); 
			chessParam.boardBitCol[srcCol]^=(1<<(9-srcRow));
		}else{
			chessParam.boardBitRow[srcRow]|=(1<<(8-srcCol)); 
			chessParam.boardBitCol[srcCol]|=(1<<(9-srcRow));
		}
//		System.out.println("还原棋子前Zobrist："+TranspositionTable.boardZobrist);
		tranTable.unMoveOperate(moveNode);
//		System.out.println("还原棋子后Zobrist："+TranspositionTable.boardZobrist);
		
		
	}
	private void setBoard(int site,int chess){
		if(site!=NOTHING){
			board[site]=chess;
		}
	}
	private void setChess(int chess,int site){
		if(chess!=NOTHING){
			allChess[chess]=site;
		}
	}

	
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 2:34:27 PM
	 * 功能：着法合理性判断
	 *@param play
	 *@param moveNode
	 *@return
	*/
	public boolean legalMove(int play,MoveNode moveNode){
		if(moveNode==null)
			return false;
		int srcChess = chessParam.board[moveNode.srcSite];
		int destChess = chessParam.board[moveNode.destSite];
		//原棋子不为已方棋子或目标棋子为已方棋子
		if((chessPlay[play] & srcChess)==0){
			return false;
		}
		if(destChess!=NOTHING && (chessPlay[play] & destChess)!=0){
			return false;
		}
		if(srcChess!=moveNode.srcChess || destChess!=moveNode.destChess){
			return false;
		}
		int srcSite=moveNode.srcSite;
		int destSite=moveNode.destSite;
		BitBoard bitBoard=null;
		switch (chessRoles[srcChess]) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			if(destChess!=NOTHING){ //是吃子走法
				//取出行列能攻击到的位置
				bitBoard=BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[srcSite][row], ChariotBitBoardOfAttackCol[srcSite][col]);
			}else{
				bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			}
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//取出被别马腿的位置
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()]);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			//取出行列能攻击到的位置
			if(destChess!=NOTHING){ //是吃子走法
				bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[srcSite][row], GunBitBoardOfAttackCol[srcSite][col]);
			}else{
				bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			}
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//取出被塞象眼的位置
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()]);
			break;
		case REDKING:
		case BLACKKING:
			//将吃子着法
			bitBoard=new BitBoard(KingBitBoard[srcSite]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			bitBoard=new BitBoard(GuardBitBoard[srcSite]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			bitBoard=new BitBoard(SoldiersBitBoard[play][srcSite]);
			break;
		default :
			System.out.println("没有这个棋子:"+srcSite);
		}
		bitBoard.assignAnd(MaskChesses[destSite]);
		if(!bitBoard.isEmpty()){
			return true;
		}else{
			return false;
		}
		
	}

	private static int[][] knights=new int[][]{{35,36},{19,20}};
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 5:36:05 PM
	 * 功能： 将军判断
	 *@param play
	 *@return
	*/
	public boolean checked(int play){
		int opponentPlay=1-play;
		//对方将被干了
		if(chessParam.allChess[chessPlay[opponentPlay]]==NOTHING){
			return false;
		} 
		int kingSite = chessParam.allChess[chessPlay[play]];
		
		int row=chessParam.boardBitRow[boardRow[kingSite]];
		int col=chessParam.boardBitCol[boardCol[kingSite]];
		
		//车将军
		BitBoard bitBoard = BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[kingSite][row], ChariotBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.CHARIOT));
		if(!bitBoard.isEmpty()){
			return true;
		}
		//将对脸
		if(!BitBoard.assignAndToNew(ChariotBitBoardOfAttackCol[kingSite][col],MaskChesses[chessParam.allChess[chessPlay[1-play]]]).isEmpty()){
			return true;
		}
		
		//炮将军
		bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[kingSite][row], GunBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.GUN));
		if(!bitBoard.isEmpty()){
			return true;
		}
		//马将军
		//取出马不考虑别腿能走到的位置
		bitBoard =new BitBoard(KnightBitBoards[kingSite]);
		int opponentKnight1 = knights[play][0],opponentKnight2 = knights[play][1];;
		int knight1Site=chessParam.allChess[opponentKnight1],knight2Site=chessParam.allChess[opponentKnight2];
		//不考虑别腿看是否能将军
		if(!BitBoard.assignAndToNew(chessParam.getBitBoardByPlayRole(opponentPlay,ChessConstant.KNIGHT),bitBoard).isEmpty()){
			//马1
			if(knight1Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight1Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight1Site],chessParam.maskBoardChesses);
				//看马1能攻击到的位置是否有将
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight1Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					return true;
				}
			}
			//马2
			if(knight2Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight2Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight2Site],chessParam.maskBoardChesses);
				//看马1能攻击到的位置是否有将
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight2Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					return true;
				}
			}
		}
		//兵将军的判断
		if(!BitBoard.assignAndToNew(KingCheckedSoldierBitBoards[kingSite],chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.SOLDIER)).isEmpty()){
			return true;
		}
		return false;
		
	}
	/**
	 *@author pengjiu 
	 *@date:Sep 20, 2011 2:13:54 PM
	 * 功能： 被几个棋子将军
	 *@param play
	 *@return
	*/
	public int chkNum(int play){
		int opponentPlay=1-play;
		int chkNum=0;
		//对方将被干了
		if(chessParam.allChess[chessPlay[opponentPlay]]==NOTHING){
			return chkNum;
		} 
		int kingSite = chessParam.allChess[chessPlay[play]];
		
		int row=chessParam.boardBitRow[boardRow[kingSite]];
		int col=chessParam.boardBitCol[boardCol[kingSite]];
		
		//车将军
		BitBoard bitBoard = BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[kingSite][row], ChariotBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.CHARIOT));
		if(!bitBoard.isEmpty()){ 
			chkNum++;
		}
		
		//炮将军
		bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[kingSite][row], GunBitBoardOfAttackCol[kingSite][col]);
		bitBoard.assignAnd(chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.GUN));
		if(!bitBoard.isEmpty()){ 
			chkNum++;
		}
		//马将军
		//取出马不考虑别腿能走到的位置
		bitBoard =new BitBoard(KnightBitBoards[kingSite]);
		int opponentKnight1 = knights[play][0],opponentKnight2 = knights[play][1];;
		int knight1Site=chessParam.allChess[opponentKnight1],knight2Site=chessParam.allChess[opponentKnight2];
		//不考虑别腿看是否能将军
		if(!BitBoard.assignAndToNew(chessParam.getBitBoardByPlayRole(opponentPlay,ChessConstant.KNIGHT),bitBoard).isEmpty()){
			//马1
			if(knight1Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight1Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight1Site],chessParam.maskBoardChesses);
				//看马1能攻击到的位置是否有将
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight1Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					chkNum++;
				}
			}
			//马2
			if(knight2Site!=NOTHING && !BitBoard.assignAndToNew(MaskChesses[knight2Site],bitBoard).isEmpty()){
				BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[knight2Site],chessParam.maskBoardChesses);
				//看马1能攻击到的位置是否有将
				if(!BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[knight2Site][legBoard.checkSumOfKnight()],MaskChesses[kingSite]).isEmpty()){
					chkNum++;
				}
			}
		}
		//兵将军的判断
		if(!BitBoard.assignAndToNew(KingCheckedSoldierBitBoards[kingSite],chessParam.getBitBoardByPlayRole(opponentPlay, ChessConstant.SOLDIER)).isEmpty()){
			chkNum++;
		}
		return chkNum;
	}
	public void setMoveNodeList(MoveNodeList generalMoveList,MoveNodeList goodMoveList,MoveNodeList repeatMoveList,BitBoard oppAttackSite){
		this.generalMoveList=generalMoveList;
		this.goodMoveList=goodMoveList;
		this.repeatMoveList=repeatMoveList;
		this.oppAttackSite=oppAttackSite;
	}
	public MoveNodeList getGeneralMoveList(){
		return generalMoveList;
	}
	public MoveNodeList getGoodMoveList(){
		return goodMoveList;
	}
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 11:01:45 AM
	 * 功能：生成所有吃子着法 note：(但所吃子的价值低于一定值时将不算为吃子着法中)
	 *@param play
	 *@param dumpMoveList
	*/
	public void genEatMoveList(int play){
		int begin=chessPlay[play];
		int end=begin+16;
		for(int i=begin+1;i<end;i++){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				 this.chessEatMove(chessRoles[i], chessSite, play);
			}
		}
		this.chessEatMove(chessRoles[begin], allChess[begin], play);
	 }
	
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 11:01:19 AM
	 * 功能：生成不吃子着法列表
	 *@param play 玩家
	 *@param dumpMoveList  
	*/
	public void genNopMoveList(int play){ 
		int begin=chessPlay[play];
		int end=begin+16;
		for(int i=begin+1;i<end;i++){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				 this.chessNopMove(chessRoles[i], chessSite, play);
			}
		}
		this.chessNopMove(chessRoles[begin], allChess[begin], play);
	 }
	//note:[play][象士将]
	private static int[][] checkMateFristUseMove= new int[][]{
			{23,24,25,26,16},
			{39,40,41,42,32} };
	//note:[play][车马炮卒] 
	private static int[][] checkMateSecondlyUseMove= new int[][]{
			{17,18,19,20,21,22,27,28,29,30,31},
			{33,34,35,36,37,38,43,44,45,46,47} };
	/**
	 *@author pengjiu 
	 *@date:Aug 31, 2011 11:00:38 AM
	 * 功能：	优先解将着法,普遍将军都会被化解 note:象士将 
	 *@param play
	 *@param dumpMoveList
	*/
	/*public void genFristMoveListCheckMate(int play ){ 
		int[] move=checkMateFristUseMove[play];
		for(int i:move){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				 this.chessAllMove(chessRoles[i], chessSite, play);
			}
		} 
	 }*/

	/**对手所有能攻击的位置
	 * @param play
	 */
	public BitBoard getOppAttackSite(int play ) {
		BitBoard oppAttack=new BitBoard();
		int begin=chessPlay[1-play];
		int end=begin+16;
		for(int i=begin;i<end;i++){
			int chessSite = allChess[i];
			if(chessSite!=NOTHING){
				oppAttack.assignOr(this.chessAttackSite(chessRoles[i], chessSite, 1-play));
			}
		}
		return oppAttack;
	}
	public BitBoard chessAttackSite(int chessRole, int srcSite, int play) {
		BitBoard bitBoard=null;
		switch (chessRole) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			//取出行列能攻击到的位置
			bitBoard=BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[srcSite][row], ChariotBitBoardOfAttackCol[srcSite][col]);			
			bitBoard.assignXor(BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]));
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//取出被别马腿的位置
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()]);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			//取出行列能攻击到的位置
			bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[srcSite][row], GunBitBoardOfAttackCol[srcSite][col]);
			//能走到的位置
//			bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			//炮伪攻击位置
			bitBoard.assignXor(BitBoard.assignXorToNew(GunBitBoardOfFakeAttackRow[srcSite][row], GunBitBoardOfFakeAttackCol[srcSite][col]));
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//取出被塞象眼的位置
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			bitBoard=new BitBoard(ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()]);
			break;
		case REDKING:
		case BLACKKING:
			//将吃子着法
			bitBoard=new BitBoard(KingBitBoard[srcSite]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			bitBoard=new BitBoard(GuardBitBoard[srcSite]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			bitBoard=new BitBoard(SoldiersBitBoard[play][srcSite]);
			break;
		default :
			System.out.println("没有这个棋子:"+srcSite);
		}
		return bitBoard;
	}
	public void chessEatMove(int chessRole, int srcSite, int play) {
		BitBoard bitBoard=null;
		switch (chessRole) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			
			//取出行列能攻击到的位置
			bitBoard=BitBoard.assignXorToNew(ChariotBitBoardOfAttackRow[srcSite][row], ChariotBitBoardOfAttackCol[srcSite][col]);
			//取出攻击到的对方棋子
			bitBoard.assignAnd(chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//取出被别马腿的位置
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//取出能攻击到对方棋子位置
			bitBoard=BitBoard.assignAndToNew(KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			//取出行列能攻击到的位置
			bitBoard=BitBoard.assignXorToNew(GunBitBoardOfAttackRow[srcSite][row], GunBitBoardOfAttackCol[srcSite][col]);
			//取出攻击到的对方棋子
			bitBoard.assignAnd(chessParam.maskBoardPersonalChesses[1-play]);
			
			
			
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//取出被塞象眼的位置
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//取出能攻击到对方棋子位置
			bitBoard=BitBoard.assignAndToNew(ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDKING:
		case BLACKKING:
			//将吃子着法
			bitBoard=BitBoard.assignAndToNew(KingBitBoard[srcSite],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			bitBoard=BitBoard.assignAndToNew(GuardBitBoard[srcSite],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			bitBoard=BitBoard.assignAndToNew(SoldiersBitBoard[play][srcSite],chessParam.maskBoardPersonalChesses[1-play]);
			break;
		default :
			System.out.println("没有这个棋子:"+srcSite);
		}
		int destSite;
		while((destSite = bitBoard.MSB(play))!=-1){
			savePlayChess(srcSite, destSite, play);
			//去掉刚保存的数据
			bitBoard.assignXor(MaskChesses[destSite]);
		}
	}
	public void chessNopMove(int chessRole, int srcSite, int play) {
		BitBoard bitBoard=null;
		switch (chessRole) {
		case REDCHARIOT:
		case BLACKCHARIOT:
			int row=chessParam.boardBitRow[boardRow[srcSite]];
			int col=chessParam.boardBitCol[boardCol[srcSite]];
			//取出行列能走到的位置
			bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			break;
		case REDKNIGHT:
		case BLACKKNIGHT:
			//取出被别马腿的位置
			BitBoard legBoard = BitBoard.assignAndToNew(KnightLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//马能着到的位置
			BitBoard attackBoard=KnightBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfKnight()];
			//走到的位置有棋子的
			bitBoard=BitBoard.assignAndToNew(attackBoard,chessParam.maskBoardChesses);
			//拿出走到的位置没有棋子的
			bitBoard.assignXor(attackBoard);
			break;
		case REDGUN:
		case BLACKGUN:
			row=chessParam.boardBitRow[boardRow[srcSite]];
			col=chessParam.boardBitCol[boardCol[srcSite]];
			bitBoard=BitBoard.assignXorToNew(MoveChariotOrGunBitBoardRow[srcSite][row], MoveChariotOrGunBitBoardCol[srcSite][col]);
			break;
		case REDELEPHANT:
		case BLACKELEPHANT:
			//取出被塞象眼的位置
			legBoard=BitBoard.assignAndToNew(ElephanLegBitBoards[srcSite],chessParam.maskBoardChesses);
			//象能着到的位置
			attackBoard=ElephanBitBoardOfAttackLimit[srcSite][legBoard.checkSumOfElephant()];
			//走到的位置有棋子的
			bitBoard=BitBoard.assignAndToNew(attackBoard,chessParam.maskBoardChesses);
			//拿出走到的位置没有棋子的
			bitBoard.assignXor(attackBoard);
			break;
		case REDKING:
		case BLACKKING:
			//将着到的位置有棋子的
			bitBoard=BitBoard.assignAndToNew(KingBitBoard[srcSite],chessParam.maskBoardChesses);
			//将着到的位置没有棋子的
			bitBoard.assignXor(KingBitBoard[srcSite]);
			break;
		case REDGUARD:
		case BLACKGUARD:
			//走的位置有棋子
			bitBoard=BitBoard.assignAndToNew(GuardBitBoard[srcSite],chessParam.maskBoardChesses);
			//取出没有棋子的
			bitBoard.assignXor(GuardBitBoard[srcSite]);
			break;
		case REDSOLDIER:
		case BLACKSOLDIER:
			//有棋子
			bitBoard=BitBoard.assignAndToNew(SoldiersBitBoard[play][srcSite],chessParam.maskBoardChesses);
			//取出无棋子
			bitBoard.assignXor(SoldiersBitBoard[play][srcSite]);
			break;
		default :
			System.out.println("没有这个棋子:"+srcSite);
		}
		int destSite;
		while((destSite = bitBoard.MSB(play))!=-1){
			savePlayChess(srcSite, destSite, play);
			//去掉刚保存的数据
			bitBoard.assignXor(MaskChesses[destSite]);
		}
	}
	
	/**
	 *@author pengjiu 
	 *@date:Aug 10, 2011 12:40:22 PM
	 * 功能：检测攻击的棋子是否为对手棋子
	 *@param srcChess
	 *@param destChess
	 *@param play
	*/
	public boolean isOpponentCheck(int destChess,int play){
		//无棋子返回
		if(destChess==NOTHING){
			return true;
		//为对手棋子	
		}
		return ((chessPlay[play] & destChess)==0);
	}
	public abstract void savePlayChess(int srcSite,int destSite,int play);
}
