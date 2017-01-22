package com.pj.chess.zobrist;

import java.util.List;

import com.pj.chess.chessmove.MoveNode;

public class HashItem {
	//校验和
	public long checkSum;
	//hash类型  上边界  下边界  精确值
	public int entry_type;
	//值
	public int value;
	//步数
	public int depth; 
	
	public MoveNode moveNode;
	//是否过期数据
	public boolean isExists=true;
	
//	public boolean isCheckMate=false;
	//是否为开局库中的数据
//	public boolean isFEN=false;
//	public short moveNum=0;
//	public int play;
	public HashItem(){};
	public HashItem(HashItem copy){
		copyToSelf(copy);
	};
	public void copyToSelf(HashItem copy){
		this.checkSum=copy.checkSum;
		this.entry_type=copy.entry_type;
		this.value=value;
		this.depth=depth;
		this.moveNode=moveNode;
		this.isExists=this.isExists;
	}
	
}
