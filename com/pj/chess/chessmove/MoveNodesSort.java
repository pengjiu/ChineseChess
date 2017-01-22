package com.pj.chess.chessmove;

import java.util.Arrays;



import com.pj.chess.BitBoard;
import com.pj.chess.movelist.MoveNodeList;
	public class  MoveNodesSort{
		public static final int TRANGODMOVE1=0,TRANGODMOVE2=7,KILLERMOVE1=1,KILLERMOVE2=8,OTHERALLMOVE=2,EATMOVE=3,OVER=-1,QUIESDEFAULT=-2;
		private int moveType,play,index;
		MoveNodeList tranGodMove;
		MoveNode[] KillerMove;
		MoveNodeList generalMoveList;
		MoveNodeList goodMoveList;
		ChessMoveAbs chessMove;
		boolean isChecked;
		MoveNodeList repeatMoveList=new MoveNodeList(4);
		BitBoard oppAttackSite;
		public static final int tran1=0,tran2=1,kill1=2,kill2=3,eatmove=4,other=5;
		public static  int trancount1=0,trancount2=0,killcount1=0,killcount2=0,eatmovecount=0,othercount=0;
		
		public int currType;
		public MoveNodesSort(int play,MoveNodeList tranGodMove,MoveNode[] KillerMove,ChessMoveAbs chessMove,boolean isChecked){
			this.play=play;
			this.tranGodMove=tranGodMove;
			this.KillerMove=KillerMove;
			this.chessMove=chessMove;
			this.moveType=TRANGODMOVE1;
			this.isChecked=isChecked;
		}
		/*
		 * ��̬����
		 */
		public MoveNodesSort(int play,ChessMoveAbs chessMove,boolean isChecked){
			this.play=play;
			this.chessMove=chessMove;
			this.moveType=QUIESDEFAULT;
			this.isChecked=isChecked;
		}
		/*
		 * ��̬����
		 */
		public MoveNode quiescNext(){
			MoveNode nextMoveNode = null;
			switch (moveType) {
			case QUIESDEFAULT:  // �����ŷ�
				setMoveType(EATMOVE);
			case EATMOVE:  // �����ŷ�
				if(index==0){
					genEatMoveList();
				}
				if (index < goodMoveList.size) {
					nextMoveNode = getSortAfterBestMove(goodMoveList);
					index++;
					return nextMoveNode;
				} else {
					if(isChecked){
						//��������ȫ���߷�
						setMoveType(OTHERALLMOVE);
					}else{
						//�ǽ���ֻ���������ŷ����������
						setMoveType(OVER);
						break;
					}
				}
			case OTHERALLMOVE:  // �����ŷ�
				if(index==0){
					genNopMoveList();
				}
				if (index < generalMoveList.size) {
					nextMoveNode = getSortAfterBestMove(generalMoveList);
					index++;
				} else {
					setMoveType(OVER);
				}
				break;
			}
			return nextMoveNode;
		}
		public MoveNode next(){
			MoveNode nextMoveNode = null;
			switch (moveType) {
			case TRANGODMOVE1:  // �û���������ŷ�
				this.currType=tran1;
				nextMoveNode = tranGodMove.get(0);
				setMoveType(TRANGODMOVE2);
				if(chessMove.legalMove(play, nextMoveNode)){
					trancount1++;
					repeatMoveList.add(nextMoveNode);
					return nextMoveNode;
				}
			case TRANGODMOVE2:  // �û���������ŷ�
				this.currType=tran2;
				nextMoveNode = tranGodMove.get(1);
				setMoveType(KILLERMOVE1);
				if(chessMove.legalMove(play, nextMoveNode) && !nextMoveNode.equals(tranGodMove.get(0))){
					trancount2++;
					repeatMoveList.add(nextMoveNode);
					return nextMoveNode;
				} 
			case KILLERMOVE1:   // ɱ�ֱ��ŷ�
				this.currType=kill1;
				nextMoveNode = KillerMove[0];
				setMoveType(KILLERMOVE2);
				if(chessMove.legalMove(play, nextMoveNode) && !nextMoveNode.equals(tranGodMove.get(0)) && !nextMoveNode.equals(tranGodMove.get(1))){
					killcount1++;
					repeatMoveList.add(nextMoveNode);
					return nextMoveNode;
				}
			case KILLERMOVE2:   // ɱ�ֱ��ŷ�
				this.currType=kill2;
				nextMoveNode = KillerMove[1];
				setMoveType(EATMOVE);
				if(chessMove.legalMove(play, nextMoveNode) && !nextMoveNode.equals(tranGodMove.get(0)) && !nextMoveNode.equals(tranGodMove.get(1)) && !nextMoveNode.equals(KillerMove[0])){
					killcount2++;
					repeatMoveList.add(nextMoveNode);
					return nextMoveNode;
				}				
			case EATMOVE:  // �����ŷ�
				this.currType=eatmove;
				if(index==0){
					oppAttackSite=chessMove.getOppAttackSite(play);
					genEatMoveList();
				}
				if (index < goodMoveList.size) {
					eatmovecount++;
					nextMoveNode = getSortAfterBestMove(goodMoveList);
					index++;
					return nextMoveNode;  
				} else {
					setMoveType(OTHERALLMOVE);
				}
			case OTHERALLMOVE:  // �����ŷ�
				this.currType=other;
				if(index==0){
					genNopMoveList();
				}
				if (index < generalMoveList.size) {
					othercount++;
					nextMoveNode = getSortAfterBestMove(generalMoveList);
					index++;
					return nextMoveNode; 
				} else {
					moveType = OVER;
				}
				break;
			} 
			return nextMoveNode;
		}
		public int getCurrTypeMoveSize(){
			switch(this.currType){
				case other:
					return generalMoveList.size;
				case eatmove:
					return goodMoveList.size;
			}
			return 100;
		}
		public boolean isOver(){
			return moveType==OVER;
		}
		public boolean isKillerMove(){
			return moveType==KILLERMOVE1 || moveType==KILLERMOVE2;
		}
		public int getMoveType(){
			return moveType;
		} 
		private void genEatMoveList(){
			generalMoveList = new MoveNodeList(100);
			goodMoveList = new MoveNodeList(30);
			chessMove.setMoveNodeList(generalMoveList, goodMoveList,repeatMoveList,oppAttackSite);
			chessMove.genEatMoveList(play);
		}
		private void genNopMoveList(){
			chessMove.setMoveNodeList(generalMoveList, goodMoveList,repeatMoveList,oppAttackSite);
			chessMove.genNopMoveList(play);
		}
		private void setMoveType(int moveType){
//			if (moveType == EATMOVE) {
//				
//				
//			} else if (moveType == OTHERALLMOVE) {
//				
//			}
			/*if(moveType==EATMOVE){
				if(!isChecked){ //û�н���
					generalMoveList=new MoveNodeList(100);
					goodMoveList=new MoveNodeList(30);
					chessMove.setMoveNodeList(generalMoveList,goodMoveList);
					chessMove.genEatMoveList(play);
				}else{  //����
					goodMoveList=new MoveNodeList(50);
					chessMove.setMoveNodeList(goodMoveList,goodMoveList);
					//�⽫������ŷ�
					chessMove.genFristMoveListCheckMate(play);
				}
			}else if(moveType==OTHERALLMOVE){
				if(!isChecked){ //û�н���
					chessMove.setMoveNodeList(generalMoveList, goodMoveList);
					chessMove.genNopMoveList(play);
				}else{
					generalMoveList=new MoveNodeList(80);
					chessMove.setMoveNodeList(generalMoveList,generalMoveList);
					//�⽫�������ŷ�
					chessMove.genSecondlyMoveListCheckMate(play);
				}
 			}*/
			this.moveType=moveType;
			this.index = 0;			
		}
		/*
		 * ����ȡ����ǰ����ŷ�
		 */
		public MoveNode getSortAfterBestMove(MoveNodeList AllmoveNode){
			int replaceIndex=index;
			for(int i=index+1;i<AllmoveNode.size;i++){
				if(AllmoveNode.get(i).score>AllmoveNode.get(replaceIndex).score){
					replaceIndex=i;
				}
			}
			if(replaceIndex!=index){
				MoveNode t=AllmoveNode.get(index);
				AllmoveNode.set(index, AllmoveNode.get(replaceIndex));
				AllmoveNode.set(replaceIndex, t);
			}
			return AllmoveNode.get(index);
		}

	}
	
	
	
	
	
	
	
	