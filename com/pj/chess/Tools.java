package com.pj.chess;

import static com.pj.chess.ChessConstant.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import sun.security.util.BitArray;

import com.pj.chess.chessmove.ChessQuiescMove;
import com.pj.chess.chessmove.MoveNode;
import com.pj.chess.chessparam.ChessParam;
import com.pj.chess.evaluate.EvaluateComputeMiddle;
import com.pj.chess.history.CHistoryHeuritic;
import com.pj.chess.movelist.MoveNodeList;
import com.pj.chess.zobrist.TranspositionTable;

public class Tools {
	public static int[] arrayCopy(int[] srcArray) {
		int length = srcArray.length;
		int[] toArray = new int[length];
		System.arraycopy(srcArray, 0, toArray, 0, length);
		return toArray;
	}
	public static String[] fenToFENArray(String fen){
		String[] fenArray= new String[8];
		Pattern p=Pattern.compile("([^\\s])++");
		Matcher m=p.matcher(fen);
		int i=0;
		while(m.find()){
			fenArray[i++]=m.group(0);
//			System.out.println(m.group(0));
		}
		return fenArray;
	}
	public static int[] parseFEN(String str){
		
		Map<Object,Integer> m=new HashMap<Object,Integer>();
		{
			m.put('k',16); //王
			m.put('r',17); //车
			m.put('n',19); //马
			m.put('b',23); //象
			m.put('a',25); //士
			m.put('c',21); //炮
			m.put('p',27); //卒
			//=============================================================
			m.put('K',32); //王
			m.put('R',33); //车
			m.put('N',35); //马
			m.put('B',39); //象
			m.put('A',41); //士
			m.put('C',37); //炮
			m.put('P',43); //卒
		}
		
		int[] board=new int[90];
		int boardIndex = 0;
		 
		String[] libArr = new String[]{str};
		int i=0;
		while(libArr[0].length()>i){
			if(libArr[0].charAt(i)>='a' && libArr[0].charAt(i)<='z'){
				int chess =m.get(libArr[0].charAt(i));
				m.put(libArr[0].charAt(i),chess+1);
				board[boardIndex]=chess;
				boardIndex++;
			}else if(libArr[0].charAt(i)>='A' && libArr[0].charAt(i)<='Z'){
				int chess =m.get(libArr[0].charAt(i));
				m.put(libArr[0].charAt(i),chess+1);
				board[boardIndex]=chess;
//				System.out.println(boardIndex+"  "+chess);
				boardIndex++;
			}else if(libArr[0].charAt(i)>='0' && libArr[0].charAt(i)<='9'){
				boardIndex+=Integer.valueOf(libArr[0].charAt(i)+"");
			}else if(libArr[0].charAt(i)=='/'){
//				boardIndex+=7;
			} 
			i++;
		}
		return board;
	}
	public static void main(String[] args) {
		System.out.println(System.getProperty("file.separator"));
	}

	public static int[] exchange(int[] srcArray) { 
		int[] temp = arrayCopy(srcArray);
		for (int srcSite = 48; srcSite < 8 * 16; srcSite++) {
			int row = srcSite / 16;
			int col = srcSite % 16;
			int destSite = (15 - row) * 16 + col;
			int srcSiteTrue=boardMap[srcSite];
			int destSiteTrue=boardMap[destSite];
			int t = temp[srcSiteTrue];
			temp[srcSiteTrue] = temp[destSiteTrue];
			temp[destSiteTrue] = t;
		}
		return temp;
	}

 
	public static  boolean isBoardTo255(int site){
		int row = site/16;
		int col=site%16;
		if(row>=3 && row<=12 && col>=3 && col<=11){ 
			return true;
		}
		return false;
	}
	public static void printBoard(int[] board){
		for(int k=0;k<board.length;k++){
			if(k%9==0){
				System.out.println();
			}
			int chessIndex=0;
			if(board[k]!=NOTHING){
				chessIndex=board[k];
			}	
			System.out.print(ChessBoardMain.chessName[chessIndex]+"\t");
			
		}
		System.out.println();
	}
	
	

