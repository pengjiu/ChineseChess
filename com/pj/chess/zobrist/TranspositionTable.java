package com.pj.chess.zobrist;

import static com.pj.chess.ChessConstant.*;
import static com.pj.chess.ChessBoardMain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.pj.chess.NodeLink;
import com.pj.chess.Tools;
import com.pj.chess.chessmove.MoveNode;
import com.pj.chess.movelist.MoveNodeList;

public class TranspositionTable {
	public static Map<Object,List<MoveNode>> fenLib=new HashMap<Object,List<MoveNode>>();
	//1 一直覆盖  0深度覆盖
	private static final int OVERRIDESTRAIGHT=1, OVERRIDESTEP=0;
	
	public  static long boardZobristStatic64;

	public  static int boardZobristStatic32;
	
	public  long boardZobrist64;

	public  int boardZobrist32;
	
	public static  int TRANZOBRISTSIZE=0;
	
	public static   HashItem[][][]  tranZobrist;

	public static final int hashBeta = 1;

	public static final int hashAlpha = 2;

	public static final int hashPV = 3;
	
	public static final int FAIL=Integer.MIN_VALUE+1;
	//命中开局库
	public static final int FEN=Integer.MIN_VALUE;
	static{
		InitZobristList32And64.initChessZobristList32();
		InitZobristList32And64.initChessZobristList64();
	}
	public TranspositionTable(int boardZobrist32,long boardZobrist64){
		this.boardZobrist32=boardZobrist32;
		this.boardZobrist64=boardZobrist64;
	}
	public TranspositionTable(){
		this.synchroStaticZobristBoardToThis();
//		initTranZobrist();
	}
	public static void setDefaultHashSize(){
		if(tranZobrist==null){
			setHashSize(0x7FFFF);
		}
	}
	public static void setHashSize(int tranzobristsize){
		TRANZOBRISTSIZE=tranzobristsize;
		tranZobrist=new HashItem[2][TRANZOBRISTSIZE][2];
	}
	/**
	 * 设置上次的置换表为过期数据
	 */
	public static void cleanTranZobrist() {
		for (int i = 0; i < TRANZOBRISTSIZE; i++) {
			
			if(tranZobrist[0][i][OVERRIDESTEP]!=null){
				tranZobrist[0][i][OVERRIDESTEP].isExists=false;
			}
			if(tranZobrist[1][i][OVERRIDESTEP]!=null){
				tranZobrist[1][i][OVERRIDESTEP].isExists=false;
			}
		}
		
	}

	/**
	 * 生成当前局面
	 */
	public static void genStaticZobrist32And64OfAllChess(int allChess[]) {
		for (int i = 0; i < allChess.length; i++) {
			if (allChess[i] > NOTHING) {
				int site = allChess[i];
				boardZobristStatic64 ^= ChessZobristList64[site][chessRoles[i]];
				boardZobristStatic32 ^= ChessZobristList32[site][chessRoles[i]];
			}
		}
	}
	/********************静态的局面****************************/
	public static void genStaticZobrist32And64OfBoard(int board[]) {
		for (int i = 0; i < board.length; i++) {
			if (board[i] > NOTHING) {
				int chess = board[i];
				boardZobristStatic64 ^= ChessZobristList64[i][chessRoles[chess]];
				boardZobristStatic32 ^= ChessZobristList32[i][chessRoles[chess]];
//				System.out.println(boardZobristStatic32+"\t->"+i);
			}
		}
	}
	public  static void genZobrist32And64OfAllChess(int allChess[]) {
		for (int i = 0; i < allChess.length; i++) {
			if (allChess[i] > NOTHING) {
				int site = allChess[i];
				boardZobristStatic64 ^= ChessZobristList64[site][chessRoles[i]];
				boardZobristStatic32 ^= ChessZobristList32[site][chessRoles[i]];
			}
		}
	}
	/************************************************/
	public  void genZobrist32And64OfBoard(int board[]) {
		for (int i = 0; i < board.length; i++) {
			if (board[i] > NOTHING) {
				int chess = board[i];
				boardZobrist64 ^= ChessZobristList64[i][chessRoles[chess]];
				boardZobrist32 ^= ChessZobristList32[i][chessRoles[chess]];
//				System.out.println(boardZobrist32+"\t->"+i);
			}
		}

	}
	public void synchroZobristBoardToStatic(){
		boardZobristStatic64=boardZobrist64;
		boardZobristStatic32=boardZobrist32;
	}
	public void synchroStaticZobristBoardToThis(){
		boardZobrist64=boardZobristStatic64;
		boardZobrist32=boardZobristStatic32;
	}
	/**
	 * 置换表key改变
	 */
	public  void moveOperate(MoveNode moveNode) {
		
		int srcSite = moveNode.srcSite;
		int destSite = moveNode.destSite;
		int srcChess = moveNode.srcChess;
		int destChess = moveNode.destChess;
		 
		boardZobrist64 ^= ChessZobristList64[srcSite][chessRoles[srcChess]];
		boardZobrist32 ^= ChessZobristList32[srcSite][chessRoles[srcChess]];
		if (destChess != NOTHING) {
			boardZobrist64 ^= ChessZobristList64[destSite][chessRoles[destChess]];
			boardZobrist32 ^= ChessZobristList32[destSite][chessRoles[destChess]];
		}
		boardZobrist64 ^= ChessZobristList64[destSite][chessRoles[srcChess]];
		boardZobrist32 ^= ChessZobristList32[destSite][chessRoles[srcChess]];
		
	}

