package Matt.Stowe.ScotlandYard;

import java.awt.*;
import java.util.Vector;

public class Player{
	private static final Color[] COLORS = new Color[]{ Color.orange, Color.green, new Color(64,64,255), new Color(128,64,28), Color.magenta, Color.white };
	private static final int[] REVEAL_TURNS = new int[]{ 3, 8, 13, 18, 24 };
	private boolean isRevealed(int turnIndex){
		boolean xIsRevealed = false;
		for(int xrv = 0; xrv < REVEAL_TURNS.length && !xIsRevealed; xrv++){
			if(REVEAL_TURNS[xrv] == turnIndex){
				xIsRevealed = true;
			}
		}
		return xIsRevealed;
	}
	
	public static final int CONTROLTYPE_HUMAN = 0;
	public static final int CONTROLTYPE_AI = 1;
	
	private int controlType;
	public boolean IsHuman(){ return this.controlType == CONTROLTYPE_HUMAN; }
	
	private int ID;
	public int[] moves;
	private int[] moveNodeIDs;
	private int moveIndex;
	public boolean UsedAllMoves(){ return moveIndex == this.moves.length; }

	private boolean usedBlackTicketSinceReveal;
	private int lastSeen;
	public String getLastSeen(){ return lastSeen < 0 ? "?" : (""+lastSeen); }

	public String Name;
	public int Taxis;
	public int Buses;
	public int Undergrounds;
	public int BlackTickets;
	public int DoubleMoves;
	private boolean doubleMoving;
	public int NodeID;
	
	public Color GetColor(){ return this.COLORS[this.ID]; }
	
	public Player(int id, String name, int startingNodeID, int controltype){
		this.usedBlackTicketSinceReveal = false;
		this.doubleMoving = false;
		this.controlType = controltype;
		this.ID = id;
		this.moves = new int[24];
		this.moveNodeIDs = new int[24];
		this.moveIndex = 0;
		this.Name = name;
		this.lastSeen = -1;

		this.Taxis = 10;
		this.Buses = 8;
		this.Undergrounds = 4;
		this.BlackTickets = 0;
		this.DoubleMoves = 0;
		if(this.IsMisterX()){
			this.Taxis = 4;
			this.Buses = 4;
			this.Undergrounds = 3;
			this.BlackTickets = 5;
			this.DoubleMoves = 2;
		}
		this.NodeID=startingNodeID;
	}
	
	public boolean IsMisterX(){ return this.ID == 5; }
	
	public void TakeBlackTicketTo(int nodeid){
		this.BlackTickets--;
		this.moves[this.moveIndex] = Board.MOVETYPE_BLACKTICKET;
		this.moveNodeIDs[this.moveIndex] = nodeid + 1;
		this.moveIndex++;
		this.NodeID = nodeid;
		if(this.isRevealed(this.moveIndex)){
			this.lastSeen = nodeid + 1;
			this.usedBlackTicketSinceReveal = false;
		}
	}

	public void TakeUndergroundTo(int nodeid){
		this.Undergrounds--;
		this.moves[this.moveIndex] = Board.MOVETYPE_UNDERGROUND;
		this.moveNodeIDs[this.moveIndex] = nodeid + 1;
		this.moveIndex++;
		this.NodeID = nodeid;
		if(this.isRevealed(this.moveIndex)){
			this.lastSeen = nodeid + 1;
			this.usedBlackTicketSinceReveal = false;
		}
	}

	public void TakeBusTo(int nodeid){
		this.Buses--;
		this.moves[this.moveIndex] = Board.MOVETYPE_BUS;
		this.moveNodeIDs[this.moveIndex] = nodeid + 1;
		this.moveIndex++;
		this.NodeID = nodeid;
		if(this.isRevealed(this.moveIndex)){
			this.lastSeen = nodeid + 1;
			this.usedBlackTicketSinceReveal = false;
		}
	}

	public void TakeTaxiTo(int nodeid){
		this.Taxis--;
		this.moves[this.moveIndex] = Board.MOVETYPE_TAXI;
		this.moveNodeIDs[this.moveIndex] = nodeid + 1;
		this.moveIndex++;
		this.NodeID = nodeid;
		if(this.isRevealed(this.moveIndex)){
			this.lastSeen = nodeid + 1;
			this.usedBlackTicketSinceReveal = false;
		}
	}
	
