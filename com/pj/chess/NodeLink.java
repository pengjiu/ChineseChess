package com.pj.chess;

import static com.pj.chess.ChessBoardMain.chessName;
import static com.pj.chess.ChessConstant.ChessZobristList64;
import static com.pj.chess.ChessConstant.boardCol;
import static com.pj.chess.ChessConstant.boardRow;
import static com.pj.chess.ChessConstant.chessRoles;

import java.io.Serializable;

import com.pj.chess.chessmove.MoveNode;
	/**棋子着法链表
	 * @author pengjiu
	 */
	public class NodeLink implements Serializable{
		private NodeLink lastLink;
		private NodeLink nextLink;
		public int depth=0;
		private MoveNode moveNode;
		public boolean isNullMove;
		public long boardZobrist64; 
		public int boardZobrist32;
		public boolean chk=false; //是否将军
		//走棋方
		public int play;
		public boolean isQuiesc;
		public NodeLink(int play,int boardZobrist32,long boardZobrist64){
			
			//默认创建状态
			this(play,false,boardZobrist32,boardZobrist64);
		}
		public NodeLink(int play,boolean isNullMove,int boardZobrist32,long boardZobrist64){
			this.play=play;
			this.isNullMove=isNullMove;
			this.boardZobrist32=boardZobrist32;
			this.boardZobrist64=boardZobrist64;
		}
		public NodeLink(int play,MoveNode moveNode,int boardZobrist32,long boardZobrist64){
			this.play=play;
			this.moveNode=moveNode;
			this.boardZobrist32=boardZobrist32;
			this.boardZobrist64=boardZobrist64;
			isQuiesc=false;
		}
		public NodeLink(int play,MoveNode moveNode,int boardZobrist32,long boardZobrist64,boolean isQuiesc){
			this.play=play;
			this.moveNode=moveNode;
			this.boardZobrist32=boardZobrist32;
			this.boardZobrist64=boardZobrist64;
			this.isQuiesc=isQuiesc; 
		}
		public void setNextLink(NodeLink nextLink){
			this.nextLink=nextLink;
			if(nextLink!=null){
				nextLink.lastLink=this;
			}
//			nextLink.depth=this.depth+1;
		}
		public  NodeLink getNextLink(){
			return nextLink;
		}
		public boolean isNullMove(){
			return isNullMove;
		}
		public MoveNode getMoveNode() {
			return moveNode;
		}
		public void setMoveNode(MoveNode moveNode) {
			this.moveNode = moveNode;
		}
		public NodeLink getLastLink() {
			return lastLink;
		}
		public void setLastLink(NodeLink previousLink) {
			this.lastLink = previousLink;
			previousLink.nextLink=this;
			this.depth=previousLink.depth+1;
		}
		public void setLastLink(NodeLink previousLink,int depth) {
			this.lastLink = previousLink;
			previousLink.nextLink=this;
			this.depth=depth;
		}
		public String toString(){
			if(this==null){
				return "the NodeLink is NULL !";
			}
			StringBuilder sb=new StringBuilder();
			NodeLink firstLink = this;
//			while(firstLink.lastLink!=null){
//				firstLink=firstLink.lastLink;
//				
//			}
			NodeLink nextLink = firstLink;
			sb.append("==========================================================\n");
			while(nextLink!=null){
					MoveNode movenode=nextLink.getMoveNode();
					sb.append(" 第->").append(nextLink.depth).append("步 ").append(movenode).append(" "+(nextLink.isQuiesc?"静态搜索":"正常搜索")+"\t"+(nextLink.chk?"将军":"无将军")+"\n");
					nextLink=nextLink.getNextLink();
			}
			sb.append("==========================================================\n");
			return sb.toString();
		}
	}
	
	
	
	
	
	
	
	
	
	
	