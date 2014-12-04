package Matt.Stowe.ScotlandYard;

import java.awt.*;
import java.util.Vector;

public class Player{
	private static final Color[] COLORS=new Color[]{Color.orange,Color.green,new Color(64,64,255),new Color(128,64,28),Color.magenta,Color.white};
	private static final int[] REVEAL_TURNS=new int[]{3,8,13,21,30};
	private boolean isRevealed(){
		boolean xIsRevealed=false;
		for(int xrv=0;xrv<REVEAL_TURNS.length&&!xIsRevealed;xrv++){
			if(REVEAL_TURNS[xrv]==this.moveIndex){
				xIsRevealed=true;
			}
		}
		return xIsRevealed;
	}
	
	public static final int CONTROLTYPE_HUMAN=0;
	public static final int CONTROLTYPE_AI=1;
	
	private int controlType;
	public boolean IsHuman(){return this.controlType==CONTROLTYPE_HUMAN;}
	
	private int ID;
	public int[] moves;
	private int moveIndex;

	private int lastSeen;
	public String getLastSeen(){return lastSeen<0?"?":(""+lastSeen);}

	public String Name;
	public int Taxis;
	public int Buses;
	public int Undergrounds;
	public int BlackTickets;
	public int DoubleMoves;
	public int NodeID;
	
	public Color GetColor(){return this.COLORS[this.ID];}
	
	public Player(int id, String name, int startingNodeID, int controltype){
		this.controlType=controltype;
		this.ID=id;
		this.moves=new int[30];
		this.moveIndex=0;
		this.Name=name;
		this.lastSeen=-1;
		if(this.IsMisterX()){
			this.Taxis=4;
			this.Buses=4;
			this.Undergrounds=3;
			this.BlackTickets=5;
			this.DoubleMoves=2;
		}else{
			this.Taxis=10;
			this.Buses=8;
			this.Undergrounds=4;
			this.BlackTickets=0;
			this.DoubleMoves=0;
		}
		this.NodeID=startingNodeID;
	}
	
	public boolean IsMisterX(){return this.ID==5;}
	
	public void TakeBlackTicketTo(int nodeid){
		this.BlackTickets--;
		this.moves[this.moveIndex]=Board.MOVETYPE_BLACKTICKET;
		this.moveIndex++;
		this.NodeID=nodeid;
		if(this.isRevealed()){
			this.lastSeen=nodeid+1;
		}
	}

	public void TakeUndergroundTo(int nodeid){
		this.Undergrounds--;
		this.moves[this.moveIndex]=Board.MOVETYPE_UNDERGROUND;
		this.moveIndex++;
		this.NodeID=nodeid;
		if(this.isRevealed()){
			this.lastSeen=nodeid+1;
		}
	}

	public void TakeBusTo(int nodeid){
		this.Buses--;
		this.moves[this.moveIndex]=Board.MOVETYPE_BUS;
		this.moveIndex++;
		this.NodeID=nodeid;
		if(this.isRevealed()){
			this.lastSeen=nodeid+1;
		}
	}

	public void TakeTaxiTo(int nodeid){
		this.Taxis--;
		this.moves[this.moveIndex]=Board.MOVETYPE_TAXI;
		this.moveIndex++;
		this.NodeID=nodeid;
		if(this.isRevealed()){
			this.lastSeen=nodeid+1;
		}
	}
	
	public void AIMove(Board b,Player[] players,SYNode currentnode){
		int minY=GraphicalUserInterface.YBUFFER+115*this.ID+35;
		b.MouseClicked(855, minY+5, true);
		SYNode[] availablenodes=currentnode.GetTaxis();
		Vector<Integer> bestnodeindexes=new Vector<Integer>();
		int bestpoints=0;
		for(int i=0;i<availablenodes.length;i++){
			int points=0;
			if(!b.IsInvestigatorAt(availablenodes[i].GetID())){
				points+=10;
			}else{
				points-=10;
			}
			
			if(points>bestpoints){
				bestnodeindexes.removeAllElements();
				bestpoints=points;
			}
			if(points==bestpoints){
				bestnodeindexes.add(new Integer(i));
			}
		}
		
		SYNode chosennode=availablenodes[bestnodeindexes.elementAt((int)(Math.random()*bestnodeindexes.size()))];
		
		b.MouseClicked(chosennode.X,chosennode.Y, true);
	}
	
