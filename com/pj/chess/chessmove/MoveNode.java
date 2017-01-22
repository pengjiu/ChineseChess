package com.pj.chess.chessmove;

import static com.pj.chess.ChessBoardMain.chessName;
import static com.pj.chess.ChessConstant.ChessZobristList32;
import static com.pj.chess.ChessConstant.boardCol;
import static com.pj.chess.ChessConstant.boardRow;
import static com.pj.chess.ChessConstant.chessRoles;

import com.pj.chess.ChessBoardMain;
import com.pj.chess.ChessConstant;
import com.pj.chess.history.CHistoryHeuritic;

public class MoveNode implements java.io.Serializable{
	public int destChess;
	public int srcChess;
	public int srcSite;
	public int destSite;
	public int score;
	public boolean isOppProtect=false;
//	public long boardZobrist64;
	public MoveNode(){
		
	}
//	public void setHistoryScore(int historyScore){
//		this.score=historyScore;
//	}
	public MoveNode(int srcSite,int destSite,int srcChess,int destChess,int score){
		this.srcSite=srcSite;
		this.destSite=destSite;
		this.destChess=destChess;
		this.srcChess=srcChess;
		this.score=score;
	}
	//是否有吃子
	public boolean isEatChess(){
		return destChess!=ChessConstant.NOTHING;
	}
	public String toString(){
		StringBuilder sb=new StringBuilder()
		.append("\t原位置:"+boardRow[srcSite]+"行"+boardCol[srcSite]+"列  原棋子："+chessName[srcChess] +"\t目标位置："+boardRow[destSite]+"行  "+boardCol[destSite] +"列   目标棋子："+(destChess!=ChessConstant.NOTHING?chessName[destChess]:"无 \t"));
		return sb.toString();
		
	}
	public boolean equals(MoveNode moveNode){
		return moveNode!=null 
				&&
				( moveNode==this || (this.srcSite==moveNode.srcSite && this.destSite==moveNode.destSite));
	}
}
