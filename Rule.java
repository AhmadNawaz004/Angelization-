

/**
 * Represents an association rule: itemsetA => itemsetB 
 * 
 */
public class Rule {

	private Itemset itemsetA;

	private Itemset itemsetB;

	public Rule(Itemset itemsetA, Itemset itemsetB) {
		this.itemsetA = itemsetA;
		this.itemsetB = itemsetB;
	}

	public Itemset getItemsetA() {
		return itemsetA;
	}

	public Itemset getItemsetB() {
		return itemsetB;
	}

	public String toString() {
		return "{" + itemsetA + "} => {" + itemsetB + "}";
	}

}
