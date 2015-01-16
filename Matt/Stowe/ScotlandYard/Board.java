package Matt.Stowe.ScotlandYard;

import java.io.*;
import java.awt.*;

public class Board{
	private SYNode[] nodes;
	private int turnIndex;
	private boolean consideringTaxiMoves;
	private boolean consideringBusMoves;
	private boolean consideringUndergroundMoves;
	private boolean consideringFerryMoves;
	private boolean investigatorsWon;
	private boolean mrXWon;
	private boolean doubleMoving;
	private boolean isExtraMove;
	public SYNode[] GetNodes(){return this.nodes;}
	public Player[] Players;

	public static final int MOVETYPE_TAXI=0;
	public static final int MOVETYPE_BUS=1;
	public static final int MOVETYPE_UNDERGROUND=2;
	public static final int MOVETYPE_BLACKTICKET=3;
	
	public Board(String netfilename){
		this.consideringTaxiMoves=false;
		this.consideringBusMoves=false;
		this.consideringUndergroundMoves=false;
		this.consideringFerryMoves=false;
		this.investigatorsWon=false;
		this.mrXWon=false;
		this.doubleMoving=false;
		this.isExtraMove=false;
		try{
			BufferedReader fin=new BufferedReader(new FileReader(netfilename));
			int nodecount=Integer.parseInt(fin.readLine());
			this.nodes=new SYNode[nodecount];
			int spirals=4;
			for(int i=0;i<nodes.length;i++){
				nodes[i]=new SYNode(i);
			}
			
			
			String nextfileline=fin.readLine();
			String nodetype="T";
			while(nextfileline!=null){
				if(nextfileline.charAt(0)=='T'
				||nextfileline.charAt(0)=='B'
				||nextfileline.charAt(0)=='U'
				||nextfileline.charAt(0)=='F'){
					nodetype=nextfileline;
					nextfileline=fin.readLine();
					continue;
				}
				
				String[] namesplit=nextfileline.split(":");
				int nodeid=Integer.parseInt(namesplit[0])-1;
				this.nodes[nodeid].Name=namesplit[0];
				int splitNodesIndex=1;
				if("T".equals(nodetype)){
					if(namesplit.length>splitNodesIndex&&!namesplit[splitNodesIndex].equals("")){
					String[] xysplit=namesplit[splitNodesIndex].split(",");
					this.nodes[nodeid].X=Integer.parseInt(xysplit[0]);
					this.nodes[nodeid].Y=Integer.parseInt(xysplit[1]);
					}else{this.nodes[nodeid].X=(nodeid%18)*40+20;this.nodes[nodeid].Y=nodeid/18*40+250;}
					splitNodesIndex++;
				}
				if(namesplit.length>splitNodesIndex){
					String[] othersplit=namesplit[splitNodesIndex].split(",");
					for(int c=0;c<othersplit.length;c++){
						int othernodeid=Integer.parseInt(othersplit[c])-1;
						if("T".equals(nodetype)){
							nodes[nodeid].AddTaxi(nodes[othernodeid]);
							nodes[othernodeid].AddTaxi(nodes[nodeid]);
						}else if("B".equals(nodetype)){
							nodes[nodeid].AddBus(nodes[othernodeid]);
							nodes[othernodeid].AddBus(nodes[nodeid]);
						}else if("U".equals(nodetype)){
							nodes[nodeid].AddUnderground(nodes[othernodeid]);
							nodes[othernodeid].AddUnderground(nodes[nodeid]);
						}else if("F".equals(nodetype)){
							nodes[nodeid].AddFerry(nodes[othernodeid]);
							nodes[othernodeid].AddFerry(nodes[nodeid]);
						}
					}
				}
				nextfileline=fin.readLine();
			}
			fin.close();
		}catch(FileNotFoundException fe){
			fe.printStackTrace();
			System.exit(1);
		}catch(IOException ioe){
			ioe.printStackTrace();
			System.exit(1);
		}
		
		this.Players=new Player[6];
		for(int p=0;p<this.Players.length;p++){
			boolean sharingASpace=true;
			int startingSpace=0;
			while(sharingASpace){
				startingSpace=(int)(Math.random()*this.nodes.length);
				sharingASpace=false;
				for(int op=p-1;op>=0&&!sharingASpace;op--){
					if(this.Players[op].NodeID==startingSpace)
						sharingASpace=true;
				}
			}
			this.Players[p]=new Player(p,p==5?"X":"P"+(p+1), startingSpace, p==5?Player.CONTROLTYPE_AI:Player.CONTROLTYPE_HUMAN);
		}
		this.turnIndex=this.Players.length-1;
		if(!this.Players[this.turnIndex].IsHuman()){
			this.Players[this.turnIndex].AIMove(this,this.Players,this.nodes[this.Players[this.turnIndex].NodeID]);
		}
	}
	
