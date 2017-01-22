package com.pj.chess.chessparam;

import static com.pj.chess.ChessConstant.BLACKPLAYSIGN;
import static com.pj.chess.ChessConstant.MaskChesses;
import static com.pj.chess.ChessConstant.NOTHING;
import static com.pj.chess.ChessConstant.REDPLAYSIGN;
import static com.pj.chess.ChessConstant.chessRoles;

import com.pj.chess.BitBoard;
import com.pj.chess.ChessConstant;
import com.pj.chess.Tools;

/**
 * @author pengjiu
 * 为防止多线程下，一些所需要的参数变量同步问题
 */
public class ChessParam {
	public  int[] board;	 // 棋盘->棋子

	public  int[] allChess; //棋子->棋盘
	
	public int[] baseScore=new int[2];
//	public int redBaseScore=0;  //红方分数
	
//	public int blackBaseScore=0; //黑方分数
	
	public int[] boardBitRow; //位棋盘  行
	
	public int[] boardBitCol; //位棋盘  列
	
	private int[] boardRemainChess; //剩余棋子数量
	
	//所有棋子位棋盘
	public BitBoard maskBoardChesses;
	//各自的位棋盘
	public BitBoard[] maskBoardPersonalChesses;
	//各自按角色分类的位棋盘[角色]
	public BitBoard[] maskBoardPersonalRoleChesses;
	
	//[玩家][0攻击棋子数量  1防御棋子数量]
	private int[][] attackAndDefenseChesses=new int[2][2];  
	
