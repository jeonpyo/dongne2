import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.nhncorp.psinfra.toolkit.SimpleCryptLib;


public class NaverShopnCancelReturn {
	
	/*
	 * SANDBOX: http://sandbox.api.naver.com/ShopN/[SellerService41] 
	 * PRODUCTION: http://ec.api.naver.com/ShopN/[SellerService41] 
	 */
	public static Connection conn = null;
	
	private String interfaceSeqNo;
	
	
	private String serviceName;
	private String operationName;
	
	private String isTest;

   

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
    		NaverShopnCancelReturn.conn = null;
    	}catch(Exception e){
    		System.out.println("destroy() : " + e.getMessage());
    	}
    
    }
    
    /**
     * intefaceSeq를 가져온다.
     */
    public void getMethodInterfaceSeqNo(){
    	Long interfaceSeqNo = 0l;
    	StringBuffer sb = new StringBuffer();
    	
    	PreparedStatement pstmt = null;
        ResultSet rs = null;       
        
        sb.append("select ktc_njoyny_hmall_if_seq.nextval interfaceSeq from dual");
       
    	try{
    		pstmt = conn.prepareStatement(sb.toString());
    		rs = pstmt.executeQuery();
    		
    		
            if(rs.next()) {
            	interfaceSeqNo = rs.getLong("interfaceSeq");
            }
    		
    	}catch(SQLException e){
    		System.out.println("[getMethodInterfaceSeqNo SQLException] ====" + e.getMessage());
    	}finally{
    		if(rs!=null) {try{rs.close();} catch(Exception e){ System.out.println(e.getStackTrace());}}
    	    if(pstmt!=null) {try{pstmt.close();} catch(Exception e){System.out.println(e.getStackTrace());}}    		
    	}
    	
    	this.setInterfaceSeqNo(interfaceSeqNo.toString());

    }
    
    /**
     * DB 중복 입력 방지를 위해 prdOrderNo 로 체크
     * @param prdOrderNo
     * @return int
     */
    public int chkDuplicateNaverOrderNo(String prdOrderNo){
    	int ret = 0;
    	StringBuffer sb = new StringBuffer();
    	
    	PreparedStatement pstmt = null;
        ResultSet rs = null;       
        
        sb.append("select count(*) cnt \n");
        sb.append("  from ktc_njoyny_hmall_if \n");
        sb.append(" where h_order_no = ? and coven_id = 27346 \n");
        
        try{
        	pstmt = conn.prepareStatement(sb.toString());
    		pstmt.setString(1,  prdOrderNo.trim());

            rs = pstmt.executeQuery();
            if(rs.next()) {
            	ret = rs.getInt("cnt");
            }
        	
        }catch(SQLException e){
        	System.out.println("[chkDuplicateNaverOrderNo SQLException] === " + e.getMessage());
        	
        }finally{
        	if(rs!=null) {try{rs.close();} catch(Exception e){ System.out.println(e.getStackTrace());}}
    	    if(pstmt!=null) {try{pstmt.close();} catch(Exception e){System.out.println(e.getStackTrace());}}        	
        }
    	
	    return ret;
    	
    }
    
    /**
     * 옵션이름으로 unitId 가져오기 
     * @param itemId
     * @param unitOptionName
     * @return Long[0]:unitId Long[1]:Itemid
     */
    public Long[] getDbUniIdItemId(String itemId, String unitOptionName){
    	Long[] unitIdAndItemId = new Long[2];
    	StringBuffer sb = new StringBuffer();
    	
    	PreparedStatement pstmt = null;
        ResultSet rs = null;  
        
        sb.append("select UNIT_ID,ITEM_ID                            \n");
        sb.append("  from (                                          \n");
        sb.append("      select item_id,                             \n");
        sb.append("             UNIT_name,                           \n");
        sb.append("             decode((                             \n");
        sb.append("                     select count(*) cnt          \n");
        sb.append("                       from ktc_unit              \n");
        sb.append("                      where item_id = ?           \n");
        sb.append("                      group by item_id), 1, UNIT_id, decode(trim(UNIT_NAME) ,trim(?), UNIT_id, '')) UNIT_id  \n");
        sb.append("        from ktc_unit                             \n");
        sb.append("       where item_id = ? )                        \n");
        sb.append(" where unit_id is not null                        \n");
        
        try{
        	pstmt = conn.prepareStatement(sb.toString());									
    		
    		pstmt.setLong(1, Long.parseLong(itemId));
    		pstmt.setString(2, unitOptionName);
    		pstmt.setLong(3, Long.parseLong(itemId));

            rs = pstmt.executeQuery();
            if(rs.next()) {
            	unitIdAndItemId[0] = rs.getLong("UNIT_ID");
            	unitIdAndItemId[1] = rs.getLong("ITEM_ID");
               
            }        	
        }catch(SQLException e){
        	System.out.println("[getDbUniIdItemId SQLException] === " + e.getMessage());
        }finally{
        	if(rs!=null) {try{rs.close();} catch(Exception e){ System.out.println(e.getStackTrace());}}
    	    if(pstmt!=null) {try{pstmt.close();} catch(Exception e){System.out.println(e.getStackTrace());}}
        	
        }
    	
    	
    	return unitIdAndItemId;
    	
    }
    
    
    /**
     * 주문정보를 ktc_njoyny_hmall_if 테이블에 저장한다.
     * 발주처리 로직이 들어가 있다.
     * @param prdOrderId
     * @param prdOrderDate
     * @param prdItem
     * @param prdUnitId
     * @param prdItemName
     * @param prdUnitPrice
     * @param prdQuantity
     * @param prdOrdererName
     * @param prdOrdererTel1
     * @param prdOrdererTel2
     * @param shipMessage
     * @param shipName
     * @param shipTel1
     * @param shipTel2
     * @param shipZipCode
     * @param shipBaseAddress
     * @param shipDetailAddress
     * @return
     */
    public int insertProduct(String prdOrderId, String prdOrderDate, Long prdItem, Long prdUnitId, String prdItemName, Long prdUnitPrice, Long prdQuantity, String prdOrdererName 
    		                , String prdOrdererTel1, String prdOrdererTel2, String shipMessage, String shipName, String shipTel1, String shipTel2, String shipZipCode, String shipBaseAddress
    		                , String shipDetailAddress){
    	
    	
    	int ret = 0;
    	String orderConfirmIsSuccees = "";
    	
    	StringBuffer sb = new StringBuffer();
    	
    	PreparedStatement pstmt = null;
    	
    	
    	sb.append(" insert  \n");
        sb.append(" into ktc_njoyny_hmall_if(  \n");
        sb.append("     h_order_no, seq, order_date, recv_date, item_id,   \n");
        sb.append("     unit_id, item_name, price, qty, orderer_name,   \n");
        sb.append("     orderer_phone, orderer_cell_phone, orderer_eng_name, orderer_resi_no, orderer_email,  \n");
        sb.append("     msg_for_mall, del_plan_date, receiver_name, receiver_phone, receiver_cell_phone,   \n");
        sb.append("     receiver_zip_code, receiver_addr1, receiver_addr2, msg_for_outbnd, interface_date, \n");
        sb.append("     interface_seq, sysregdt, coven_id \n");
        sb.append(" ) values (  \n");
        sb.append("     ?, ktc_njoyny_hmall_if_seq.nextval, to_date(?,'yyyy/mm/dd hh24:mi:ss'), sysdate, ?,  \n");
        sb.append("     ?, ?, ?, ?, ?,  \n");
        sb.append("     ?, ?, ?, ?, ?,  \n");
        sb.append("     ?, sysdate+5, ?, ?, ?,  \n");
        sb.append("     ?, ?, ?, ?, sysdate,  \n");
        sb.append("     ?, sysdate, ? \n");
        sb.append(" )  \n");
        
        try{
        	pstmt = conn.prepareStatement(sb.toString());
        	
        	pstmt.setString(1, prdOrderId );         // h_order_no            
            pstmt.setString(2 , prdOrderDate.substring(0,10));     // order_date            
            pstmt.setLong(3 , prdItem);                            // item_id            
            pstmt.setLong(4 , prdUnitId);	                       // unit_id
            pstmt.setString(5 , prdItemName);                      // item_name            
            pstmt.setLong(6 , prdUnitPrice);                       // price ->  0.95  네이버체크아웃 결제금액 +네이버체크아웃 적립금 +네이버체크아웃 쿠폰            
            pstmt.setLong(7 , prdQuantity);                        // qty
            pstmt.setString(8 , prdOrdererName);                   // orderer_name            
            pstmt.setString(9 , prdOrdererTel1);                   // orderer_phone           
            pstmt.setString(10, prdOrdererTel2);                   // orderer_cell_phone
            pstmt.setString(11, null);                             // orderer_eng_name
            pstmt.setString(12, null);                             // orderer_resi_no
            pstmt.setString(13, null);                             // orderer_email
            pstmt.setString(14, shipMessage);                      // msg_for_mall
            pstmt.setString(15, shipName);                         // receiver_name         
            pstmt.setString(16, shipTel1);                         // receiver_phone            
            pstmt.setString(17, shipTel2);                         // receiver_cell_phone
            pstmt.setString(18, shipZipCode);                      //receiver_zip_code
            pstmt.setString(19, shipBaseAddress);                  //receiver_addr1
            pstmt.setString(20, shipDetailAddress);                //receiver_addr2
            pstmt.setString(21, shipMessage);                      // msg_for_outbnd
            pstmt.setLong(22, Long.parseLong(this.getInterfaceSeqNo()));  // interface_seq
            pstmt.setString(23, "27346");                                 // coven_id

            ret = pstmt.executeUpdate();
            
            
            //DB에 입력할 정보가 있으면 발주 처리한다.
            if(ret != 0){
            	orderConfirmIsSuccees = this.orderProductIdConfirm(prdOrderId);
            	//발주처리 성공하면 우리쪽 DB 에 Order 정보 입
            	if("SUCCESS".equals(orderConfirmIsSuccees)){
            		System.out.println("[insertProduct] === 발주 성공으로 인한 order정보 입력");
            		conn.commit();             		
            	}else{
            		System.out.println("[insertProduct] === 발주 Send Error["+ prdOrderId +"]");
            		conn.rollback();
            	}
            	
            }else{
            	//보험?? 안해도 되지만...
            	conn.rollback();            	
            
            }
            
        	
        }catch(Exception e){
        	System.out.println("[insertProduct SQLException] ===" + e.getMessage());
        	try { conn.rollback();}catch (SQLException e1) {}
        }finally{
        	if(pstmt != null) {try{ pstmt.close(); } catch(Exception ex){}}        	
        }
        
        return ret;
    	
    }
    
    /**
     * InterfaceDb에 입력후 최종 엔조이뉴욕 Order를 생성한다.
     */
    public void orderConfirmationProc(){
    	String returnCode = "";
    	String returnMsg = "";
    	
    	CallableStatement cStmt = null;
    	
    	try{
    		cStmt = conn.prepareCall("{ call CREAT_NJOYNY_HMALL_ORDER(?,?,?) }");
            cStmt.setLong(1, Long.parseLong(this.getInterfaceSeqNo()));
            cStmt.registerOutParameter(2, Types.VARCHAR);
            cStmt.registerOutParameter(3, Types.VARCHAR);
            cStmt.executeUpdate();
            returnCode = cStmt.getString(2);
            returnMsg = cStmt.getString(3);    		
            conn.commit();
    		
    	}catch (SQLException e) {
    		System.out.println("[orderConfirmationProc SQLException] ===" + e.getMessage());
    		try{ conn.rollback();}catch(SQLException e1){ e1.getStackTrace();}		
    	
    	}finally{
    		if(cStmt != null){ try{ cStmt.close(); } catch(Exception e){}}
    	
    	}
    	
    	
    	
    	System.out.println("[orderConfirmationProc] ====== returnCode : " + returnCode);
		System.out.println("[orderConfirmationProc] ====== returnMsg : " + returnMsg);
    	
    	
    
    }
    
    
    
    
    /**
     * 실시간 암호화 생
     * @param serviceName
     * @param operationName
     * @throws Exception
     */
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

        //result = "2012-01-03T00:00:00+09:00";
        //result = "2012-01-02T09:47:50.554Z";

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
        //result = "2012-01-01T09:47:50.554Z";
        

        return result;
}
    
    
    
    

	public String soapMsgHeader(){
		String soapHeader = "<?xml version='1.0' encoding='UTF-8'?> \n";
		soapHeader += "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sel=\"http://seller.shopn.platform.nhncorp.com/\"> \n";
		soapHeader += "     <soapenv:Header/> \n";
		soapHeader += "         <soapenv:Body> \n";	
		
		return soapHeader.trim();
		
	}
	
	/**
	 * 결제된 상품 ID 를 받아오기 위한 soapBody Message
	 * soapMsgHeader() 와 soapMsgFooter()의 조합으로 완성된 soapMsg를 생성한다.
	 * @return
	 */
	public String soapBody(String OrderStat){
		String soapBody = "";
		
		soapBody += "<sel:GetChangedProductOrderListRequest> \n";
		soapBody += "    <sel:AccessCredentials>\n";
		soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
		soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
		soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
		soapBody += "    </sel:AccessCredentials> \n"; 
		soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
		soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
		soapBody += "    <sel:Version>4.1</sel:Version> \n"; 
		soapBody += "    <sel:InquiryTimeFrom>" + getYesterday() +"</sel:InquiryTimeFrom> \n"; 
		soapBody += "    <sel:InquiryTimeTo>" + getToday() + "</sel:InquiryTimeTo> \n";		
		soapBody += "    <sel:LastChangedStatusCode>"+ OrderStat +"</sel:LastChangedStatusCode> \n";		
		soapBody += "</sel:GetChangedProductOrderListRequest> \n"; 
		
		return soapBody.trim();
		
	
	}
	/**
	 * 받아온 네이버측 prdId 를 가지고 Order 상세 정보를 가져오기 위한 soapBoby Message
	 * soapMsgHeader() 와 soapMsgFooter()의 조합으로 완성된 soapMsg를 생성한다.
	 * @param productOrderNo
	 * @return
	 */
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
	
	
	/**
	 * 받아온 네이버측 prdId 를 가지고 Order 상세 정보를 가져오기 위한 soapBoby Message
	 * soapMsgHeader() 와 soapMsgFooter()의 조합으로 완성된 soapMsg를 생성한다.
	 * @param productOrderNo
	 * @return
	 */
	public String soapBody_orderInfo(String productOrderNo , String orderStat ){
		
		
		String soapBody = "";
		if(orderStat.equals("CancelSale")){ //판매취소
			
			soapBody += "<sel:CancelSaleRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";		
			soapBody += "    <sel:CancelReasonCode>PRODUCT_UNSATISFIED</sel:CancelReasonCode> \n";		
			soapBody += "</sel:CancelSaleRequest> \n"; 			
			
		}else if(orderStat.equals("ApproveCancelApplication")){ //취소승인 
			
			soapBody += "<sel:ApproveCancelApplicationRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";		
			soapBody += "</sel:ApproveCancelApplicationRequest> \n"; 		
			
		}else if(orderStat.equals("DelayProductOrder")){ //발송지연처리 
			
			soapBody += "<sel:DelayProductOrderRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";		
			soapBody += "    <sel:DispatchDueDate>"+this.getToday()+"</sel:DispatchDueDate> \n";		
			soapBody += "    <sel:DispatchDelayReasonCode>PRODUCT_PREPARE</sel:DispatchDelayReasonCode> \n";					
			soapBody += "</sel:DelayProductOrderRequest> \n"; 
			
			/*
			 * 	PRODUCT_PREPARE 상품 준비 중
				CUSTOMER_REQUEST 고객 요청
				CUSTOM_BUILD 주문 제작
				RESERVED_DISPATCH 예약 발송
				ETC 기타
			 */
			
		}else if(orderStat.equals("RequestReturn")){ //반품접수 
			
			soapBody += "<sel:RequestReturnRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";	
			soapBody += "    <sel:ReturnReasonCode>SOLD_OUT</sel:ReturnReasonCode> \n";		//반품사유코드
			soapBody += "    <sel:CollectDeliveryMethodCode>RETURN_DESIGNATED</sel:CollectDeliveryMethodCode> \n";	//수거배송방법
			soapBody += "    <sel:CollectDeliveryCompanyCode>KOREX</sel:CollectDeliveryCompanyCode> \n"; //수거택배사코드
			soapBody += "    <sel:CollectTrackingNumber>3235451233</sel:CollectTrackingNumber>	\n";//수거송장번호
			soapBody += "</sel:RequestReturnRequest> \n"; 		
			
		}else if(orderStat.equals("ApproveReturnApplication")){ //반품승인 
			
			soapBody += "<sel:ApproveReturnApplicationRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";			
			soapBody += "</sel:ApproveReturnApplicationRequest> \n"; 	
			
		}else if(orderStat.equals("ApproveCollectedExchange")){ //교환수거완료  
			
			soapBody += "<sel:ApproveCollectedExchangeRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";		
			soapBody += "</sel:ApproveCollectedExchangeRequest> \n"; 	
			
		}else if(orderStat.equals("ReDeliveryExchange")){	//교환재배송처리   
			
			soapBody += "<sel:ReDeliveryExchangeRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";		
			soapBody += "    <sel:ReDeliveryMethodCode>DELIVERY</sel:ReDeliveryMethodCode> \n";		
			soapBody += "    <sel:ReDeliveryCompanyCode>KOREX</sel:ReDeliveryCompanyCode> \n";	 	//택배사코드
			soapBody += "    <sel:ReDeliveryTrackingNumber>3235451233</sel:ReDeliveryTrackingNumber> \n";	//송장번호
			soapBody += "</sel:ReDeliveryExchangeRequest> \n"; 	
			
		}
		 else if(orderStat.equals("ShipProductOrder")){	//발송처리
				
				soapBody += "<sel:ShipProductOrderRequest> \n";
				soapBody += "    <sel:AccessCredentials>\n";
				soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
				soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
				soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
				soapBody += "    </sel:AccessCredentials> \n"; 
				soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
				soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
				soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
				soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";
				soapBody += "    <sel:DeliveryMethodCode>DELIVERY</sel:DeliveryMethodCode> \n";		
				soapBody += "    <sel:DeliveryCompanyCode>KOREX</sel:DeliveryCompanyCode> \n";	 	//택배사코드
				soapBody += "    <sel:TrackingNumber>3235451233</sel:TrackingNumber> \n";	//송장번호
				soapBody += "    <sel:DispatchDate>"+this.getToday()+"</sel:DispatchDate> \n";	//송장번호				
				soapBody += "</sel:ShipProductOrderRequest> \n"; 	
				
			}else{ //그외 상황 임의로 판매취소
			
			soapBody += "<sel:CancelSaleRequest> \n";
			soapBody += "    <sel:AccessCredentials>\n";
			soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
			soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
			soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
			soapBody += "    </sel:AccessCredentials> \n"; 
			soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
			soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
			soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
			soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";		
			soapBody += "    <sel:CancelReasonCode>PRODUCT_UNSATISFIED</sel:CancelReasonCode> \n";		
			soapBody += "</sel:CancelSaleRequest> \n"; 			
		}

		
			/*구매거부 사유코드
			 * 
			PRODUCT_UNSATISFIED 서비스 및 상품 불만족 				판매 취소 시 사용 가능
			DELAYED_DELIVERY 배송 지연
			SOLD_OUT 상품 품절
			
			INTENT_CHANGED 구매 의사 취소                                                    		반품 접수 시 사용 가능
			COLOR_AND_SIZE 색상 및 사이즈 변경
			WRONG_ORDER 다른 상품 잘못 주문
			PRODUCT_UNSATISFIED 서비스 및 상품 불만족
			DELAYED_DELIVERY 배송 지연
			SOLD_OUT 상품 품절
			DROPPED_DELIVERY 배송 누락
			NOT_YET_DELIVERY 미배송
			BROKEN 상품 파손
			INCORRECT_INFO 상품 정보 상이
			WRONG_DELIVERY 오배송
			WRONG_OPTION 색상 등이 다른 상품을 잘못 배송 		
			 */
		
		return soapBody.trim();
	
	}
	
	/**
	 * 네이버측에 발주 처리하기 위한 soapBoby Message
	 * soapMsgHeader() 와 soapMsgFooter()의 조합으로 완성된 soapMsg를 생성한다.
	 * @param productOrderNo
	 * @return
	 */
	public String soapBody_orderConfirm(String productOrderNo){
		String soapBody = "";
		
		soapBody += "<sel:PlaceProductOrderRequest> \n";
		soapBody += "    <sel:AccessCredentials>\n";
		soapBody += "       <sel:AccessLicense>" + this.getAccessLicense() + "</sel:AccessLicense> \n";  
		soapBody += "       <sel:Timestamp>" + this.getTimestamp() +"</sel:Timestamp> \n";  
		soapBody += "       <sel:Signature>" + this.getSignature() +"</sel:Signature> \n";  
		soapBody += "    </sel:AccessCredentials> \n"; 
		soapBody += "    <sel:RequestID>" + this.getId() + "</sel:RequestID> \n"; 
		soapBody += "    <sel:DetailLevel>Full</sel:DetailLevel> \n"; 
		soapBody += "    <sel:Version>4.1</sel:Version> \n";		 
		soapBody += "    <sel:ProductOrderID>" + productOrderNo + "</sel:ProductOrderID> \n";		
		soapBody += "</sel:PlaceProductOrderRequest> \n"; 
		
		return soapBody.trim();
	
	}
	
	
	
	public String soapMsgFooter(){
		String soapFooter = "        </soapenv:Body> \n";
		soapFooter += "</soapenv:Envelope>";		                         
		
		return soapFooter.trim();
		
	}
	
	/**
	 * 완성된 soapMsg를 생성한다.
	 * @param soapBodyMsg 조합에 필요한 soapBody parameter
	 * @return
	 */
	public String soapMessage(String soapBodyMsg){
		String soapMessage = "";
		
		soapMessage = this.soapMsgHeader()
					+ soapBodyMsg
					+ this.soapMsgFooter();
		
		return soapMessage;
	
	}
	
	
	/**
	 * 완성된 soapMsg를 전송후 Response를 리턴한다.
	 * @param isTest 
	 * @param soapMessage
	 * @return
	 * @throws Exception
	 */
	public String soapSend(String isTest, String soapMessage) throws Exception{
		String retResult = "";
		String SOAPUrl = "";
		if(!"test".equals(isTest.toLowerCase())){
			SOAPUrl = "http://ec.api.naver.com/ShopN/" + this.getServiceName();									
		}else{
			SOAPUrl = "http://sandbox.api.naver.com/ShopN/" + this.getServiceName();						
		}
		
		/*System.out.println("SendUrl ===== " + SOAPUrl);
		System.out.println("soapMessage ============ " );
		System.out.println(soapMessage);
		System.out.println("=============soapMessageEnd" );
		*/
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
	
	
	/**
	 * 상세정보를 얻어오기위해 우선 네이버측 productOrderNo를 가져온다.
	 * 
	 * @param soapMessage
	 * @return productOrderNo를 배열로 리
	 * @throws Exception
	 */
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
	
	/**
	 * DB Insert하기전에 네이버Nshop에 발주처리를 한다. 
	 * @return
	 * @throws Exception
	 */
	public String orderProductIdConfirm(String productOrderNo) throws Exception{
		String ret ="";
		String soapRetMsg = "";
		
		
		
		//확정 Encrypt 재설정 
		this.encryptInit("SellerService41", "PlaceProductOrder");
		soapRetMsg = this.soapSend(this.getIsTest(), this.soapMessage(this.soapBody_orderConfirm(productOrderNo)));
		
		// XML Document 객체 생성
        InputSource is = new InputSource(new StringReader(soapRetMsg));
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        
        // xpath 생성
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        // Orderer 정보 
        NodeList isSuccessNode = (NodeList)xpath.evaluate("//PlaceProductOrderResponse/ResponseType", document, XPathConstants.NODESET);
		
        for( int idx=0; idx<isSuccessNode.getLength(); idx++ ){
            System.out.println("[orderDateNode ===== " + isSuccessNode.item(idx).getTextContent());
            ret = isSuccessNode.item(idx).getTextContent().trim();
            
        } 
		
		
		
		return ret;
		
	}
	
	/**
	 * Order 상세 정보를 parsing하여 DB 에 입력한다. 
	 * 중복제거 로직이 들어가있다.
	 * @param soapMessage
	 * @param prdOrdNo
	 * @throws Exception
	 */
	public String orderProductInfoClaim(String soapMessage, String prdOrdNo) throws Exception{
		
		//Order 변수 
		String ClaimStatus = "";
		/*String orderId = "";
		String ordererId = "";
		String ordererName = "";
		String ordererTel1 = "";
		String ordererTel2 = "";
		String ordererPamentMeans = "";		
		String shipMemo = "";*/
		
		
		// XML Document 객체 생성
        InputSource is = new InputSource(new StringReader(soapMessage));
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        
        
        
        
        // xpath 생성
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        // Orderer 정보 
        NodeList ClaimStatusNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ProductOrderStatus", document, XPathConstants.NODESET);
        /*NodeList orderIdNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/Order/OrderID", document, XPathConstants.NODESET);
        NodeList ordererIdNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/Order/OrdererID", document, XPathConstants.NODESET);
        NodeList ordererNameNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/Order/OrdererName", document, XPathConstants.NODESET);
        NodeList ordererTel1Node = (NodeList)xpath.evaluate("//ProductOrderInfoList/Order/OrdererTel1", document, XPathConstants.NODESET);
        NodeList ordererTel2Node = (NodeList)xpath.evaluate("//ProductOrderInfoList/Order/OrdererTel2", document, XPathConstants.NODESET);
        NodeList ordererPaymentMeansNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/Order/PaymentMeans", document, XPathConstants.NODESET);
        NodeList ordererShippingMemoNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/Order/ShippingMemo", document, XPathConstants.NODESET);
        */
        
        System.out.println("[Orderer ===== 정보]");
        for( int idx=0; idx<ClaimStatusNode.getLength(); idx++ ){
            System.out.println("[orderDateNode ===== " + ClaimStatusNode.item(idx).getTextContent());
            ClaimStatus = ClaimStatusNode.item(idx).getTextContent();
            
        } 
        /*
        for( int idx=0; idx<orderIdNode.getLength(); idx++ ){
            System.out.println("[orderIdNode ===== " + orderIdNode.item(idx).getTextContent());
            orderId = orderIdNode.item(idx).getTextContent();
            
        }        
        
        for( int idx=0; idx<ordererIdNode.getLength(); idx++ ){
            System.out.println("[ordererId ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererIdNode.item(idx).getTextContent()), "UTF-8"));
            ordererId = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererIdNode.item(idx).getTextContent()), "UTF-8");
            
        }
        
        for( int idx=0; idx<ordererNameNode.getLength(); idx++ ){
            System.out.println("[ordererNameNode ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererNameNode.item(idx).getTextContent()), "UTF-8"));
            ordererName = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererNameNode.item(idx).getTextContent()), "UTF-8");
        }
        
        for( int idx=0; idx<ordererTel1Node.getLength(); idx++ ){
            System.out.println("[ordererTel1Node ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererTel1Node.item(idx).getTextContent()), "UTF-8"));
            ordererTel1 = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererTel1Node.item(idx).getTextContent()), "UTF-8");
        }
        
        for( int idx=0; idx<ordererTel2Node.getLength(); idx++ ){
            System.out.println("[ordererTel2Node ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererTel2Node.item(idx).getTextContent()), "UTF-8"));
            ordererTel2 = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),ordererTel2Node.item(idx).getTextContent()), "UTF-8");
        }
        
        for( int idx=0; idx<ordererPaymentMeansNode.getLength(); idx++ ){
            System.out.println("[ordererPaymentMeansNode ===== " + ordererPaymentMeansNode.item(idx).getTextContent());
            ordererPamentMeans = ordererPaymentMeansNode.item(idx).getTextContent();
            
        }
        
        for( int idx=0; idx<ordererShippingMemoNode.getLength(); idx++ ){
            System.out.println("[ordererShippingMemoNode ===== " + ordererShippingMemoNode.item(idx).getTextContent());
            shipMemo = ordererShippingMemoNode.item(idx).getTextContent();
            
        }
        
        
        
        
        //배송지 정보 변수 
        String shipBaseAddress = "";
        String shipDetailAddress = "";
        String shipName = "";
        String shipTel1 = "";
        String shipTel2 = "";
        String shipZipCode = "";
       
        
        
        //배송지 정보  
        NodeList shipBaseAddressNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ShippingAddress/BaseAddress", document, XPathConstants.NODESET);
        NodeList shipDetailedAddressNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ShippingAddress/DetailedAddress", document, XPathConstants.NODESET);
        NodeList shipNameNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ShippingAddress/Name", document, XPathConstants.NODESET);
        NodeList shipTel1Node = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ShippingAddress/Tel1", document, XPathConstants.NODESET);
        NodeList shipTel2Node = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ShippingAddress/Tel2", document, XPathConstants.NODESET);
        NodeList shipZipCodeNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ShippingAddress/ZipCode", document, XPathConstants.NODESET);
        
        
        
        System.out.println("[ShippingAddress ===== 정보]");
        
        for( int idx=0; idx<shipBaseAddressNode.getLength(); idx++ ){
            System.out.println("[shipBaseAddressNode ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipBaseAddressNode.item(idx).getTextContent()), "UTF-8"));
            shipBaseAddress = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipBaseAddressNode.item(idx).getTextContent()), "UTF-8");
           
        }
        
        for( int idx=0; idx<shipDetailedAddressNode.getLength(); idx++ ){
            System.out.println("[shipDetailedAddressNode ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipDetailedAddressNode.item(idx).getTextContent()), "UTF-8"));
            shipDetailAddress = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipDetailedAddressNode.item(idx).getTextContent()), "UTF-8");
            
        }
        
        for( int idx=0; idx<shipNameNode.getLength(); idx++ ){
            System.out.println("[shipNameNode ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipNameNode.item(idx).getTextContent()), "UTF-8"));
            shipName = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipNameNode.item(idx).getTextContent()), "UTF-8");
        }
        
        for( int idx=0; idx<shipTel1Node.getLength(); idx++ ){
            System.out.println("[shipTel1Node ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipTel1Node.item(idx).getTextContent()), "UTF-8"));
            shipTel1 = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipTel1Node.item(idx).getTextContent()), "UTF-8");
        }
        
        for( int idx=0; idx<shipTel2Node.getLength(); idx++ ){
            System.out.println("[shipTel2Node ===== " + new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipTel2Node.item(idx).getTextContent()), "UTF-8"));
            shipTel2 = new String(SimpleCryptLib.decrypt(this.getEncryptKey(),shipTel2Node.item(idx).getTextContent()), "UTF-8");
        }

        for( int idx=0; idx<shipZipCodeNode.getLength(); idx++ ){
            System.out.println("[shipZipCodeNode ===== " + shipZipCodeNode.item(idx).getTextContent());
            shipZipCode = shipZipCodeNode.item(idx).getTextContent();
        }
        
        
        
        //상품 정보 변수        
        String prdName = "";
        String prdOptionName = "";
        String prdQuantity = "";
        String prdUnitPrice = "";
        String sellerPrdCode = "";
        
        //Product 정보  
        NodeList productNameNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ProductName", document, XPathConstants.NODESET);
        NodeList productOptionNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/ProductOption", document, XPathConstants.NODESET);
        NodeList productQuantityNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/Quantity", document, XPathConstants.NODESET);
        NodeList productUnitPriceNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/UnitPrice", document, XPathConstants.NODESET);
        NodeList sellerProductCodeNode = (NodeList)xpath.evaluate("//ProductOrderInfoList/ProductOrder/SellerProductCode", document, XPathConstants.NODESET);
        
        for( int idx=0; idx<productNameNode.getLength(); idx++ ){
            System.out.println("[productNameNode ===== " + productNameNode.item(idx).getTextContent());
            prdName = productNameNode.item(idx).getTextContent();
        }
        
        for( int idx=0; idx<productOptionNode.getLength(); idx++ ){
            System.out.println("[productOptionNode ===== " + productOptionNode.item(idx).getTextContent());
            prdOptionName = productOptionNode.item(idx).getTextContent();
        }
        
        for( int idx=0; idx<productQuantityNode.getLength(); idx++ ){
            System.out.println("[productQuantityNode ===== " + productQuantityNode.item(idx).getTextContent());
            prdQuantity = productQuantityNode.item(idx).getTextContent();
        }
        
        for( int idx=0; idx<productUnitPriceNode.getLength(); idx++ ){
            System.out.println("[productUnitPriceNode ===== " + productUnitPriceNode.item(idx).getTextContent());
            prdUnitPrice = productUnitPriceNode.item(idx).getTextContent();
        }
        
        for( int idx=0; idx<sellerProductCodeNode.getLength(); idx++ ){
            System.out.println("[productUnitPriceNode ===== " + sellerProductCodeNode.item(idx).getTextContent());
            sellerPrdCode = sellerProductCodeNode.item(idx).getTextContent();
            
            
            
        }
   
        
        
        //임의 itemid 부여
        sellerPrdCode = "8885449";
        prdOptionName = "black(a)-gray";
        //임의 itemid 부여끝 
        
        */
		return ClaimStatus;     
        
        /*
         * DB 입력후 주문 처리를 위한 Logic 시작 
         */
        
        //변수
       /* int duplicateInt = 0;
        
        
        //중복 입력을 방지하기 위해 아래 메소드를 호출한다.
        duplicateInt = this.chkDuplicateNaverOrderNo(prdOrdNo);
        
        
        if(duplicateInt == 0){  //중복이 아니면 DB 입력을 수행한다.
        	//itemid 와 optionName 으로 엔조이뉴욕 unitId와 itemId 를 가져온다.
            Long[] unitAndItem = new Long[2];        
            unitAndItem = getDbUniIdItemId(sellerPrdCode, prdOptionName.trim());
            
            System.out.println("[orderProductInfoParseAndDbInsert] == prdOrdNo ==== " + prdOrdNo);
            System.out.println("[orderProductInfoParseAndDbInsert] == orderDate ==== " + orderDate);
            System.out.println("[orderProductInfoParseAndDbInsert] == unitAndItem[1] ==== " + unitAndItem[1]);
            System.out.println("[orderProductInfoParseAndDbInsert] == unitAndItem[0] ==== " + unitAndItem[0]);
            System.out.println("[orderProductInfoParseAndDbInsert] == prdName ==== " + prdName);
            System.out.println("[orderProductInfoParseAndDbInsert] == prdUnitPrice.trim() ==== " + prdUnitPrice.trim());
            System.out.println("[orderProductInfoParseAndDbInsert] == prdQuantity.trim() ==== " + prdQuantity.trim());
            System.out.println("[orderProductInfoParseAndDbInsert] == ordererName ==== " + ordererName);
            System.out.println("[orderProductInfoParseAndDbInsert] == ordererTel1 ==== " + ordererTel1);
            System.out.println("[orderProductInfoParseAndDbInsert] == ordererTel2 ==== " + ordererTel2);
            System.out.println("[orderProductInfoParseAndDbInsert] == shipMemo ==== " + shipMemo);
            System.out.println("[orderProductInfoParseAndDbInsert] == shipName ==== " + shipName);
            System.out.println("[orderProductInfoParseAndDbInsert] == shipTel1 ==== " + shipTel1);
            System.out.println("[orderProductInfoParseAndDbInsert] == shipTel2 ==== " + shipTel2);
            System.out.println("[orderProductInfoParseAndDbInsert] == shipZipCode ==== " + shipZipCode);
            System.out.println("[orderProductInfoParseAndDbInsert] == shipBaseAddress ==== " + shipBaseAddress);
            System.out.println("[orderProductInfoParseAndDbInsert] == shipDetailAddress ==== " + shipDetailAddress);
            
            
            this.insertProduct(prdOrdNo, orderDate, unitAndItem[1], unitAndItem[0], prdName, Long.parseLong(prdUnitPrice.trim()), Long.parseLong(prdQuantity.trim()), ordererName 
                    , ordererTel1, ordererTel2, shipMemo, shipName, shipTel1, shipTel2, shipZipCode, shipBaseAddress, shipDetailAddress);              
            
        }else{
        	System.out.println("[orderProductInfoParseAndDbInsert][" + prdOrdNo + "] === 중복이 발생하여 입력 불가");
        
        }
        */
        
	}
		
		
		
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{			
		/*if(args.length < 1){
			System.out.println("parameter로 test OR real 둘중 하나를 입력하라고 이생퀴야!");
			System.exit(1);
		}*/
		
		
		// TODO Auto-generated method stub
		NaverShopnCancelReturn nsg = new NaverShopnCancelReturn();
		
		//테스트&운영 선택
		//nsg.setIsTest(args[0]);
		nsg.setIsTest("real");
		
	
		
		//DB init
		nsg.init();
		
		//interfaceSEQ init
		nsg.getMethodInterfaceSeqNo();
		System.out.println("SetInterfaceSeq === " + nsg.getInterfaceSeqNo());
		
		//encrypt init
		nsg.encryptInit("SellerService41", "GetChangedProductOrderList");		

		
		
		/*
		 * 
		상품 주문 변경 상태 코드
		
	  	PAY_WAITING 입금 대기
		PAYED 결제 완료
		--DISPATCHED 발송 처리
		--CANCEL_REQUESTED 취소 요청
		--RETURN_REQUESTED 반품 요청
		--EXCHANGE_REQUESTED 교환 요청
		EXCHANGE_REDELIVERY_READY 교환 재배송 준비
		HOLDBACK_REQUESTED 구매 확정 보류 요청
		CANCELED 취소
		--RETURNED 반품
		EXCHANGED 교환
		PURCHASE_DECIDED 구매 확정
		
		
		상품 주문 상태 코드
		
		PAYMENT_WAITING 입금 대기 정상
		PAYED 결제 완료
		DELIVERING 배송 중
		DELIVERED 배송 완료
		PURCHASE_DECIDED 구매 확정
		EXCHANGED 교환
		CANCELED 취소 취소
		RETURNED 반품
		CANCELED_BY_NOPAYMENT 미입금 취소	
	 */
		
		//GetChangedProductOrderList 을 이용하여 productOrderNo 를 생성		
		String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("PURCHASE_DECIDED"))));
		//String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("DISPATCHED"))));
		//String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("CANCELED"))));		
		//String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("CANCEL_REQUESTED"))));
		//String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("RETURN_REQUESTED"))));
		//String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("EXCHANGE_REQUESTED"))));
		//String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("EXCHANGE_REDELIVERY_READY"))));		
		//String[] productOrderNo = nsg.orderProductListParser(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody("RETURNED"))));	
		
		//productOrderNo = null;

		
		String OrderStat = "";
		
		/*
		 - 판매취소 : CancelSale  //결제완료상태에서 실행
		 - 취소승인 : ApproveCancelApplication // 취소 요청상태에서 실행
		 - 발송지연처리 : DelayProductOrder //결제완료상태에서 실행
		 - 발송 처리:	ShipProductOrder	//결제완료상태에서 실행	 
		 -- 반품접수 : RequestReturn  //DISPATCHED 발송 처리상태에서 실행
		 - 반품승인 : ApproveReturnApplication //반품 요청상태에서 실행	
		 - 교환수거완료 : ApproveCollectedExchange // 교환 요청상태에서 실행	 
		 -- 교환재배송처리 : ReDeliveryExchange //EXCHANGE_REQUESTED교환 요청 상태에서 실행	(데이터없음)

		 
		*/
		OrderStat = "ApproveReturnApplication";	
		
		String ClaimResult = "";
		for (int i=0; i<productOrderNo.length; i++){
			
			if(OrderStat.equals("RequestReturn")){    //반품접수
				nsg.encryptInit("SellerService41", "GetProductOrderInfoList");
				
				//개별 주문정보 DB 입력
				ClaimResult = nsg.orderProductInfoClaim(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody_orderInfo(productOrderNo[i]))), productOrderNo[i]);			
				System.out.println("ClaimResult:"+ClaimResult);
				//if(ClaimResult.equals("DISPATCHED")){
					
					nsg.encryptInit("SellerService41", OrderStat); //교환수거완료  
	
					//개별 주문정보 DB 입력
					nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody_orderInfo(productOrderNo[i],OrderStat)));
					System.out.println("#########################################################################");
				//}
				
				System.out.println("[Total PrdInfo = " + (i + 1) + "]");
			}else{   	
				
				nsg.encryptInit("SellerService41", OrderStat); //교환수거완료  
				
				//개별 주문정보 DB 입력
				nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody_orderInfo(productOrderNo[i],OrderStat)));
				System.out.println("#########################################################################");
			}
		}
		
		/*
		
		for (int i=0; i<productOrderNo.length; i++){

			nsg.encryptInit("SellerService41", OrderStat); //교환수거완료  

			
			//개별 주문정보 DB 입력
			nsg.orderProductInfoParseAndDbInsert(nsg.soapSend(nsg.getIsTest(), nsg.soapMessage(nsg.soapBody_orderInfo(productOrderNo[i],OrderStat))), productOrderNo[i]);			
			System.out.println("[Total PrdInfo = " + (i + 1) + "]");
		}
		*/
		
		
		//PON300000000009
		
		
		System.out.println("SetInterfaceSeq === " + nsg.getInterfaceSeqNo());
		
		//주문확정 프로시져 호출
		//nsg.orderConfirmationProc();
				
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

	public String getInterfaceSeqNo() {
		return interfaceSeqNo;
	}

	public void setInterfaceSeqNo(String interfaceSeqNo) {
		this.interfaceSeqNo = interfaceSeqNo;
	}

	public String getIsTest() {
		return isTest;
	}

	public void setIsTest(String isTest) {
		this.isTest = isTest;
	}
	
	
	
}


