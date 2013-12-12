import com.nhncorp.psinfra.toolkit.SimpleCryptLib;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import java.net.*;
import java.io.*;
import javax.xml.parsers.*;
import java.util.*;

import org.xml.sax.InputSource;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.sql.CallableStatement;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import oracle.jdbc.*;
import oracle.sql.*;
import javax.sql.*;

public class NaverShopnGetArea { 


        public static Connection conn = null;

        private String ORADriver = "";
        private String ORAURL = "";
        private String ORAUser = "";
        private String ORAPass = "";

        public void init() {


                try {
                        //ORADriver = dbProps.getProperty("B2CDB1.ORADriver");
                        ORADriver = "oracle.jdbc.driver.OracleDriver";
                        ORAURL = "jdbc:oracle:thin:@10.125.10.128:1521:B2CDB1"; //운영b2c
                        ORAUser = "KTC_B2C";
                        ORAPass = "KTC_B2C123";

                        //Class.forName(ORADriver);
                        Class.forName("oracle.jdbc.driver.OracleDriver");

                        conn = DriverManager.getConnection(ORAURL, ORAUser, ORAPass);
                        conn.setAutoCommit(false);
                }catch(Exception e) {
                        System.out.println("init() : "+e.getMessage());
                }
        }

        public void distroy() {
                try {
                        this.conn.close();
                } catch(Exception e) {
                        System.out.println("distroy() : "+e.getMessage());
                } finally {
                }
        }


        private String getFilePrefix() {
                String result = "";
                java.util.Date today = new java.util.Date();
                long currDate = today.getTime();
                //long addDate = currDate - (24 * (60 * (60 * 1000)));
                long addDate = currDate;
                today.setTime(addDate);
                SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMdd");
                result = dateForm.format(today);

                //result = "20090912";

                return result;
        }

        private String getToday() {
                String result = "";
                java.util.Date today = new java.util.Date();
                long currDate = today.getTime();
                //long addDate = currDate - (24 * (60 * (60 * 1000)));
                long addDate = currDate - (9 * (60 * (60 * 1000))); //한국시간은 -9 시간 해야 된다.
                today.setTime(addDate);
                SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                result = dateForm.format(today);

                //result = "2009-09-12";

                return result;
        }
 

        private String getYesterday() {
                String result = "";
                java.util.Date today = new java.util.Date();
                long currDate = today.getTime();
                //long addDate = currDate - (9 * (60 * (61 * 1000))) ; //10분전 한국시간은 -9 시간 해야 된다.
                long addDate = currDate - (33 * (60 * (60 * 1000))) + 1 ; //10분전 한국시간은 -9 시간 해야 된다. 하루전
                //long addDate = currDate;
                today.setTime(addDate);
                SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                result = dateForm.format(today);

                //result = "2009-09-12";

                return result;
        }

        private String getElementData(Element e, String name) {
                return getElementData(e, name, "");
        }

        private String getElementData(Element e, String name, String def) {
                if (e == null) return def;
                if (name == null) return def;

                Element tmp = e.getChild(name);
                if (tmp == null) return def;

                String retData = tmp.getTextTrim();
                if (retData == null) return def;

                return retData;
        }

        private String getTel( String def) {
			String tel_no = "";
            if(def.length() < 10){
            	tel_no = def.substring(0,2)+"-"+def.substring(2,5)+"-"+def.substring(5,9);
            }else if(def.length() < 11){
            	if(def.substring(0,2).equals("02")){
            		tel_no = def.substring(0,2)+"-"+def.substring(2,6)+"-"+def.substring(6,10);
            	}else{
            		tel_no = def.substring(0,3)+"-"+def.substring(3,6)+"-"+def.substring(6,10);
            	}
        	}else if(def.length() < 12){
        		tel_no = def.substring(0,3)+"-"+def.substring(3,7)+"-"+def.substring(7,11);
        	}
            return tel_no;
        }




