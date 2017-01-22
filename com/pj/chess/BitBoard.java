package com.pj.chess;
 
 
 

public class BitBoard {
	private int Low, Mid1,Mid2, Hi;
	public BitBoard(){};
	public BitBoard(int[] board){
		for(int i=0;i<board.length;i++){
			if(board[i]!=0){
				this.assignXor(ChessConstant.MaskChesses[i]);
			}
		}
	}
	public BitBoard(BitBoard arg){
		this.Hi=arg.Hi;
		this.Mid1=arg.Mid1;
		this.Mid2=arg.Mid2;
		this.Low=arg.Low;
	};
	public void assignAnd(final BitBoard Arg) {
		Low &= Arg.Low;
		Mid1 &= Arg.Mid1;
		Mid2 &= Arg.Mid2;
		Hi &= Arg.Hi;
	}
	public void assignOr(final BitBoard Arg) {
		Low |= Arg.Low;
		Mid1 |= Arg.Mid1;
		Mid2 |= Arg.Mid2;
		Hi |= Arg.Hi;
	}
	public void assignXor(final BitBoard Arg) {
		Low ^= Arg.Low;
		Mid1 ^= Arg.Mid1;
		Mid2 ^= Arg.Mid2;
		Hi ^= Arg.Hi;
	}
	
	public static BitBoard assignXorToNew(final BitBoard Arg,final BitBoard Arg1) {
		BitBoard re = new BitBoard(Arg1);
		re.Low ^= Arg.Low;
		re.Mid1 ^= Arg.Mid1;
		re.Mid2 ^= Arg.Mid2;
		re.Hi ^= Arg.Hi;
		return re;
	}
	public static BitBoard assignOrToNew(final BitBoard Arg,final BitBoard Arg1) {
		BitBoard re = new BitBoard(Arg1);
		re.Low |= Arg.Low;
		re.Mid1 |= Arg.Mid1;
		re.Mid2 |= Arg.Mid2;
		re.Hi |= Arg.Hi;
		return re;
	}
	public static BitBoard assignAndToNew(final BitBoard Arg,final BitBoard Arg1) {
		BitBoard re = new BitBoard(Arg1);
		re.Low &= Arg.Low;
		re.Mid1 &= Arg.Mid1;
		re.Mid2 &= Arg.Mid2;
		re.Hi &= Arg.Hi;
		return re;
	}
	

	public BitBoard(int site){
		if(site<27){
			Low=1<<site;
		}else if(site<27*2){
			Mid1=1<<site-27;
		}else if(site<27*3){
			Mid2=1<<site-27*2;
		}else if(site<27*4){
			Hi=1<<site-27*3;
		}
	}
	public  boolean isEmpty() {
		int temp1 = Low | Mid1|Mid2 | Hi;
		return temp1==0;
	}
	//象眼的唯一值
	public  int checkSumOfElephant() {
		int temp1 = (Low) ^ (Mid1)^(Mid2) ^ (Hi);
		int r=(temp1&0x7f)+(temp1>>>6&0x7f)+(temp1>>>13&0x7f)+(temp1>>>19&0x7f);
		
		return r;
	}
	//马眼的唯一值
	public  int checkSumOfKnight() {
		int temp1 = (Low) ^ (Mid1)^(Mid2) ^ (Hi);
		int r=(temp1&0x7f)+(temp1>>>7&0x7f)+(temp1>>>14&0x7f)+(temp1>>>21&0x7f);
		return r;
	}

	private static int Msb32(int Arg) {
		
		return Integer.numberOfTrailingZeros(Arg);
	}
	public  int MSB(int play) {
		switch(play){
		case ChessConstant.REDPLAYSIGN: 
			if (Low!=0) {
				return Msb32(Low);
			} else if (Mid1!=0) {
				return Msb32(Mid1) + 27;
			} else if (Mid2!=0) {
				return Msb32(Mid2) + 54;
			}  else if (Hi!=0) {
				return Msb32(Hi) +81;
			} 
			break;
		case ChessConstant.BLACKPLAYSIGN:					
			if (Hi!=0) {
				return Msb32(Hi) +81;
			}else if (Mid2!=0) {
				return Msb32(Mid2) + 54;
			} else if (Mid1!=0) {
				return Msb32(Mid1) + 27;
			} else if (Low!=0) {
				return Msb32(Low);
			} 
		}
		return -1;
	}
	
	//计算非零位个数
	private static int Count32(int Arg) {
		int t;
		t = ((Arg >>> 1) & 0x55555555) + (Arg & 0x55555555);
		t = ((t >>> 2) & 0x33333333) + (t & 0x33333333);
		t = ((t >>> 4) & 0x0f0f0f0f) + (t & 0x0f0f0f0f);
		t = ((t >>> 8) & 0x00ff00ff) + (t & 0x00ff00ff);
		return (t >>> 16) + (t & 0x0000ffff);
	}
	public  int Count() {
		return Count32(Low) + Count32(Mid1)+Count32(Mid2) + Count32(Hi);
	}	
	public static boolean equals(BitBoard a,BitBoard b) {
		return a.Hi==b.Hi && a.Low==b.Low && a.Mid1==b.Mid1 && a.Mid2==b.Mid2; 
	}
	public String toString(){
		StringBuffer sb=new StringBuffer(); 
		for(int i=0;i<27;i++){
			if(i!=0 && i%9==0){
				sb.append("\n");
			}
			char c=((Low>>>i)&1)==0?'O':'X';
			sb.append(c+" ");
		}
		for(int i=0;i<27;i++){
			if(i%9==0){
				sb.append("\n");
			}
			char c=((Mid1>>>i)&1)==0?'O':'X';
			sb.append(c+" ");
		}
		for(int i=0;i<27;i++){
			if(i%9==0){
				sb.append("\n");
			}
			char c=((Mid2>>>i)&1)==0?'O':'X';
			sb.append(c+" ");
		}
		for(int i=0;i<9;i++){
			if(i%9==0){
				sb.append("\n");
			}
			char c=((Hi>>>i)&1)==0?'O':'X';
			sb.append(c+" ");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {   
		System.out.println(Integer.numberOfTrailingZeros(4));
	}
}
