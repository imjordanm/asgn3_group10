/*
  File: PrescriptionData.java
  Jordan McRae
*/

/** 
 * Support Class PrescriptionData
 * Stores the data fields for new prescription entries
 */

import java.util.*;
import java.sql.*;

public class PrescriptionData {

    private int prescription_num; 
    private String instructions;
    private String treatment_slot;
    private String vet_ird;
	
    //default constructor
    public PrescriptionData() { }
	
	
//    /*sets the value of prescription_num data field*/
//    public void setPrescriptionNum(int prescription_num){
//        this.prescription_num = prescription_num;      
//    }//end method
    
    /**sets the value of prescription_num data field*/
    public void setPrescriptionNum(Connection c){
    	String command = "SELECT MAX(prescription_num) FROM prescription";
    	Statement stmt = null;
        ResultSet result = null;
        int maxpnum = 0;
        
        try {
            stmt = c.createStatement();
            result = stmt.executeQuery(command);
            result.next();
            maxpnum = result.getInt(1);
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
        prescription_num = maxpnum + 1; 
        
    }//end method
    

    /**sets the value of instructions data field*/
    public void setInstructions(String instructions){
        this.instructions = instructions;
    }//end method

    /**set the value of treatment_slot datafield*/
    public void setTreatmentSlot(String treatment_slot){
        this.treatment_slot = treatment_slot;
    }//end method
	
    /**set the value of vet_ird data field*/
    public void setVetIRD (String vet_ird){
        this.vet_ird = vet_ird;
    }

    //do I need?
    /**return stored value of vet_ird*/
    public String getVetIrd(){
        return vet_ird;
    }

    public int getPrescriptionNum(){
        return prescription_num;
    }

    /**create prescription entry in database*/
    public void insertPrescription(Connection c) {
	
        String command = "INSERT INTO prescription "
            + "VALUES(?, ?, TO_DATE(?, 'yyyy-mm-dd hh24:mi:ss'), ?)";
						
        PreparedStatement pstmt = null;
		
        try {
            pstmt = c.prepareStatement(command);
            pstmt.setInt(1, prescription_num);
            pstmt.setString(2, instructions);
            pstmt.setString(3, treatment_slot);
            pstmt.setString(4, vet_ird);
			
            pstmt.executeUpdate();
            System.out.println("Prescription information:\nPrescription: " + prescription_num + "\nInstructions: " + instructions + "\nTreatment Slot: " + treatment_slot + "\nVet: " + vet_ird);
        }
        catch (SQLException e ) {
            quit(e.getMessage());
        } finally {
            try {
                if (pstmt != null) { pstmt.close(); }
            } catch (Exception e) {
                System.out.println("Error in closing " + e.getMessage());
            }
        }
    }
    
  
    // Used to output an error message and exit
    private void quit(String message) {
        System.err.println(message);
        System.exit(1);
    }	
}