	public boolean MouseClicked(int x, int y, boolean aisimulated){
		if(!this.Players[this.turnIndex].IsHuman()&&!aisimulated
		||this.investigatorsWon||this.mrXWon)
			return false;
		int minY=GraphicalUserInterface.YBUFFER+115*this.turnIndex+35;
		if(x>850&&x<900&&y>minY&&y<minY+20&&this.Players[this.turnIndex].Taxis>0){
			this.consideringTaxiMoves=true;
			this.consideringBusMoves=false;
			this.consideringUndergroundMoves=false;
			this.consideringFerryMoves=false;
			removeHighlightFromAllNodes();
			highlightTaxiNodes();
			return true;
		}else if(x>850&&x<900&&y>minY+25&&y<minY+45&&this.Players[this.turnIndex].Buses>0){
			this.consideringTaxiMoves=false;
			this.consideringBusMoves=true;
			this.consideringUndergroundMoves=false;
			this.consideringFerryMoves=false;
			removeHighlightFromAllNodes();
			highlightBusNodes();
			return true;
		}else if(x>850&&x<900&&y>minY+50&&y<minY+70&&this.Players[this.turnIndex].Undergrounds>0){
			this.consideringTaxiMoves=false;
			this.consideringBusMoves=false;
			this.consideringUndergroundMoves=true;
			this.consideringFerryMoves=false;
			removeHighlightFromAllNodes();
			highlightUndergroundNodes();
			return true;
		}else if(x>850&&x<900&&y>minY+75&&y<minY+95&&this.Players[this.turnIndex].BlackTickets>0){
			this.consideringTaxiMoves=true;
			this.consideringBusMoves=true;
			this.consideringUndergroundMoves=true;
			this.consideringFerryMoves=true;
			removeHighlightFromAllNodes();
			highlightBlackTicketNodes();
			return true;
		}else if(x>850&&x<900&&y>minY+100&&y<minY+120&&this.Players[this.turnIndex].DoubleMoves>0&&!this.doubleMoving&&!this.isExtraMove){
			this.doubleMoving=true;
			this.Players[this.turnIndex].DoubleMoves--;
			return true;
		}else{
			for(int n=0;n<this.nodes.length;n++){
				if(x>this.nodes[n].X-SYNode.NODE_SIZE/4&&x<this.nodes[n].X+SYNode.NODE_SIZE/4
				&&y>this.nodes[n].Y-SYNode.NODE_SIZE/4&&y<this.nodes[n].Y+SYNode.NODE_SIZE/4){
					if(this.nodes[n].Highlighted){
						if(this.consideringFerryMoves){
							this.Players[this.turnIndex].TakeBlackTicketTo(n);
						}else if(this.consideringUndergroundMoves){
							this.Players[this.turnIndex].TakeUndergroundTo(n);
							if(!this.Players[this.turnIndex].IsMisterX()){
								this.Players[this.Players.length-1].Undergrounds++;
							}
						}else if(this.consideringBusMoves){
							this.Players[this.turnIndex].TakeBusTo(n);
							if(!this.Players[this.turnIndex].IsMisterX()){
								this.Players[this.Players.length-1].Buses++;
							}
						}else{
							this.Players[this.turnIndex].TakeTaxiTo(n);
							if(!this.Players[this.turnIndex].IsMisterX()){
								this.Players[this.Players.length-1].Taxis++;
							}
						}
						removeHighlightFromAllNodes();
						int totalInvestigatorMovesRemaining=0;
						for(int wincheck=0;wincheck<this.Players.length-1;wincheck++){
							if(this.Players[wincheck].NodeID==this.Players[this.Players.length-1].NodeID){
								this.investigatorsWon=true;
								return true;
							}
							if(this.playerCanMove(wincheck)){
								totalInvestigatorMovesRemaining+=this.Players[wincheck].Taxis
									+this.Players[wincheck].Buses
									+this.Players[wincheck].Undergrounds;
							}
						}
						if(totalInvestigatorMovesRemaining==0){
							this.mrXWon=true;
							return true;
						}
						if(this.doubleMoving&&playerCanMove(this.turnIndex)){
							this.doubleMoving=false;
							this.isExtraMove=true;
							if(!this.Players[this.turnIndex].IsHuman()){
								this.Players[this.turnIndex].AIMove(this,this.Players,this.nodes[this.Players[this.turnIndex].NodeID]);
							}
						}else{
							this.isExtraMove=false;
							do{
								this.turnIndex=(this.turnIndex+1)%this.Players.length;
							}while(!CurrentPlayerCanMove());
							if(!this.Players[this.turnIndex].IsHuman()){
								this.Players[this.turnIndex].AIMove(this,this.Players,this.nodes[this.Players[this.turnIndex].NodeID]);
							}
						}
						return true;
					}else{
						break;
					}
				}
			}
		}
		
		return false;
	}
	
