package stockfish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jline.internal.Log;

public class Moves extends ArrayList<Move> {
	
	public List<RowLog> getBestScores() {
		Iterator<Move> it = this.iterator();
		List<RowLog> scores = new ArrayList<RowLog>();
		
		while(it.hasNext()) {
			scores.add(it.next().getBestScore());
		}
		return scores;
	}
	
	public Moves getByPV(int pv) {
		Iterator<Move> it = this.iterator();
		Moves rl = new Moves();
		
		while(it.hasNext()) {
			Move move =  it.next();
			rl.add(move.getByPV(pv));
		}
		return rl;
	}
	
	public Moves getByDepth(int pv) {
		Iterator<Move> it = this.iterator();
		Moves rl = new Moves();
		
		while(it.hasNext()) {
			Move move =  it.next();
			rl.add(move.getByDepth(pv));
		}
		return rl;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Move> it = this.iterator();
		
		while(it.hasNext()) {
			Move move = it.next();
			Log.info(move);
			sb.append(move + "\n");
		}
		return sb.toString();
	}

}