	/**
	 * 置换表key改变
	 */
	public  void unMoveOperate(MoveNode moveNode) {
		int srcSite = moveNode.destSite;
		int srcChess = moveNode.destChess;
		int destSite = moveNode.srcSite;
		int destChess = moveNode.srcChess;
		boardZobrist64 ^= ChessZobristList64[destSite][chessRoles[destChess]];
		boardZobrist32 ^= ChessZobristList32[destSite][chessRoles[destChess]];
		if (srcChess != NOTHING) {
			boardZobrist64 ^= ChessZobristList64[srcSite][chessRoles[srcChess]];
			boardZobrist32 ^= ChessZobristList32[srcSite][chessRoles[srcChess]];
		}
		if (destChess != NOTHING) {
			boardZobrist64 ^= ChessZobristList64[srcSite][chessRoles[destChess]];
			boardZobrist32 ^= ChessZobristList32[srcSite][chessRoles[destChess]];
		}
	}


	public void isCheckMate(){
		
	}	
	public  void setRootTranZobrist(int play,MoveNode moveNode) {
		int x = boardZobrist32 & TRANZOBRISTSIZE;
		HashItem hi0 = tranZobrist[play][x][OVERRIDESTEP];
		if(hi0==null){
			hi0=new HashItem();
		}
		hi0.checkSum = boardZobrist64;
		hi0.moveNode = moveNode;
	}
	public  void setRootTranZobrist2(int play,MoveNode moveNode) {
		int x = boardZobrist32 & TRANZOBRISTSIZE;
		HashItem hi0 = tranZobrist[play][x][OVERRIDESTRAIGHT];
		if(hi0==null){
			hi0=new HashItem();
		}
		hi0.checkSum = boardZobrist64;
		hi0.moveNode = moveNode;
	}
	private static final Object lock=new Object();
	/**
	 * 深度优先 始终覆盖过期数据
	 */
	/*public void setTranZobrist(int boardZobrist32, long boardZobrist64,
			int entry_type, int value, int depth, int play, MoveNode moveNode,
			int[] board, NodeLink nodeLink) {
		int x = boardZobrist32 & TRANZOBRISTSIZE;
		HashItem hi = tranZobrist[x];
		//深度小于等于现在深度
		if (hi == null || depth <= hi.depth)
			return;
		hi = new HashItem();
		hi.checkSum = boardZobrist64;
		hi.depth = depth;
		hi.entry_type = entry_type;
		hi.value = value;
		hi.moveNode = moveNode;
		hi.play=play;
		tranZobrist[x] = hi;
	}
	int  mateNode=maxScore-100;
	public int getTranZobrist(int alpha, int beta, int depth,int play, MoveNode tranGodMoveNode) {
		int x = boardZobrist32 &TRANZOBRISTSIZE; 
		HashItem hi = tranZobrist[x];
		if (hi!=null && play==hi.play && hi.checkSum == boardZobrist64) {
			int value=hi.value; 
			tranGodMoveNode=hi.moveNode;
			if(value>mateNode){
				//是否将军节点
				value-=(depth-hi.depth);
			}else if(value<-mateNode){
				value+=(depth-hi.depth);
			}else if(hi.depth < depth){
				//当是浅层的节点时返回浅层的最佳走法
				return FAIL;
			}
			String[] alterEntry=new String[]{"","上边界","下边界","精确值"};
			System.out.println("========================当次要比较数据===========================");
			for(MoveNode movenode:moveLists){
				System.out.println("\t\t原位置:"+bitBoardRow[movenode.srcSite]+"行"+bitBoardCol[movenode.srcSite]+"列  原棋子："+chessName[movenode.srcChess] +"\t目标位置："+bitBoardRow[movenode.destSite]+"行  "+bitBoardCol[movenode.destSite] +"列  目标棋子"+chessName[movenode.destChess]);
			}
			System.out.println("========================置换表中数据  ("+alterEntry[hi.entry_type]+")=========================");
			for(MoveNode movenode:hi.moveNodes){
				System.out.println("\t\t原位置:"+bitBoardRow[movenode.srcSite]+"行"+bitBoardCol[movenode.srcSite]+"列  原棋子："+chessName[movenode.srcChess] +"\t目标位置："+bitBoardRow[movenode.destSite]+"行  "+bitBoardCol[movenode.destSite] +"列  目标棋子"+chessName[movenode.destChess]);
			}
			switch (hi.entry_type) {
			// 精确值
			case EXACT:
				return value;
				// 下边界(因为是剪枝的结果所以只要当前置换表中的值大于当前的剪枝条件 (>beta) )
			case LOWER_BOUND:
				if (value>= beta) {
					return value;
				}
				break;
			// 上边界
			case UPPER_BOUND:
				if (value <= alpha) {
					return value;
				}
				break;
			}
		}
		return FAIL;
	}*/
	/*
	 * 使终覆盖策略
	 */
	public boolean setTranZobristOverride(int entry_type, int value, int depth, int play, MoveNode moveNode,int x,HashItem hi0){
		if(hi0!=null){
			tranZobrist[play][x][OVERRIDESTRAIGHT]=hi0;
		}else{
			HashItem hi1 = tranZobrist[play][x][OVERRIDESTRAIGHT];
			if (hi1==null){
				hi1 = new HashItem();
			}
			hi1.checkSum = boardZobrist64;
			hi1.depth = depth;
			hi1.entry_type = entry_type;
			hi1.value = value;
			if(moveNode!=null){
				hi1.moveNode = moveNode;
			}
			hi1.isExists=true;
			tranZobrist[play][x][OVERRIDESTRAIGHT] = hi1;
		}
		return true;
	}
	/*
	 * 深度覆盖策略
	 */
	public HashItem setTranZobristOverrideByStep(int entry_type, int value, int depth, int play, MoveNode moveNode,int x){
		HashItem hi1=null; 
		HashItem hi0 = tranZobrist[play][x][OVERRIDESTEP];
		if (hi0==null){
			hi0 = new HashItem();
		}else if(hi0.isExists){ //为最新节点
			//比之前存储的节点小直接返回
			if(hi0.depth>depth){
				return null;
			}
			else{ //比之前存储的节点大，保存当前，将之前节点返回给始终覆盖策略
				hi1=hi0;
				hi0 = new HashItem();
			}
		}else{
			hi1=hi0;
			hi0 = new HashItem();
		}
		hi0.checkSum = boardZobrist64;
		hi0.depth = depth;
		hi0.entry_type = entry_type;
		hi0.value = value;
		if(moveNode!=null){
			hi0.moveNode = moveNode;
		}
		hi0.isExists=true;
		tranZobrist[play][x][OVERRIDESTEP] = hi0;
		return hi1;
	}
	/*
	 * 存储置换表
	 */
	public void setTranZobrist(int entry_type, int value, int depth, int play, MoveNode moveNode) {
		if((value>=8000 && value<=9000) || (value>=-9000 && value<=-8000)){ //长将不存
			return;
		}
		int x = boardZobrist32 & TRANZOBRISTSIZE;
		HashItem hi0 = this.setTranZobristOverrideByStep(entry_type, value, depth, play, moveNode,x);
		//深度策略中没有被覆盖
//		if(hi0!=null){
			//执行使终覆盖
		this.setTranZobristOverride(entry_type, value, depth, play, moveNode,x,hi0);
//		}
	}
	int  mateNode=maxScore-100;
	/*
	 * 从置换表获取
	 */
	public int getTranZobrist(int alpha, int beta, int depth,int play, MoveNodeList tranGodMoveNode,int[] value) {
		int x = boardZobrist32 &TRANZOBRISTSIZE;
		int result0=0;
		HashItem hi =getTranZobristOverrideByStep(play,x);
		tranGodMoveNode.size=1;
		if(hi!=null){
			result0= this.getTranZobristByHashItem(hi, depth, alpha, beta);
			if(result0!=FAIL){
				return result0;
			}
			tranGodMoveNode.set(0,hi.moveNode);
			value[0]=hi.value;
		}
		HashItem hi2 = getTranZobristOverride(play,x);
		if(hi2!=null){
			int result1 = this.getTranZobristByHashItem(hi2, depth, alpha, beta);
			if(result1!=FAIL){
				return result1;
			}
			tranGodMoveNode.set(0, hi2.moveNode);
			if(hi==null ||hi.depth<hi2.depth ){
				value[0]=hi2.value;
			}
		}
		return FAIL;
	}
	public int getTranZobristByHashItem(HashItem hi,int depth,int alpha,int beta){
		int value = hi.value;
		if (value > mateNode) {
			// 是否将军节点
			value -= (depth - hi.depth);
		} else if (value < -mateNode) {
			value += (depth - hi.depth);
		} 
		else if (hi.depth < depth) {
			// 当是浅层的节点时返回浅层的最佳走法
			return FAIL;
		}
		switch (hi.entry_type) {
		// 精确值
		case hashPV:
			return value;
			// 下边界(因为是剪枝的结果所以只要当前置换表中的值大于当前的剪枝条件 (>beta) )
		case hashBeta:
			if (value >= beta) {
				return value;
			}
			break;
		// 上边界
		case hashAlpha:
			if (value <= alpha) {
				return value;
			}
			break;
		}
		return FAIL;
	}
	/*
	 * 获取深度覆盖中的值
	 */
	public HashItem getTranZobristOverrideByStep(int play,int x){
		HashItem hashItem0 = tranZobrist[play][x][OVERRIDESTEP];
		if(hashItem0!=null  && hashItem0.checkSum==boardZobrist64){
			return hashItem0;
		}else{
			return null;
		}
	}
	/*
	 * 获取使终覆盖中的值
	 */
	public HashItem getTranZobristOverride(int play,int x){
		HashItem hashItem1 = tranZobrist[play][x][OVERRIDESTRAIGHT];
		if(hashItem1!=null && hashItem1.checkSum==boardZobrist64){
			return hashItem1;
		}else{
			return null;
		}
	}
	public  void setTranZobrist(MoveNode moveNode){
		List<MoveNode> moveNodeList= fenLib.get(boardZobrist64);
		if(moveNodeList==null){
			moveNodeList=new ArrayList<MoveNode>(5);
		}
		moveNodeList.add(moveNode);
		fenLib.put(boardZobrist64,moveNodeList);
	}
	//开局库是否有这个走法
	public static MoveNode getTranZobristFen(int play) {
		List<MoveNode> moveNodeList = fenLib.get(boardZobristStatic64);
		if (moveNodeList != null) {
			int randSize = moveNodeList.size();
			int index = new Random().nextInt(randSize);
			return (MoveNode)moveNodeList.get(index) ;
		} else {
			return null;
		}
	}
}
