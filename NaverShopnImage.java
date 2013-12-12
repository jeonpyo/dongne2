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

public class NaverShopnImage { 


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

        /*private String run2(String OrderID){
        	String response_type = "";
        	
	        try {
	          	SimpleCryptLib SimpleCryptLib = new SimpleCryptLib();      	
				Security.addProvider(new BouncyCastleProvider());
			
				String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43";
				String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";		
		
		
		
				String serviceName = "ProductService";
				String id = "eugink";
				String password = "asdf0101";
				String timestamp = null;
				String signature = null;
				String data = null;
		
				
				byte[] encryptKey = null;
				
				String encryptedData = null;
				String decryptedData = null;
				String hashedData = null;
				
				String operationName = "PlaceOrder";
				
				//timestamp create
				timestamp = SimpleCryptLib.getTimestamp();
			
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
		
		
				NaverShopnImage NaverShopnImage =new NaverShopnImage();
		
				String orderlist = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:mall=\"http://mall.checkout.platform.nhncorp.com/\" xmlns:base=\"http://base.checkout.platform.nhncorp.com/\">" +
								   "<soapenv:Header/>" +
								   "<soapenv:Body>" +				
									"<mall:PlaceOrderRequest>" +
								        "<base:RequestID>?</base:RequestID>" +					
										"<base:AccessCredentials>" +
											"<base:AccessLicense>"+ accessLicense +"</base:AccessLicense>" +
											"<base:Timestamp>"+ timestamp +"</base:Timestamp>" +
											"<base:Signature>"+ signature +"</base:Signature>" +
										"</base:AccessCredentials>" +
										"<base:DetailLevel>Full</base:DetailLevel>" +
										"<base:Version>2.0</base:Version>" +
										"<base:OrderID>"+ OrderID +"</base:OrderID>" +
									"</mall:PlaceOrderRequest>" +
								  "</soapenv:Body>" +
								  "</soapenv:Envelope>";		
								  
								  
								  
								  
								  
				  System.out.println("OrderID:**********"+OrderID+"**********");	  	
			      //Create socket
			      //String hostname = "sandbox.api.naver.com";
			      String hostname = "api.naver.com";
			      int port = 80;
			      InetAddress  addr = InetAddress.getByName(hostname);
			      Socket sock = new Socket(addr, port);
					
			      //Send header 
			      String path = "/Checkout/MallService2";
			      BufferedWriter  wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"));
			      // You can use "UTF8" for compatibility with the Microsoft virtual machine.
			      wr.write("POST " + path + " HTTP/1.0 \r\n");
			      wr.write("Host: api.naver.com \r\n");
			      wr.write("Content-Length: " + orderlist.length() + "\r\n");
			      wr.write("Content-Type: text/xml; charset=\"UTF-8\"\r\n");
			      wr.write("SOAPAction: \"http://api.naver.com/Checkout/MallService2\" \r\n");
			      wr.write("\r\n");
						
			      //Send data
		
			      wr.write(orderlist);
			      System.out.println("orderlist:"+orderlist);	
			      wr.flush();
		
		      	// Response
			      BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream(),"UTF-8"));
			      String line="";
			      String line_total="";
				   String tmp = "";
				   String tmp2 = "";
				   String newxml = "";      
			      
			      while((line = rd.readLine()) != null){
				      if (line.startsWith("<?xml")){
				      	line_total= line_total + line ;
				      }
			
			      
			  	  }
		
		
		
				line_total = line_total.replaceAll("n1:", "");
			  	System.out.println(line_total);	
		
			  
			  
				//xml파싱2
				InputStream in = new ByteArrayInputStream(line_total.getBytes("UTF-8"));
		
		
				SAXBuilder builder = new SAXBuilder();
                Document document = builder.build(in);



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
                
                System.out.println("DBconnect");
                //conn.setAutoCommit(false);

                long UNIT_ID = 0;
                long cnt = 0;
                long interface_seq = 0;
                long DEL_QTY = 0;
                long ITEM_ID = 0;
                String ITEM_NAME = null;
                					
				System.out.println("envel_list.size:"+envel_list.size());
				Element envel_el = (Element) envel_list.get(0);
				body_list = envel_el.getChildren();
				
				System.out.println("+++++++el.getName() : " + envel_el.getChildren());
				
				System.out.println("body_list.size:"+body_list.size());
				Element body_el = (Element) body_list.get(0);
				response_type = body_el.getChildText("ResponseType");
				System.out.println("response_type:"+response_type);
				String Error = body_el.getChildText("Error");
				System.out.println("Error:"+Error);
				String QuotaStatus = body_el.getChildText("QuotaStatus");
				System.out.println("QuotaStatus:"+QuotaStatus);

				

							
															
            } catch(Exception e) {
                    System.out.println("run() : "+e.getMessage());
            } finally {
            }
            System.out.println("FINAL response_type:"+response_type);
            return response_type;	
            
	            
    	}*/



