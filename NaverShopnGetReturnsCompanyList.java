import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.jdom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.nhncorp.psinfra.toolkit.SimpleCryptLib;


public class NaverShopnGetReturnsCompanyList<Hashmap> {
	
	/*
	 * SANDBOX: http://sandbox.api.naver.com/ShopN/[SellerService41] 
	 * PRODUCTION: http://ec.api.naver.com/ShopN/[SellerService41] 
	 */
	public static Connection conn = null;
	
	
	private String serviceName;
	private String operationName;

   

	private String ORADriver = "";
    private String ORAURL = "";
    private String ORAUser = "";
    private String ORAPass = "";
    
    //네이버 암호화 전역 변수 
    private String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43"; 
	private String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";
	
	
	private String id = "eugink";  
	private String password = "asdf0101";
	
	
	private String timestamp;
	private String signature;
	private String data;

	
	private byte[] encryptKey;
	
	private String encryptedData;
	private String decryptedData;
	private String hashedData;
   

	public void init() {
    	try {
        	
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
    
    public void destroy(){
    	try{
    		NaverShopnGetReturnsCompanyList.conn = null;
    	}catch(Exception e){
    		System.out.println("destroy() : " + e.getMessage());
    	}
    
    }
    
    
    
    public void encryptInit(String serviceName, String operationName) throws Exception{
    	this.setServiceName(serviceName);
    	this.setOperationName(operationName);
    	Security.addProvider(new BouncyCastleProvider());
    	
    	this.setTimestamp(SimpleCryptLib.getTimestamp());
    	
    	this.setData(this.getTimestamp() + this.getServiceName() + this.getOperationName());
    	
    	//generateSign
    	this.setSignature(SimpleCryptLib.generateSign(this.getData(), this.getSecretKey()));
    	
    	
    	//generateKey
    	this.setEncryptKey(SimpleCryptLib.generateKey(this.getTimestamp(), this.getSecretKey()));
    	
    	//encrypt
    	this.setEncryptedData(SimpleCryptLib.encrypt(this.getEncryptKey(), this.getPassword().getBytes("UTF-8")));
    	
    	this.setDecryptedData(new String(SimpleCryptLib.decrypt(this.getEncryptKey(), this.getEncryptedData()), "UTF-8"));
    	
    	this.setHashedData(SimpleCryptLib.sha256(this.getPassword()));
    	
    	System.out.println("accessLicense : [" + this.getAccessLicense() + "]");
		System.out.println("secretKey : [" + this.getSecretKey() + "]");
		System.out.println("serviceName : [" + this.getServiceName() + "]");
		System.out.println("operationName : [" + this.getOperationName() + "]");
		System.out.println("timestamp : [" + this.getTimestamp() + "]");
		System.out.println("signature : [" + this.getSignature() + "]");
		System.out.println("encryptKey : [" + new String(Hex.encode(this.getEncryptKey())) + "]");
		System.out.println("encryptedData : [" + this.getEncryptedData() + "]");
		System.out.println("decryptedData : [" + this.getDecryptedData() + "]");
		System.out.println("sha256Data : [" + this.getHashedData() + "]");
		
		
		System.out.println("[==============================================================]");
		System.out.println("[==============================================================]");
		System.out.println("[==================encryptInit()===============================]");
		System.out.println("[==================encryptInit()===============================]");
		System.out.println("[==============================================================]");
		System.out.println("[==============================================================]");
    	
    }
    
    public String getToday() {
        String result = "";
        java.util.Date today = new java.util.Date();
        long currDate = today.getTime();
        //long addDate = currDate - (24 * (60 * (60 * 1000)));
        long addDate = currDate - (9 * (60 * (60 * 1000))); //한국시간은 -9 시간 해야 된다.
        today.setTime(addDate);
        SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        result = dateForm.format(today);

        //result = "2012-01-02T00:00:00+09:00";
        result = "2012-01-02T09:47:50.554Z";

        return result;
    }


    public String getYesterday() {
        String result = "";
        java.util.Date today = new java.util.Date();
        long currDate = today.getTime();
        //long addDate = currDate - (9 * (60 * (61 * 1000))) ; //10분전 한국시간은 -9 시간 해야 된다.
        long addDate = currDate - (33 * (60 * (60 * 1000))) + 1 ; //10분전 한국시간은 -9 시간 해야 된다. 하루전
        //long addDate = currDate;
        today.setTime(addDate);
        SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        result = dateForm.format(today);
        
        //Calendar cal = Calendar.getInstance();        
        //cal.set(2012, 0, 1, 0, 0, 0);
        
        //result = "2012-01-01T00:00:00+09:00";
        result =   "2012-01-01T09:47:50.554Z";
        

        return result;
}
    
    public String transDate(String transDate) {
    	String result = null;
    	Date result1 = null;
        java.util.Date today = new java.util.Date();
        SimpleDateFormat dateForm1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00");
        try {
        	result1 = dateForm1.parse(transDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        result = dateForm.format(result1);
        

        //result =   "2012-01-01T09:47:50.554Z";
        

        return result;
}    
    
    

	public String soapMsgHeader(){
		String soapHeader = "<?xml version='1.0' encoding='UTF-8'?> \n";
		soapHeader += "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:shop=\"http://shopn.platform.nhncorp.com/\"> \n";
		soapHeader += "     <soap:Header/> \n";
		soapHeader += "         <soap:Body> \n";	
		
		return soapHeader.trim();
		
	}
	
	public String soapBody(){
		String soapBody = "";
	
		
		soapBody += "<shop:GetReturnsCompanyListRequest>\n";
		soapBody += "    <shop:RequestID>" + this.getId() + "</shop:RequestID> \n"; 		         
		soapBody += "    <shop:AccessCredentials>\n";
		soapBody += "       <shop:AccessLicense>" + this.getAccessLicense() + "</shop:AccessLicense> \n";  
		soapBody += "       <shop:Timestamp>" + this.getTimestamp() +"</shop:Timestamp> \n";  
		soapBody += "       <shop:Signature>" + this.getSignature() +"</shop:Signature> \n";  
		soapBody += "    </shop:AccessCredentials> \n"; 
		soapBody += "    <shop:Version>4.1</shop:Version> \n"; 
		soapBody += "    <SellerId>njoyny2</SellerId> \n"; 
		soapBody += "    <ReturnsCompanyName><![CDATA[]]></ReturnsCompanyName> \n"; 
		soapBody += "</shop:GetReturnsCompanyListRequest>\n"; 
		
		return soapBody.trim();
	
	}
	
	/*
	public String soapBody_orderInfo(String productOrderNo){
		String soapBody = "";
		
		soapBody += "<sel:GetProductOrderInfoListRequest> \n";
		soapBody += "    <sel:AccessCredentials>\n";
		soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
		soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
		soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
		soapBody += "    </sel:AccessCredentials> \n"; 
		soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
		soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
		soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
		soapBody += "    <sel:ProductOrderIDList>" + productOrderNo + "</sel:ProductOrderIDList> \n";		
		soapBody += "</sel:GetProductOrderInfoListRequest> \n"; 
		
		return soapBody.trim();
	
	}
	*/
	
	public String soapMsgFooter(){
		String soapFooter = "        </soap:Body> \n";
		soapFooter += "</soap:Envelope>";		                         
		
		return soapFooter.trim();
		
	}
	
	public String soapMessage(String soapBodyMsg){
		String soapMessage = "";
		
		soapMessage = this.soapMsgHeader()
					+ soapBodyMsg
					+ this.soapMsgFooter();
		System.out.println("soapMessage:"+soapMessage);
		return soapMessage;
	
	}
	
	
	public String soapSend(String isTest, String soapMessage) throws Exception{
		String retResult = "";
		String SOAPUrl = "";
		if(!"test".equals(isTest.toLowerCase())){
			SOAPUrl = "http://ec.api.naver.com/ShopN/" + this.getServiceName();									
		}else{
			SOAPUrl = "http://sandbox.api.naver.com/ShopN/" + this.getServiceName();						
		}
		
		System.out.println("SendUrl ===== " + SOAPUrl);
		
		URL url = new URL(SOAPUrl);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;
        
        byte[] byteMessage = soapMessage.getBytes();
        
     // Set the appropriate HTTP parameters.
        httpConn.setRequestProperty( "Content-Length", String.valueOf(byteMessage.length));
        httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction",SOAPUrl);
        httpConn.setRequestMethod( "POST" );
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);

        // Everything's set up; send the XML that was read in to b.
        OutputStream out = httpConn.getOutputStream();
        out.write( byteMessage );    
        out.close();
        
        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream(), "UTF-8");
        BufferedReader in = new BufferedReader(isr);

        String inputLine = "";

        while ((inputLine = in.readLine()) != null){
        	retResult += inputLine;       	
        	
        }               

        in.close();
        
        System.out.println("[resultMessage] \n");
        System.out.println("[" + retResult + "]");
		
		return retResult;
	}
	
	
	public String[] orderProductListParser(String soapMessage) throws Exception{
		String[] productOrderId = null; //productId 담을 배열 
		
		
		// XML Document 객체 생성
        InputSource is = new InputSource(new StringReader(soapMessage));
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is); 
        
         
        // xpath 생성
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        // NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
        NodeList cols = (NodeList)xpath.evaluate("//ChangedProductOrderInfoList/ProductOrderID", document, XPathConstants.NODESET);
        
        productOrderId = new String[cols.getLength()];
        
        System.out.println("cols.getLength()=============" + cols.getLength());
        
        for( int idx=0; idx<cols.getLength(); idx++ ){
            System.out.println(cols.item(idx).getTextContent());
            productOrderId[idx] = cols.item(idx).getTextContent();
        }
        
        return productOrderId;
    }
	

		
    public HashMap getDbUniIdItemId(){

    	//Long[][] unitIdAndItemId = new Long[2];
    	StringBuffer sb = new StringBuffer();
    	
    	PreparedStatement pstmt = null;
        ResultSet rs = null;  
		HashMap unitList = new HashMap();        

		sb.append("					 select aa.h_order_no, \n");
		sb.append("					   c.outbnd_date, \n");
		sb.append("					   c.out_bnd_type_code, \n");
		sb.append("					   d.ship_no, \n");
		sb.append("             	   (select code_hangul_name from ktc_code where code_id = d.deliver_service_code) shipcode           \n");
		sb.append("					 from ktc_njoyny_hmall_if aa, \n");
		sb.append("					   ktc_orderitem a, \n");
		sb.append("					   ktc_reqoutbnd b, \n");
		sb.append("					   ktc_shipping_list c, \n");
		sb.append("					   ktc_shipping d \n");
		sb.append("					 where a.order_no=aa.order_no \n");
		sb.append("					   and a.order_no = b.order_no \n");
		sb.append("					   and a.order_seq = b.order_seq \n");
		sb.append("					   and a.order_item_seq = b.order_item_seq \n");
		sb.append("					   and a.order_item_detail_seq = b.order_item_detail_seq \n");
		sb.append("					   and b.order_no=c.order_no(+) \n");
		sb.append("					   and b.order_seq=c.order_seq(+) \n");
		sb.append("					   and b.order_item_seq=c.order_item_seq(+) \n");
		sb.append("					   and b.order_item_detail_seq=c.order_item_detail_seq(+) \n");
		sb.append("					   and b.outbound_seq=c.outbound_seq(+) \n");
		sb.append("					   and c.ship_seq=d.ship_seq(+) \n");
		//sb.append("					   and aa.coven_id = 27346 \n");		
		//sb.append("					   and b.finish_date > sysdate - 1/12 \n");
		sb.append("					   and b.finish_date > sysdate - 1 \n");		
		sb.append("					 group by aa.H_ORDER_NO, b.finish_date, c.out_bnd_type_code, d.ship_no ,d.deliver_service_code ,c.outbnd_date \n");
		
        System.out.println("sb.toString():"+sb.toString());
        try{
        	pstmt = conn.prepareStatement(sb.toString());									
    		

    		int list_cnt = 0;
            rs = pstmt.executeQuery();
            while(rs.next()) {
				HashMap rowData = new HashMap();
				rowData.put("h_order_no", rs.getString("h_order_no"));
				rowData.put("outbnd_date", rs.getString("outbnd_date"));
				rowData.put("out_bnd_type_code", rs.getString("out_bnd_type_code"));
				rowData.put("ship_no", rs.getString("ship_no"));
				rowData.put("shipcode", rs.getString("shipcode"));
				unitList.put(Integer.toString(list_cnt), rowData);
				System.out.println("list_cnt:"+list_cnt);
				list_cnt = list_cnt + 1;


            }    
            
            // 해시맵 받아서 메인에서 전송로직 실행
        }catch(SQLException e){
        	System.out.println("[getDbUniIdItemId SQLException] === " + e.getMessage());
        }finally{
        	if(rs!=null) {try{rs.close();} catch(Exception e){ System.out.println(e.getStackTrace());}}
    	    if(pstmt!=null) {try{pstmt.close();} catch(Exception e){System.out.println(e.getStackTrace());}}
        	
        }
    	
    	
    	return unitList;
    	
    }		
		
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		NaverShopnGetReturnsCompanyList nsg = new NaverShopnGetReturnsCompanyList();
		
		//DB init
		nsg.init();
		

		

		

					
					//encrypt init
					nsg.encryptInit("DeliveryInfoService", "GetReturnsCompanyList");
					System.out.println("soapMessage ============ " );
					System.out.println(nsg.soapBody());
					System.out.println("=============soapMessageEnd" );
					//GetChangedProductOrderList 을 이용하여 productOrderNo 를 생성
					
					nsg.soapSend("test", nsg.soapMessage(nsg.soapBody()));
					System.out.println("=============soapSend   End" );
					//nsg.soapSend("test", nsg.soapMessage(nsg.soapBody(nsg.getDbUniIdItemId(),cnt )));
	

		
		
		//DB Close
		nsg.destroy();

	}
	
	
	
	/**
	 * Set Get Method Logic 없음.
	 * @return
	 */	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public byte[] getEncryptKey() {
		return encryptKey;
	}

	public void setEncryptKey(byte[] encryptKey) {
		this.encryptKey = encryptKey;
	}

	public String getEncryptedData() {
		return encryptedData;
	}

	public void setEncryptedData(String encryptedData) {
		this.encryptedData = encryptedData;
	}

	public String getDecryptedData() {
		return decryptedData;
	}

	public void setDecryptedData(String decryptedData) {
		this.decryptedData = decryptedData;
	}

	public String getHashedData() {
		return hashedData;
	}

	public void setHashedData(String hashedData) {
		this.hashedData = hashedData;
	}

	public String getAccessLicense() {
		return accessLicense;
	}

	public void setAccessLicense(String accessLicense) {
		this.accessLicense = accessLicense;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	
		

}


