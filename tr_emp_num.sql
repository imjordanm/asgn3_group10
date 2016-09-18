/* Maintain no_of_employees in clinic table
	Samuel Gabbusch
*/

CREATE OR REPLACE TRIGGER tr_emp_num
       AFTER INSERT OR DELETE OR UPDATE ON employees
             BEGIN               
                --Dunedin
                UPDATE clinic SET no_of_employees
                = (SELECT count(*) FROM employees
                WHERE clinic_name='Pet Clinic Dunedin')
                WHERE name='Pet Clinic Dunedin';

                --Christchurch
                UPDATE clinic SET no_of_employees
                = (SELECT count(*) FROM employees
                WHERE clinic_name='Pet Clinic Christchurch')
                WHERE name='Pet Clinic Christchurch';

                --Auckland
                UPDATE clinic SET no_of_employees
                = (SELECT count(*) FROM employees
                WHERE clinic_name='Pet Clinic Auckland')
                WHERE name='Pet Clinic Auckland';
                
   
              END;
/
