package Matt.Stowe.ScotlandYard;

import java.util.Vector;
import java.awt.*;

public class SYNode{
	public static final int NODE_SIZE=50;
	private Vector<SYNode> taxis;
	private Vector<SYNode> buses;
	private Vector<SYNode> undergrounds;
	private Vector<SYNode> ferries;
	private int id;
	public int GetID(){return this.id;}
	
	public int X;
	public int Y;
	public String Name;
	public boolean Highlighted;
	
	public SYNode(int ID){
		this.taxis=new Vector<SYNode>();
		this.buses=null;
		this.undergrounds=null;
		this.ferries=null;
		this.X=0;
		this.Y=0;
		this.id=ID;
		this.Highlighted=false;
	}
	
	public void AddTaxi(SYNode other){
		this.taxis.add(other);
	}
	
	public void AddBus(SYNode other){
		if(this.buses==null)
			this.buses=new Vector<SYNode>();
		this.buses.add(other);
	}
	
	public boolean IsBusStop(){
		return this.buses!=null;
	}
	
	public void AddUnderground(SYNode other){
		if(this.undergrounds==null)
			this.undergrounds=new Vector<SYNode>();
		this.undergrounds.add(other);
	}
	
	public boolean IsUndergroundStop(){
		return this.undergrounds!=null;
	}
	
	public void AddFerry(SYNode other){
		if(this.ferries==null)
			this.ferries=new Vector<SYNode>();
		this.ferries.add(other);
	}
	
	public SYNode[] GetTaxis(){
		SYNode[] nodes=new SYNode[this.taxis.size()];
		return this.taxis.toArray(nodes);
	}
	
	public SYNode[] GetBuses(){
		if(this.buses==null)
			return new SYNode[0];
		SYNode[] nodes=new SYNode[this.buses.size()];
		return this.buses.toArray(nodes);
	}

	public SYNode[] GetUndergrounds(){
		if(this.undergrounds==null)
			return new SYNode[0];
		SYNode[] nodes=new SYNode[this.undergrounds.size()];
		return this.undergrounds.toArray(nodes);
	}
	
	public SYNode[] GetFerries(){
		if(this.ferries==null)
			return new SYNode[0];
		SYNode[] nodes=new SYNode[this.ferries.size()];
		return this.ferries.toArray(nodes);
	}

	public void draw(Graphics g){
		if(this.Highlighted){
			g.setColor(GraphicalUserInterface.HIGHLIGHTEDNODECOLOR);
		}else{
			g.setColor(Color.white);
		}
		g.fillOval(this.X-NODE_SIZE/4,this.Y-NODE_SIZE/4,NODE_SIZE/2,NODE_SIZE/2);
		if(this.IsBusStop()){
			g.setColor(GraphicalUserInterface.BUSCOLOR);
			g.fillArc(this.X-NODE_SIZE/4,this.Y-NODE_SIZE/4,NODE_SIZE/2,NODE_SIZE/2,180,180);
		}
		g.setColor(Color.black);
		g.drawOval(this.X-NODE_SIZE/4,this.Y-NODE_SIZE/4,NODE_SIZE/2,NODE_SIZE/2);
		if(this.IsUndergroundStop()){
			g.setColor(GraphicalUserInterface.UNDERGROUNDCOLOR);
		}
		g.drawString(this.Name,this.X-3*this.Name.length(),this.Y+5);
	}
}