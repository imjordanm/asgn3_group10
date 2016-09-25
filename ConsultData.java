/*
 *File: ConsultData.java
 *Justin Teare
 */

/** 
 * Support Class ConsultData
 * Stores the data fields for consultation entry
 */

import java.util.*;
import java.sql.*;

public class ConsultData {

    private String timeslot; 
    private String vet_ird;
    private String animal_id;
    private String owner_id;
    private String description;
    private double cost;
	
    //default constructor
    public ConsultData() { }
	
    /**sets the value of the data field timeslot to input parameter value*/
    public void setTimeslot(String timeslot){
        this.timeslot = timeslot;
    }//end method

    /**sets the value of vet_ird data field*/
    public void setVetIrd(String vet_ird){
        this.vet_ird = vet_ird;
    }//end method

    /**set the value of animal_id datafield*/
    public void setAnimalID (String animal_id){
        this.animal_id = animal_id;
    }//end method

    /**set the value of owner_id datafield*/
    public void setOwnerID (Connection c){
        // Run query and process results
        String animal_ID = getAnimalID();
        
        String command = "SELECT owner_ID FROM animal " +
            "WHERE animal_id = ?";
        
        PreparedStatement pstmt = null;
        ResultSet result = null;
        String owner_id = new String();

        try {
            pstmt = c.prepareStatement(command);
            pstmt.setString(1, animal_ID);
            result = pstmt.executeQuery();
            result.next();
            owner_id = result.getString(1);
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
        this.owner_id = owner_id;
    }
	
    /**set the value of description data field*/
    public void setDescription (String description){
        this.description = description;
    }
	
    /**set the value of billing data field*/
    public void setCost (double cost){
        this.cost = cost;
    }
	
    /**return stored value of vet_ird*/
    public String getVetIrd(){
        return vet_ird;
    }

    public String getAnimalID(){
        return animal_id;
    } 
	
    /**create consultation entry in database*/
    public void insertConsult(Connection c) {
	
        String command = "INSERT INTO consultation "
            + "VALUES(TO_DATE(? ,'yyyy-mm-dd hh24:mi:ss'), ?, ?, ?, ?, ?)";
						
        PreparedStatement pstmt = null;
		
        try {
            pstmt = c.prepareStatement(command);
            pstmt.setString(1, timeslot);
            pstmt.setString(2, vet_ird);
            pstmt.setString(3, animal_id);
            pstmt.setString(4, owner_id);
            pstmt.setString(5, description);
            pstmt.setDouble(6, cost);

            pstmt.executeUpdate();
            System.out.println("Consultation information:\nDate: " + timeslot + "\nVet: " + vet_ird + "\nOwner: " + owner_id + "\nAnimal: " + animal_id + "\nDescription: " + description + "\nCost: $" + cost);
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
