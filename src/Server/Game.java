package Server;
import java.util.ArrayList;
public class Game {
	public ArrayList<String> names;
	public ArrayList<Integer> status= new ArrayList<Integer>();
	public Game(ArrayList<String> names,String name)
	{
		this.names=names;
		this.names.add(name);
	}

}