	private boolean nodeIsOneAwayFromInvestigator(Board b, SYNode node){
		SYNode[] oneawaytaxis = node.GetTaxis();
		for(int oa = 0; oa < oneawaytaxis.length; oa++){
			if(b.IsInvestigatorAt(oneawaytaxis[oa].GetID())){
				return true;
			}
		}
		
		SYNode[] oneawaybuses = node.GetBuses();
		for(int oa = 0; oa < oneawaybuses.length; oa++){
			if(b.IsInvestigatorAt(oneawaybuses[oa].GetID())){
				return true;
			}
		}

		SYNode[] oneawayundergrounds = node.GetUndergrounds();
		for(int oa = 0; oa < oneawayundergrounds.length; oa++){
			if(b.IsInvestigatorAt(oneawayundergrounds[oa].GetID())){
				return true;
			}
		}

		return false;
	}
	
	private int pointsForNode(Board b, SYNode node){
		int points = 0;
		if(b.IsInvestigatorAt(node.GetID())){
			points -= 100;
		}
		
		SYNode[] oneawaytaxis = node.GetTaxis();
		for(int oa = 0; oa < oneawaytaxis.length; oa++){
			if(b.IsInvestigatorAt(oneawaytaxis[oa].GetID())){
				points -= 10;
			}
		}
		
		SYNode[] oneawaybuses = node.GetBuses();
		for(int oa = 0; oa < oneawaybuses.length; oa++){
			if(b.IsInvestigatorAt(oneawaybuses[oa].GetID())){
				points -= 10;
			}
		}

		SYNode[] oneawayundergrounds = node.GetUndergrounds();
		for(int oa = 0; oa < oneawayundergrounds.length; oa++){
			if(b.IsInvestigatorAt(oneawayundergrounds[oa].GetID())){
				points -= 10;
			}
		}
		
		points += oneawaytaxis.length + oneawaybuses.length + oneawayundergrounds.length + (int)(Math.random()*3);
		
		return points;
	}
	