	private void removeHighlightFromAllNodes(){
		for(int n=0;n<this.nodes.length;n++){
			this.nodes[n].Highlighted=false;
		}
	}
	
	private boolean CurrentPlayerCanMove(){
		return this.playerCanMove(this.turnIndex);
	}
	
	private boolean playerCanMove(int pindex){
		if(this.Players[pindex].IsMisterX()&&this.Players[this.Players.length-1].UsedAllMoves()){
			return false;
		}
		SYNode[] reachables=this.nodes[this.Players[pindex].NodeID].GetTaxis();
		for(int t=0;t<reachables.length&&this.Players[pindex].Taxis>0;t++){
			if(!this.Players[pindex].IsMisterX()){
				boolean takenByOtherInvestigator=false;
				for(int p=0;p<this.Players.length&&!takenByOtherInvestigator;p++){
					if(this.Players[p].IsMisterX())
						continue;
					if(this.Players[p].NodeID==reachables[t].GetID()){
						takenByOtherInvestigator=true;
					}
				}
				if(takenByOtherInvestigator)
					continue;
			}
			return true;
		}
		
		reachables=this.nodes[this.Players[pindex].NodeID].GetBuses();
		for(int t=0;t<reachables.length&&this.Players[pindex].Buses>0;t++){
			if(!this.Players[pindex].IsMisterX()){
				boolean takenByOtherInvestigator=false;
				for(int p=0;p<this.Players.length&&!takenByOtherInvestigator;p++){
					if(this.Players[p].IsMisterX())
						continue;
					if(this.Players[p].NodeID==reachables[t].GetID()){
						takenByOtherInvestigator=true;
					}
				}
				if(takenByOtherInvestigator)
					continue;
			}
			return true;
		}
		
		reachables=this.nodes[this.Players[pindex].NodeID].GetUndergrounds();
		for(int t=0;t<reachables.length&&this.Players[pindex].Undergrounds>0;t++){
			if(!this.Players[pindex].IsMisterX()){
				boolean takenByOtherInvestigator=false;
				for(int p=0;p<this.Players.length&&!takenByOtherInvestigator;p++){
					if(this.Players[p].IsMisterX())
						continue;
					if(this.Players[p].NodeID==reachables[t].GetID()){
						takenByOtherInvestigator=true;
					}
				}
				if(takenByOtherInvestigator)
					continue;
			}
			return true;
		}
		return false;
	}
	
	private void highlightTaxiNodes(){
		SYNode[] reachables=this.nodes[this.Players[this.turnIndex].NodeID].GetTaxis();
		for(int t=0;t<reachables.length;t++){
			if(!this.Players[this.turnIndex].IsMisterX()){
				boolean takenByOtherInvestigator=false;
				for(int p=0;p<this.Players.length&&!takenByOtherInvestigator;p++){
					if(this.Players[p].IsMisterX())
						continue;
					if(this.Players[p].NodeID==reachables[t].GetID()){
						takenByOtherInvestigator=true;
					}
				}
				if(takenByOtherInvestigator)
					continue;
			}
			reachables[t].Highlighted=true;
		}
	}
	
	private void highlightBusNodes(){
		SYNode[] reachables=this.nodes[this.Players[this.turnIndex].NodeID].GetBuses();
		for(int t=0;t<reachables.length;t++){
			if(!this.Players[this.turnIndex].IsMisterX()){
				boolean takenByOtherInvestigator=false;
				for(int p=0;p<this.Players.length&&!takenByOtherInvestigator;p++){
					if(this.Players[p].IsMisterX())
						continue;
					if(this.Players[p].NodeID==reachables[t].GetID()){
						takenByOtherInvestigator=true;
					}
				}
				if(takenByOtherInvestigator)
					continue;
			}
			reachables[t].Highlighted=true;
		}
	}

