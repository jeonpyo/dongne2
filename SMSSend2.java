import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Hashtable;

public class SMSSend2 {
	
	public static Connection conn = null;

    private String ORADriver = "";
    private String ORAURL = "";
    private String ORAUser = "";
    private String ORAPass = "";

    public void init() {


            try {
                    //ORADriver = dbProps.getProperty("B2CDB1.ORADriver");
                    ORADriver = "oracle.jdbc.driver.OracleDriver";
                    ORAURL = "jdbc:oracle:thin:@10.125.10.128:1521:B2CDB"; 
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
            	SMSSend2.conn.close();
            } catch(Exception e) {
                    System.out.println("destroy() : "+e.getMessage());
            } finally {
            }
    }
    
    public int sendSMS(String pSendPhone, String pCallPhone, String pSendMsg){

        int result = 0;
        PreparedStatement pstmt = null;

        StringBuffer sql=new StringBuffer();
        sql.append(" insert into SC_TRAN   \n");
        sql.append("                    (TR_NUM ,TR_SENDDATE , TR_SENDSTAT ,TR_MSGTYPE ,TR_PHONE ,TR_CALLBACK , TR_MSG) \n");
        sql.append("       values(SC_TRAN_SEQ.NEXTVAL, SYSDATE, '0', '0', ?, ?, ?) \n");

        try{
        	pstmt =  conn.prepareStatement(sql.toString());
            pstmt.setString(1, pSendPhone);
            pstmt.setString(2, pCallPhone);
            pstmt.setString(3, pSendMsg);

            
            result = pstmt.executeUpdate(); 
            
            conn.commit();
            
            
            
        }catch(Exception e){
        	System.out.println("=========================================================================");
            System.out.println(" SendSMS Occured Exception : "+e.getMessage());
            System.out.println("=========================================================================");
            try{
            	conn.rollback();
            	
            }catch(Exception ex){
            	System.out.println("SendSMS rollBack Error : " + e.getMessage());
            }
            
            
            
        }finally{
        	if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
                
        }

        return result;
    }
    
    public String sendSMS_URL(String address, String pSendPhone, String pSendMsg){
	String retResult = "";
	try {
         // Construct data
         String data = "kind=0&sendPhone=" + pSendPhone;
         data += "&callPhone=15771533&sendMsg=" + java.net.URLEncoder.encode(pSendMsg, "euc-kr");
         
         // Send data
         URL url = new URL(address);
         URLConnection conn = url.openConnection();
         // If you invoke the method setDoOutput(true) on the URLConnection, it will always use the POST method.
         conn.setDoOutput(true);
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(data);
         wr.flush();
     
         // Get the response
         BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"euc-kr"));
        