	public void AIMove(Board b, Player[] players, SYNode currentnode){
		boolean wasDoubleMove = this.doubleMoving;
	
		SYNode[] availabletaxis = currentnode.GetTaxis();
		Vector<Integer> besttaxinodeindexes = new Vector<Integer>();
		int besttaxipoints = -999999;
		for(int i = 0; i < availabletaxis.length; i++){
			int points = this.pointsForNode(b, availabletaxis[i]);
		
			if(points > besttaxipoints){
				besttaxinodeindexes.removeAllElements();
				besttaxipoints = points;
			}
			if(points == besttaxipoints){
				besttaxinodeindexes.add(new Integer(i));
			}
		}
		
		SYNode[] availablebuses = currentnode.GetBuses();
		Vector<Integer> bestbusnodeindexes = new Vector<Integer>();
		int bestbuspoints = -999999;
		for(int i = 0; i < availablebuses.length; i++){
			int points = this.pointsForNode(b, availablebuses[i]);
		
			if(points > bestbuspoints){
				bestbusnodeindexes.removeAllElements();
				bestbuspoints = points;
			}
			if(points == bestbuspoints){
				bestbusnodeindexes.add(new Integer(i));
			}
		}

		SYNode[] availableundergrounds = currentnode.GetUndergrounds();
		Vector<Integer> bestugnodeindexes = new Vector<Integer>();
		int bestugpoints = -999999;
		for(int i = 0; i < availableundergrounds.length; i++){
			int points = this.pointsForNode(b, availableundergrounds[i]);
		
			if(points > bestugpoints){
				bestugnodeindexes.removeAllElements();
				bestugpoints = points;
			}
			if(points == bestugpoints){
				bestugnodeindexes.add(new Integer(i));
			}
		}

		SYNode[] availableferries = currentnode.GetFerries();
		Vector<Integer> bestferrynodeindexes = new Vector<Integer>();
		int bestferrypoints = -999999;
		for(int i = 0; i < availableferries.length; i++){
			int points = this.pointsForNode(b, availableferries[i]);
		
			if(points > bestferrypoints){
				bestferrynodeindexes.removeAllElements();
				bestferrypoints = points;
			}
			if(points == bestferrypoints){
				bestferrynodeindexes.add(new Integer(i));
			}
		}

		SYNode chosennode = null;
		boolean hasBlackTickets = this.BlackTickets > 0;
		boolean revealedAtLeastOnce = this.moveIndex > 3;
		boolean aboutToReveal = this.isRevealed(this.moveIndex + 1);
		boolean canConsiderBlackTickets = hasBlackTickets && revealedAtLeastOnce && !this.usedBlackTicketSinceReveal && !aboutToReveal;
		int minY = GraphicalUserInterface.YBUFFER + 115 * this.ID;
		int buttonsX = 855;
		int doubleMoveY = minY + 140;
		int blackTicketY = minY + 115;
		int undergroundY = minY + 90;
		int busY = minY + 65;
		int taxiY = minY + 40;

		if(hasBlackTickets && bestferrypoints > bestugpoints && bestferrypoints > bestbuspoints && bestferrypoints > besttaxipoints){
			chosennode = availableferries[bestferrynodeindexes.elementAt((int)(Math.random() * bestferrynodeindexes.size()))];
			if(this.nodeIsOneAwayFromInvestigator(b, chosennode) && this.DoubleMoves > 0 && !this.doubleMoving){
				this.doubleMoving = true;
				b.MouseClicked(buttonsX, doubleMoveY, true);
			}
			b.MouseClicked(buttonsX, blackTicketY, true);
		}else if(bestugpoints > bestbuspoints && bestugpoints > besttaxipoints){
			chosennode = availableundergrounds[bestugnodeindexes.elementAt((int)(Math.random() * bestugnodeindexes.size()))];
			if(this.nodeIsOneAwayFromInvestigator(b, chosennode) && this.DoubleMoves > 0 && !this.doubleMoving){
				this.doubleMoving = true;
				b.MouseClicked(buttonsX, doubleMoveY, true);
			}
			boolean revealingAfterDoubleMove = (this.doubleMoving && this.isRevealed(this.moveIndex + 2));
			if(canConsiderBlackTickets && !revealingAfterDoubleMove
			&&((Math.random() < .5)
				|| (bestugpoints < 0 && !(!wasDoubleMove && this.doubleMoving)))){
				b.MouseClicked(buttonsX, blackTicketY, true);
				this.usedBlackTicketSinceReveal = true;
			}else
				b.MouseClicked(buttonsX, undergroundY, true);
		}else if(bestbuspoints > besttaxipoints){
			chosennode = availablebuses[bestbusnodeindexes.elementAt((int)(Math.random() * bestbusnodeindexes.size()))];
			if(this.nodeIsOneAwayFromInvestigator(b, chosennode) && this.DoubleMoves > 0 && !this.doubleMoving){
				this.doubleMoving = true;
				b.MouseClicked(buttonsX, doubleMoveY, true);
			}
			boolean revealingAfterDoubleMove = (this.doubleMoving && this.isRevealed(this.moveIndex + 2));
			if(canConsiderBlackTickets && !revealingAfterDoubleMove
			&&((Math.random() < .5 && availableundergrounds.length > 0)
				|| (bestbuspoints < 0 && !(!wasDoubleMove && this.doubleMoving)))){
				b.MouseClicked(buttonsX, blackTicketY, true);
				this.usedBlackTicketSinceReveal = true;
			}else
				b.MouseClicked(buttonsX, busY, true);
		}else{
			chosennode = availabletaxis[besttaxinodeindexes.elementAt((int)(Math.random() * besttaxinodeindexes.size()))];
			if(this.nodeIsOneAwayFromInvestigator(b, chosennode) && this.DoubleMoves > 0 && !this.doubleMoving){
				this.doubleMoving = true;
				b.MouseClicked(buttonsX, doubleMoveY, true);
			}
			boolean revealingAfterDoubleMove = (this.doubleMoving && this.isRevealed(this.moveIndex + 2));
			if(canConsiderBlackTickets && !revealingAfterDoubleMove
			&&((Math.random() < .5 && availableundergrounds.length > 0)
				|| (besttaxipoints < 0 && availablebuses.length > 0 && !(!wasDoubleMove && this.doubleMoving)))){
				b.MouseClicked(buttonsX, blackTicketY, true);
				this.usedBlackTicketSinceReveal = true;
			}else
				b.MouseClicked(buttonsX, taxiY, true);
		}
		
		if(wasDoubleMove)
			this.doubleMoving = false;
		b.MouseClicked(chosennode.X, chosennode.Y, true);
	}
	
	public void draw(Graphics g, int x, int y, int whoseTurn){
		this.drawInvestigator(g, x, y);
		if(this.IsMisterX()){
			this.drawMisterX(g, x, y);
			if((this.ID == whoseTurn && this.IsHuman()) || this.isRevealed(this.moveIndex)){
				this.drawPawn(g, x, y);
			}
		}
		else{
			this.drawPawn(g, x, y);
		}
	}
	
	public void drawMoves(Graphics g, int minX, int minY){
		for(int t = 0; t < this.moveIndex; t++){
			g.setColor(Color.white);
			g.fillRect(minX + (t / 8) * 50 + 5, minY + (t % 8) * 20, 30, 20);
			
			g.setColor(Color.black);
			g.drawRect(minX + (t / 8) * 50 + 5, minY + (t % 8) * 20, 30, 20);
			g.drawString("" + this.moveNodeIDs[t], minX + (t / 8) * 50 + 8, minY + (t % 8) * 20 + 16);
		}
	}
	