    //每个棋子对应attackAndDefenseChesses 的下标表
	public static  final int[] indexOfAttackAndDefense=new int[]{0,
		0,1,1,0,0,0,1,
		0,1,1,0,0,0,1
	};
	public ChessParam(int[] board,int[] allChess,int[] baseScore,int[] boardBitRow,int[] boardBitCol,int[] boardRemainChess,BitBoard maskBoardChesses,BitBoard[] maskBoardPersonalChesses,BitBoard[] maskBoardPersonalRoleChesses){
		this.board=board;
		this.allChess=allChess;
		this.baseScore=baseScore;
		this.boardBitRow=boardBitRow;
		this.boardBitCol=boardBitCol;
		this.boardRemainChess=boardRemainChess;
		this.maskBoardChesses=maskBoardChesses;
		this.maskBoardPersonalChesses=maskBoardPersonalChesses;
		this.maskBoardPersonalRoleChesses=maskBoardPersonalRoleChesses;
	}
	public ChessParam(ChessParam param){
		this.copyToSelf(param);
	}
	public void copyToSelf(ChessParam param){
		//棋子copy
		int[] allChessTemp = param.allChess;
		this.allChess=new int[allChessTemp.length];
		for(int i=0;i<allChessTemp.length;i++){
			this.allChess[i]=allChessTemp[i];
		}
		//棋盘copy
		int[] boardTemp = param.board;
		this.board=new int[boardTemp.length];
		for(int i=0;i<boardTemp.length;i++){
			this.board[i]=boardTemp[i];
		}
		//位棋盘行
		int[] boardBitRowTemp = param.boardBitRow;
		this.boardBitRow=new int[boardBitRowTemp.length];
		for(int i=0;i<boardBitRowTemp.length;i++){
			this.boardBitRow[i]=boardBitRowTemp[i];
		} 
		//位横向列
		int[] boardBitColTemp = param.boardBitCol;
		this.boardBitCol=new int[boardBitColTemp.length];
		for(int i=0;i<boardBitColTemp.length;i++){
			this.boardBitCol[i]=boardBitColTemp[i];
		} 
		//棋子数量
		int[] boardRemainChessTemp = param.boardRemainChess;
		this.boardRemainChess=new int[boardRemainChessTemp.length];
		for(int i=0;i<boardRemainChessTemp.length;i++){
			this.boardRemainChess[i]=boardRemainChessTemp[i];
		} 
		// 攻击性棋子和防御性棋子数量
		int[][] attackAndDefenseChessesTemp = param.attackAndDefenseChesses;
		for(int i=0;i<attackAndDefenseChessesTemp.length;i++){
			for(int j=0;j<attackAndDefenseChessesTemp[i].length;j++){
				this.attackAndDefenseChesses[i][j]=attackAndDefenseChessesTemp[i][j];
			}
		} 
		//所有子力的位棋盘
		this.maskBoardChesses = new BitBoard(param.maskBoardChesses);
		
		this.maskBoardPersonalChesses=new BitBoard[param.maskBoardPersonalChesses.length];
		//各方子力的位棋盘
		this.maskBoardPersonalChesses[ChessConstant.REDPLAYSIGN] =  new BitBoard(param.maskBoardPersonalChesses[ChessConstant.REDPLAYSIGN]);
		this.maskBoardPersonalChesses[ChessConstant.BLACKPLAYSIGN] =  new BitBoard(param.maskBoardPersonalChesses[ChessConstant.BLACKPLAYSIGN]);
		//各方子力按角色分类
		maskBoardPersonalRoleChesses=new BitBoard[param.maskBoardPersonalRoleChesses.length];
		for(int i=0;i<param.maskBoardPersonalRoleChesses.length;i++){
			this.maskBoardPersonalRoleChesses[i]=new BitBoard(param.maskBoardPersonalRoleChesses[i]);
		}
		
		
		//分数
		this.baseScore[ChessConstant.REDPLAYSIGN] = param.baseScore[ChessConstant.REDPLAYSIGN];
		this.baseScore[ChessConstant.BLACKPLAYSIGN] = param.baseScore[ChessConstant.BLACKPLAYSIGN];
		
	}
	private int getPlayByChessRole(int chessRole){
		return chessRole>ChessConstant.REDKING?ChessConstant.BLACKPLAYSIGN:ChessConstant.REDPLAYSIGN;
	}
	public int getChessesNum(int chessRole){
		return boardRemainChess[chessRole];
	}
	public int getChessesNum(int play,int chessRole){
		return boardRemainChess[getRoleIndexByPlayRole(play, chessRole)];
	}
	/**
	 * @param chessRole 棋子角色
	 *  减少棋子数量
	 */
	public void reduceChessesNum(int chessRole){
		boardRemainChess[chessRole]--;
		attackAndDefenseChesses[getPlayByChessRole(chessRole)][indexOfAttackAndDefense[chessRole]]--;
	}
	/**
	 * @param chessRole 柜子角色
	 * 增加棋子数量
	 */
	public void increaseChessesNum(int chessRole){
		boardRemainChess[chessRole]++;
		int play=getPlayByChessRole(chessRole);
		attackAndDefenseChesses[play][indexOfAttackAndDefense[chessRole]]++;
	}
	/**
	 * @return 所有棋子数量
	 */
	public int getAllChessesNum(){
		int num=0;
		for(int i:boardRemainChess){
			num+=i;
		}
		return num;
	}
	//所有攻击棋子数量
	public int getAttackChessesNum(int play){
		return attackAndDefenseChesses[play][0];
	}
	//所有防御棋子数量
	public int getDefenseChessesNum(int play){
		return attackAndDefenseChesses[play][1];
	}
	public BitBoard getBitBoardByPlayRole(int play,int role){
		return maskBoardPersonalRoleChesses[this.getRoleIndexByPlayRole(play, role)];
	}
	public int getRoleIndexByPlayRole(int play,int role){
		return role=play==ChessConstant.REDPLAYSIGN?role:(role+7);
	}
	public static void main(String[] args) {
		/*ChessParam chess1 = new ChessParam(new int[64],new int[32],0,0);
		ChessParam chess2 = new ChessParam(chess1);
		chess1.allChess[0]=10;
		System.out.println(chess2.allChess[0]);*/
	}
	public void initChessBaseScoreAndNum(){
		
		for (int i = 16; i < allChess.length; i++) {
			if(allChess[i]!=NOTHING){
				int site=allChess[i];
				int chessRole = chessRoles[board[allChess[i]]];
				int play=i < 32?BLACKPLAYSIGN:REDPLAYSIGN;
				increaseChessesNum(chessRole);
//				chessParamCont.baseScore[play]+=EvaluateComputeMiddle.chessBaseScore[i];
//				chessParamCont.baseScore[play]+=new EvaluateComputeMiddle(chessParamCont).chessAttachScore(chessRole,allChess[i]);
				maskBoardChesses.assignXor(MaskChesses[site]);
				maskBoardPersonalChesses[play].assignXor(MaskChesses[site]);
				maskBoardPersonalRoleChesses[chessRole].assignXor(MaskChesses[site]);
			}
		} 
		
	}
}











