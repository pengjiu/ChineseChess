package com.pj.chess;
public enum ComputerLevel{
		//菜鸟
		greenHand(6,4),
		//入门
		introduction(7,8),
		//业余
		amateur(8,16),
		//专业
		career(9,32),
		//大师
		master(15,64),
		//无敌
		invincible(32,60*60);
		public int depth;
		public long time;
		private ComputerLevel(int depth,long time){
			//等级
			this.depth=depth;
			//最长可等待时间
			this.time=time*1000;
		}
	}