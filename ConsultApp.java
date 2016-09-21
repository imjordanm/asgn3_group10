/*
 * File: ConsultApp.java
 * Justin Teare
 */

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * Main application program for assignment 3
 */

public class ConsultApp {

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
			
			//USE THIS SECTION TO ADD YOUR QUERIES

			/*
			 * timestamp testing
			 */			
			consult.setTimeslot(timeSlot());
					
			/*
 		 	 * vet_ird attribute
			 * No error checking implemented yet
			 */ 			
			String vetquery = getVetInfo(); // prompt for name and generate query			
			String vetird = vetIRD(vetquery, con); //execute query
			consult.setVetIrd(vetird);				
			
			System.out.print("Stored vet_ird: ");
			System.out.println(consult.getVetIrd());
			
			
			//dummy data
			consult.setAnimalID(123456);
			consult.setDescription("test description");
			
			//add consult entry to DB
			consult.insertConsult(con);
			
			// Close connection
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					quit(e.getMessage());
				}
			}


		
	}  // end go()

	
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
		
		/*finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					quit(e.getMessage());
				}
			}
		}
		*/
		return con;		
	} // end createCon
	
	//Create timeslot entry using the current system date/time
	public String timeSlot() {
	
		java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());		
		java.sql.Time sqlTime = new java.sql.Time(new java.util.Date().getTime());
		System.out.println(sqlDate + " " + sqlTime);
		
		String outString = sqlDate + " " + sqlTime;
		return outString;
	}
	
	// Ask user for first name and last name to retrieve Vet IRD
	// returns Sql Query to retireive ird
	public String getVetInfo() {

		// Get vet f_name and l_name from user input
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter First Name: ");	
		String fname = (scan.nextLine());
		System.out.println();		
		System.out.print("Enter Last Name: ");	
		String lname = (scan.nextLine());
		System.out.println();

		// Create SQL Query
		String command = "SELECT ird FROM employees " +
			"WHERE UPPER(f_name) = UPPER('" + fname +
			"') AND UPPER(l_name) = UPPER('" + lname + "')";

		System.out.println("SQL: " + command);
		return command;
	} // end getVetInfo()

	// Runs the query from getVetInfo
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

	// Used to output an error message and exit
	private void quit(String message) {
		System.err.println(message);
		System.exit(1);
	}

} // end class VetQuery
