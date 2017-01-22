package com.pj.chess.history;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.pj.chess.ChessConstant;
import com.pj.chess.chessmove.MoveNode;
import com.pj.chess.movelist.MoveNodeList;

public class CHistoryHeuritic {
	 public static  int[][] cHistory=new int[ChessConstant.chessRoles_eight.length][256];
	 //排序最少分数
	 private static final Object lock=new Object();
	 public void setCHistoryGOOD(MoveNode moveNode,int depth){ 
		 	if(moveNode!=null){
		 		cHistory[ChessConstant.chessRoles_eight[moveNode.srcChess]][moveNode.destSite]+=2<<depth;
//		 		if(cHistory[moveNode.srcSite][moveNode.destSite]>65535){
//		 			cHistory[moveNode.srcSite][moveNode.destSite]-=10000;
//		 		}
		 	}
	 }
	 public int getCHistory(MoveNode moveNode){ 
		 	return cHistory[ChessConstant.chessRoles_eight[moveNode.srcChess]][moveNode.destSite];
	 }
	 public void setCHistoryBad(MoveNode moveNode,int depth){ 
		 	if(moveNode!=null){
		 		cHistory[ChessConstant.chessRoles_eight[moveNode.srcChess]][moveNode.destSite]-=2>>depth;
		 	}
	 }
	 public void sort(MoveNodeList moveNodeList){
		 if(moveNodeList.size>0){
			 Arrays.sort(moveNodeList.tables,0,moveNodeList.size);
		 }
	 }
 
	      
	    public static void main(String[] args){  
	         System.out.println(24>>2);
	    }
	 
	 
}
