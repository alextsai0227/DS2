package Server;
import java.util.ArrayList;
public class Game {
	public ArrayList<String> names;
	public ArrayList<Integer> status= new ArrayList<Integer>();
	public int vote_response = 0;
	public int response_yes = 0;
	public int passInRow = 0;
	public Game(ArrayList<String> names,String name)
	{
		this.names=names;
		this.names.add(name);
	}

}