        public String run() {
        	String  response_type="FALSE";
                try {
        SimpleCryptLib SimpleCryptLib = new SimpleCryptLib();      	
		Security.addProvider(new BouncyCastleProvider());

		String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43";
		String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";		



		String serviceName = "ProductService";  // 서비스명
		String id = "eugink";  
		String password = "asdf0101";
		String timestamp = null;
		String signature = null;
		String data = null;

		
		byte[] encryptKey = null;
		
		String encryptedData = null;
		String decryptedData = null;
		String hashedData = null;
		
		String operationName = "GetOriginAreaList";
		//String orderID = "200087036";		


		
		//timestamp create
		timestamp = SimpleCryptLib.getTimestamp();
		System.out.println("timestamp:"+timestamp);

		
		//generateSign
		data = timestamp + serviceName + operationName;
		signature = SimpleCryptLib.generateSign(data, secretKey);

		
		//generateKey
		encryptKey = SimpleCryptLib.generateKey(timestamp, secretKey);

		
		//encrypt
		encryptedData = SimpleCryptLib.encrypt(encryptKey, password.getBytes("UTF-8"));

		
		//decrypt
		decryptedData = new String(SimpleCryptLib.decrypt(encryptKey, encryptedData), "UTF-8");


		//sha256
		hashedData = SimpleCryptLib.sha256(password);


		NaverShopnGetArea NaverShopnGetArea =new NaverShopnGetArea();
		System.out.println("NaverShopnGetArea.getYesterday():"+NaverShopnGetArea.getYesterday());
		System.out.println("NaverShopnGetArea.getYesterday():"+NaverShopnGetArea.getToday());



		
		System.out.println("accessLicense : [" + accessLicense + "]");
		System.out.println("secretKey : [" + secretKey + "]");
		System.out.println("serviceName : [" + serviceName + "]");
		System.out.println("operationName : [" + operationName + "]");
		System.out.println("timestamp : [" + timestamp + "]");
		System.out.println("signature : [" + signature + "]");
		System.out.println("encryptKey : [" + new String(Hex.encode(encryptKey)) + "]");
		System.out.println("encryptedData : [" + encryptedData + "]");
		System.out.println("decryptedData : [" + decryptedData + "]");
		System.out.println("sha256Data : [" + hashedData + "]");
		


		System.out.println("NaverShopnGetArea.getYesterday():"+NaverShopnGetArea.getYesterday());
		System.out.println("NaverShopnGetArea.getToday():"+NaverShopnGetArea.getToday());	
		String ctglist =   	"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:shop=\"http://shopn.platform.nhncorp.com/\">" +
										   "<soap:Header/>" +
										   "<soap:Body>" +
										      "<shop:GetOriginAreaListRequest>" +
										         "<!--Optional:-->" +
										         "<shop:RequestID>njoyny2</shop:RequestID>" +
										         "<shop:AccessCredentials>" +
										            "<shop:AccessLicense>"+ accessLicense +"</shop:AccessLicense>" +
										            "<shop:Timestamp>"+ timestamp +"</shop:Timestamp>" +
										            "<shop:Signature>"+ signature +"</shop:Signature>" +
										         "</shop:AccessCredentials>" +
										         "<shop:Version>2.0</shop:Version>" +
										         "<!--Optional:-->" +
										         "<OriginAreaName>상세설명에 표시</OriginAreaName>" +
										         //"<CategoryId></CategoryId>" +
										      "</shop:GetOriginAreaListRequest>" +
										   "</soap:Body>" +
										"</soap:Envelope>";						  

			
			
	      //Create socket
	      String hostname = "sandbox.api.naver.com";
	      //String hostname = "api.naver.com";
	      int port = 80;
	      InetAddress  addr = InetAddress.getByName(hostname);
	      Socket sock = new Socket(addr, port);
			
	      //Send header 
	      String path = "/ShopN/ProductService";
	      BufferedWriter  wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"));
	      // You can use "UTF8" for compatibility with the Microsoft virtual machine.
	      wr.write("POST " + path + " HTTP/1.0 \r\n");
	      wr.write("Host: sandbox.api.naver.com \r\n");
	      //wr.write("Host: api.naver.com \r\n");
	      //wr.write("Content-Length: " + xmldata.length() + "\r\n");
	      wr.write("Content-Length: " + ctglist.length() + "\r\n");
	      wr.write("Content-Type: text/xml; charset=\"UTF-8\"\r\n");
	      wr.write("SOAPAction: \"http://sandbox.api.naver.com/ShopN/ProductService\" \r\n");
	      //wr.write("SOAPAction: \"http://api.naver.com/Checkout/MallService2\" \r\n");
	      wr.write("\r\n");
				
	      //Send data
	      //wr.write(xmldata);
	      wr.write(ctglist);
	      wr.flush();
		 // InputStream test = new InputStream(sock.getInputStream(),"UTF-8");	


      // Response
      BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream(),"UTF-8"));
      String line="";
      String line_total="";
	   String tmp = "";
	   String tmp2 = "";
	   String newxml = "";      
      
      while((line = rd.readLine()) != null){
	      if (line.startsWith("<?xml")){
	      	line = line.replaceAll("&#xd;", " ");
	      	line_total= line_total + line ;
	      	System.out.println(line);	
	      }

  	  }
  	  
	    StringBuffer sf=new StringBuffer();
	   /* while((line = rd.readLine()) != null){
			if (line.startsWith("<?xml")){
				sf.append(line+"\n");        //스트링버퍼에 한줄씩 읽어 기록한다.
			}
		}*/
  	  
        //char[] bufferResult = new char[1048576];
       /* char[] bufferResult = new char[60000000];
        int index = -1;
        while((index = rd.read(bufferResult)) != -1) {

                sf.append(bufferResult, 135, index); //response헤더 제거  133에서 135변경 

        }*/

		//line_total = sf.toString().trim();        
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");	

	 line_total = line_total.replaceAll("n:", "");
	  //System.out.println(sf.toString().trim());	

	 System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%"+line_total+"%%%%%%%%%%%%%%%%%%%%%%%%%%");	 
	  
	//xml파싱2
	InputStream in = new ByteArrayInputStream(line_total.getBytes("UTF-8"));


						SAXBuilder builder = new SAXBuilder();
                        Document document = builder.build(in);

System.out.println("%%%%%%%%%%%%%%%%2222%%%%%%%%%%%%%%%%%");	