	public void draw(Graphics g,int x,int y,int whoseTurn){
		this.drawInvestigator(g,x,y);
		if(this.IsMisterX()){
			this.drawMisterX(g,x,y);
			if((this.ID==whoseTurn&&this.IsHuman())||this.isRevealed()){
				this.drawPawn(g,x,y);
			}
		}
		else{
			this.drawPawn(g,x,y);
		}
	}
	
	private void drawMisterX(Graphics g,int x,int y){
		int minY=GraphicalUserInterface.YBUFFER;
		int minX=915;
		for(int t=0;t<this.moveIndex;t++){
			switch(this.moves[t]){
			case Board.MOVETYPE_TAXI:
				this.drawTaxiTile(g, minX+(t/10)*50, minY+(t%10)*20);
			break;
			case Board.MOVETYPE_BUS:
				this.drawBusTile(g, minX+(t/10)*50, minY+(t%10)*20);
			break;
			case Board.MOVETYPE_UNDERGROUND:
				this.drawUndergroundTile(g, minX+(t/10)*50, minY+(t%10)*20);
			break;
			case Board.MOVETYPE_BLACKTICKET:
				this.drawBlackTicketTile(g, minX+(t/10)*50, minY+(t%10)*20);
			break;
			}
		}
		g.drawRect(minX,minY,150,200);
		g.drawRect(minX+50,minY,50,200);
		g.setColor(Color.red);
		for(int rvt=0;rvt<REVEAL_TURNS.length;rvt++){
			g.drawRect(minX+((REVEAL_TURNS[rvt]-1)/10)*50, minY+((REVEAL_TURNS[rvt]-1)%10)*20,50,20);
		}
	}
	
	private void drawInvestigator(Graphics g,int x,int y){
		g.setColor(COLORS[this.ID]);
		int minY=GraphicalUserInterface.YBUFFER+115*this.ID;
		g.fillRect(810,minY,100,30);
		g.setColor(Color.black);
		g.drawRect(810,minY,100,30);
		g.drawRect(810,minY,100,this.IsMisterX()?160:110);

		this.drawTaxiTile(g,850, minY+35);
		g.drawString(""+this.Taxis+" x",815,minY+50);
		
		this.drawBusTile(g,850,minY+60);
		g.drawString(""+this.Buses+" x",815,minY+75);
		
		this.drawUndergroundTile(g, 850, minY+85);
		g.drawString(""+this.Undergrounds+" x",815,minY+100);
		
		if(this.IsMisterX()){
			this.drawBlackTicketTile(g, 850, minY+110);
			g.setColor(Color.black);
			g.drawString(""+this.BlackTickets+" x",815,minY+125);
			
			g.drawRect(850,minY+135,50,20);
			g.drawString("x2",868,minY+150);
			g.drawString(""+this.DoubleMoves+" x",815,minY+150);

			g.drawString(this.getLastSeen(),845,minY+20);
		}
		
		g.drawString(this.Name,815,minY+20);
	}
	
	private void drawPawn(Graphics g, int x, int y){
		g.setColor(this.GetColor());
		int radius=SYNode.NODE_SIZE/2+10;
		g.fillOval(x-radius/2,y-radius/2,radius,radius);
		if(this.IsMisterX()){
			g.setColor(Color.red);
			g.fillArc(x-radius/2,y-radius/2,radius,radius,35,20);
			g.fillArc(x-radius/2,y-radius/2,radius,radius,125,20);
			g.fillArc(x-radius/2,y-radius/2,radius,radius,215,20);
			g.fillArc(x-radius/2,y-radius/2,radius,radius,305,20);
		}
		g.setColor(Color.black);
		g.drawOval(x-radius/2,y-radius/2,radius,radius);
	}
	
	private void drawTaxiTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.TAXICOLOR);
		g.fillRect(x,y,50,20);
		g.setColor(Color.black);
		g.drawString("TAXI",x+13,y+15);
		g.drawRect(x,y,50,20);
	}
	
	private void drawBusTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.BUSCOLOR);
		g.fillRect(x,y,50,20);
		g.setColor(Color.black);
		g.drawString("BUS",x+13,y+15);
		g.drawRect(x,y,50,20);
	}
	
	private void drawUndergroundTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.UNDERGROUNDCOLOR);
		g.fillRect(x,y,50,20);
		g.setColor(Color.black);
		g.drawString("U",x+22,y+15);
		g.drawRect(x,y,50,20);
	}

	private void drawBlackTicketTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.BLACKTICKETCOLOR);
		g.fillRect(x,y,50,20);
		g.setColor(Color.white);
		g.drawString("?",x+22,y+15);
	}
}