	private void highlightUndergroundNodes(){
		SYNode[] reachables=this.nodes[this.Players[this.turnIndex].NodeID].GetUndergrounds();
		for(int t=0;t<reachables.length;t++){
			if(!this.Players[this.turnIndex].IsMisterX()){
				boolean takenByOtherInvestigator=false;
				for(int p=0;p<this.Players.length&&!takenByOtherInvestigator;p++){
					if(this.Players[p].IsMisterX())
						continue;
					if(this.Players[p].NodeID==reachables[t].GetID()){
						takenByOtherInvestigator=true;
					}
				}
				if(takenByOtherInvestigator)
					continue;
			}
		reachables[t].Highlighted=true;
		}
	}

	private void highlightBlackTicketNodes(){
		highlightTaxiNodes();
		highlightBusNodes();
		highlightUndergroundNodes();
		SYNode[] reachables=this.nodes[this.Players[this.turnIndex].NodeID].GetFerries();
		for(int t=0;t<reachables.length;t++){
			reachables[t].Highlighted=true;
		}
	}
	
	public boolean IsInvestigatorAt(int id){
		boolean takenByInvestigator=false;
		for(int p=0;p<this.Players.length&&!takenByInvestigator;p++){
			if(this.Players[p].IsMisterX())
				continue;
			if(this.Players[p].NodeID==id){
				takenByInvestigator=true;
			}
		}
		return takenByInvestigator;
	}
	
	public void draw(Graphics g){
		for(int n=0;n<this.nodes.length;n++){
			g.setColor(GraphicalUserInterface.TAXICOLOR);
			SYNode[] othertaxis=this.nodes[n].GetTaxis();
			for(int onode=0;onode<othertaxis.length;onode++){
				if(othertaxis[onode].GetID()<n)
					continue;
				g.drawLine(this.nodes[n].X,this.nodes[n].Y,othertaxis[onode].X,othertaxis[onode].Y);
			}
			g.setColor(GraphicalUserInterface.BUSCOLOR);
			SYNode[] otherbuses=this.nodes[n].GetBuses();
			for(int onode=0;onode<otherbuses.length;onode++){
				if(otherbuses[onode].GetID()<n)
					continue;
				g.drawLine(this.nodes[n].X-1,this.nodes[n].Y-1,otherbuses[onode].X-1,otherbuses[onode].Y-1);
			}
			g.setColor(GraphicalUserInterface.UNDERGROUNDCOLOR);
			SYNode[] otherundergrounds=this.nodes[n].GetUndergrounds();
			for(int onode=0;onode<otherundergrounds.length;onode++){
				if(otherundergrounds[onode].GetID()<n)
					continue;
				g.drawLine(this.nodes[n].X+1,this.nodes[n].Y+1,otherundergrounds[onode].X+1,otherundergrounds[onode].Y+1);
			}
			g.setColor(GraphicalUserInterface.BLACKTICKETCOLOR);
			SYNode[] otherferries=this.nodes[n].GetFerries();
			for(int onode=0;onode<otherferries.length;onode++){
				if(otherferries[onode].GetID()<n)
					continue;
				g.drawLine(this.nodes[n].X,this.nodes[n].Y,otherferries[onode].X,otherferries[onode].Y);
			}
		}

		for(int p=0;p<this.Players.length;p++){
			this.Players[p].draw(g,this.nodes[this.Players[p].NodeID].X,this.nodes[this.Players[p].NodeID].Y,this.turnIndex);
		}

		g.setColor(Color.black);
		g.fillOval(850,GraphicalUserInterface.YBUFFER+5+115*this.turnIndex,20,20);

		for(int n=0;n<this.nodes.length;n++){
			this.nodes[n].draw(g);
		}
		
		if(this.investigatorsWon){
			g.drawString("Mr. X has been captured!",915,500);
			this.Players[5].drawMoves(g, 915, GraphicalUserInterface.YBUFFER);
		}
		if(this.mrXWon){
			g.drawString("Mr. X has blown up the city!", 915, 500);
			this.Players[5].drawMoves(g, 915, GraphicalUserInterface.YBUFFER);
		}
	}
}