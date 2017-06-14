import java.io.*;
import java.util.*;
public class TailoredFacilityLayout
{
	public static final int NUMBER_OF_DEPARTMENTS = 16;
	public static final int FACILITY_ROWS = 12;
	public static final int FACILITY_COLS = 10;
	public static final int FACILITY_FLOORS = 2;
	public static final int PRESET_DEPARTMENT = 1;
	
	public static void main(String[] args) throws IOException
	{
		
		// FILE_PATH is the file path for the text file containing the facility layout, this file must be a text file
		// to create this file from an excel sheet, copy and paste the 1st floor into notepad
		// then, copy and paste the 2nd floor below it
		final String FILE_PATH = "C:/Users/Thomas/Documents/school/classes/MFE 476/layout.txt";
		
		// stores facility layout as array
		int facilityArray[][][] = storeLayout(FILE_PATH);
		
		// the size of each department is stored in the array departmentSizes
		int departmentSizes[] = storeSize(facilityArray);
		
		// calculate and store centroids of each department in centroidArray
		// departmentSizes is referenced by getCentroid methods within storeCentroid
		double centroidArray[][] = storeCentroid(facilityArray, departmentSizes);
		
		// testCentroidArray is used later in program when testing whether a new layout will lower cost
		double[][] testCentroidArray = new double[NUMBER_OF_DEPARTMENTS][3];
		
		// copies centroidArray to testCentroidArray
		for (int col = 0; col < 3; col++)
		{
			for (int row = 0; row < NUMBER_OF_DEPARTMENTS; row++)
			{
				testCentroidArray[row][col] = centroidArray[row][col];
			}
		}
		
		// initialCentroidArray is used as a record
		double[][] initialCentroidArray = new double[NUMBER_OF_DEPARTMENTS][3];
				
		// copies centroidArray to oldCentroidArray
		for (int col = 0; col < 3; col++)
		{
			for (int row = 0; row < NUMBER_OF_DEPARTMENTS; row++)
			{
				initialCentroidArray[row][col] = centroidArray[row][col];
			}
		}
		
		double initialCost = getTotalCost(centroidArray);
		double lowestCost = initialCost;
		
		// 1 dimensional array departmentRecord is initialized
		int[] departmentRecord = new int[NUMBER_OF_DEPARTMENTS];
		// for the array departmentRecord the index + 1 is the original department space, the value is the current  
		// department occupying that space in the algorithm
		for (int col = 0; col < NUMBER_OF_DEPARTMENTS; col++)
		{
			departmentRecord[col] = col + 1;
		}
		
		// the 2 dimensional array switchRecord is initialized
		// switchRecord records whether switches have been considered
		// the index + 1 of each dimension correspond to each department in a switch
		boolean[][] switchRecord= new boolean[NUMBER_OF_DEPARTMENTS][NUMBER_OF_DEPARTMENTS];
		
		double testCost = 0;
		double previousCost = 0;
		
		// repeats until there is not decrease in total cost and therefore no more improvements to be made
		while (previousCost != lowestCost)
		{
			previousCost = lowestCost;
			for (int col = 0; col < NUMBER_OF_DEPARTMENTS; col++)
			{
				for (int row = 0; row < NUMBER_OF_DEPARTMENTS; row++)
				{
					// switches involving the preset department are all set to true because department the preset department can't be moved
					// department - 1 = corresponding index
					// switches from a higher department to a lower one are all set to true because only the reverse switch of each of these is needed
					// e.g. switch between departments 3 and 2 corresponding to index (2,1) would be set to true because it is 
					// already considered by index (1,2)
					if (row >= col || row == (PRESET_DEPARTMENT-1) || col == (PRESET_DEPARTMENT-1))
					{
						switchRecord[row][col] = true;
					}
					else
					{
						switchRecord[row][col] = false;
					}
				}
			}

			// each possible switch is considered sequentially
			for (int col = 0; col < NUMBER_OF_DEPARTMENTS; col++)
			{
				for (int row = 0; row < NUMBER_OF_DEPARTMENTS; row++)
				{
					// if the corresponding switch has a true value in the switchRecord array or if the sizes of the arrays to be switched are 
					// not equal, the switch does not need to be considered
					if (!switchRecord[row][col] && departmentSizes[row] == departmentSizes[col])
					{
						// switch x coordinates
						testCentroidArray[row][0] = centroidArray[col][0];
						testCentroidArray[col][0] = centroidArray[row][0];
						// switch y coordinates
						testCentroidArray[row][1] = centroidArray[col][1];
						testCentroidArray[col][1] = centroidArray[row][1];
						// switch z coordinates
						testCentroidArray[row][2] = centroidArray[col][2];
						testCentroidArray[col][2] = centroidArray[row][2];

						// set testCost equal to the cost if the switch being considered were to take place
						testCost = getTotalCost(testCentroidArray); 

						// if the new layout has a lower cost, departmentRecord records the switch, the centroids are switched in centroidArray and 
						if (testCost < lowestCost)
						{	
							// switch coordinates in centroidArray to switched coordinates in testCentroidArray
							// switch x coordinates
							centroidArray[row][0] = testCentroidArray[row][0];
							centroidArray[col][0] = testCentroidArray[col][0];
							// switch y coordinates
							centroidArray[row][1] = testCentroidArray[row][1];
							centroidArray[col][1] = testCentroidArray[col][1];
							// switch z coordinates
							centroidArray[row][2] = testCentroidArray[row][2];
							centroidArray[col][2] = testCentroidArray[col][2];

							// lowestCost is updated with the lower cost
							lowestCost = testCost;
						}
						else
						{
							// switch back testCentroidArray to what it was before most recent coordinate switch
							// switch x coordinates
							testCentroidArray[row][0] = centroidArray[row][0];
							testCentroidArray[col][0] = centroidArray[col][0];
							// switch y coordinates
							testCentroidArray[row][1] = centroidArray[row][1];
							testCentroidArray[col][1] = centroidArray[col][1];
							// switch z coordinates
							testCentroidArray[row][2] = centroidArray[row][2];
							testCentroidArray[col][2] = centroidArray[col][2];
						}
					}
					// as a switch is considered, its corresponding value in switchRecord is changed to true
					switchRecord[row][col] = true;
				}
			}
			
		}
		
		// changes the departments in departmentRecord to account for changes made between the initial and optomized facility layout
		for (int n=0; n < NUMBER_OF_DEPARTMENTS; n++)
		{
			int m = 0;
			// finds the index m in the centroidArray that has a centroid matching the centroid at index n in the initialCentroidArray
			while (centroidArray[m][0] != initialCentroidArray[n][0] || centroidArray[m][1] != initialCentroidArray[n][1] || centroidArray[m][2] != initialCentroidArray[n][2])
			{
				m++;
			}
			
			// sets the department of the optomized layout to its corresponding department of the initial layout
			// no department 0, therefore department = index + 1
			departmentRecord[n] = m + 1;
		}

		
		// declare new array to hold new, improved facility layout
		int[][][] improvedFacilityArray = new int[FACILITY_ROWS][FACILITY_COLS][FACILITY_FLOORS];
		
		// populate improvedFacilityLayout
		for (int row = 0; row < FACILITY_ROWS; row++)
		{
			for (int col = 0; col < FACILITY_COLS; col++)
			{
				for (int floor = 0; floor < FACILITY_FLOORS; floor++)
				{
					improvedFacilityArray[row][col][floor] = departmentRecord[facilityArray[row][col][floor] - 1];
				}
			}
		}
		
		// the rest of the main method outputs data to user
		System.out.println("initial facility layout");
		for (int floor = 0; floor < facilityArray[0][0].length; floor++)
		{
			System.out.println("floor" + (floor + 1));
			for (int col = 0; col < facilityArray[0].length; col++)
			{
				System.out.println();
				for (int row = 0; row < facilityArray.length; row++)
				{
					System.out.print(facilityArray[row][col][floor] + " ");
				}
			}
			System.out.println();
		}
		System.out.println();
		
		System.out.println("improved facility layout");
		for (int floor = 0; floor < facilityArray[0][0].length; floor++)
		{
			System.out.println("floor" + (floor + 1));
			for (int col = 0; col < facilityArray[0].length; col++)
			{
				System.out.println();
				for (int row = 0; row < facilityArray.length; row++)
				{
					System.out.print(improvedFacilityArray[row][col][floor] + " ");
				}
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("department sizes");
		for (int row = 0; row < departmentSizes.length; row++)
		{
			System.out.println("Dep " + (row + 1) + " " + departmentSizes[row]);
		}
	
		System.out.println();
		System.out.println("initial facility layout centroids");
		for (int row = 0; row < centroidArray.length; row++)	
		{
			System.out.print("Dep " + (row + 1) + " ");
			for (int col = 0; col < centroidArray[0].length; col++)
				{
					System.out.print(initialCentroidArray[row][col] + " ");
				}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("improved facility layout centroids");
		for (int row = 0; row < centroidArray.length; row++)	
		{
			System.out.print("Dep " + (row + 1) + " ");
			for (int col = 0; col < centroidArray[0].length; col++)
				{
					System.out.print(centroidArray[row][col] + " ");
				}
			System.out.println();
		}
	
		/*
		System.out.println();
		System.out.print("switch record");
		for (int col = 0; col < NUMBER_OF_DEPARTMENTS; col++)
		{
			System.out.println();
			for (int row = 0; row < NUMBER_OF_DEPARTMENTS; row++)
			{
				System.out.print(switchRecord[row][col] + " ");
			}
		}
		*/
		
		System.out.println();
		System.out.println();
		System.out.printf("initial cost $%.2f \n", initialCost);
		System.out.println();
		System.out.printf("improved cost $%.2f \n", lowestCost);
		System.out.println();
		System.out.println("the improved layout has the following switches");
		for (int row = 0; row < NUMBER_OF_DEPARTMENTS; row++)
		{
			System.out.println("Dep " + (row + 1) + " switched to " + departmentRecord[row]);
		}
	}
	
	/** storeLayout stores facility layout from text file as array
	* stored numbers correspond to department numbers
	* @param FILE_PATH is the file path for the text file containing the facility layout, this file must be a text file
	* to create this file from an excel sheet, copy and paste the 1st floor into notepad
	* then, copy and paste the 2nd floor below it
	* @return facility layout, 1st dimension of array is x value, 2nd is y value, 3rd is z value
	*/
	public static int[][][] storeLayout(String FILE_PATH) throws IOException
	{

		int[][][] layoutArray = new int[FACILITY_ROWS][FACILITY_COLS][FACILITY_FLOORS];

		// opens input file “layout.txt”
		File infile = new File(FILE_PATH);

		// handles if input file exists
		if (!infile.exists())
		{
			System.out.println("File not found");
			System.exit(0);
		}

		// reads data from “layout.txt”
		Scanner inputFile = new Scanner(infile);

		for (int floor = 0; floor < layoutArray[0][0].length; floor++)
		{
			for (int col = 0; col < layoutArray[0].length; col++)
			{
				for (int row = 0; row < layoutArray.length; row++)
				{
					layoutArray[row][col][floor] = inputFile.nextInt();
				}
			}
		}
		inputFile.close();
		return layoutArray;
	}


	/** storeSize the size of each department is calculated and stored
	* the index + 1 = department number, values are sizes
	* @param facilityArray is the array holding the facility layout
	* @return the sizes of each department
	*/
	public static int[] storeSize(int[][][] facilityArray)
	{
		int[] layoutArray = new int[NUMBER_OF_DEPARTMENTS];
		for(int i=0; i < NUMBER_OF_DEPARTMENTS; i++)
		{
			layoutArray[i] = getSize(i + 1, facilityArray);
		}
		return layoutArray;
	}

	/** getSize the size of each department is calculated 
	* @param department is the department having its size calculated
	* @param facilityArray is the array holding the facility layout
	* @return the size of the department
	*/
	public static int getSize(int department, int[][][] facilityArray)
	{
		int size = 0;
		for (int floor = 0; floor < facilityArray[0][0].length; floor++)
		{
			for (int col = 0; col < facilityArray[0].length; col++)
			{
				for (int row = 0; row < facilityArray.length; row++)
				{
					if (facilityArray[row][col][floor] == department)
					{
						++size;
					}
				}
			}
		}

		return size;
	}
	
	/** storeCentroid calculates and stores centroids of each department
	* for each department, the average x and y values are taken
	* centroids are stored in an array
	* the values of the 1st row are centroid x values
	* the values in the 2nd row are centroid y values 
	* the values in the 3rd row give the floor level
	* @return array centroidArray holding the centroid of each department
	*/
	public static double[][] storeCentroid(int facilityArray[][][], int departmentSizes[])
	{
		double[][] centroidArray = new double[NUMBER_OF_DEPARTMENTS][3];
		for (int row = 0; row < NUMBER_OF_DEPARTMENTS; row++)
		{
			for (int col = 0; col < 3; col++)
			{
				// row + 1 = department number
				if (col == 0) {
					centroidArray[row][col] = getCentroid('x', row + 1, facilityArray, departmentSizes);
				}
				else if (col == 1) {
					centroidArray[row][col] = getCentroid('y', row + 1, facilityArray, departmentSizes);
				}
				else if (col == 2) {
					centroidArray[row][col] = getCentroid('z', row + 1, facilityArray, departmentSizes);
				}
			}
		}
		return centroidArray;
	}

	/** getCentroid calculates centroid x, y or z value of a department
	* @param char dimension is x, y or z, determines which value is calculated for centroid
	* @param int department number
	* @return double centroid x value of department
	*/
	public static double getCentroid(char dimension, int department, int facilityArray[][][], int departmentSizes[])
	{
		double centroid = 0;
		for (int row = 0; row < FACILITY_ROWS; row++)
		{
			for (int col = 0; col < FACILITY_COLS; col++)
			{
				for (int floor = 0; floor < FACILITY_FLOORS; floor++)
				{
					if (facilityArray[row][col][floor] == department) 
					{
						if (dimension == 'x')
						{
							centroid += row;
						}
						else if (dimension == 'y')
						{
							centroid += col;
						}
						else if (dimension == 'z')
						{
							return floor;
						}
					}
				}
			}
		}
		centroid /= departmentSizes[department - 1];
		return centroid;
	}
	/** finds the total cost of moving material between departments
	* @param centriodArray holds the centroids of each department
	* @return total cost of material handling in a given facilty layout
	*/
	public static double getTotalCost(double centroidArray[][])
	{
		double totalCost = 0;
		// Main PAR Assembly
		totalCost += getCost( 2, 1, 2, centroidArray);
		totalCost += getCost( 4, 2, 3, centroidArray);
		totalCost += getCost( 8, 3, 4, centroidArray);
		totalCost += getCost( 8, 4, 5, centroidArray);
		totalCost += getCost( 2, 5, 1, centroidArray);
		// Par subassembly 1
		totalCost += getCost( 8, 1, 6, centroidArray);
		totalCost += getCost( 8, 6, 7, centroidArray);
		totalCost += getCost( 16, 7, 4, centroidArray);
		// Main DEC Assembly
		totalCost += getCost( 4, 1, 2, centroidArray);
		totalCost += getCost( 4, 2, 8, centroidArray);
		totalCost += getCost( 8, 8, 9, centroidArray);
		totalCost += getCost( 16, 9, 5, centroidArray);
		totalCost += getCost( 2, 5, 1, centroidArray);
		// DEC subassembly 1
		totalCost += getCost( 8, 1, 10, centroidArray);
		totalCost += getCost( 16, 10, 11, centroidArray);
		totalCost += getCost( 32, 11, 9, centroidArray);
		// DEC subassembly 2
		totalCost += getCost( 2, 1, 12, centroidArray);
		totalCost += getCost( 8, 12, 13, centroidArray);
		totalCost += getCost( 4, 13, 11, centroidArray);
		// 3-Way Assembly 
		totalCost += getCost( 8, 1, 2, centroidArray);
		totalCost += getCost( 16, 2, 14, centroidArray);
		totalCost += getCost( 32, 14, 15, centroidArray);
		totalCost += getCost( 32, 15, 5, centroidArray);
		totalCost += getCost( 16, 5, 1, centroidArray);
		return totalCost;
	}

	/** finds the cost of moving material from one department to another
	* @param unitFlow number of units moving from one department to another
	* @param department1 the department material is moving from
	* @param department2 the department material is moving to
	* @param centroidArray holds the centroids of each department
	* @return distance between department centroids
	*/	
	public static double getCost(int unitFlow, int department1, int department2, double centroidArray[][])	
	{
		double cost = 0;
		double m, n;
		// if department 1 is involved, different costs are used
		if (department1 == 1 || department2 == 1)
		{
			m = 0.125;
			n = 0.025;
		}
		else
		{
			m = 0.25;
			n = 0.1;
		}
		
		// cost for moving up or down a floor
		if (centroidArray[department1][2] != centroidArray[department2][2])
		{
			cost += unitFlow*m;
		}
		
		// cost for horizontal travel
		cost += getDistance(department1, department2, centroidArray)*unitFlow*n;
	
		return cost;
	}

	/** finds the distance between the centroids of two departments
	* @param department1 the department material is moving from
	* @param department2 the department material is moving to
	* @param centriodArray holds the centroids of each department
	* @return distance between department centroids
	*/	
	public static double getDistance(int department1, int department2, double centroidArray[][])
	{
		double distance = 0;
		double xDist = centroidArray[department1][0] - centroidArray[department2][0];
		double yDist = centroidArray[department1][1] - centroidArray[department2][1];
		distance = Math.sqrt(xDist*xDist + yDist*yDist);
		return distance;
	}
}