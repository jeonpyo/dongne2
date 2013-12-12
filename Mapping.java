import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Hashtable;
import java.sql.ResultSet;


public class Mapping {
	public static Connection conn = null;

    private String ORADriver = "";
    private String ORAURL = "";
    private String ORAUser = "";
    private String ORAPass = "";

    public void init() {


            try {
                    //ORADriver = dbProps.getProperty("B2CDB1.ORADriver");
                    ORADriver = "oracle.jdbc.driver.OracleDriver";
                    ORAURL = "jdbc:oracle:thin:@10.125.10.128:1521:B2CDB"; //占쏘영b2c
                    ORAUser = "pion";
                    ORAPass = "pion123";

                    //Class.forName(ORADriver);
                    Class.forName("oracle.jdbc.driver.OracleDriver");

                    conn = DriverManager.getConnection(ORAURL, ORAUser, ORAPass);
                    conn.setAutoCommit(false);
            }catch(Exception e) {
                    System.out.println("init() : "+e.getMessage());
            }
    }

    public void destroy() {
            try {
            	Mapping.conn.close();
            } catch(Exception e) {
                    System.out.println("destroy() : "+e.getMessage());
            } finally {
            }
            
    }
    
    public String getOrderNo(String hMallOrder){

        String OrderResult = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
/*
 select order_no
   from ktc_njoyny_hmall_if
  where h_order_no like '%20121210447249%'
*/
        StringBuffer sql=new StringBuffer();
        sql.append(" select order_no  \n");
        sql.append("   from shop_asso_bridge \n");
        sql.append(" where h_order_no like (?) \n");

        try{
        	pstmt =  conn.prepareStatement(sql.toString());
            pstmt.setString(1, "%" + hMallOrder + "%");
            rs = pstmt.executeQuery();

            
            while(rs.next()) {
            	OrderResult = rs.getString("order_no");
            	
            }
            
          
            
            
            
        }catch(Exception e){
        	System.out.println("=========================================================================");
            System.out.println(" hmallOrder Occured Exception : "+e.getMessage());
            System.out.println("=========================================================================");
        }finally{
        	if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
        	if(rs!=null) {try{rs.close();} catch(Exception e){ System.out.println(e.getStackTrace());}}
                
        }

        return OrderResult;
    }
    
    public Hashtable getHmallOrder(String filePath){
    	Hashtable hs = new Hashtable();
    	int count = 0;
    	 try {
    	      ////////////////////////////////////////////////////////////////
    	      BufferedReader in = new BufferedReader(new FileReader(filePath));
    	      String s;
    	      

    	      while ((s = in.readLine()) != null) {    	    	
    	        hs.put("HMALLORDERNO" + count, s);
    	        count++;
    	      }
    	      in.close();
    	      ////////////////////////////////////////////////////////////////
    	    } catch (IOException e) {
    	        System.err.println(e); // 占쏙옙占쏙옙占쏙옙 占쌍다몌옙 占쌨쏙옙占쏙옙 占쏙옙占�   	        
    	    }
    	
    	
    	return hs;
    	
    	
    }
    
    public static void main(String[] args) {
    	Mapping mp = new Mapping();
    	
    	Hashtable hMallOrderHs = new Hashtable();
    	
    	mp.init();
    	
    	hMallOrderHs = mp.getHmallOrder("C:/java_project/hmall.txt");
    	
    	System.out.println("占싼곤옙占쏙옙 = " + hMallOrderHs.size());
    	
    	String hmallOrderNo = "";
    	String njoyNyOrderNo = ""; 
    	for(int i=0; i<hMallOrderHs.size(); i++){
    		hmallOrderNo = hMallOrderHs.get("HMALLORDERNO" + i).toString().trim();
    		njoyNyOrderNo = mp.getOrderNo(hmallOrderNo);
    		
    		System.out.println(hmallOrderNo + "\t" + njoyNyOrderNo);
    		
    	}
    	
    	mp.destroy();
    	
    	System.out.println("End");
    	
    
    }

}
