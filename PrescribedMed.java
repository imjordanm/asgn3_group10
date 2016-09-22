import java.util.*;
import java.sql.*;

public class PrescribedMed {

    private int quantity; 
    private int p_num;
    private String name;
    private String dosage;
	
    //default constructor
    public PrescribedMed() { }
    
    /**sets the value of the data field quantity to input parameter value*/
  	public void setQuantity(int quantity){
            this.quantity = quantity;
  	}//end method

	/**sets the value of p_num data field*/
	public void setP_num(int p_num){
		this.p_num = p_num;
	}//end method

	/**set the value of name datafield*/
	public void setName (String name){
		this.name = name;
	}//end method
	
	/**set the value of dosage data field*/
	public void setDosage (String dosage){
		this.dosage = dosage;
	}
	
	/**retreive quantity prescribed*/	
	public int getQuantity() {
		return quantity;
	}
    

	/**check remaining stock*/
	public int checkStock(Connection c) {
		String command = "SELECT stock FROM medicine "
				+ "where name = ? and dosage = ?";
		PreparedStatement pstmt = null;
		ResultSet result = null;
        	int stock = 0;
        	try {
            		pstmt = c.prepareStatement(command);
			pstmt.setString(1, name);
			pstmt.setString(2, dosage);
			pstmt.executeQuery();		
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
		return stock;
	}


	
	/**create prescribed_in entry in database*/
	public void insertMedicine(Connection c) {
	
		String command = "INSERT INTO prescribed_in "
						+ "VALUES(?, ?, ?, ?)";
						
		PreparedStatement pstmt = null;
		
		try {
			pstmt = c.prepareStatement(command);
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, p_num);
			pstmt.setString(3, name);
			pstmt.setString(4, dosage);
			
			pstmt.executeUpdate();
                        System.out.println("Medicine information:\nQuantity: " 
				+ quantity + "\nPrescription number: " 
				+ p_num + "\nMedicine name: " 
				+ name + "\nDosage: " + dosage);
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

