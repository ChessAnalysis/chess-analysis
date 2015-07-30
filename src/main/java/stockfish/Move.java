package stockfish;

import java.util.ArrayList;
import java.util.Iterator;

import jline.internal.Log;

public class Move extends ArrayList<RowLog> {
	
	String move;
	
	public Move() {
		super();
	}
	
	public Move(String move) {
		super();
		this.move = move;
	}
	
	public RowLog getBestScore() {
		return getByDepth(20).get(0);
	}

	public Move getByPV(int pv) {
		Iterator<RowLog> it = this.iterator();
		Move rl = new Move();
		
		while(it.hasNext()) {
			RowLog crtRow =  it.next();
			if(crtRow.getMultipv() == pv) {
				rl.add(crtRow);
			}
		}
		return rl;
	}
	
	public Move getByDepth(int depth) {
		Iterator<RowLog> it = this.iterator();
		Move rl = new Move();
		
		while(it.hasNext()) {
			RowLog crtRow =  it.next();
			if(crtRow.getDepth() == depth) {
				rl.add(crtRow);
			}
		}
		return rl;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<RowLog> it = this.iterator();
		
		while(it.hasNext()) {
			sb.append(it.next() + "\n");
		}
		
		return sb.toString();
	}

	public String getMove() {
		return move;
	}

	public void setMove(String move) {
		this.move = move;
	}

	
}
