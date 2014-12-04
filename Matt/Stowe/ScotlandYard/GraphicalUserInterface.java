package Matt.Stowe.ScotlandYard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GraphicalUserInterface extends JFrame implements KeyListener, MouseListener{
	private Board board;
	
	private int activenode;
	
	public static final int WIDTH=1100;
	public static final int HEIGHT=800;
	public static final int XBUFFER=20;
	public static final int YBUFFER=50;
	
	public static final Color TAXICOLOR=new Color(128,128,0);
	public static final Color BUSCOLOR=Color.cyan;
	public static final Color UNDERGROUNDCOLOR=Color.red;
	public static final Color BLACKTICKETCOLOR=Color.black;
	
	public static final Color HIGHLIGHTEDNODECOLOR=Color.yellow;
	
	public GraphicalUserInterface(Board b){
		super("Scotland Yard");
		
		this.board=b;
		
		activenode=0;
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.addKeyListener(this);
		this.addMouseListener(this);
		
		this.setSize(WIDTH,HEIGHT);

		this.setVisible(true);
	}
	
	public void paint(Graphics og){
		Image doublebuffer=this.createImage(WIDTH,HEIGHT);
		Graphics g=doublebuffer.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,WIDTH,HEIGHT);
		this.board.draw(g);

		og.drawImage(doublebuffer,0,0,null);
	}
	
	public void keyTyped(KeyEvent ke){}
	public void keyReleased(KeyEvent ke){}
	public void mouseEntered(MouseEvent me){}
	public void mouseExited(MouseEvent me){}
	public void mousePressed(MouseEvent me){}
	public void mouseReleased(MouseEvent me){}
	public void mouseClicked(MouseEvent me){
		if(this.board.MouseClicked(me.getX(), me.getY(), false))
			repaint();
	}
	public void keyPressed(KeyEvent ke){}
}