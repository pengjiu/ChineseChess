package com.pj.chess.movelist;

import java.util.ArrayList;
import java.util.List;

import com.pj.chess.chessmove.MoveNode;

public class MoveNodeList{
	public MoveNode[] tables=null;
	public int size=0; 
	public  MoveNodeList(int length){
		if(tables==null){
			tables=new MoveNode[length];
		}
		size=0; 
	}
	public  MoveNodeList(MoveNodeList copy){
//		if(copy!=null){
//			tables=new MoveNode[copy.size];
//		}
	}
	public void clear(){
		size=0;
	}
	public void set(int index,MoveNode moveNode){
		tables[index]=moveNode; 
	}
	public void add(MoveNode moveNode){
		if(moveNode!=null){
			tables[size++]=moveNode;
		}
	}
	public MoveNode get(int index){
		if(index<size)
			return tables[index];
		else 
			return null;
	}
	public void addAll(MoveNodeList moveNodeList){
		if(moveNodeList!=null && moveNodeList.size>0){
			System.arraycopy(moveNodeList.tables,0, tables, size,moveNodeList.size);
			size+=moveNodeList.size;
		}
	}
	public static void main(String[] args) {
		/*int[] k=new int[]{1,2,3,4,5,0,0,0,0,0,0};
		int[] k2=new int[]{8,9,1,1};
		System.arraycopy(k2, 0, k, 5, k2.length-1);
		
		for(int vv:k){
			System.out.println(vv); 
		}*/
		
		
		MoveNodeList mn=new MoveNodeList(20);
		MoveNode moveNode=new MoveNode();
		moveNode.destChess=10;
		mn.add(moveNode);
		 moveNode=new MoveNode();
		moveNode.destChess=20;
		mn.add(moveNode);
		 moveNode=new MoveNode();
		moveNode.destChess=30;
		mn.add(moveNode);
		 moveNode=new MoveNode();
		moveNode.destChess=40;
		mn.add(moveNode);
		
		MoveNodeList mnTest=new MoveNodeList(10);
		 moveNode=new MoveNode();
			moveNode.destChess=70;
			mnTest.add(moveNode);
			 moveNode=new MoveNode();
				moveNode.destChess=100;
				mnTest.add(moveNode);		
				mnTest.add(moveNode);	
			
		mn.addAll(mnTest);
		for(int i=0;i<mn.size;i++){
			System.out.println(mn.get(i).destChess);
		}
	}
}