	public static void writeToFile(String fen){
		java.io.BufferedOutputStream buff=null;
		try {
			buff=new  java.io.BufferedOutputStream(new java.io.FileOutputStream("c://1.txt"));
			buff.write(fen.getBytes());
			buff.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				buff.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void saveFEN(int[] board,NodeLink backMove){
 
		
		String[] sFen=new String[]{"","P","A","B","C","N","R","K","p","a","b","c","n","r","k"};
		 
 
//		String s="c6c5  rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR b - - 0 1";
		int t=0;
		StringBuilder sb=null;
		for (int i = 0; i < board.length; i++) {
			int row = boardRow[i];
			int col = boardCol[i];
			if (col == 0) {
				if (sb == null) {
					sb = new StringBuilder("0000 ");
				} else {
					if (t != 0) {
						sb.append(t);
						t = 0;
					}
					sb.append("/");
				}
			}
			if (board[i] != ChessConstant.NOTHING) {
				if (t != 0) {
					sb.append(t);
				}
				int role = chessRoles[board[i]];
				sb.append(sFen[role]);
				t = 0;
			} else {
				t++;
			}
		}
		java.io.BufferedOutputStream buff=null;
		ObjectOutputStream out =null;
		try {
			buff=new  java.io.BufferedOutputStream(new java.io.FileOutputStream("chess.txt"));
			buff.write(sb.toString().getBytes());
			buff.flush();
			
			out = new ObjectOutputStream(new FileOutputStream("moves.dat"));
			out.writeObject(backMove);
			out.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(buff!=null){
					buff.close();
				}
				if(out!=null){
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//加载开局库
		private static void loadBook(){
						
			String path="D://book.txt";
			File file=new File(path) ;
			java.io.BufferedReader bufferedReader=null;
			int b=0,r=0;
			
			try {
				FileInputStream fileInput = new java.io.FileInputStream(file);  
				bufferedReader=new BufferedReader(new java.io.InputStreamReader(fileInput));
				while(bufferedReader.ready()){
					TranspositionTable tranTable=new TranspositionTable(0,0);
					String fen = bufferedReader.readLine();
					String[] fenArray = Tools.fenToFENArray(fen);
					
					int[] boardTemp = Tools.parseFEN(fenArray[1]);
					for(int i=0;i<boardTemp.length;i++){
						if(boardTemp[i]==0){
							boardTemp[i]=-1;
						}
					}
					
					tranTable.genZobrist32And64OfBoard(boardTemp);
					
					String moveSite =fenArray[0];
					String srcSite = moveSite.substring(0,2);
					String destSite = moveSite.substring(2,4);
					 
					int play=0;
					if(fenArray[2].equalsIgnoreCase("b")){ //黑方
						play=BLACKPLAYSIGN;
						b++;
					}else{ //红方
						play=REDPLAYSIGN;
						r++;
					}
					char srcColChar = srcSite.charAt(0);
					int srcColInt = Math.abs('a'-srcColChar);
					char srcRowChar = srcSite.charAt(1);
					int srcRowInt = Integer.parseInt(srcRowChar+"");
					srcRowInt=Math.abs(9-srcRowInt);
					
					char destColChar = destSite.charAt(0);
					int destColInt = Math.abs('a'-destColChar);
					char destRowChar = destSite.charAt(1);
					int destRowInt = Integer.parseInt(destRowChar+"");
					destRowInt=Math.abs(9-destRowInt);
					
					int srcSiteBorad = getBoradSite(srcRowInt,srcColInt);
					int destSiteBorad = getBoradSite(destRowInt,destColInt);
					
					MoveNode moveNode = new MoveNode(srcSiteBorad,destSiteBorad,boardTemp[srcSiteBorad],boardTemp[destSiteBorad],0);
					
					tranTable.setTranZobrist(moveNode);
//					System.out.println("book->"+tranTable.boardZobrist32+"\t"+tranTable.boardZobrist64);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(bufferedReader!=null){
					try {
						bufferedReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			ObjectOutputStream oi=null;
			try {
				oi=new ObjectOutputStream(new FileOutputStream("book.dat"));
				oi.writeObject(TranspositionTable.fenLib);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(oi!=null){
					try {
						oi.flush();
						oi.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
//			TranspositionTable.fenLib
		}
		public static int getBoradSite(int row,int col){
//			int site=BOARDSTARTINDEX+row*16;
//			site+=COLOFFSET+col;
			int site=row*9;
			site+=col;
			return site;
		}

	public static void printBitBoard(ChessParam chessParam){
//		System.out.println("===========全局===========");
//		System.out.println(chessParam.maskBoardChesses);
//		System.out.println("===========红方===========");
//		System.out.println(chessParam.maskBoardPersonalChesses[REDPLAYSIGN]);
//		System.out.println("===========黑方===========");
//		System.out.println(chessParam.maskBoardPersonalChesses[BLACKPLAYSIGN]);
		BitBoard bitBoard = new BitBoard();
		for(int i=0;i<chessParam.maskBoardPersonalRoleChesses.length;i++){
			bitBoard.assignXor(chessParam.maskBoardPersonalRoleChesses[i]);
		}
		System.out.println("===========各角色棋子组合一起===========");
		System.out.println(bitBoard);
	}
	
	
}
