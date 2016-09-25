/*
 * File: ConsultApp.java
 * Justin Teare
 * Jordan McCrae
 */

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Main application program for assignment 3
 */

public class ConsultApp {

	public int p_num;

	public static void main (String[] args) {
		new ConsultApp().go();
	}

	// This is the function that does all the work
	private void go() {

		// Read pass.dat
		UserPass login = new UserPass();
		String user = login.getUserName();
		String pass = login.getPassWord();
		String host = "silver";

		/*
		 * Establish connection
		 * We can keep this connection open
		 * Make sure you close your statement and resultset at the end of your method
		 * See vetIRD() for example on how to close statement and resultset 
		 */
		Connection con = createCon(user, pass, host);

		ConsultData consult = new ConsultData(); // Create consultation data object

		/*
 		*datetime set as own string so consultation and prescription
		*(and whatever else) can use this to set their treatment slots
		*so the times are identical (setting prescription's timeslot
		*would be seconds or minutes off from consultations).
		*/
		String dateTime = timeSlot();
		consult.setTimeslot(dateTime); // Store timeslot in consultation object

		/*
		 * vet_ird attribute
		 * No check to make sure user is a vet implemented
		 */ 			
		String vetquery = getVetInfo(); // prompt for name and generate query			
		String vetird = vetIRD(vetquery, con); //execute query
		consult.setVetIrd(vetird);				

		//Animal Id 	
		//dummy data
		//to be completed by Sam
		consult.setAnimalID("54825821654321"); // Store animal id number in consultation object

		consult.setOwnerID("30-819-444");
		
		consult.setDescription(getDescription()); // Prompt user for description
		
		double cost = 100.00;
		consult.setCost(cost);			


		// Inser consult entry in DB
		consult.insertConsult(con);

		// Prompt to add new prescription, if yes calls prescribe method
		System.out.println("\nAdd a prescription?");
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter yes or no: ");	
		String decision = (scan.nextLine());
		//New prescription if user answers yes on prompt
		if (decision.equalsIgnoreCase("yes") || decision.equalsIgnoreCase("y")) {
			prescribe(dateTime, vetird, con);

			//Prompt for the number of medicines
			System.out.println("\nNumber of medicines (1-5):");
			int numMeds = (scan.nextInt());
			while (numMeds > 5) {
				System.out.println("Number of medicines exceeds 5, please enter again");
				numMeds = (scan.nextInt());
			}
			prescribedIn(p_num, numMeds, con); // Create prescribed_in entry for medicine in DB 
		}

		// Close connection
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				quit(e.getMessage());
			}
		}		
	}  // end go()

	/** 
	 * Method to create connection to the database
	 * Takes username password and host address
	 * Returns connection object
	 * From cosc344 examples
	 */
	public Connection createCon(String user, String pass, String host) {
		Connection con = null;	
		try {
			// Register the driver and connect to Oracle
			DriverManager.registerDriver 
				(new oracle.jdbc.driver.OracleDriver());
			String url = "jdbc:oracle:thin:@" + host + ":1527:cosc344";
			System.out.println("url: " + url);
			con = DriverManager.getConnection(url, user, pass);
			System.out.println("Connected to Oracle");				
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} 
		return con;		
	} // end createCon


	//Consultation methods

	/**
	 * Create timeslot entry using the current system date/time
	 * Returns a string suitable for entry into DB as a DATE atrribute
	 * Jordan McRae, Justin Teare
	 */
	public String timeSlot() {
		java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());		
		java.sql.Time sqlTime = new java.sql.Time(new java.util.Date().getTime());
		System.out.println(sqlDate + " " + sqlTime);
		String outString = sqlDate + " " + sqlTime;
		return outString;
	}

	/**  
	 * Ask user for first name and last name to retrieve Vet IRD
	 * Returns Sql Query to retrieve ird as a string
	 * Justin Teare
	 */
	public String getVetInfo() {

		// Get vet f_name and l_name from user input
		Scanner scan = new Scanner(System.in);
		System.out.print("Vet First Name: ");	
		String fname = (scan.nextLine());
		System.out.println();		
		System.out.print("Vet Last Name: ");	
		String lname = (scan.nextLine());
		System.out.println();

		// Create SQL Query
		String command = "SELECT ird FROM employees " +
			"WHERE UPPER(f_name) = UPPER('" + fname +
			"') AND UPPER(l_name) = UPPER('" + lname + "')";
		return command;
	} // end getVetInfo()


	/** 
	 * Runs the query from getVetInfo
	 * Takes query as string and connection object as input
	 * Returns vet IRD as string
	 * Justin Teare
	 */
	public String vetIRD(String command, Connection c) { 

		// Run query and process results
		Statement stmt = null;
		ResultSet result = null;
		String outString = new String();

		try {
			stmt = c.createStatement();
			result = stmt.executeQuery(command);
			result.next();
			outString = result.getString(1);
			//System.out.println(outString);
		}
		catch (SQLException e ) {
			quit(e.getMessage());

		} finally {
			try {
				if (stmt != null) { stmt.close(); }
				result.close();
			} catch (Exception e) {
				System.out.println("Error in closing " + e.getMessage());
			}
		}	
		return outString;
	} // end vetIRD()   

	/**
	 * Asks the user for the consultation description
	 * Returns the description that is scanned in
	 * Jordan McCrae
	 */

	public String getDescription(){
		// Get description of the consultation
		Scanner scan = new Scanner(System.in);
		System.out.print("Description: ");	
		String desc = (scan.nextLine());
		System.out.println();

		return desc;
	}//end getDescription


	//Prescription methods
	
	/**
 	*Asks if the user wants to add a new prescription
	*Takes timeslot and vet IRD strings, and connection object as inputs
	*Jordan McCRae
	*/
	public void prescribe(String dateTime, String vetird, Connection con) {
		PrescriptionData prescription = new PrescriptionData();
		prescription.setPrescriptionNum(con);
		prescription.setInstructions(getInstructions());
		prescription.setTreatmentSlot(dateTime);
		prescription.setVetIRD(vetird);
		prescription.insertPrescription(con);
		p_num = prescription.getPrescriptionNum();     
		prescriptionNum(p_num);
	}//end prescribe

	/**
 	*Get instructions for the prescription
	*Asks user to input instructions
	*Returns instructions as string
	*Jordan McRae
	*/
	public String getInstructions(){       
		Scanner scan = new Scanner(System.in);
		System.out.print("Instructions: ");	
		String instruct = (scan.nextLine());
		System.out.println();        
		return instruct;
	}//end getInstructions

	// Sets data field for prescription number to make it accesible
	// Takes int, returns int
	public int prescriptionNum(int p_num) {
		this.p_num = p_num;
	        return p_num;
	}  
	
	//PrescribedIn methods
	
	/**
	*Prompt user to input prescribed medicine information
	*Create prescribed_in DB entry for each medicine
	*Takes prescription number and number of medicines as int, and a connection object as input
	*Jordan McCrae, Justin Teare
	*/
	public void prescribedIn(int p_num, int numMeds, Connection con) {
		PrescribedMed[] medicines = new PrescribedMed[numMeds];
		for (int i = 0; i < medicines.length; i++) {
			medicines[i] = new PrescribedMed();

			medicines[i].setP_num(p_num);
			medicines[i].setName(getMedName());
			medicines[i].setDosage(getMedDosage());
			int stock = medicines[i].checkStock(con);
			medicines[i].setQuantity(getUserQuantity(stock));
			int quan = medicines[i].getQuantity();
			int newstock = getNewStock(stock, quan);
			medicines[i].setNewStock(newstock);
			medicines[i].insertMedicine(con);
			medicines[i].updateStock(newstock, con);			
			int x = i + 1;
			System.out.println("Medicine " + x + " added to database");
		}
	}
	
	/**
 	*Asks for input of medicine quantity
	*Returns int
	*Jordan McRae, Justin Teare
	*/
	public int getUserQuantity(int stock){
		//Prompt for quantity for each medicine
		System.out.println("Remaining stock: " + stock);
		System.out.println("\nQuantity of medicine:");
		Scanner scan = new Scanner(System.in);
		int quantityMed = (scan.nextInt());
		while (quantityMed > stock) {
			System.out.println("Quantity must be less than stock");
			quantityMed = (scan.nextInt());
		}
		return quantityMed;
	}

	public int getNewStock(int stock, int quan) {
		int newstock = stock - quan;
		return newstock;	
	}
	
	/**
 	*Asks user for medicine name
	*Returns string
	*Jordan McRae
	*/
	public String getMedName(){
		//Prompt for name of medicine
		System.out.println("\nMedicine Name:");
		Scanner scan = new Scanner(System.in);
		String nameMed = (scan.nextLine());
		return nameMed;
	}
	
	/**
	*Asks user for medicone dosage
	*returns string
	*Jordan McRae
	*/
	public String getMedDosage(){
		//Prompt for dosage of medicine
		System.out.println("\nDosage:");
		Scanner scan = new Scanner(System.in);
		String dosageMed = (scan.nextLine());
		System.out.println();
		return dosageMed;
	}


	// Used to output an error message and exit
	private void quit(String message) {
		System.err.println(message);
		System.exit(1);
	}

} // end class ConsultApp
