package com.pj.chess.searchengine;
 
import static com.pj.chess.ChessConstant.BLACKPLAYSIGN;
import static com.pj.chess.ChessConstant.CHARIOT;
import static com.pj.chess.ChessConstant.GUN;
import static com.pj.chess.ChessConstant.KNIGHT;
import static com.pj.chess.ChessConstant.MAXDEPTH;
import static com.pj.chess.ChessConstant.MaskChesses;
import static com.pj.chess.ChessConstant.NOTHING;
import static com.pj.chess.ChessConstant.REDPLAYSIGN;
import static com.pj.chess.ChessConstant.chessRoles;
import static com.pj.chess.ChessConstant.drawScore;
import static com.pj.chess.ChessConstant.LONGCHECKSCORE;
import static com.pj.chess.ChessConstant.maxScore;
 
import static com.pj.chess.ChessConstant.chessPlay; 

import java.util.ArrayList;
import java.util.List;

import com.pj.chess.BitBoard;
import com.pj.chess.ChessConstant;
import com.pj.chess.NodeLink;
import com.pj.chess.Tools;
import com.pj.chess.chessmove.ChessMovePlay;
import com.pj.chess.chessmove.ChessQuiescMove;
import com.pj.chess.chessmove.MoveNode;
import com.pj.chess.chessmove.MoveNodesSort;
import com.pj.chess.chessparam.ChessParam;
import com.pj.chess.evaluate.EvaluateCompute;
import com.pj.chess.evaluate.EvaluateComputeMiddle;
import com.pj.chess.evaluate.EvaluateComputeMiddleGame;
import com.pj.chess.history.CHistoryHeuritic;
import com.pj.chess.zobrist.TranspositionTable;

public abstract class SearchEngine implements Runnable{
	public volatile boolean isStop=false;
	public int stopDepth=0;
	public int countDepth=MAXDEPTH;
	public int count=0,mtdfV;
	public MoveNode computeMoveNode=null;
	//置换表
	public TranspositionTable  transTable;
	public CHistoryHeuritic cHistorySort=new CHistoryHeuritic();
	public ChessParam chessParam ;
	public NodeLink moveHistory;
	MoveNode[][] killerMove=new MoveNode[64][2];
	public ChessMovePlay chessMove ;
	public ChessQuiescMove chessQuiescMove; 
	EvaluateCompute evaluate;
	//      将军延伸的权值
	int R=0,CheckedStretch=14;
	public static  final int[] chessStretchNum=new int[]{ 
		0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		0,3,3,2,2,2,2,2,2,2,2,1,1,1,1,1,
		0,3,3,2,2,2,2,2,2,2,2,1,1,1,1,1,
	};
	public int StretchNeedNum=0;//延伸所需要的数量
	
	public SearchEngine(ChessParam chessParam,EvaluateCompute evaluate,TranspositionTable  transTable,NodeLink moveHistory){
		this.transTable=transTable;
		this.chessParam=chessParam;
		chessMove= new ChessMovePlay(chessParam,transTable,evaluate);
		chessQuiescMove= new ChessQuiescMove(chessParam,transTable,evaluate);
		this.evaluate=evaluate;
		this.moveHistory=moveHistory;
		initChessSiteScore();
	}
	private void initChessSiteScore(){
		int[] s=getChessBaseScore();
		chessParam.baseScore[0]=s[0];
		chessParam.baseScore[1]=s[1];
	}
	public int[] getChessBaseScore(){
		int[] allChess = chessParam.allChess;
		int[] board = chessParam.board;
		int s[]=new int[2];
		for (int i = 16; i < allChess.length; i++) {
			if(allChess[i]!=NOTHING){   
				int chessRole = chessRoles[board[allChess[i]]];
				int play=i < 32?BLACKPLAYSIGN:REDPLAYSIGN;
				s[play]+=evaluate.chessAttachScore(chessRole,allChess[i]);
				s[play]+=EvaluateCompute.chessBaseScore[i];
			}
		} 
		return s;
	}
	public void setStretchNeedNumByDepth(int depth){
		if(depth>=7){
			StretchNeedNum=23;
		}else if(depth>=6){
			StretchNeedNum=19;
		}else{
			StretchNeedNum=15;
		}
	}
	
