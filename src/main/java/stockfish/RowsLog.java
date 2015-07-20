package stockfish;

import java.util.ArrayList;
import java.util.Iterator;

import jline.internal.Log;

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

	public String toAnalyse() {
		StringBuilder sb = new StringBuilder();
		RowsLog tmpRows = this.getByDepth(20);
		Iterator<RowLog> it = tmpRows.iterator();
		
		int count = 0;
		Integer previous = null;
		
		while(it.hasNext()) {
			RowLog tmpRow = it.next();
			if (tmpRow.getScoreType().equals("mate")) {
				if((count%2)==0)
					sb.append("[" + ((count/2)+1) + "] \t" + "..." + "\t\t" + tmpRow.getScoreResult() + "\n");
				else
					sb.append("[" + ((count/2)+1) + "] \t\t" + "..." + "\t" + tmpRow.getScoreResult() + "\n");
				previous = null;
			} else {
				Integer score = Integer.valueOf(tmpRow.getScoreResult());
				if((count%2)==0)
					score = -score;

				int eval;
				if(previous == null)
					eval = 0;
				else if(score<previous)
					eval = score-previous;
				else
					eval = previous-score;
				previous = score;

				if((count%2)==0) {
					sb.append("[" + ((count/2)+1) + "] \t" + "..." + "\t\t" + Double.valueOf(score)/100 + "\t" + Double.valueOf(eval)/100 + "\n");
				} else {
					sb.append("[" + ((count/2)+1) + "] \t\t" + "..." + "\t" + Double.valueOf(score)/100 + "\t" + Double.valueOf(eval)/100 + "\n");
				}
			}
			count++;
		}
		
		return sb.toString();
	}
}