	private void drawMisterX(Graphics g, int x, int y){
		int minY = GraphicalUserInterface.YBUFFER;
		int minX = 915;
		for(int t = 0; t < this.moveIndex; t++){
			switch(this.moves[t]){
			case Board.MOVETYPE_TAXI:
				this.drawTaxiTile(g, minX + (t / 8) * 50, minY + (t % 8) * 20);
			break;
			case Board.MOVETYPE_BUS:
				this.drawBusTile(g, minX + (t / 8) * 50, minY + (t % 8) * 20);
			break;
			case Board.MOVETYPE_UNDERGROUND:
				this.drawUndergroundTile(g, minX + (t / 8) * 50, minY + (t % 8) * 20);
			break;
			case Board.MOVETYPE_BLACKTICKET:
				this.drawBlackTicketTile(g, minX + (t / 8) * 50, minY + (t % 8) * 20);
			break;
			}
		}
		g.setColor(Color.black);
		g.drawRect(minX, minY, 150, 160);
		g.drawRect(minX + 50, minY, 50, 160);
		g.setColor(Color.red);
		for(int rvt = 0; rvt < REVEAL_TURNS.length; rvt++){
			g.drawRect(minX + ((REVEAL_TURNS[rvt] - 1) / 8) * 50, minY + ((REVEAL_TURNS[rvt] - 1) % 8) * 20, 50, 20);
		}
	}
	
	private void drawInvestigator(Graphics g, int x, int y){
		g.setColor(COLORS[this.ID]);
		int minY = GraphicalUserInterface.YBUFFER + 115 * this.ID;
		g.fillRect(810, minY, 100, 30);
		g.setColor(Color.black);
		g.drawRect(810, minY, 100, 30);
		g.drawRect(810, minY, 100, this.IsMisterX() ? 160 : 110);

		this.drawTaxiTile(g, 850, minY + 35);
		g.drawString("" + this.Taxis + " x", 815, minY + 50);
		
		this.drawBusTile(g, 850, minY + 60);
		g.drawString("" + this.Buses + " x", 815, minY + 75);
		
		this.drawUndergroundTile(g, 850, minY + 85);
		g.drawString("" + this.Undergrounds + " x", 815, minY + 100);
		
		if(this.IsMisterX()){
			this.drawBlackTicketTile(g, 850, minY + 110);
			g.setColor(Color.black);
			g.drawString("" + this.BlackTickets + " x", 815, minY + 125);
			
			g.drawRect(850, minY + 135, 50, 20);
			g.drawString("x2", 868, minY + 150);
			g.drawString("" + this.DoubleMoves + " x", 815, minY + 150);

			g.drawString(this.getLastSeen(), 845, minY + 20);
		}
		
		g.drawString(this.Name, 815, minY + 20);
	}
	
	private void drawPawn(Graphics g, int x, int y){
		g.setColor(this.GetColor());
		int radius = SYNode.NODE_SIZE / 2 + 10;
		g.fillOval(x - radius / 2, y - radius / 2, radius, radius);
		if(this.IsMisterX()){
			g.setColor(Color.red);
			g.fillArc(x - radius / 2, y - radius / 2, radius, radius, 35, 20);
			g.fillArc(x - radius / 2, y - radius / 2, radius, radius, 125, 20);
			g.fillArc(x - radius / 2, y - radius / 2, radius, radius, 215, 20);
			g.fillArc(x - radius / 2, y - radius / 2, radius, radius, 305, 20);
		}
		g.setColor(Color.black);
		g.drawOval(x - radius / 2, y - radius / 2, radius, radius);
	}
	
	private void drawTaxiTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.TAXICOLOR);
		g.fillRect(x, y, 50, 20);
		g.setColor(Color.black);
		g.drawString("TAXI", x + 13, y + 15);
		g.drawRect(x, y, 50, 20);
	}
	
	private void drawBusTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.BUSCOLOR);
		g.fillRect(x, y, 50, 20);
		g.setColor(Color.black);
		g.drawString("BUS", x + 13, y + 15);
		g.drawRect(x, y, 50, 20);
	}
	
	private void drawUndergroundTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.UNDERGROUNDCOLOR);
		g.fillRect(x, y, 50, 20);
		g.setColor(Color.black);
		g.drawString("U", x + 22, y + 15);
		g.drawRect(x, y, 50, 20);
	}

	private void drawBlackTicketTile(Graphics g, int x, int y){
		g.setColor(GraphicalUserInterface.BLACKTICKETCOLOR);
		g.fillRect(x, y, 50, 20);
		g.setColor(Color.white);
		g.drawString("?", x + 22, y + 15);
	}
}