         while ((retResult = rd.readLine()) != null) {
            System.out.println(retResult);
         }
         wr.close();
         rd.close();
     }
     catch (Exception e) {
     }
	
	return retResult;
	
}
    
    public int sendMMS(String pSendTitle,  String pSendPhone, String pCallPhone, String pSendMsg){
       
    	int result = 0;
        PreparedStatement pstmt = null;

        StringBuffer sql=new StringBuffer();
        sql.append(" insert into MMS_MSG   \n");
        sql.append("                    (MSGKEY, SUBJECT, PHONE, CALLBACK, STATUS, REQDATE, MSG, TYPE) \n");
        sql.append("       values( MMS_MSG_SEQ.NEXTVAL,? ,?, ?, '0', SYSDATE,? , '0' ) \n");

        try{
                
                pstmt =  conn.prepareStatement(sql.toString());


                pstmt.setString(1, pSendTitle);
                pstmt.setString(2, pSendPhone);
                pstmt.setString(3, pCallPhone);
                pstmt.setString(4, pSendMsg);

                result = pstmt.executeUpdate();
                
                conn.commit();


                
                if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
        }catch(Exception e){
                System.out.println("=========================================================================");
                System.out.println(" sendMMS Occured Exception : "+e.getMessage());
                System.out.println("=========================================================================");
                try{
                	conn.rollback();
                	
                }catch(Exception ex){
                	System.out.println("SendSMS rollBack Error : " + e.getMessage());
                }
               
                //out.print("99||" + e.getMessage());
        }finally{
                
                if(pstmt!=null) try{pstmt.close();}catch(Exception e){}
                
        }

        return result;
    }
    
    public String __sendSMS(String address, String pSendPhone, String pSendMsg){
	String retResult = "";
	try {
         // Construct data
         String data = "kind=0&sendPhone=" + pSendPhone;
         data += "&callPhone=0234303106&sendMsg=" + java.net.URLEncoder.encode(pSendMsg);
         
         // Send data
         URL url = new URL(address);
         URLConnection conn = url.openConnection();
         // If you invoke the method setDoOutput(true) on the URLConnection, it will always use the POST method.
         conn.setDoOutput(true);
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(data);
         wr.flush();
     
         // Get the response
         BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        
         while ((retResult = rd.readLine()) != null) {
            System.out.println(retResult);
         }
         wr.close();
         rd.close();
     }
     catch (Exception e) {
     }
	
	return retResult;
	
}

    
    public Hashtable getPhoneNo(String filePath){
    	Hashtable hs = new Hashtable();
    	int count = 0;
    	 try {
    	      ////////////////////////////////////////////////////////////////
    	      //BufferedReader in = new BufferedReader(new FileReader(filePath));
    	      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"EUC-KR"));
    	      String s;
    	      

    	      while ((s = in.readLine()) != null) {    	    	
    	        hs.put("MDN" + count, s);
    	        count++;
    	      }
    	      in.close();
    	      ////////////////////////////////////////////////////////////////
    	    } catch (IOException e) {
    	        System.err.println(e);       
    	    }
    	
    	
    	return hs;
    	
    	
    }
    
    public String urlCall(String pSendPhone, String pSendMsg){

    	String result = "";
    	String fullUrl = "";
    	
    	fullUrl = "http://www.njoyny.com/lgsms/smsSend.jsp?kind=0&sendPhone=" + pSendPhone + "&callPhone=0234303106&sendMsg=" + java.net.URLEncoder.encode(pSendMsg);
    	
    	URL url = null;
    	BufferedReader input = null;  
    	
    	try {
    	 url = new URL(fullUrl);
    	 input = new BufferedReader(new InputStreamReader(url.openStream()));   
    	
    	
    	  
    	while((result = input.readLine()) !=null) {
    	 System.out.println(result);
    	 }
    	 
    	 input.close();
    	 }catch(Exception e) {
    	 e.printStackTrace();
    	 }
    	
    	return result;


    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		SMSSend2 sms = new SMSSend2();
		
		Hashtable mdnHs = new Hashtable();
		String str[] = new String[3];
		sms.init();
		
		
		mdnHs = sms.getPhoneNo("C://Tmp//smssend2.txt");
		//sms.sendSMS("01068220883", "1577-1533", "신속한 복구로 최대한 빠른 배송 받아 보실 수 있도록 전력을 다하겠습니다.");
		System.out.println("전송 사이즈 = " + mdnHs.size());
		for(int i=0; i<mdnHs.size(); i++){
			str = mdnHs.get("MDN" + i).toString().trim().split(",");
			
			//if(i%1000 == 0){
				System.out.println("전송중 " + i + " 번");
				System.out.println("MDN = " + str[0] + " %% callBack = " + str[2] + " %% msg = " + str[1]);
				
			
			//}
			
			
			//sms.sendSMS(str[0].trim(), str[2].trim(), str[1].trim());
			sms.sendMMS("엔조이뉴욕",  str[0], str[2], str[1]);
			
			
			
		}
		
		System.out.println("전송완료!");
		
		
		
		
		sms.destroy();
		
	}

}
