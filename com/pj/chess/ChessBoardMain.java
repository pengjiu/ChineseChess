package com.pj.chess;
import static com.pj.chess.ChessConstant.*; 

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import com.pj.chess.chessmove.ChessMovePlay;
import com.pj.chess.chessmove.MoveNode;
import com.pj.chess.chessparam.ChessParam;
import com.pj.chess.evaluate.EvaluateCompute; 
import com.pj.chess.evaluate.EvaluateComputeMiddleGame;
import com.pj.chess.zobrist.TranspositionTable;

public class ChessBoardMain extends JFrame {

	private static final long serialVersionUID = 1L;
	public static  final String[] chessName=new String[]{ 
		"   ",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
		"黑将","黑车","黑车","黑马","黑马","黑炮","黑炮","黑象","黑象","黑士","黑士","黑卒","黑卒","黑卒","黑卒","黑卒",
		"红将","红车","红车","红马","红马","红炮","红炮","红象","红象","红士","红士","红卒","红卒","红卒","红卒","红卒",
	};
	public static  final String[] chessIcon=new String[]{ 
		 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
		"BK","BR","BR","BN","BN","BC","BC","BB","BB","BA","BA","BP","BP","BP","BP","BP",
		"RK","RR","RR","RN","RN","RC","RC","RB","RB","RA","RA","RP","RP","RP","RP","RP",		
	};
	int lastTimeCheckedSite=-1; //上次选中棋子的位置
	private ButtonActionListener my = new ButtonActionListener(); 
	JLabel[] buttons=new JLabel[BOARDSIZE90];
	int play=1;
	volatile boolean[] android=new boolean[]{false,false};
	int begin=-1;
	int end=0;
	private static ComputerLevel computerLevel=ComputerLevel.greenHand; //默认
	boolean isBackstageThink=false;
	boolean computeFig=false;
	TranspositionTable  transTable;
	ChessMovePlay cmp=null; 
	AICoreHandler _AIThink=new AICoreHandler();
	AICoreHandler backstageAIThink=new AICoreHandler();
//	public static List<MoveNode> backMove=new ArrayList<MoveNode>();
	NodeLink moveHistory;
	int turn_num=0;//回合数
	ChessParam chessParamCont;
	private static boolean isSound=false;
	public void initHandler(){
		String startFen="c6c5  rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR b - - 0 1";
		
//		String startFen="c6c5  9/CP2k4/9/9/9/9/9/9/9/4K4 b - - 0 1";
//		Tools.parseFENtoBoardZobrist(fenStr);
		startFen=readSaved();
		
		String[] fenArray = Tools.fenToFENArray(startFen);
		int[] boardTemp = Tools.parseFEN(fenArray[1]);
		//根据棋盘初始参数		
		chessParamCont=ChessInitialize.getGlobalChessParam(boardTemp);
		//清除所有界面图片
//		clearBoardIcon();
		//初始界面棋子
		for(int i=0;i<boardTemp.length;i++){
			if(boardTemp[i]>0){
				this.setBoardIconUnchecked(i,boardTemp[i]);
			}
		}
		
		//初始局面(要把棋子摆好后才能计算局面值)
		transTable=new TranspositionTable() ;
		if(moveHistory==null){
			moveHistory=new NodeLink(1-play,transTable.boardZobrist32,transTable.boardZobrist64);
		}
		play=1-moveHistory.play;
		android[1-play]=true;
		cmp=new ChessMovePlay(chessParamCont,transTable,new EvaluateComputeMiddleGame(chessParamCont));
	}
	JPanel jpanelContent;
	private void setCenter(){
		if(jpanelContent!=null){
			this.remove(jpanelContent);
		}
		jpanelContent= new javax.swing.JPanel() {
			protected void paintComponent(Graphics g) {
				try { 
					BufferedImage img=ImageIO.read(getClass().getResource("/images/MAIN.GIF"));
					g.drawImage(img, 0, 0,  null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		this.setLayout(new BorderLayout());
		
		JPanel panel  = new javax.swing.JPanel();
		
		jpanelContent.setLayout(new BorderLayout());
		//北		
		JPanel jpNorth=new JPanel(); 
		jpNorth.setPreferredSize(new Dimension(25,25));
//		jpNorth.setBackground(Color.white);
		jpNorth.setOpaque(false);
		jpanelContent.add(jpNorth,BorderLayout.NORTH);
		//南	
		JPanel jpSouth=new JPanel(); 
		jpSouth.setPreferredSize(new Dimension(5,5));
		jpSouth.setBackground(Color.black);
		jpSouth.setOpaque(false);
		jpanelContent.add(jpSouth,BorderLayout.SOUTH);	
		//西
		JPanel jpWest=new JPanel();
		jpWest.setPreferredSize(new Dimension(20,20));
		jpWest.setBackground(Color.blue);
		jpWest.setOpaque(false);
		jpanelContent.add(jpWest,BorderLayout.WEST);		
		//东
		JPanel jpEast=new JPanel(); 
		jpEast.setPreferredSize(new Dimension(20,20));
		jpEast.setBackground(Color.CYAN);
		jpEast.setOpaque(false);
		jpanelContent.add(jpEast,BorderLayout.EAST);	
		//中
		panel.setLayout(new GridLayout(10, 9));
		panel.setPreferredSize(new Dimension(100,100));
		panel.setOpaque(false);
		jpanelContent.add(panel,BorderLayout.CENTER);	
		
		for (int i = 0; i < BOARDSIZE90; i++) { 
			JLabel  p = new JLabel(); 
			p.addMouseListener(my);
			p.setBackground(Color.red);
			p.setSize(55, 55);
			buttons[i]=p;			
			panel.add(p);
		}
		this.add(jpanelContent,BorderLayout.CENTER);
	}
	public ChessBoardMain() {
		super("中国象棋");  
		setCenter();
		
		JPanel constrol=new JPanel();
		constrol.setLayout(new GridLayout(1, 3));
		
		Button button = new Button("悔棋");
		button.addActionListener(my); 
		Button computerMove = new Button("立即走棋");
		computerMove.addActionListener(my);
		constrol.add(button);
		constrol.add(computerMove);
		this.add(constrol,BorderLayout.SOUTH);
		
		this.addWindowListener(my);
		//初始处理器
		initHandler();
		this.setJMenuBar(setJMenuBar());
		
		this.setSize(568, 680); 
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
	}
	private MenuItemActionListener menuItemAction=new MenuItemActionListener();
	JRadioButtonMenuItem hashSize2M = new JRadioButtonMenuItem("HASH表小",true);
	JRadioButtonMenuItem hashSize32M = new JRadioButtonMenuItem("HASH表中",false);
	JRadioButtonMenuItem hashSize64M = new JRadioButtonMenuItem("HASH表大",false);
	private JMenuBar setJMenuBar(){
		JMenuBar jmb = new JMenuBar();
		JMenu menu_file = new JMenu("文件");
		JMenuItem create = new JMenuItem("新建");
		JMenuItem save= new JMenuItem("保存");
		JRadioButtonMenuItem mi_6 = new JRadioButtonMenuItem("菜鸟",true);
		JRadioButtonMenuItem mi_7 = new JRadioButtonMenuItem("入门",false);
		JRadioButtonMenuItem mi_8 = new JRadioButtonMenuItem("业余",false);
		JRadioButtonMenuItem mi_9 = new JRadioButtonMenuItem("专家",false);
		JRadioButtonMenuItem mi_10 = new JRadioButtonMenuItem("大师",false);
		JRadioButtonMenuItem mi_11 = new JRadioButtonMenuItem("无敌",false);
		
		ButtonGroup group=new ButtonGroup();
		group.add(mi_6);
		group.add(mi_7);
		group.add(mi_8);
		group.add(mi_9);
		group.add(mi_10);
		group.add(mi_11);
		create.addActionListener(menuItemAction);
		save.addActionListener(menuItemAction);
		mi_6.addActionListener(menuItemAction);
		mi_7.addActionListener(menuItemAction);
		mi_8.addActionListener(menuItemAction);
		mi_9.addActionListener(menuItemAction);
		mi_10.addActionListener(menuItemAction);
		mi_11.addActionListener(menuItemAction);
		
		create.setMnemonic(10);
		mi_6.setMnemonic(2);
		mi_7.setMnemonic(3);
		mi_8.setMnemonic(4);
		mi_9.setMnemonic(5);
		mi_10.setMnemonic(6);
		menu_file.setMnemonic('0');
		menu_file.add(create); 
		menu_file.add(mi_6);
		menu_file.add(mi_7);
		menu_file.add(mi_8);
		menu_file.add(mi_9);
		menu_file.add(mi_10);
		menu_file.add(mi_11);
		menu_file.add(save);
		jmb.add(menu_file);
		//------------------------------------------------------
		JMenu menu_set = new JMenu("设置");
		JCheckBoxMenuItem redCmp = new JCheckBoxMenuItem("电脑红方",play!=REDPLAYSIGN);
		JCheckBoxMenuItem blackCmp = new JCheckBoxMenuItem("电脑黑方",play!=BLACKPLAYSIGN);

		JCheckBoxMenuItem isSoundBox= new JCheckBoxMenuItem("音效",isSound);

		
		ButtonGroup hashSizeGroup=new ButtonGroup();
		hashSizeGroup.add(hashSize2M);
		hashSizeGroup.add(hashSize32M);
		hashSizeGroup.add(hashSize64M);
		
		
		JCheckBoxMenuItem backstageThink=new JCheckBoxMenuItem("后台思考",isBackstageThink); 
		
		redCmp.addActionListener(menuItemAction);
		blackCmp.addActionListener(menuItemAction);
		hashSize2M.addActionListener(menuItemAction);
		hashSize32M.addActionListener(menuItemAction);
		hashSize64M.addActionListener(menuItemAction);
		backstageThink.addActionListener(menuItemAction);
		isSoundBox.addActionListener(menuItemAction);
		
		menu_set.add(blackCmp);
		menu_set.add(redCmp);
		menu_set.add(hashSize2M);
		menu_set.add(hashSize32M);
		menu_set.add(hashSize64M);
		menu_set.add(backstageThink);
		menu_set.add(isSoundBox);		
		jmb.add(menu_set);
		return jmb;
	}
	public void setBoardIconUnchecked(int site,int chess){
//		site=boardMap[site];
//		initBoardRelation(site,chess);
		if(chess==NOTHING){
			buttons[site].setIcon(null);	
		}else{
			buttons[site].setIcon(getImageIcon(chessIcon[chess]));
		}
	}
	public void setBoardIconChecked(int site,int chess){
		buttons[site].setIcon(getImageIcon(chessIcon[chess]+"S"));
	}
	public void setCheckedLOSS(int play){
		buttons[chessParamCont.allChess[chessPlay[play]]].setIcon(getImageIcon(chessIcon[chessPlay[play]]+"M"));
	}
	public void clearBoardIcon(){
		for(int i=0;i<buttons.length;i++){
			buttons[i].setIcon(null);
		}
	}
	public  void initBoardRelation(int destSite,int chess){
		
		chessParamCont.board[destSite]=chess;
		chessParamCont.allChess[chess]=destSite;
		
		int destRow = boardRow[destSite];
		int destCol = boardCol[destSite];
		chessParamCont.boardBitRow[destRow]|=(1<<(8-destCol));
		chessParamCont.boardBitCol[destCol]|=(1<<(9-destRow));  
		 
	}
	
	public void move(MoveNode moveNode){
		
		if(lastTimeCheckedSite!=-1){
			setBoardIconUnchecked(lastTimeCheckedSite,chessParamCont.board[lastTimeCheckedSite]);
		}
		setBoardIconUnchecked(moveNode.srcSite,NOTHING);
		setBoardIconChecked(moveNode.destSite,moveNode.srcChess); 
		lastTimeCheckedSite=moveNode.destSite;
	}
	class ButtonActionListener   implements ActionListener, WindowListener,MouseListener {
		public void actionPerformed(ActionEvent e) {
			Button sour = (Button)e.getSource();
			if(sour.getLabel().equals("悔棋")){
				if(moveHistory.getMoveNode()!=null ){
					MoveNode moveNode=moveHistory.getMoveNode();
					unMoveNode(moveNode); 
					moveHistory=moveHistory.getLastLink();
					turn_num--;
					play=1-play; //交换双方
				}
			}else if(sour.getLabel().equals("立即走棋")){ 
				if(_AIThink!=null){
					_AIThink.setStop();
				}
			}
			
			
		}
		private boolean checkZFPath(int srcSite,int destSite,int play){
			if(chessParamCont.board[srcSite]==NOTHING){
				return false;
			}
//			int row=chessParamCont.boardBitRow[boardRow[srcSite]];
//			int col=chessParamCont.boardBitCol[boardCol[srcSite]];
			/*BitBoard bt = BitBoard.assignXorToNew(GunBitBoardOfFakeAttackRow[srcSite][row],GunBitBoardOfFakeAttackCol[srcSite][col]);
			System.out.println(chessParamCont.maskBoardChesses);
			System.out.println("============炮伪攻击的位置==========");
			System.out.println(bt);*/
//			System.out.println("车或炮的机动性为->>"+(ChariotAndGunMobilityRow[srcSite][row]+ChariotAndGunMobilityCol[srcSite][col]));
			
			MoveNode moveNode = new MoveNode(srcSite,destSite,chessParamCont.board[srcSite],chessParamCont.board[destSite],0);
			return cmp.legalMove(play,moveNode);
		}  
		private void unMoveNode(MoveNode moveNode){
			MoveNode unmoveNode=new MoveNode();
			unmoveNode.srcChess=moveNode.destChess;
			unmoveNode.srcSite=moveNode.destSite;
			unmoveNode.destChess=moveNode.srcChess;
			unmoveNode.destSite=moveNode.srcSite;
			unMove(unmoveNode);
			cmp.unMoveOperate(moveNode);
		}
		private void unMove(MoveNode moveNode){
			if(lastTimeCheckedSite!=-1){
				setBoardIconUnchecked(lastTimeCheckedSite,chessParamCont.board[lastTimeCheckedSite]);
			}
			if(moveNode.srcChess==NOTHING){
				buttons[moveNode.srcSite].setIcon(null); 
			}else{
				setBoardIconUnchecked(moveNode.srcSite,moveNode.srcChess);
			} 
			if(moveNode.destChess==NOTHING){
				buttons[moveNode.destChess].setIcon(null); 
			}else{
				setBoardIconChecked(moveNode.destSite,moveNode.destChess);
			} 
			lastTimeCheckedSite=moveNode.destSite;
		}
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
			System.exit(1);
		}

		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}
		
		
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void mousePressed(MouseEvent e) {
			if(android[play]){
				return;
			}
			for (int i = 0; i < buttons.length; i++) {
					JLabel p = buttons[i];
					if(p==e.getSource()){
						if(chessParamCont.board[i]!=NOTHING &&  (chessParamCont.board[i]&chessPlay[play])==chessPlay[play]){//自方子力
							if(i!=begin){
								begin=i;
								
								setBoardIconChecked(i,chessParamCont.board[i]);
								if(lastTimeCheckedSite!=-1){
									setBoardIconUnchecked(lastTimeCheckedSite,chessParamCont.board[lastTimeCheckedSite]);
								}
								lastTimeCheckedSite=begin;
							}
							return;
						}else if(begin==-1){
							return;
						}
						end=i;
						if (this.checkZFPath(begin, end, play)) {
							MoveNode moveNode = new MoveNode(begin, end, chessParamCont.board[begin], chessParamCont.board[end], 0);
							showMoveNode(moveNode); 
							NodeLink nextLink = new NodeLink(play, transTable.boardZobrist32, transTable.boardZobrist64);
							nextLink.setMoveNode(moveNode);
							moveHistory.setNextLink(nextLink);
							moveHistory = moveHistory.getNextLink();
							begin = -1;
							opponentMove();
						}
					}
			} 
			
		}
		
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	public void gameOverMsg(String msg){
		if (JOptionPane.showConfirmDialog(this, msg + "是否继续？", "信息",
				 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			dispose();
			new ChessBoardMain();
		} else{
			dispose();
		}
	}

	private ImageIcon getImageIcon(String chessName){
		String path="/images/"+chessName+".GIF";
		ImageIcon  imageIcon=new  ImageIcon(getClass().getResource(path));
		return imageIcon;
	}
	
	private boolean checkGameOver(){
		boolean isGameOver=false;
		String msg=null;
		if(moveHistory==null || moveHistory.getMoveNode()==null){
			msg=(play==BLACKPLAYSIGN?"黑方":"红方")+"被残忍的将死！";
			isGameOver=true;
		//自己帅被吃
		}else if(chessParamCont.allChess[chessPlay[BLACKPLAYSIGN]]==NOTHING || moveHistory.getMoveNode().destChess==chessPlay[BLACKPLAYSIGN]){
			isGameOver=true;
			msg="黑方被完虐！";
		}else if(chessParamCont.allChess[chessPlay[REDPLAYSIGN]]==NOTHING || moveHistory.getMoveNode().destChess==chessPlay[REDPLAYSIGN]){
			msg="红方被完虐！";
			isGameOver=true; 
		}else if(moveHistory.getMoveNode().score==-LONGCHECKSCORE){
			msg=(play==BLACKPLAYSIGN?"黑方":"红方")+"长将判负！";
			isGameOver=true;
		}else if(moveHistory.getMoveNode().score<=-(maxScore-2)){
			setCheckedLOSS(play);
			msg=(play==BLACKPLAYSIGN?"黑方":"红方")+"被残忍的将死！";
			isGameOver=true;
		}else if(moveHistory.getMoveNode().score>=(maxScore-2)){
			setCheckedLOSS(1-play);
			msg=(play==BLACKPLAYSIGN?"黑方":"红方")+"赢得了最终的胜利！";
			isGameOver=true;
		}else if(chessParamCont.getAttackChessesNum(REDPLAYSIGN)==0 && chessParamCont.getAttackChessesNum(BLACKPLAYSIGN)==0){
			msg="双方都无攻击棋子此乃和棋！";
			isGameOver=true;
		}else if(turn_num>=300){
			msg="大战300回合未分胜负啊！";
			isGameOver=true;
		}
		if(isGameOver){
			launchSound(SoundEffect.LOSS_SOUND);
			gameOverMsg(msg);
		}else{
			MoveNode moveNode = moveHistory.getMoveNode();
			if(cmp.checked(1-play)){//对手是否被将
				launchSound(SoundEffect.CHECKED_SOUND);
			}else if(moveNode.destChess!=NOTHING){
				launchSound(SoundEffect.CAPTURE_SOUND);
			}else{
				launchSound(SoundEffect.MOVE_SOUND);
			}
		}
		return isGameOver;
	}
	class MenuItemActionListener   implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			String actionCommand=e.getActionCommand();
			if("新建".equals(actionCommand)){
				dispose();
				new ChessBoardMain();
			}else if("保存".equalsIgnoreCase(actionCommand)){
				Tools.saveFEN(chessParamCont.board,moveHistory);
			}else if("菜鸟".equals(actionCommand)){
				computerLevel=ComputerLevel.greenHand;
			}else if("入门".equals(actionCommand)){
				computerLevel=ComputerLevel.introduction;
			}else if("业余".equals(actionCommand)){
				computerLevel=ComputerLevel.amateur;
			}else if("专家".equals(actionCommand)){
				computerLevel=ComputerLevel.career;
			}else if("大师".equals(actionCommand)){
				computerLevel=ComputerLevel.master;
			}else if("无敌".equals(actionCommand)){
				computerLevel=ComputerLevel.invincible;
			}else if("电脑红方".equals(actionCommand)){				
				android[REDPLAYSIGN]=!android[REDPLAYSIGN];
				if(android[REDPLAYSIGN] && (REDPLAYSIGN==play || turn_num<=0) ){
					if(turn_num<=0){
						play=REDPLAYSIGN;
						moveHistory.play=1-REDPLAYSIGN;
					}
					computeThinkStart();
				}
			}else if("电脑黑方".equals(actionCommand)){
				android[BLACKPLAYSIGN]=!android[BLACKPLAYSIGN];
				if(android[BLACKPLAYSIGN] && (BLACKPLAYSIGN==play || turn_num<=0)){
					if(turn_num<=0){
						play=BLACKPLAYSIGN;
						moveHistory.play=1-BLACKPLAYSIGN;
					}
					computeThinkStart();
				}
			}else if("HASH表小".equals(actionCommand)){
				if(turn_num==0){
					TranspositionTable.setHashSize(0x7FFFF);
				}
			}else if("HASH表中".equals(actionCommand)){
				if(turn_num==0){
					TranspositionTable.setHashSize(0xFFFFF);
				}
			}else if("HASH表大".equals(actionCommand)){
				if(turn_num==0){
					TranspositionTable.setHashSize(0x1FFFFF);
				}
			}else if("后台思考".equals(actionCommand)){
				isBackstageThink=!isBackstageThink;
			}else if("音效".equals(actionCommand)){
				isSound=!isSound;
			}
		}
			
	}
 
	private void opponentMove(){
		setHashTablesEnabled();
		//查看是否以胜利
		if(!checkGameOver()){
			turn_num++;
			play=1-play; //交换双方
			//对手是否为电脑
			if(android[play]){
				computeThinkStart();
			}
		}
	}
	private void computeThinkStart(){
		//设置后台思考
		if(isBackstageThink && (guessLink!=null && moveHistory!=null) ){
			//查看是否猜中
			if(guessLink.getMoveNode().equals(moveHistory.getMoveNode())){
				new Thread(){
					public void run(){
						System.out.println("---->猜测命中！！");
						try {
							//加入时间控制
							backstageAIThink.launchTimer();
							backstageThinkThread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
							computeThink();
						}
						computeAIMoving(guessLink.getNextLink());
					}
				}.start();
			}else{
				new Thread(){
					public void run(){
						System.out.println("--->未命中");
						//如果没中进行运算
						backstageAIThink.setStop();
						try {
							backstageThinkThread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("--->重新思考");
						computeThink();
					}
				}.start();
			}
		}else{
			computeThink();
		}
	}
	private  void computeThink(){ 
		new Thread(){
			public void run(){
				_AIThink.setLocalVariable(computerLevel,chessParamCont,moveHistory);
				_AIThink.launchTimer();
				_AIThink.run();
				computeAIMoving(moveHistory.getNextLink());
			}
		}.start();
	}

	private void computeAIMoving(NodeLink nodeLink) {
		moveHistory = nodeLink;
		// if(!checkGameOver()){
		if (nodeLink != null && nodeLink.getMoveNode() != null) {
			MoveNode moveNode = nodeLink.getMoveNode();
			showMoveNode(moveNode);
		}
		opponentMove();
		backstageThink();
		// }
	}
	private Thread backstageThinkThread=null;
	private NodeLink guessLink ;
	//后台思考
	private void backstageThink(){
		if(!isBackstageThink){return;}
		if(moveHistory.getNextLink()!=null && moveHistory.getNextLink().getMoveNode()!=null){

			backstageThinkThread=new Thread(){
				public void run(){
					//猜测的着法
					guessLink = moveHistory.getNextLink();
					backstageAIThink.setLocalVariable(computerLevel,chessParamCont,guessLink);
					System.out.println("---->开始猜测("+guessLink.getMoveNode()+")");
					backstageAIThink.guessRun(guessLink.getMoveNode());
				}
			};
			backstageThinkThread.start();
		}
	}
	private void showMoveNode(MoveNode moveNode){
		if(moveNode!=null){
			move(moveNode);
			cmp.moveOperate(moveNode); 
			transTable.synchroZobristBoardToStatic();
		}
	}
	private void setHashTablesEnabled(){
		hashSize2M.setEnabled(false);
		hashSize32M.setEnabled(false);
		hashSize64M.setEnabled(false);
	}
	
	/*
	 * 记取上次保存记录
	 */
	public  String readSaved(){
		String fen = null;
		FileInputStream fileInput = null;
		try {
			File chessFile = new File("chess.txt");
			fileInput = new java.io.FileInputStream(chessFile);
			BufferedReader bufferedReader = new BufferedReader(
					new java.io.InputStreamReader(fileInput));

			while (bufferedReader.ready()) {
				fen = bufferedReader.readLine();
			}
			if (fen != null) {
				if (JOptionPane.showConfirmDialog(this, "检测到有存档是否继续上次游戏?", "信息",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					ObjectInputStream objInput =null;
					try{
						objInput = new ObjectInputStream(new FileInputStream("moves.dat"));
						moveHistory=(NodeLink) objInput.readObject();
						turn_num=20;
					}catch(Exception e){
						System.err.println("========读取历史记录出错 moves.dat");
					}finally{
						if(objInput!=null){
							objInput.close();
						}
					}
				} else {
					chessFile.deleteOnExit();
					fen="c6c5  rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR b - - 0 1";
				}
			}
		} catch (Exception e) {
			fen="c6c5  rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR b - - 0 1";
		} finally {
			if(fileInput!=null){
				try {
					fileInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fen;
	}
	public static void main(String args[]) {
		new ChessBoardMain();
	}
	public void launchSound(int type){
		if(isSound){ //开启音效
			new SoundEffect(type).start();
		}
	}
	private static final String movePathPath="/sounds/MOVE.WAV";
	private static final String checkedPath="/sounds/CHECKED.WAV";
	private static final String capturePath="/sounds/CAPTURE.WAV";
	private static final String lossPath="/sounds/LOSS.WAV";
	private static final URL MOVEPATHURL = ChessBoardMain.class.getResource(movePathPath);
	private static final URL CHECKEDURL = ChessBoardMain.class.getResource(checkedPath);
	private static final URL CAPTUREURL = ChessBoardMain.class.getResource(capturePath);
	private static final URL LOSSURL = ChessBoardMain.class.getResource(lossPath);
	class SoundEffect extends Thread{
		public final static int MOVE_SOUND=1;
		public final static int CAPTURE_SOUND=2;
		public final static int CHECKED_SOUND=3;
		public final static int LOSS_SOUND=4;
		 
		URL url=null;
		
		public SoundEffect(int k){
			this.setDaemon(true);
			switch(k){
				case MOVE_SOUND:
					url=MOVEPATHURL;
					break;
				case CAPTURE_SOUND:
					url=CAPTUREURL;
					break;
				case CHECKED_SOUND:
					url=CHECKEDURL;
					break;
				case LOSS_SOUND:
					url=LOSSURL;
					break;
			}
		}
		public void run(){ 
			AudioClip  clip  =  Applet.newAudioClip(url);  
			clip.play();
		}
	}
}