                        Element element = document.getRootElement();
						List envel_list = element.getChildren();
						List body_list = null;
						List body_list1 = null;
						List result_list = null;
						List result_list1 = null;
						List result_list2 = null;
						List result_list3 = null;
						List result_list4 = null;
						List result_list5 = null;
						List info_list = null;
						List contr_group_list = null;
						List contr_info_list = null;

                                                              


                        PreparedStatement pstmt = null;
                        ResultSet rs = null;
                        CallableStatement cStmt = null;
                        

                        //conn.setAutoCommit(false);

                        long UNIT_ID = 0;
                        long cnt = 0;
                        long interface_seq = 0;
                        long DEL_QTY = 0;
                        long ITEM_ID = 0;
                        String ITEM_NAME = null;
                        					
						System.out.println("GetOriginAreaListResponse.size:"+envel_list.size());
						Element envel_el = (Element) envel_list.get(0);
						body_list = envel_el.getChildren();

						
						//System.out.println("+++++++11+++++++++++el.getName() : " + envel_el.getChildren());
						
						//System.out.println("body_list.size:"+body_list.size());
						Element body_el = (Element) body_list.get(0);
						result_list = body_el.getChildren();
						
						System.out.println(body_list);
						System.out.println("###################################");
						
						System.out.println(result_list);
						System.out.println("###################################");
						
						
						Element body_el1 = (Element) result_list.get(5);
						result_list1 = body_el1.getChildren();

						

						System.out.println(body_el1);
						System.out.println("###################################");
						
						System.out.println(result_list1);
						System.out.println("###################################");		
						
						Element body_el2 = (Element) result_list1.get(0);
						result_list2 = body_el2.getChildren();
						System.out.println(body_el2);
						System.out.println("###################################");
						
						System.out.println(result_list2);
						System.out.println("###################################");							
																
						System.out.println("Code:"+body_el2.getChildText("Code"));
						//코드값 리턴								
						//System.out.println("body_list.size:"+body_list.size());
						
						//System.out.println("++++++++22++++++++++el.getName() : " + body_el.getChildren());
						/*for (int h = 0; h < result_list.size(); h++) {

							Element body_el1 = (Element) result_list.get(h);

							result_list1 = body_el1.getChildren("Category");
							//System.out.println("result_list1.size:"+result_list1.size());

							
							//System.out.println("++++++++33++++++++++el.getName() : " + body_el1.getChildren());
							for (int i = 0; i < result_list1.size(); i++) {

								Element body_el2 = (Element) result_list1.get(i);
								System.out.println("CategoryName:"+body_el2.getChildText("CategoryName"));
								System.out.println("Id:"+body_el2.getChildText("Id"));
								System.out.println("Name:"+body_el2.getChildText("Name"));
								System.out.println("Last:"+body_el2.getChildText("Last"));
//카테고리 저장
			                                StringBuffer setOrder = new StringBuffer();
			                                setOrder.append(" insert  \n");
			                                setOrder.append(" into mirus_navershopnctg(  \n");
			                                setOrder.append("     CategoryName, Id, Name, Last, insert_date ,modify_date \n");
			                                setOrder.append(" ) values (  \n");
			                                setOrder.append("     ?, ?, ?, ?, sysdate,null  \n");
			                                setOrder.append(" )  \n");
			
			                                pstmt = conn.prepareStatement(setOrder.toString());
			                                
			                                System.out.println("query:"+setOrder.toString());
			
			                                int insert_cnt = 0;
			
			                                try {
			                                        pstmt.clearParameters();
			
			                                        //파라미터셋팅
			                                        pstmt.setString(1, body_el2.getChildText("CategoryName") );                           // CategoryName
			                                        pstmt.setLong( 2, Long.parseLong(body_el2.getChildText("Id")));     // Id
			                                        pstmt.setString(3 , body_el2.getChildText("Name"));                                 //Name
			                                        pstmt.setString(4 , body_el2.getChildText("Last"));	// Last

			                                        pstmt.executeUpdate();
			
			                                        System.out.println("\n+ insert_cnt ["+i+"]\n");
			
			
			                                    } catch(Exception e) {
			                                    		response_type="FALSE";
			                                            e.printStackTrace();
			                                            conn.rollback();
			                                            break;
			                                    }
												conn.commit();
			
			                                    if(pstmt != null) {try{ pstmt.close(); } catch(Exception ex){ response_type="FALSE";}}
								
	
							
							}
						}
						               */ 	
				
                } catch(Exception e) {
                        System.out.println("run() : "+e.getMessage());
                        response_type="FALSE";
                } finally {
                }
                
                return response_type;
        }


        
    public String execute() {     
    	NaverShopnGetArea _CJILR = new NaverShopnGetArea();
        _CJILR.init();
		String return_type = _CJILR.run();
		_CJILR.distroy();
		return return_type;
    	
    }
        
    public static void main(String args[]) throws Exception{
    	
    	NaverShopnGetArea _CJILR = new NaverShopnGetArea();
        _CJILR.init();
		_CJILR.run();
		_CJILR.distroy();
		
		
		
	}
	
}




