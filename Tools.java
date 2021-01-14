

import javax.swing.JOptionPane;

//import com.design.AngelImpl;
//import com.design.AngelGUI;


public class Tools {

	GT newFPtree;
	AngelGUI home;

	public void init(AngelGUI home, String[] arg) {
		this.home = home;
		newFPtree = new GT(arg);

		newFPtree.inputDataSet(home);

		newFPtree.idInputDataOrdering();
		newFPtree.recastInputDataAndPruneUnsupportedAtts();
		newFPtree.setNumOneItemSets();
		double time1 = (double) System.currentTimeMillis();
		newFPtree.createFPtree();
		newFPtree.outputDuration(home.jtaFPDetails, time1, (double) System
				.currentTimeMillis());
		newFPtree.outputFPtreeStorage(home.jtaFPDetails);

	}

	public void mining(AngelImpl growth) {

		double time1 = (double) System.currentTimeMillis();
		newFPtree.startMining();
		newFPtree.outputDuration(growth.jtaShowMining, time1, (double) System
				.currentTimeMillis());
		newFPtree.outputStorage(growth.jtaShowMining);
		newFPtree.outputNumFreqSets(growth.txtNoofFreqSets);

		int i = JOptionPane.showConfirmDialog(null, "Start Generalization?");
		double bt = System.currentTimeMillis();
		if (i == 0) {
			JOptionPane.showMessageDialog(null, "Mining Starting Time : " + bt
					+ " (millis)");
			newFPtree.outputTtree();
			newFPtree.outputRules(growth.jtaAsso);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double et = System.currentTimeMillis();
			JOptionPane.showMessageDialog(null, "Mining Ending Time : " + et
					+ " (millis)");
			JOptionPane.showMessageDialog(null, "Total Mining Duration : "
					+ (et - bt) + " (millis)");

		} else {
			JOptionPane.showMessageDialog(null, "Mining Closed.");
		}

	}
}
