package stockfish;

import java.util.ArrayList;
import java.util.Iterator;

public class RowsLog extends ArrayList<RowLog> {

	public RowsLog getByPV(int pv) {
		Iterator<RowLog> it = this.iterator();
		RowsLog rl = new RowsLog();
		
		while(it.hasNext()) {
			RowLog crtRow =  it.next();
			if(crtRow.getMultipv().equals(String.valueOf(pv))) {
				rl.add(crtRow);
			}
		}
		
		return rl;
	}
	
	public RowsLog getByDepth(int depth) {
		Iterator<RowLog> it = this.iterator();
		RowsLog rl = new RowsLog();
		
		while(it.hasNext()) {
			RowLog crtRow =  it.next();
			if(crtRow.getDepth().equals(depth)) {
				rl.add(crtRow);
			}
		}
		
		return rl;
	}

	public RowsLog getBlunders() {
		
		RowsLog lastDepth = getByDepth(20);
		
		Iterator<RowLog> it = lastDepth.iterator();
		RowsLog rl = new RowsLog();
		
		int lastEval = 0;
		int crtEval = 0;
		
		while(it.hasNext()) {
			RowLog crtRow =  it.next();
			crtEval = Integer.valueOf(crtRow.getScoreResult());
			int diff = Math.abs(lastEval - crtEval);
			if(diff > 300) {
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

}
