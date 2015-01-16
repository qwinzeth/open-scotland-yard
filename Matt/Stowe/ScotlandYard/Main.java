package Matt.Stowe.ScotlandYard;

public class Main{
	public static void main(String[] args){
		Board b=new Board("synet.txt");
		GraphicalUserInterface gui=new GraphicalUserInterface(b);
	}
}