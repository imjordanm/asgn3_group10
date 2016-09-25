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

        //Establish connection
        Connection con = createCon(user, pass, host);

        //Create consultation data object using ConsultData support class
        ConsultData consult = new ConsultData(); // Create consultation data object
               
        String dateTime = timeSlot();
              
        //Heading for the Pet Clinic application
        System.out.println("\nPet Clinic Consultation and Prescription Entry\n"); 

        // Store consultation object data fields
        consult.setTimeslot(dateTime); 	       
        String vetird = null;
        vetird = vetIRD(con);      
        String position = posCheck(vetird, con); //check employee position
		
		while(!position.equals("vet")) {
			System.out.println("Not a vet");
			vetird = vetIRD(con);
			position = posCheck(vetird, con);
		}      
        
        consult.setVetIrd(vetird);				
        consult.setAnimalID(getAnimalID());
        consult.setOwnerID(con);
        consult.setDescription(getDescription()); // Prompt user for description
        double cost = 100.00;
        consult.setCost(cost);			
        // Insert consult entry in DB
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
            int numMeds = 0;
            boolean error = true;
            do {
                try {
                    numMeds = (scan.nextInt());
                    if (numMeds > 5) {
                        System.err.println("Error: Number of medicines exceeds 5, please retry (1-5):");
                        scan.nextLine();
                    } else {
                        error = false;
                    }
                } catch (Exception e) {
                    System.err.println("Error: Please enter number of medicines as a digit (1-5):");
                    scan.nextLine();
                }
            } while (error);
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
	 * Takes connection object as input
	 * Returns vet IRD as string
	 * Justin Teare
	 */
	public String vetIRD(Connection c) { 
		
		String command = "SELECT ird FROM employees " +
			"WHERE UPPER(f_name) = UPPER(?) " +
			"AND UPPER(l_name) = UPPER(?)";
				
		// Run query and process results
		PreparedStatement pstmt = null;
		ResultSet result = null;
		String outString = new String();

		try {
			Scanner scan = new Scanner(System.in);
			String fname = new String();
			String lname = new String();
			pstmt = c.prepareStatement(command);
			System.out.println("Vet First Name: ");
			fname = scan.nextLine();
			pstmt.setString(1, fname);
			System.out.println("Vet Last Name: ");
			lname = scan.nextLine();
			pstmt.setString(2, lname);		
			result = pstmt.executeQuery();
			
			
			//result.next();
			
			while(!result.next()) {
				result.close();
				System.out.println("Employee not found");
				System.out.println("Vet First Name: ");
				pstmt.setString(1, scan.nextLine());
				System.out.println("Vet Last Name: ");
				pstmt.setString(2, scan.nextLine());
				result = pstmt.executeQuery();
			}								
			outString = result.getString(1);
			System.out.println(outString);
		}
		catch (SQLException e ) {
			quit(e.getMessage());

		} finally {
			try {
				if (pstmt != null) { pstmt.close(); }
				if (result != null) {result.close(); }
			} catch (Exception e) {
				System.out.println("Error in closing " + e.getMessage());
			}
		}	
		return outString;
	} // end vetIRD()
	
	public String posCheck(String ird, Connection c) {
		String command = "SELECT position FROM employees WHERE ird=?";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		String outString = new String();
		try { 
			pstmt = c.prepareStatement(command);
			pstmt.setString(1, ird);
			result = pstmt.executeQuery();
			result.next();
			outString = result.getString(1);
		}
		catch (SQLException e ) {
                        quit(e.getMessage());
        } finally {
                try {
                	if (pstmt != null) { pstmt.close(); }
                    result.close();
                } catch (Exception e) {
                    System.out.println("Error in closing " + e.getMessage());
                }
        	}
        return outString;
    } // end posCheck()
   
    
    /**
     * Asks the user for the animal ID
     * Returns the animal ID that is scanned in
     * Jordan McRae
     */
    public String getAnimalID(){
        // Get animal ID from user input
        Scanner scan = new Scanner(System.in);
        System.out.print("Animal ID: ");	
        String animalID = (scan.nextLine());
        
        return animalID;
    }

    /**
     * Asks the user for the consultation description
     * Returns the description that is scanned in
     * Jordan McRae
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
     * Asks user to input instructions for the prescription
     * Returns instructions as string
     * Jordan McRae
     */
    public String getInstructions(){       
        Scanner scan = new Scanner(System.in);
        System.out.print("Instructions: ");	
        String instruct = (scan.nextLine());
        System.out.println();        
        return instruct;
    }//end getInstructions

    /**
     * Sets data field for prescription number to make it accesible
     * Takes int, returns int
     */
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

    /**
     * Takes the current stock and the quantity input as parameters
     * Subtracts the quantity from stock to calculate new stock
     * Takes int, returns int
     */
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
        Scanner scan = new Scanner(System.in);
        System.out.println("\nMedicine Name: ");
        String nameMed = (scan.nextLine());
        return nameMed;
    }
	
    /**
     *Asks user for medicine dosage
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