        public String run() {
        	String  response_type="FALSE";
                try {
        SimpleCryptLib SimpleCryptLib = new SimpleCryptLib();      	
		Security.addProvider(new BouncyCastleProvider());

		String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43";
		String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";		



		String serviceName = "ImageService";  // 서비스명
		String id = "eugink";  
		String password = "asdf0101";
		String timestamp = null;
		String signature = null;
		String data = null;

		
		byte[] encryptKey = null;
		
		String encryptedData = null;
		String decryptedData = null;
		String hashedData = null;
		
		String operationName = "UploadImage";
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


		NaverShopnImage NaverShopnImage =new NaverShopnImage();
		System.out.println("NaverShopnImage.getYesterday():"+NaverShopnImage.getYesterday());
		System.out.println("NaverShopnImage.getYesterday():"+NaverShopnImage.getToday());



		
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
		

		/*String xmldata = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:mall=\"http://mall.checkout.platform.nhncorp.com/\" xmlns:base=\"http://base.checkout.platform.nhncorp.com/\">" +
						   "<soapenv:Header/>" +
						   "<soapenv:Body>" +
						      "<mall:GetOrderInfoRequest>" +
						         "<base:RequestID>?</base:RequestID>" +
						         "<base:AccessCredentials>" +
						            "<base:AccessLicense>"+ accessLicense +"</base:AccessLicense>" +
						            "<base:Timestamp>"+ timestamp +"</base:Timestamp>" +
						            "<base:Signature>"+ signature +"</base:Signature>" +
						         "</base:AccessCredentials>" +
						         "<base:DetailLevel>Full</base:DetailLevel>" + //<!--해당 엘리먼트의 value는 Full, Compact 앞글자모두 대문자입니다)
						         "<base:Version>2.0</base:Version>" +
						         "<OrderID>"+ orderID +"</OrderID>" +
						      "</mall:GetOrderInfoRequest>" +
						   "</soapenv:Body>" +
						"</soapenv:Envelope>";*/

		System.out.println("NaverShopnImage.getYesterday():"+NaverShopnImage.getYesterday());
		System.out.println("NaverShopnImage.getToday():"+NaverShopnImage.getToday());	
		String imglist =   "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:shop=\"http://shopn.platform.nhncorp.com/\">" +
							   "<soap:Header/>" +
							   "<soap:Body>" +
							      "<shop:UploadImageRequest>" +
							         "<!--Optional:-->" +
							         "<shop:RequestID>njoyny2</shop:RequestID>" +
							         "<shop:AccessCredentials>" +
							            "<shop:AccessLicense>" +accessLicense +"</shop:AccessLicense>" +
							            "<shop:Timestamp>" +timestamp +"</shop:Timestamp>" +
							            "<shop:Signature>" +signature +"</shop:Signature>" +
							         "</shop:AccessCredentials>" +
							         "<shop:Version>2.0</shop:Version>" +
							         "<SellerId>njoyny2</SellerId>" +
							         "<ImageURLList>" +
							            "<!--Zero or more repetitions:-->" +
							            "<shop:URL>http://img.buynjoy.com/images_2011_1/b2c/thumb/12/X/X_van5502.jpg</shop:URL>" +
							         "</ImageURLList>" +
							      "</shop:UploadImageRequest>" +
							   "</soap:Body>" +
							"</soap:Envelope>" ;					  

		
		
				
			
			
	      //Create socket
	      String hostname = "sandbox.api.naver.com";
	      //String hostname = "api.naver.com";
	      int port = 80;
	      InetAddress  addr = InetAddress.getByName(hostname);
	      Socket sock = new Socket(addr, port);
			
	      //Send header 
	      String path = "/ShopN/ImageService";
	      BufferedWriter  wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(),"UTF-8"));
	      // You can use "UTF8" for compatibility with the Microsoft virtual machine.
	      wr.write("POST " + path + " HTTP/1.0 \r\n");
	      wr.write("Host: sandbox.api.naver.com \r\n");
	      //wr.write("Host: api.naver.com \r\n");
	      //wr.write("Content-Length: " + xmldata.length() + "\r\n");
	      wr.write("Content-Length: " + imglist.length() + "\r\n");
	      wr.write("Content-Type: text/xml; charset=\"UTF-8\"\r\n");
	      wr.write("SOAPAction: \"http://sandbox.api.naver.com/ShopN/ImageService\" \r\n");
	      //wr.write("SOAPAction: \"http://api.naver.com/Checkout/MallService2\" \r\n");
	      wr.write("\r\n");
				