	public abstract int searchMove(int alpha, int beta, int depth);
	public  int alphaBetaSearchExt(int alpha, int beta, int depth,List<MoveNode> moveNodes) {
		return 0;
	}
	public int RAdapt(int depth) {
		//根据不同情况来调整R值的做法,称为“适应性空着裁剪”(Adaptive Null-Move Pruning)，
		//a. 深度小于或等于6时，用R = 2的空着裁剪进行搜索
		//b. 深度大于8时，用R = 3；  
		if (depth <= 6) {
			return 2;
		} else if(depth<=8){
			return 3;
		}else{
			return 4;
		}
	}
	
	public int fineEvaluate(int play){
		count++; 
		return evaluate.evaluate(play);
	}
	/**粗略评价
	 * @param play
	 * @return
	 */
	public int roughEvaluate(int play){
		return chessParam.baseScore[play]-chessParam.baseScore[1-play];
	}
	public int testLink(NodeLink nodeLink){
		if(nodeLink==null){
			 return 0;
		}
//		StringBuilder sb=new StringBuilder();
//		NodeLink firstLink = nodeLink;
//		while(firstLink.getLastLink()!=null){
//			firstLink=firstLink.getLastLink();
//			
//		}
		

		List<MoveNode> unMovenode=new ArrayList();
		NodeLink nextLink = nodeLink.getNextLink();
		while(nextLink!=null){
				MoveNode movenode=nextLink.getMoveNode();
				chessQuiescMove.moveOperate(movenode);
				nextLink=nextLink.getNextLink();
				unMovenode.add(movenode);
		}
		int score=this.fineEvaluate(1-nodeLink.play);
		int s[]=this.getChessBaseScore(); 
		if((s[1-nodeLink.play]-s[nodeLink.play])!=score){
			System.err.println("分数->"+(s[1-nodeLink.play]-s[nodeLink.play]));
		}
		for(int i=unMovenode.size()-1;i>=0;i--){
			chessQuiescMove.unMoveOperate(unMovenode.get(i));
		}
		return score;
	}
	//交换走棋方
	public int swapPlay(int currplay){
		return 1-currplay;
	}
	/*
	 * 静态搜索
	 */
	public int quiescSearch(int alpha, int beta,NodeLink lastLink,boolean isChecked){
		int play = swapPlay(lastLink.play);
		//自己将被吃
		if (chessParam.allChess[chessPlay[play]] == NOTHING) {
			return -(maxScore-lastLink.depth);
		}
		boolean isMove=false;
//		是否被将军
//		boolean isChecked = chessQuiescMove.checked(play); 
		//设置前上步是否将军
		lastLink.chk=isChecked; 
		if(isLongChk(lastLink)){ //判断长将
			return LONGCHECKSCORE;
		}
		//和棋
		if(isDraw(lastLink)){
			return drawScore;
		}
		int thisValue = 0, bestValue = -ChessConstant.maxScore-2;
		//达到上限
		if(lastLink.depth>=64){
			return fineEvaluate(play);
		}
		if (!isChecked) {
			isMove= true;
			thisValue = fineEvaluate(play);
			if (thisValue > bestValue) {
				if (thisValue >= beta) {
						return thisValue;	
				}
				bestValue = thisValue;
				if (thisValue > alpha) {
					alpha = thisValue;
				}
			}
			/*if(!lastLink.isNullMove && chessParam.getAttackChessesNum(play)>2){
				NodeLink nodeLinkNULL = new NodeLink(play,true,transTable.boardZobrist32,transTable.boardZobrist64);
				int val = -quiescSearch( -beta, -beta + 1,nodeLinkNULL,false);
				if(val>=beta){
					return val;
				}
			}*/
		} 
		MoveNodesSort moveNodeSort = new MoveNodesSort(play,chessQuiescMove,isChecked);
		MoveNode moveNode=null;
		NodeLink nodeLinkTemp,godNodeLink = null;
		
		while((moveNode=moveNodeSort.quiescNext())!=null && !moveNodeSort.isOver()){
			chessQuiescMove.moveOperate(moveNode);
			//走完自己被将军
			if(chessQuiescMove.checked(play)){
				chessQuiescMove.unMoveOperate(moveNode);
				continue;
			}
			nodeLinkTemp = new NodeLink(play,moveNode,transTable.boardZobrist32,transTable.boardZobrist64,true);
			nodeLinkTemp.setLastLink(lastLink);
			thisValue = -quiescSearch(-beta, -alpha,nodeLinkTemp,chessQuiescMove.checked(1-play));
			chessQuiescMove.unMoveOperate(moveNode);
			isMove=true;
			if (thisValue > bestValue) {
				bestValue = thisValue;
				godNodeLink=nodeLinkTemp;
				if (thisValue > alpha) {
					alpha = thisValue;
				}
				if (thisValue >= beta) {
					break;
				}
			}
		}
		if(isMove){
			lastLink.setNextLink(godNodeLink);
			return bestValue;
		}else{
			return -(maxScore-lastLink.depth);
		}
	}
	public boolean isLongChk(NodeLink lastLink ){
		if(!lastLink.chk){
			return false;
		}
//		int num=0;
		NodeLink tempLink = lastLink.getLastLink();
		while(tempLink.getMoveNode()!=null){
			if(tempLink.boardZobrist32==lastLink.boardZobrist32 && tempLink.boardZobrist64==lastLink.boardZobrist64){
//				num++;
//				return true;
//				if(num>=2){
					return true;
//				}
			} 
			if(tempLink.getMoveNode().isEatChess()){
				return false;
			}
			tempLink=tempLink.getLastLink();
		}
		return false;
	}
	/**
	 *@author pengjiu 
	 *@date:Sep 21, 2011 5:55:06 PM
	 * 功能：和棋判断
	 *@return
	*/
	protected boolean isDraw(NodeLink lastLink) {
			//无任何进攻棋子
		if(chessParam.getAttackChessesNum(REDPLAYSIGN)==0 && chessParam.getAttackChessesNum(BLACKPLAYSIGN)==0){
			return true;
		}
		return false;
	}
	protected boolean isDanger(int play){
		int opptPlay = this.swapPlay(play);
		BitBoard bitBoard=new BitBoard(chessParam.getBitBoardByPlayRole(opptPlay,CHARIOT));
		bitBoard=BitBoard.assignXorToNew(chessParam.getBitBoardByPlayRole(opptPlay,KNIGHT),bitBoard);
		bitBoard=BitBoard.assignXorToNew(chessParam.getBitBoardByPlayRole(opptPlay,GUN),bitBoard);
		bitBoard.assignAnd(DangerMarginBit[play]);
		//在禁区有超过3个棋子的认为不安全
		if(bitBoard.Count()>=3){
			return true;
		}else{
			return false;
		}
	}
	private static final int[] blackDangerMarginArray =new int[]{
		      1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1    
			 ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  
			 ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  
			 ,0  ,0  ,0  ,1  ,1  ,1  ,0  ,0  ,0   
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0   
	};
	private static final int[] redDangerMarginArray=new int[]{
			  0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 
			 ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  ,0  
			 ,0  ,0  ,0  ,1  ,1  ,1  ,0  ,0  ,0   
		     ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1    
			 ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  
			 ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1  ,1   
	};
	private static final BitBoard[] DangerMarginBit= new BitBoard[]{new BitBoard(blackDangerMarginArray),new BitBoard(redDangerMarginArray)};
}



















