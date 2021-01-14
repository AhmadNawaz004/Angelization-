

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

//import com.design.AngelGUI;

public class Angel extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected class RuleNode {
		protected short[] antecedent;
		protected short[] consequent;
		double confidenceForRule = 0.0;
		RuleNode next = null;

		protected RuleNode(short[] ante, short[] cons, double confValue) {
			antecedent = ante;
			consequent = cons;
			confidenceForRule = confValue;
		}
	}

	protected RuleNode startRulelist = null;
	protected short[][] dataArray = null;
	protected int[][] conversionArray = null;
	protected short[] reconversionArray = null;

	private static final double MIN_SUPPORT = 0.0;
	private static final double MAX_SUPPORT = 100.0;
	private static final double MIN_CONFIDENCE = 0.0;
	private static final double MAX_CONFIDENCE = 100.0;

	protected String fileName = null;
	protected int numCols = 0;
	protected int numRows = 0;
	protected double support = 20.0;
	protected double minSupport = 0;
	protected double confidence = 80.0;
	protected int numOneItemSets = 0;
	protected boolean errorFlag = true;
	protected boolean inputFormatOkFlag = true;
	private boolean haveDataFlag = false;
	protected boolean isOrderedFlag = false;
	protected boolean isPrunedFlag = false;
	protected BufferedReader fileInput;
	protected File filePath = null;

	public Angel(String[] args) {
		for (int index = 0; index < args.length; index++)
			idArgument(args[index]);

		if (errorFlag)
			CheckInputArguments();
		else
			outputMenu();
	}

	protected void idArgument(String argument) {

		if (argument.charAt(0) == '-') {
			char flag = argument.charAt(1);
			argument = argument.substring(2, argument.length());
			switch (flag) {
			case 'C':
				confidence = Double.parseDouble(argument);
				break;
			case 'F':
				fileName = argument;
				break;
			case 'S':
				support = Double.parseDouble(argument);
				break;
			default:
				System.out.println("INPUT ERROR: Unrecognise command "
						+ "line  argument -" + flag + argument);
				errorFlag = false;
			}
		} else {
			System.out.println("INPUT ERROR: All command line arguments "
					+ "must commence with a '-' character (" + argument + ")");
			errorFlag = false;
		}
	}

	protected void CheckInputArguments() {
		checkSupportAndConfidence();
		checkFileName();
		if (errorFlag)
			outputSettings();
		else
			outputMenu();
	}

	protected void checkSupportAndConfidence() {

		if ((support < MIN_SUPPORT) || (support > MAX_SUPPORT)) {
			System.out.println("INPUT ERROR: Support must be specified "
					+ "as a percentage (" + MIN_SUPPORT + " - " + MAX_SUPPORT
					+ ")");
			errorFlag = false;
		}
		if ((confidence < MIN_CONFIDENCE) || (confidence > MAX_CONFIDENCE)) {
			System.out.println("INPUT ERROR: Confidence must be "
					+ "specified as a percentage (" + MIN_CONFIDENCE + " - "
					+ MAX_CONFIDENCE + ")");
			errorFlag = false;
		}
	}

	protected void checkFileName() {
		if (fileName == null) {
			System.out.println("INPUT ERROR: Must specify file name (-F)");
			errorFlag = false;
		}
	}

	public void inputDataSet() {
		// Read the file
		readFile();

		// Check ordering (only if input format is OK)
		if (inputFormatOkFlag) {
			if (checkOrdering()) {
				countNumCols();
				
				minSupport = (numRows * support) / 100.0;
			} else {
				System.out.println("Error reading file: " + fileName + "\n");
				closeFile();
				System.exit(1);
			}
		}
	}
	public void inputDataSet(AngelGUI home) {
		// Read the file
		readFile();

		// Check ordering (only if input format is OK)
		if (inputFormatOkFlag) {
			if (checkOrdering()) {
				home.jtaFPDetails.append("Number of records = " + numRows
						+ "\n");
				countNumCols();
				home.jtaFPDetails.append("Number of columns = " + numCols
						+ "\n");
				minSupport = (numRows * support) / 100.0;
				home.jtaFPDetails.append("Min support       = "
						+ twoDecPlaces(minSupport) + " (records)" + "\n");
			} else {
				System.out.println("Error reading file: " + fileName + "\n");
				closeFile();
				System.exit(1);
			}
		}
	}

	protected void readFile() {
		try {
			// Dimension data structure
			inputFormatOkFlag = true;
			numRows = getNumberOfLines(fileName);
			if (inputFormatOkFlag) {
				dataArray = new short[numRows][];
				// Read file
				System.out.println("Reading input file: " + fileName);
				readInputDataSet();
			} else
				System.out.println("Error reading file: " + fileName + "\n");
		} catch (IOException ioException) {
			System.out.println("Error reading File");
			closeFile();
			System.exit(1);
		}
	}

	protected int getNumberOfLines(String nameOfFile) throws IOException {
		int counter = 0;

		// Open the file
		if (filePath == null)
			openFileName(nameOfFile);
		else
			openFilePath();

		// Loop through file incrementing counter
		// get first row.
		String line = fileInput.readLine();
		while (line != null) {
			checkLine(counter + 1, line);
			StringTokenizer dataLine = new StringTokenizer(line);
			int numberOfTokens = dataLine.countTokens();
			if (numberOfTokens == 0)
				break;
			counter++;
			line = fileInput.readLine();
		}

		// Close file and return
		closeFile();
		return (counter);
	}

	protected void checkLine(int counter, String str) {

		for (int index = 0; index < str.length(); index++) {
			if (!Character.isDigit(str.charAt(index))
					&& !Character.isWhitespace(str.charAt(index))) {
				JOptionPane.showMessageDialog(null, "FILE INPUT ERROR:\n"
						+ "charcater on line " + counter
						+ " is not a digit or white space");
				inputFormatOkFlag = false;
				haveDataFlag = false;
				break;
			}
		}
	}

	public void readInputDataSet() throws IOException {
		readInputDataSet(fileName);
	}

	protected void readInputDataSet(String fName) throws IOException {
		int rowIndex = 0;

		// Open the file
		if (filePath == null)
			openFileName(fName);
		else
			openFilePath();

		// Get first row.
		String line = fileInput.readLine();

		// Preocess rest of file
		while (line != null) {
			// Process line
			if (!processInputLine(line, rowIndex))
				break;
			// Increment first (row) index in 2-D data array
			rowIndex++;
			// get next line
			line = fileInput.readLine();
		}

		// Close file
		closeFile();
	}

	protected void readInputDataSetSeg(String fName, int startRowIndex,
			int endRowIndex) throws IOException {
		int rowIndex = startRowIndex;

		// Open the file
		if (filePath == null)
			openFileName(fName);
		else
			openFilePath();

		// get first row.
		String line = fileInput.readLine();
		for (int index = startRowIndex; index < endRowIndex; index++) {
			// Process line
			processInputLine(line, index);
			// get next line
			line = fileInput.readLine();
		}

		// Close file
		closeFile();
	}

	private boolean processInputLine(String line, int rowIndex) {
		// If no line return false
		if (line == null)
			return (false);

		// Tokenise line
		StringTokenizer dataLine = new StringTokenizer(line);
		int numberOfTokens = dataLine.countTokens();

		// Empty line or end of file found, return false
		if (numberOfTokens == 0)
			return (false);

		// Convert input string to a sequence of short integers
		short[] code = binConversion(dataLine, numberOfTokens);

		// Dimension row in 2-D dataArray
		int codeLength = code.length;
		dataArray[rowIndex] = new short[codeLength];
		// Assign to elements in row
		for (int colIndex = 0; colIndex < codeLength; colIndex++)
			dataArray[rowIndex][colIndex] = code[colIndex];

		// Return
		return (true);
	}

	protected boolean checkOrdering() {
		boolean result = true;

		// Loop through input data
		for (int index = 0; index < dataArray.length; index++) {
			if (!checkLineOrdering(index + 1, dataArray[index])) {
				haveDataFlag = false;
				result = false;
			}
		}

		// Return
		return (result);
	}

	protected boolean checkLineOrdering(int lineNum, short[] itemSet) {
		for (int index = 0; index < itemSet.length - 1; index++) {
			if (itemSet[index] >= itemSet[index + 1]) {
				JOptionPane.showMessageDialog(null, "FILE FORMAT ERROR:\n"
						+ "Attribute data in line " + lineNum
						+ " not in numeric order");
				return (false);
			}
		}

		// Default return
		return (true);
	}

	/* COUNT NUMBER OF COLUMNS */
	/** Counts number of columns represented by input data. */

	protected void countNumCols() {
		int maxAttribute = 0;

		// Loop through data array
		for (int index = 0; index < dataArray.length; index++) {
			int lastIndex = dataArray[index].length - 1;
			if (dataArray[index][lastIndex] > maxAttribute)
				maxAttribute = dataArray[index][lastIndex];
		}

		numCols = maxAttribute;
		numOneItemSets = numCols; // default value only
	}

	protected void openFileName(String nameOfFile) {
		try {
			// Open file
			FileReader file = new FileReader(nameOfFile);
			fileInput = new BufferedReader(file);
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(this, "Error Opening File",
					"Error: ", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	protected void openFilePath() {
		try {
			// Open file
			FileReader file = new FileReader(filePath);
			fileInput = new BufferedReader(file);
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(this, "Error Opening File",
					"Error: ", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	protected void closeFile() {
		if (fileInput != null) {
			try {
				fileInput.close();
			} catch (IOException ioException) {
				JOptionPane.showMessageDialog(this, "Error Closeing File",
						"Error: ", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
	}

	protected short[] binConversion(StringTokenizer dataLine, int numberOfTokens) {
		short number;
		short[] newItemSet = null;

		// Load array

		for (int tokenCounter = 0; tokenCounter < numberOfTokens; tokenCounter++) {
			number = new Short(dataLine.nextToken()).shortValue();
			newItemSet = realloc1(newItemSet, number);
		}

		// Return itemSet

		return (newItemSet);
	}

	public void idInputDataOrdering() {

		// Count singles and store in countArray;
		int[][] countArray = countSingles();

		// Bubble sort count array on support value (second index)
		orderCountArray(countArray);

		// Define conversion and reconversion arrays
		defConvertArrays(countArray);

		// Set sorted flag
		isOrderedFlag = true;
	}

	/* COUNT SINGLES */

	/**
	 * Counts number of occurrences of each single attribute in the input data.
	 * 
	 * @return 2-D array where first row represents column numbers and second
	 *         row represents support counts.
	 */

	protected int[][] countSingles() {

		// Dimension and initialize count array

		int[][] countArray = new int[numCols + 1][2];
		for (int index = 0; index < countArray.length; index++) {
			countArray[index][0] = index;
			countArray[index][1] = 0;
		}

		// Step through input data array counting singles and incrementing
		// appropriate element in the count array

		for (int rowIndex = 0; rowIndex < dataArray.length; rowIndex++) {
			if (dataArray[rowIndex] != null) {
				for (int colIndex = 0; colIndex < dataArray[rowIndex].length; colIndex++)
					countArray[dataArray[rowIndex][colIndex]][1]++;
			}
		}

		// Return

		return (countArray);
	}

	private void orderCountArray(int[][] countArray) {
		int attribute, quantity;
		boolean isOrdered;
		int index;

		do {
			isOrdered = true;
			index = 1;
			while (index < (countArray.length - 1)) {
				if (countArray[index][1] >= countArray[index + 1][1])
					index++;
				else {
					isOrdered = false;
					// Swap
					attribute = countArray[index][0];
					quantity = countArray[index][1];
					countArray[index][0] = countArray[index + 1][0];
					countArray[index][1] = countArray[index + 1][1];
					countArray[index + 1][0] = attribute;
					countArray[index + 1][1] = quantity;
					// Increment index
					index++;
				}
			}
		} while (isOrdered == false);
	}

	protected void orderFirstNofCountArray(int[][] countArray, int endIndex) {
		int attribute, quantity;
		boolean isOrdered;
		int index;

		do {
			isOrdered = true;
			index = 1;
			while (index < endIndex) {
				if (countArray[index][1] >= countArray[index + 1][1])
					index++;
				else {
					isOrdered = false;
					// Swap
					attribute = countArray[index][0];
					quantity = countArray[index][1];
					countArray[index][0] = countArray[index + 1][0];
					countArray[index][1] = countArray[index + 1][1];
					countArray[index + 1][0] = attribute;
					countArray[index + 1][1] = quantity;
					// Increment index
					index++;
				}
			}
		} while (isOrdered == false);
	}

	protected void defConvertArrays(int[][] countArray) {

		// Dimension arrays

		conversionArray = new int[numCols + 1][2];
		reconversionArray = new short[numCols + 1];

		// Assign values

		for (int index = 1; index < countArray.length; index++) {
			conversionArray[countArray[index][0]][0] = index;
			conversionArray[countArray[index][0]][1] = countArray[index][1];
			reconversionArray[index] = (short) countArray[index][0];
		}

		// Diagnostic ouput if desired
		// outputConversionArrays();
	}

	public void recastInputData() {
		short[] itemSet;
		int attribute;

		// Step through data array using loop construct

		for (int rowIndex = 0; rowIndex < dataArray.length; rowIndex++) {
			itemSet = new short[dataArray[rowIndex].length];
			// For each element in the itemSet replace with attribute number
			// from conversion array
			for (int colIndex = 0; colIndex < dataArray[rowIndex].length; colIndex++) {
				attribute = dataArray[rowIndex][colIndex];
				itemSet[colIndex] = (short) conversionArray[attribute][0];
			}
			// Sort itemSet and return to data array
			sortItemSet(itemSet);
			dataArray[rowIndex] = itemSet;
		}
	}

	public void recastInputDataAndPruneUnsupportedAtts() {
		short[] itemSet;
		int attribute;

		// Step through data array using loop construct

		for (int rowIndex = 0; rowIndex < dataArray.length; rowIndex++) {
			// Check for empty row
			if (dataArray[rowIndex] != null) {
				itemSet = null;
				// For each element in the current record find if supported with
				// reference to the conversion array. If so add to "itemSet".
				for (int colIndex = 0; colIndex < dataArray[rowIndex].length; colIndex++) {
					attribute = dataArray[rowIndex][colIndex];
					// Check support
					if (conversionArray[attribute][1] >= minSupport) {
						itemSet = reallocInsert(itemSet,
								(short) conversionArray[attribute][0]);
					}
				}
				// Return new item set to data array
				dataArray[rowIndex] = itemSet;
			}
		}

		// Set isPrunedFlag (used with GUI interface)
		isPrunedFlag = true;
		// Reset number of one item sets field
		numOneItemSets = getNumSupOneItemSets();
	}

	protected int getNumSupOneItemSets() {
		int counter = 0;

		// Step through conversion array incrementing counter for each
		// supported element found

		for (int index = 1; index < conversionArray.length; index++) {
			if (conversionArray[index][1] >= minSupport)
				counter++;
		}

		// Return

		return (counter);
	}

	public void resizeInputData(double percentage) {
		// Redefine number of rows
		numRows = (int) ((double) numRows * (percentage / 100.0));
		System.out.println("Recast input data, new num rows = " + numRows);

		// Dimension and populate training set.
		short[][] trainingSet = new short[numRows][];
		for (int index = 0; index < numRows; index++)
			trainingSet[index] = dataArray[index];

		// Assign training set label to input data set label.
		dataArray = trainingSet;

		// Determine new minimum support threshold value

		minSupport = (numRows * support) / 100.0;
	}

	protected short[] reconvertItemSet(short[] itemSet) {
		// If no conversion return orginal item set
		if (reconversionArray == null)
			return (itemSet);

		// If item set null return null
		if (itemSet == null)
			return (null);

		// Define new item set
		short[] newItemSet = new short[itemSet.length];

		// Copy
		for (int index = 0; index < newItemSet.length; index++) {
			newItemSet[index] = reconversionArray[itemSet[index]];
		}

		// Return
		return (newItemSet);
	}

	protected short reconvertItem(short item) {
		// If no conversion return orginal item
		if (reconversionArray == null)
			return (item);

		// Otherwise rerturn reconvert item
		return (reconversionArray[item]);
	}

	protected void insertRuleintoRulelist(short[] antecedent,
			short[] consequent, double confidenceForRule) {

		// Create new node
		RuleNode newNode = new RuleNode(antecedent, consequent,
				confidenceForRule);

		// Empty list situation
		if (startRulelist == null) {
			startRulelist = newNode;
			return;
		}

		// Add new node to start
		if (confidenceForRule > startRulelist.confidenceForRule) {
			newNode.next = startRulelist;
			startRulelist = newNode;
			return;
		}

		// Add new node to middle
		RuleNode markerNode = startRulelist;
		RuleNode linkRuleNode = startRulelist.next;
		while (linkRuleNode != null) {
			if (confidenceForRule > linkRuleNode.confidenceForRule) {
				markerNode.next = newNode;
				newNode.next = linkRuleNode;
				return;
			}
			markerNode = linkRuleNode;
			linkRuleNode = linkRuleNode.next;
		}

		// Add new node to end
		markerNode.next = newNode;
	}

	protected short[] reallocInsert(short[] oldItemSet, short newElement) {

		// No old item set

		if (oldItemSet == null) {
			short[] newItemSet = { newElement };
			return (newItemSet);
		}

		// Otherwise create new item set with length one greater than old
		// item set

		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength + 1];

		// Loop

		int index1;
		for (index1 = 0; index1 < oldItemSetLength; index1++) {
			if (newElement < oldItemSet[index1]) {
				newItemSet[index1] = newElement;
				// Add rest
				for (int index2 = index1 + 1; index2 < newItemSet.length; index2++)
					newItemSet[index2] = oldItemSet[index2 - 1];
				return (newItemSet);
			} else
				newItemSet[index1] = oldItemSet[index1];
		}

		// Add to end

		newItemSet[newItemSet.length - 1] = newElement;

		// Return new item set

		return (newItemSet);
	}

	protected short[] realloc1(short[] oldItemSet, short newElement) {

		// No old item set

		if (oldItemSet == null) {
			short[] newItemSet = { newElement };
			return (newItemSet);
		}

		// Otherwise create new item set with length one greater than old
		// item set

		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength + 1];

		// Loop

		int index;
		for (index = 0; index < oldItemSetLength; index++)
			newItemSet[index] = oldItemSet[index];
		newItemSet[index] = newElement;

		// Return new item set

		return (newItemSet);
	}

	protected short[] realloc2(short[] oldItemSet, short newElement) {

		// No old array

		if (oldItemSet == null) {
			short[] newItemSet = { newElement };
			return (newItemSet);
		}

		// Otherwise create new array with length one greater than old array

		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength + 1];

		// Loop

		newItemSet[0] = newElement;
		for (int index = 0; index < oldItemSetLength; index++)
			newItemSet[index + 1] = oldItemSet[index];

		// Return new array

		return (newItemSet);
	}

	protected short[] removeElementN(short[] oldItemSet, int n) {
		if (oldItemSet.length <= n)
			return (oldItemSet);
		else {
			short[] newItemSet = new short[oldItemSet.length - 1];
			for (int index = 0; index < n; index++)
				newItemSet[index] = oldItemSet[index];
			for (int index = n + 1; index < oldItemSet.length; index++)
				newItemSet[index - 1] = oldItemSet[index];
			return (newItemSet);
		}
	}

	protected short[] complement(short[] itemSet1, short[] itemSet2) {
		int lengthOfComp = itemSet2.length - itemSet1.length;

		// Return null if no complement
		if (lengthOfComp < 1)
			return (null);

		// Otherwsise define combination array and determine complement
		short[] complement = new short[lengthOfComp];
		int complementIndex = 0;
		for (int index = 0; index < itemSet2.length; index++) {
			// Add to combination if not in first itemset
			if (notMemberOf(itemSet2[index], itemSet1)) {
				complement[complementIndex] = itemSet2[index];
				complementIndex++;
			}
		}

		// Return
		return (complement);
	}

	protected void sortItemSet(short[] itemSet) {
		short temp;
		boolean isOrdered;
		int index;

		do {
			isOrdered = true;
			index = 0;
			while (index < (itemSet.length - 1)) {
				if (itemSet[index] <= itemSet[index + 1])
					index++;
				else {
					isOrdered = false;
					// Swap
					temp = itemSet[index];
					itemSet[index] = itemSet[index + 1];
					itemSet[index + 1] = temp;
					// Increment index
					index++;
				}
			}
		} while (isOrdered == false);
	}

	protected boolean notMemberOf(short number, short[] itemSet) {

		// Loop through itemSet

		for (int index = 0; index < itemSet.length; index++) {
			if (number < itemSet[index])
				return (true);
			if (number == itemSet[index])
				return (false);
		}

		// Got to the end of itemSet and found nothing, return true

		return (true);
	}

	protected short[][] combinations(short[] inputSet) {
		if (inputSet == null)
			return (null);
		else {
			short[][] outputSet = new short[getCombinations(inputSet)][];
			combinations(inputSet, 0, null, outputSet, 0);
			return (outputSet);
		}
	}

	private int combinations(short[] inputSet, int inputIndex, short[] sofar,
			short[][] outputSet, int outputIndex) {
		short[] tempSet;
		int index = inputIndex;

		// Loop through input array

		while (index < inputSet.length) {
			tempSet = realloc1(sofar, inputSet[index]);
			outputSet[outputIndex] = tempSet;
			outputIndex = combinations(inputSet, index + 1,
					copyItemSet(tempSet), outputSet, outputIndex + 1);
			index++;
		}

		// Return

		return (outputIndex);
	}

	private int getCombinations(short[] set) {
		int counter = 0, numComb;

		numComb = (int) Math.pow(2.0, set.length) - 1;

		// Return

		return (numComb);
	}

	protected short[] copyItemSet(short[] itemSet) {

		// Check whether there is a itemSet to copy
		if (itemSet == null)
			return (null);

		// Do copy and return
		short[] newItemSet = new short[itemSet.length];
		for (int index = 0; index < itemSet.length; index++) {
			newItemSet[index] = itemSet[index];
		}

		// Return
		return (newItemSet);
	}

	public void outputDataArray() {
		if (isPrunedFlag)
			System.out.println("DATA SET (Ordered and Pruned)\n"
					+ "-----------------------------");
		else {
			if (isOrderedFlag)
				System.out.println("DATA SET (Ordered)\n"
						+ "------------------");
			else
				System.out.println("DATA SET\n" + "--------");
		}

		// Loop through data array
		for (int index = 0; index < dataArray.length; index++) {
			outputItemSet(dataArray[index]);
			System.out.println();
		}
	}

	protected void outputDataArray(short[][] dataSet) {
		if (dataSet == null) {
			System.out.println("null");
			return;
		}

		// Loop through data array
		for (int index = 0; index < dataSet.length; index++) {
			outputItemSet(dataSet[index]);
			System.out.println();
		}
	}

	protected void outputItemSet(short[] itemSet) {
		if (itemSet == null)
			System.out.print(" null ");
		else {

			short[] tempItemSet = reconvertItemSet(itemSet);
			// Loop through item set elements
			int counter = 0;
			for (int index = 0; index < tempItemSet.length; index++) {
				if (counter == 0) {
					counter++;
					jtaAsso.append(" {");
					System.out.print(" {");
				} else {
					System.out.print(" ");
					jtaAsso.append(" ");
				}
				System.out.print(tempItemSet[index]);
				jtaAsso.append("" + tempItemSet[index]);
			}
			System.out.print("} ");
			jtaAsso.append("} ");
		}
	}

	public void outputDataArraySize() {
		int numRecords = 0;
		int numElements = 0;

		// Loop through data array

		for (int index = 0; index < dataArray.length; index++) {
			if (dataArray[index] != null) {
				numRecords++;
				numElements = numElements + dataArray[index].length;
			}
		}

		// Output

		System.out.println("Number of records        = " + numRecords);
		System.out.println("Number of elements       = " + numElements);
		double density = (double) numElements / (numCols * numRecords);
		System.out.println("Data set density   = " + twoDecPlaces(density)
				+ "%");
	}

	public void outputConversionArrays() {

		// Conversion array
		System.out.println("Conversion Array = ");
		for (int index = 1; index < conversionArray.length; index++) {
			System.out.println("(" + index + ") " + conversionArray[index][0]
					+ " = " + conversionArray[index][1]);
		}

		// Reconversion array
		System.out.println("Reonversion Array = ");
		for (int index = 1; index < reconversionArray.length; index++) {
			System.out.println("(" + index + ") " + reconversionArray[index]);
		}
	}

	protected void outputMenu() {
		System.out.println();
		System.out.println("-C  = Confidence (default 80%)");
		System.out.println("-F  = File name");
		System.out.println("-N  = Number of classes (Optional)");
		System.out.println("-S  = Support (default 20%)");
		System.out.println();

		// Exit

		System.exit(1);
	}

	protected void outputSettings() {
		System.out.println("SETTINGS\n--------");
		System.out.println("File name                = " + fileName);
		System.out.println("Support (default 20%)    = " + support);
		System.out.println("Confidence (default 80%) = " + confidence);
		System.out.println();
	}

	/* OUTPUT SETTINGS */
	/** Outputs instance field values. */

	protected void outputSettings2() {
		System.out.println("SETTINGS\n--------");
		System.out.println("Number of records        = " + numRows);
		System.out.println("Number of columns        = " + numCols);
		System.out.println("Support (default 20%)    = " + support);
		System.out.println("Confidence (default 80%) = " + confidence);
		System.out.println("Min support              = " + minSupport
				+ " (records)");
		System.out.println("Num one itemsets         = " + numOneItemSets);
	}

	public void outputSuppAndConf() {
		System.out.println("Support = " + twoDecPlaces(support)
				+ ", Confidence = " + twoDecPlaces(confidence));
	}

	JTextArea jtaAsso;

	public void outputRules(JTextArea jtaAsso) {
		this.jtaAsso = jtaAsso;
		jtaAsso.append("\nAngelization\n=================\n");
		System.out.println("\nAngelization\n=================");
		outputRules(startRulelist);
	}

	public void outputRules(RuleNode ruleList) {
		// Check for empty rule list
		if (ruleList == null)
			System.out.println("No rules generated!");

		// Loop through rule list
		int number = 1;
		RuleNode linkRuleNode = ruleList;
		while (linkRuleNode != null) {
			// /System.out.print("(" + number + ") ");
			outputRule(linkRuleNode);
			// System.out.println(" " +
			// twoDecPlaces(linkRuleNode.confidenceForRule) + "%");
			jtaAsso
					.append("(" + number + ") " + " "
							+ twoDecPlaces(linkRuleNode.confidenceForRule)
							+ "%" + "\n");
			number++;
			linkRuleNode = linkRuleNode.next;
		}
	}

	private void outputRule(RuleNode rule) {
		outputItemSet(rule.antecedent);
		jtaAsso.append(" -> ");
		System.out.print(" -> ");
		outputItemSet(rule.consequent);
	}

	public void outputRulesWithDefault() {
		int number = 1;
		RuleNode linkRuleNode = startRulelist;

		while (linkRuleNode != null) {
			System.out.print("(" + number + ") ");
			if (linkRuleNode.next == null)
				System.out.print("Default -> ");
			else {
				outputItemSet(linkRuleNode.antecedent);
				System.out.print(" -> ");
			}
			outputItemSet(linkRuleNode.consequent);
			System.out.println(" "
					+ twoDecPlaces(linkRuleNode.confidenceForRule) + "%");
			number++;
			linkRuleNode = linkRuleNode.next;
		}
	}

	public double outputDuration(JTextArea jtaFPDetails, double time1,
			double time2) {
		double duration = (time2 - time1) / 1000;
		System.out.println("Generation time = " + twoDecPlaces(duration)
				+ " seconds (" + twoDecPlaces(duration / 60) + " mins)");
		jtaFPDetails.append("Generation time = " + twoDecPlaces(duration)
				+ " seconds (" + twoDecPlaces(duration / 60) + " mins)" + "\n");
		return (duration);
	}

	protected double twoDecPlaces(double number) {
		int numInt = (int) ((number + 0.005) * 100.0);
		number = ((double) numInt) / 100.0;
		return (number);
	}
}