	      //Send data
	      //wr.write(xmldata);
	      wr.write(imglist);
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
                        					
						System.out.println("CategoryId.size:"+envel_list.size());
						Element envel_el = (Element) envel_list.get(0);
						body_list = envel_el.getChildren();
						
						//System.out.println("+++++++11+++++++++++el.getName() : " + envel_el.getChildren());
						
						//System.out.println("body_list.size:"+body_list.size());
						Element body_el = (Element) body_list.get(0);
						result_list = body_el.getChildren("ImageList");
						
						//System.out.println("++++++++22++++++++++el.getName() : " + body_el.getChildren());
						for (int h = 0; h < result_list.size(); h++) {

							Element body_el1 = (Element) result_list.get(h);

							result_list1 = body_el1.getChildren("Image");
							//System.out.println("result_list1.size:"+result_list1.size());

							
							//System.out.println("++++++++33++++++++++el.getName() : " + body_el1.getChildren());
							for (int i = 0; i < result_list1.size(); i++) {

								Element body_el2 = (Element) result_list1.get(i);
								System.out.println("Source:"+body_el2.getChildText("Source"));
								System.out.println("URL:"+body_el2.getChildText("URL"));
/*
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


*/

							
	/*
								Element body_el3 = (Element) result_list1.get(i);
								result_list3 = body_el3.getChildren("OrderProductList");
	
								
								
								Element body_el4 = (Element) result_list1.get(i);
								result_list4 = body_el4.getChildren("Shipping");
	
	*/
				/*				
								for (int j = 0; j < result_list2.size(); j++) {
									
									Element body_el5 = (Element) result_list2.get(j);//주문
									Element body_el7 = (Element) result_list4.get(j);//배송
									Element body_el8 = (Element) result_list3.get(j);
									result_list5 = body_el8.getChildren("OrderProduct");//상품
									//result_list5  = (Element) result_list3.get(j);//상품
									
									System.out.println("$$55555$$");
									if(body_el5.getChildText("OrderStatusCode").equals("OD0002")){//주문상태 결제완료시에 주문진행
	
									pstmt = conn.prepareStatement(selectChk.toString());
									pstmt.setString(1, body_el5.getChildText("OrderID"));
	
						            rs = pstmt.executeQuery();
						            if(rs.next()) {
						                cnt = rs.getLong("cnt");
						            }
	
									System.out.println("기존데이터 확인 cnt:"+cnt);
									if(rs!=null) {try{rs.close();} catch(Exception e){ response_type="FALSE";}}
								    if(pstmt!=null) {try{pstmt.close();} catch(Exception e){ response_type="FALSE";}}
									
									System.out.println("result_list5.size():"+result_list5.size());
	
	
									for (int k = 0; k < result_list5.size(); k++) {
										
										Element body_el9 = (Element) result_list5.get(k);
	
										ITEM_NAME = body_el9.getChildText("ProductName");
										DEL_QTY = Long.parseLong(body_el9.getChildText("Quantity"));
										           									
										pstmt = conn.prepareStatement(getInterfacedata);									
										System.out.println("body_el9.getChildText:"+body_el9.getChildText("ProductID"));
										System.out.println("body_el9.getChildText:"+body_el9.getChildText("ProductOption"));
										pstmt.setLong(1, Long.parseLong(body_el9.getChildText("ProductID")));
										pstmt.setString(2, body_el9.getChildText("ProductOption"));
										pstmt.setLong(3, Long.parseLong(body_el9.getChildText("ProductID")));
		
							            rs = pstmt.executeQuery();
							            if(rs.next()) {
							                UNIT_ID = rs.getLong("UNIT_ID");
							                ITEM_ID = rs.getLong("ITEM_ID");
							                //DEL_QTY = rs.getLong("DEL_QTY");
							            }
										System.out.println("UNIT_ID:"+UNIT_ID);
										System.out.println("ITEM_ID:"+ITEM_ID);
										System.out.println("ITEM_NAME:"+ITEM_NAME);
										if(rs!=null) {try{rs.close();} catch(Exception e){ response_type="FALSE";}}
									    if(pstmt!=null) {try{pstmt.close();} catch(Exception e){ response_type="FALSE";}}
									}//배열로 받지만 1개만 가져온다.
	
	
									System.out.println("1111시퀀스아이디 가져옴333331");
			                        String getInterfaceSeq = " select ktc_njoyny_hmall_if_seq.nextval interfaceSeq from dual ";
		
			                        pstmt = conn.prepareStatement(getInterfaceSeq);
						            rs = pstmt.executeQuery();
						            if(rs.next()) {
						                interface_seq = rs.getLong("interfaceSeq");
						            }
		
			                        System.out.println("+ interface_seq ["+interface_seq+"]");
	
			                        if(rs!=null) {try{rs.close();} catch(Exception e){ response_type="FALSE";}}
			                        if(pstmt!=null) {try{pstmt.close();} catch(Exception e){ response_type="FALSE";}}
	
	
									
		                            if(cnt == 0) { // 기존의 주문건이 없는것만 넣는다..
		                            	System.out.println("body_el5.getChildText:"+body_el5.getChildText("OrderID"));
		                            	
		                            	if(NaverShopnImage.run2(body_el5.getChildText("OrderID")).equals("SUCCESS")){//네이버체크아웃주문접수 성공
		                            		
			                                StringBuffer setOrder = new StringBuffer();
			                                setOrder.append(" insert  \n");
			                                setOrder.append(" into ktc_njoyny_hmall_if(  \n");
			                                setOrder.append("     h_order_no, seq, order_date, recv_date, item_id,   \n");
			                                setOrder.append("     unit_id, item_name, price, qty, orderer_name,   \n");
			                                setOrder.append("     orderer_phone, orderer_cell_phone, orderer_eng_name, orderer_resi_no, orderer_email,  \n");
			                                setOrder.append("     msg_for_mall, del_plan_date, receiver_name, receiver_phone, receiver_cell_phone,   \n");
			                                setOrder.append("     receiver_zip_code, receiver_addr1, receiver_addr2, msg_for_outbnd, interface_date, \n");
			                                setOrder.append("     interface_seq, sysregdt, coven_id \n");
			                                setOrder.append(" ) values (  \n");
			                                setOrder.append("     'naver_'||?, ktc_njoyny_hmall_if_seq.nextval, to_date(?,'yyyy/mm/dd hh24:mi:ss'), sysdate, ?,  \n");
			                                setOrder.append("     ?, ?, ?, ?, ?,  \n");
			                                setOrder.append("     ?, ?, ?, ?, ?,  \n");
			                                setOrder.append("     ?, sysdate+5, ?, ?, ?,  \n");
			                                setOrder.append("     ?, ?, ?, ?, sysdate,  \n");
			                                setOrder.append("     ?, sysdate, ? \n");
			                                setOrder.append(" )  \n");
			
			                                pstmt = conn.prepareStatement(setOrder.toString());
			
			                                int insert_cnt = 0;
			
			                                try {
			                                        pstmt.clearParameters();
			
			                                        //파라미터셋팅
			                                        pstmt.setLong(1, Long.parseLong(body_el5.getChildText("OrderID")) );                           // h_order_no
			                                        System.out.println(body_el5.getChildText("OrderID"));
			                                        pstmt.setString(2 , body_el5.getChildText("OrderDateTime").substring(0,10));                                 //order_date
			                                        System.out.println(body_el5.getChildText("OrderDateTime").substring(0,10));
			                                        pstmt.setLong(3 , ITEM_ID);     // item_id
			                                       	System.out.println(ITEM_ID);
			                                       	pstmt.setLong(4 , UNIT_ID);	// unit_id
			                                        System.out.println(UNIT_ID);
										
			                                        pstmt.setString(5 , ITEM_NAME);     // ITEM_NAME
			                                        System.out.println(ITEM_NAME);
			                                        pstmt.setLong(6 , Long.parseLong(body_el5.getChildText("TotalProductAmount")));       //price ->  0.95  네이버체크아웃 결제금액 +네이버체크아웃 적립금 +네이버체크아웃 쿠폰
			                                        System.out.println(Long.parseLong(body_el5.getChildText("TotalProductAmount")));
			                                        pstmt.setLong(7 , DEL_QTY);    //qty
			                                        System.out.println(DEL_QTY);
		
			                                        pstmt.setString(8 , new String(SimpleCryptLib.decrypt(encryptKey, body_el5.getChildText("OrdererName")), "UTF-8"));                             // orderer_name
			                                        System.out.println(new String(SimpleCryptLib.decrypt(encryptKey, body_el5.getChildText("OrdererName")), "UTF-8"));
			                                        pstmt.setString(9 , NaverShopnImage.getTel(new String(SimpleCryptLib.decrypt(encryptKey,body_el5.getChildText("OrdererTel")), "UTF-8")));                               //orderer_phone
			                                        System.out.println(NaverShopnImage.getTel(new String(SimpleCryptLib.decrypt(encryptKey, body_el5.getChildText("OrdererTel")), "UTF-8")));
			                                        pstmt.setString(10, NaverShopnImage.getTel(new String(SimpleCryptLib.decrypt(encryptKey,body_el5.getChildText("OrdererTel")), "UTF-8")));               // orderer_cell_phone
			                                        pstmt.setString(11, null);                      // orderer_eng_name
			                                        pstmt.setString(12, null);                      // orderer_resi_no
			                                        pstmt.setString(13, null);                      // orderer_email
			                                        pstmt.setString(14, body_el7.getChildText("ShippingMessage"));                      // msg_for_mall
			                                        pstmt.setString(15, new String(SimpleCryptLib.decrypt(encryptKey,body_el7.getChildText("Recipient")), "UTF-8")); // receiver_name
			                                        System.out.println(new String(SimpleCryptLib.decrypt(encryptKey, body_el7.getChildText("Recipient")), "UTF-8"));
		
			                                        pstmt.setString(16, NaverShopnImage.getTel(new String(SimpleCryptLib.decrypt(encryptKey,body_el7.getChildText("RecipientTel1")), "UTF-8")));                    // receiver_phone
			                                        System.out.println(NaverShopnImage.getTel(new String(SimpleCryptLib.decrypt(encryptKey, body_el7.getChildText("RecipientTel1")), "UTF-8")));
		
			                                        pstmt.setString(17, NaverShopnImage.getTel(new String(SimpleCryptLib.decrypt(encryptKey,body_el7.getChildText("RecipientTel1")), "UTF-8")));                    // receiver_cell_phone
			                                        System.out.println(NaverShopnImage.getTel(new String(SimpleCryptLib.decrypt(encryptKey, body_el7.getChildText("RecipientTel1")), "UTF-8")));
		
			                                        pstmt.setString(18, body_el7.getChildText("ZipCode"));                              //receiver_zip_code
			                                        pstmt.setString(19, new String(SimpleCryptLib.decrypt(encryptKey,body_el7.getChildText("ShippingAddress1")), "UTF-8"));                           //receiver_addr1
			                                        System.out.println(new String(SimpleCryptLib.decrypt(encryptKey, body_el7.getChildText("ShippingAddress1")), "UTF-8"));
		
			                                        pstmt.setString(20, new String(SimpleCryptLib.decrypt(encryptKey,body_el7.getChildText("ShippingAddress2")), "UTF-8"));                            //receiver_addr2
			                                        System.out.println(new String(SimpleCryptLib.decrypt(encryptKey, body_el7.getChildText("ShippingAddress2")), "UTF-8"));
		
			                                        pstmt.setString(21, body_el7.getChildText("ShippingMessage"));                                  // msg_for_outbnd
			                                        System.out.println(body_el7.getChildText("ShippingMessage"));
		
			                                        pstmt.setLong(22, interface_seq);                                       // interface_seq
			                                        pstmt.setString(23, "26050");                                                   // coven_id
			
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
												System.out.println("인서트완료 프로시져 호출");
												System.out.println("interface_seq:"+interface_seq);
						                        // 주문 프로시져....
						                        cStmt = conn.prepareCall("{ call CREAT_NJOYNY_HMALL_ORDER(?,?,?) }");
									            cStmt.setLong(1, interface_seq);
									            cStmt.registerOutParameter(2, Types.VARCHAR);
									            cStmt.registerOutParameter(3, Types.VARCHAR);
									            cStmt.executeUpdate();
									            String r_code = cStmt.getString(2);
									            String r_msg = cStmt.getString(3);
												System.out.println("++++++++++++++++r_code : " + r_code);
												System.out.println("++++++++++++++++r_msg : " + r_msg);
						                        conn.commit();
												System.out.println("인서트완료 프로시져 호출완료");	
					                        if(cStmt != null){ try{ cStmt.close(); } catch(Exception e){ response_type="FALSE";}}
					                        conn.setAutoCommit(true);
					                    	response_type="TRUE";
					                    	}else{
					                    		System.out.println("체크아웃 주문접수 실패" );
					                    	}
		                                }else{
		                                        System.out.println("이미 주문건 있음=.." );
		                                }
		                                	cnt = 0; // 초기화..
	
	
	
										
									}
								}
*/
														
	
							
							}
						}
						                	
				
                } catch(Exception e) {
                        System.out.println("run() : "+e.getMessage());
                        response_type="FALSE";
                } finally {
                }
                
                return response_type;
        }


        
    public String execute() {     
    	NaverShopnImage _CJILR = new NaverShopnImage();
        _CJILR.init();
		String return_type = _CJILR.run();
		_CJILR.distroy();
		return return_type;
    	
    }
        
    public static void main(String args[]) throws Exception{
    	
    	NaverShopnImage _CJILR = new NaverShopnImage();
        _CJILR.init();
		_CJILR.run();
		_CJILR.distroy();
		
		
		
	}
	
}




