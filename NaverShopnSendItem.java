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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class NaverShopnSendItem {

	public static Connection conn = null;

	private String ORADriver = "";
	private String ORAURL = "";
	private String ORAUser = "";
	private String ORAPass = "";

	public void init() {

		try {
			// ORADriver = dbProps.getProperty("B2CDB1.ORADriver");
			ORADriver = "oracle.jdbc.driver.OracleDriver";
			ORAURL = "jdbc:oracle:thin:@10.125.10.128:1521:B2CDB1"; // �b2c
			ORAUser = "KTC_B2C";
			ORAPass = "KTC_B2C123";

			// Class.forName(ORADriver);
			Class.forName("oracle.jdbc.driver.OracleDriver");

			conn = DriverManager.getConnection(ORAURL, ORAUser, ORAPass);
			conn.setAutoCommit(false);
		} catch (Exception e) {
			System.out.println("init() : " + e.getMessage());
		}
	}

	public void distroy() {
		try {
			this.conn.close();
		} catch (Exception e) {
			System.out.println("distroy() : " + e.getMessage());
		} finally {
		}
	}

	private String getFilePrefix() {
		String result = "";
		java.util.Date today = new java.util.Date();
		long currDate = today.getTime();
		// long addDate = currDate - (24 * (60 * (60 * 1000)));
		long addDate = currDate;
		today.setTime(addDate);
		SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMdd");
		result = dateForm.format(today);

		// result = "20090912";

		return result;
	}

	private String getToday() {
		String result = "";
		java.util.Date today = new java.util.Date();
		long currDate = today.getTime();
		// long addDate = currDate - (24 * (60 * (60 * 1000)));
		long addDate = currDate - (9 * (60 * (60 * 1000))); // �ѱ��ð��� -9 �ð� �ؾ�
															// �ȴ�.
		today.setTime(addDate);
		SimpleDateFormat dateForm = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		result = dateForm.format(today);

		// result = "2009-09-12";

		return result;
	}

	private String getYesterday() {
		String result = "";
		java.util.Date today = new java.util.Date();
		long currDate = today.getTime();
		// long addDate = currDate - (9 * (60 * (61 * 1000))) ; //10���� �ѱ��ð��� -9
		// �ð� �ؾ� �ȴ�.
		long addDate = currDate - (33 * (60 * (60 * 1000))) + 1; // 10���� �ѱ��ð���
																	// -9 �ð� �ؾ�
																	// �ȴ�. �Ϸ���
		// long addDate = currDate;
		today.setTime(addDate);
		SimpleDateFormat dateForm = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		result = dateForm.format(today);

		// result = "2009-09-12";

		return result;
	}

	private String getElementData(Element e, String name) {
		return getElementData(e, name, "");
	}

	private String getElementData(Element e, String name, String def) {
		if (e == null)
			return def;
		if (name == null)
			return def;

		Element tmp = e.getChild(name);
		if (tmp == null)
			return def;

		String retData = tmp.getTextTrim();
		if (retData == null)
			return def;

		return retData;
	}

	private String getTel(String def) {
		String tel_no = "";
		if (def.length() < 10) {
			tel_no = def.substring(0, 2) + "-" + def.substring(2, 5) + "-"
					+ def.substring(5, 9);
		} else if (def.length() < 11) {
			if (def.substring(0, 2).equals("02")) {
				tel_no = def.substring(0, 2) + "-" + def.substring(2, 6) + "-"
						+ def.substring(6, 10);
			} else {
				tel_no = def.substring(0, 3) + "-" + def.substring(3, 6) + "-"
						+ def.substring(6, 10);
			}
		} else if (def.length() < 12) {
			tel_no = def.substring(0, 3) + "-" + def.substring(3, 7) + "-"
					+ def.substring(7, 11);
		}
		return tel_no;
	}

	// XML�������� filename�� Ư�����ڸ� �Ľ��Ѵ�.
	private String parsingSpecialforXml(String fileName) {

		CharBuffer cb = CharBuffer.wrap(fileName);
		String xmlString = "";
		while (cb.hasRemaining()) {

			char tempChar = cb.get();

			if (tempChar == '"') {
				xmlString += "&quot;";
			} else if (tempChar == '&') {
				xmlString += "&amp;";
			} else if (tempChar == '\'') {
				xmlString += "&apos;";
			} else if (tempChar == '<') {
				xmlString += "&lt;";
			} else if (tempChar == '>') {
				xmlString += "&gt;";
			} else {
				xmlString += tempChar;
			}

		}

		return xmlString;
	}

	public static String removeXssString(String s) {

		if (s == null) {
			s = "";
		}

		String strWork = "";
		strWork = s;
		String[] spChars = { "`", "-", "=", ";", "'", "/", "~", "!", "@", "#",
				"$", "%", "&", "|", ":", "\"", "<", ">" };

		strWork = strWork.replaceAll("\\?", "");
		strWork = strWork.replaceAll("\\&lt;", "");
		strWork = strWork.replaceAll("\\:", "");
		strWork = strWork.replaceAll("\\*", "");
		strWork = strWork.replaceAll("\\|", "");
		int spCharLen = spChars.length;

		for (int i = 0; i < spCharLen; i++) {

			strWork = strWork.replaceAll(spChars[i], "");
		}


		return strWork;
	}

	
	 public String extract_numeral(String str){ 

		   String numeral = "";
		    if( str == null )
		    { 
		      numeral = null;
		    }
		    else {
		      String patternStr = "\\d"; //���ڸ� �������� ����
		      Pattern pattern = Pattern.compile(patternStr); 
		      Matcher matcher = pattern.matcher(str); 

		      while(matcher.find()) { 
		      numeral += matcher.group(0); //������ ���ϰ� ��Ī�Ǹ� numeral ������ �ִ´�. ���⼭�� ����!!
		      }
		    } 

		   return numeral;
		  }
	
	
	
	private String run4(String ProductId, String n_item_id, Integer cnt) { // �ɼ�
																			// ��������
																			// �޼ҵ�
		String response_type = "FALSE";
		NaverShopnSendItem NaverShopnSendItem = new NaverShopnSendItem();
		System.out.println("�ڡڡڡڡڡڡڡڡڡڡڡڡڡڡڡ�run4�ڡڡڡڡڡڡڡڡڡڡڡ�");
		try {
			/* �ɼǵ�� ���� */

			// ���̹� �������� ����
			SimpleCryptLib SimpleCryptLib = new SimpleCryptLib();
			Security.addProvider(new BouncyCastleProvider());

			String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43";
			String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";

			String serviceName = "ProductService"; // ���񽺸�
			String id = "eugink";
			String password = "asdf0101";
			String timestamp = null;
			String signature = null;
			String data = null;
			String item_id = "";

			byte[] encryptKey = null;

			String encryptedData = null;
			String decryptedData = null;
			String hashedData = null;

			String operationName = "GetOption";
			String ResponseType = "";

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			CallableStatement cStmt = null;

			operationName = "ManageOption";

			// String orderID = "200087036";

			// timestamp create
			timestamp = SimpleCryptLib.getTimestamp();
			System.out.println("timestamp:" + timestamp);
			System.out.println("ManageOption");

			// generateSign
			data = timestamp + serviceName + operationName;
			signature = SimpleCryptLib.generateSign(data, secretKey);

			// generateKey
			encryptKey = SimpleCryptLib.generateKey(timestamp, secretKey);

			// encrypt
			encryptedData = SimpleCryptLib.encrypt(encryptKey,
					password.getBytes("UTF-8"));

			// decrypt
			decryptedData = new String(SimpleCryptLib.decrypt(encryptKey,
					encryptedData), "UTF-8");

			// sha256
			hashedData = SimpleCryptLib.sha256(password);

			// ���̹� �������� ��
			StringBuffer unitlistsql = new StringBuffer(); // �ɼ���ȸ����

			try {
				StringBuffer selectUnit = new StringBuffer();

				selectUnit
						.append(" select unit_name,co_unit_id,unit_id,  vir_stock_qty  ,use_yn \n");
				selectUnit.append(" from ( \n");
				selectUnit
				.append("  select nvl(b.coven_id,0) coven_id,decode(a.use_yn , 'Y',a.unit_name,a.unit_name||'('||a.unit_id||')') unit_name,nvl(b.co_unit_id,0) co_unit_id,a.unit_id,(CASE WHEN  vir_stock_qty <= 0 then decode(use_yn,'Y',1,0) else decode(use_yn,'Y',vir_stock_qty,0) end) vir_stock_qty,decode(a.use_yn,'Y','Y','N') use_yn \n");
				selectUnit.append(" from ktc_unit a,ktc_unit_interlock b \n");
				selectUnit.append(" where a.unit_id = b.unit_id(+) \n");
				selectUnit.append(" and a.unit_name is not null \n");
				selectUnit.append(" and a.item_id = ? \n");
				selectUnit.append(" )where  coven_id in (27346,0)    \n");


				//System.out.println("selectUnit.toString():"+ selectUnit.toString());
				HashMap unitList = new HashMap();
				pstmt = conn.prepareStatement(selectUnit.toString());

				// pstmt.clearParameters();

				// �Ķ���ͼ���
				pstmt.setString(1, n_item_id);
				rs = pstmt.executeQuery();
				int list_cnt = 1;


				while (rs.next()) {

					HashMap rowData = new HashMap();
					rowData.put("unit_name", rs.getString("unit_name"));
					rowData.put("co_unit_id", rs.getString("co_unit_id"));
					rowData.put("unit_id", rs.getString("unit_id"));
					rowData.put("vir_stock_qty", rs.getString("vir_stock_qty"));
					rowData.put("use_yn", rs.getString("use_yn"));
					unitList.put(Integer.toString(list_cnt), rowData);

					list_cnt = list_cnt + 1;
				}

				/* unit xml ���� */

				unitlistsql.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n ");
				unitlistsql.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:shop=\"http://shopn.platform.nhncorp.com/\"> ");
				unitlistsql.append("   <soap:Header/>                                                                                                    ");
				unitlistsql.append("   <soap:Body>                                                                                                       ");
				unitlistsql.append("      <shop:ManageOptionRequest>                                                                                     ");
				unitlistsql.append("         <!--Optional:-->                                                                                            ");
				unitlistsql.append("         <shop:RequestID>njoyny2</shop:RequestID>");
				unitlistsql.append("         <shop:AccessCredentials>");
				unitlistsql.append("            <shop:AccessLicense>"
						+ accessLicense + "</shop:AccessLicense>");
				unitlistsql.append("            <shop:Timestamp>" + timestamp
						+ "</shop:Timestamp>");
				unitlistsql.append("            <shop:Signature>" + signature
						+ "</shop:Signature>");
				unitlistsql.append("         </shop:AccessCredentials>");
				unitlistsql.append("         <shop:Version>2.0</shop:Version>");
				unitlistsql.append("         <SellerId>njoyny2</SellerId>");

				unitlistsql.append("         <Option>                                                                                                    ");
				unitlistsql.append("            <shop:ProductId>"
								+ ProductId
								+ "</shop:ProductId>                                                                       ");
				unitlistsql.append("            <!--Optional:-->                                                                                         ");
				unitlistsql.append("                        <shop:Combination>                                         ");

				unitlistsql.append("   						<shop:Names> ");
				unitlistsql.append("      						<shop:Name1><![CDATA[����]]></shop:Name1> ");
				unitlistsql.append("   						</shop:Names> ");

				unitlistsql.append("   						<shop:ItemList>   ");

				System.out.println("unitlistsql:" + unitlistsql);
				System.out.println("!!!!cnt:" + cnt);
				String unit_name = "";
				String co_unit_id = "";
				String unit_id = "";
				String vir_stock_qty = "";
				String use_yn = "";
				HashMap rowData1 = null;

				for (int j = 1; j <= cnt; j++) {


					rowData1 = (HashMap) unitList.get(String.valueOf(j));

					unit_name = NaverShopnSendItem.parsingSpecialforXml(
							(String) rowData1.get("unit_name")).replaceAll(":",
							"-"); // njoyny �ɼ�id
					co_unit_id = (String) rowData1.get("co_unit_id"); // shopN
																		// �ɼ�id
					unit_id = (String) rowData1.get("unit_id");
					vir_stock_qty = (String) rowData1.get("vir_stock_qty");
					use_yn = (String) rowData1.get("use_yn");


					unitlistsql.append("                           <shop:Item>                                          ");
					if (!co_unit_id.equals("0")) {
						unitlistsql.append("                              <shop:Id>"
										+ co_unit_id
										+ "</shop:Id>                                ");
					}
					unitlistsql.append("                              <shop:Value1>"
									+ unit_name
									+ "</shop:Value1>                            ");
					unitlistsql.append("                              <shop:Quantity>"
									+ vir_stock_qty
									+ "</shop:Quantity>                            ");
					unitlistsql.append("                              <shop:SellerManagerCode>"
									+ unit_id
									+ "</shop:SellerManagerCode>                          ");
					unitlistsql.append("                              <shop:Usable>"
									+ use_yn
									+ "</shop:Usable>                        ");
					unitlistsql.append("                           </shop:Item>                                         ");

				}

				unitlistsql.append("   						</shop:ItemList>   ");
				unitlistsql.append("                        </shop:Combination>                                        ");
				unitlistsql.append("         </Option>                                                                                                   ");
				unitlistsql.append("      </shop:ManageOptionRequest>                                                                                    ");
				unitlistsql.append("   </soap:Body>                                                                                                      ");
				unitlistsql.append("</soap:Envelope>                                                                                                     ");


				/* unit xml �� */

				System.out.println("unitlist:" + unitlistsql.toString());
				if (rs != null) {
					try {
						rs.close();
					} catch (Exception e) {
						response_type = "FALSE";
					}
				}
				if (pstmt != null) {
					try {
						pstmt.close();
					} catch (Exception e) {
						response_type = "FALSE";
					}
				}

			} catch (Exception e) {
				System.out.println("run4() : " + e.getMessage());
			} finally {
			}


			//System.out.println(unitlistsql.toString());


			String line_total = "";
			URL url = new URL("http://ec.api.naver.com/ShopN/ProductService");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			// �����κ��� �޼����� ���� �� �ֵ��� �Ѵ�. �⺻���� true�̴�.
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);
			// Header ������ ����
			con.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");

			// BODY ������ ����
			OutputStreamWriter wr = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");
			wr.write(unitlistsql.toString());
			wr.flush();

			// ���ϵ� ��� �б�
			String inputLine = null;
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));

			while ((inputLine = rd.readLine()) != null) {
				line_total = line_total + inputLine;
				//System.out.println(inputLine);
			}

			rd.close();
			wr.close();

			line_total = line_total.replaceAll("n:", "");
			System.out.println(line_total);

			// xml�Ľ�2
			InputStream in = new ByteArrayInputStream(
					line_total.getBytes("UTF-8"));

			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(in);

			Element element = document.getRootElement();
			List envel_list = element.getChildren();
			//System.out.println("envel_list:" + envel_list);

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

			pstmt = null;
			rs = null;
			cStmt = null;

			//System.out.println("envel_list.size:" + envel_list.size());
			Element envel_el = (Element) envel_list.get(0);
			body_list = envel_el.getChildren();

			//System.out.println("body_list.size:" + body_list.size());
			Element body_el = (Element) body_list.get(0);
			result_list = body_el.getChildren("ManageProductResponse");

			ResponseType = body_el.getChildText("ResponseType");
			ProductId = body_el.getChildText("ProductId");
			//System.out.println("ResponseType:"+ body_el.getChildText("ResponseType"));
			System.out.println("ProductId:" + body_el.getChildText("ProductId"));

			StringBuffer optlistsql = new StringBuffer();
			if (ResponseType.equals("SUCCESS")) {
				// ���̹� �������� ����

				NaverShopnSendItem.run3(ProductId, n_item_id);

			}

		} catch (Exception e) {
			System.out.println("run111() : " + e.getMessage());
		} finally {
		}

		return response_type;
	}

	private String run3(String ProductId, String n_item_id) { // �ɼ� �������� �޼ҵ�
		System.out.println("�ڡڡڡڡڡڡڡڡڡڡڡڡڡڡڡ�run3�ڡڡڡڡڡڡڡڡڡڡڡ�");
		String response_type = "FALSE";
		NaverShopnSendItem NaverShopnSendItem = new NaverShopnSendItem();
		try {
			SimpleCryptLib SimpleCryptLib = new SimpleCryptLib();
			Security.addProvider(new BouncyCastleProvider());

			String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43";
			String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";

			String serviceName = "ProductService"; // ���񽺸�
			String id = "eugink";
			String password = "asdf0101";
			String timestamp = null;
			String signature = null;
			String data = null;
			String item_id = "";

			byte[] encryptKey = null;

			String encryptedData = null;
			String decryptedData = null;
			String hashedData = null;

			String operationName = "GetOption";
			String ResponseType = "";

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			CallableStatement cStmt = null;
			// String orderID = "200087036";

			// timestamp create
			timestamp = SimpleCryptLib.getTimestamp();
			System.out.println("timestamp:" + timestamp);
			System.out.println("ProductService");

			// generateSign
			data = timestamp + serviceName + operationName;
			signature = SimpleCryptLib.generateSign(data, secretKey);

			// generateKey
			encryptKey = SimpleCryptLib.generateKey(timestamp, secretKey);

			// encrypt
			encryptedData = SimpleCryptLib.encrypt(encryptKey,
					password.getBytes("UTF-8"));

			// decrypt
			decryptedData = new String(SimpleCryptLib.decrypt(encryptKey,
					encryptedData), "UTF-8");

			// sha256
			hashedData = SimpleCryptLib.sha256(password);

			StringBuffer optlistsql = new StringBuffer();
			optlistsql.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n ");
			optlistsql
					.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:shop=\"http://shopn.platform.nhncorp.com/\"> ");
			optlistsql
					.append("   <soap:Header/>                                                                                                    ");
			optlistsql
					.append("   <soap:Body>                                                                                                       ");
			optlistsql
					.append("      <shop:GetOptionRequest>                                                                                     ");
			optlistsql
					.append("         <shop:RequestID>njoyny2</shop:RequestID>");
			optlistsql.append("         <shop:AccessCredentials>");
			optlistsql.append("            <shop:AccessLicense>"
					+ accessLicense + "</shop:AccessLicense>");
			optlistsql.append("            <shop:Timestamp>" + timestamp
					+ "</shop:Timestamp>");
			optlistsql.append("            <shop:Signature>" + signature
					+ "</shop:Signature>");
			optlistsql.append("         </shop:AccessCredentials>");
			optlistsql.append("         <shop:Version>2.0</shop:Version>");
			optlistsql.append("         <SellerId>njoyny2</SellerId>");
			optlistsql
					.append("            <shop:ProductId>"
							+ ProductId
							+ "</shop:ProductId>                                                                       ");
			optlistsql
					.append("        </shop:GetOptionRequest>                                                                                         ");
			optlistsql
					.append("    </soap:Body>                                         ");
			optlistsql
					.append("    </soap:Envelope>                                         ");

			System.out.println("optlistsql:"+optlistsql);
			String line_total = "";
			URL url = new URL(
					"http://ec.api.naver.com/ShopN/ProductService");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			// �����κ��� �޼����� ���� �� �ֵ��� �Ѵ�. �⺻���� true�̴�.
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);
			// Header ������ ����
			con.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");

			// BODY ������ ����
			OutputStreamWriter wr = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");
			wr.write(optlistsql.toString());
			wr.flush();

			// ���ϵ� ��� �б�
			String inputLine = null;
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));

			while ((inputLine = rd.readLine()) != null) {
				line_total = line_total + inputLine;
				//System.out.println(inputLine);
			}

			rd.close();
			wr.close();

			line_total = line_total.replaceAll("n:", "");
			//System.out.println(line_total);

			// xml�Ľ�2
			ByteArrayInputStream in = new ByteArrayInputStream(
					line_total.getBytes("UTF-8"));

			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(in);

			Element element = document.getRootElement();
			List envel_list = element.getChildren();
			//System.out.println("envel_list:" + envel_list);

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


			Element envel_el = (Element) envel_list.get(0);
			body_list = envel_el.getChildren();

			Element body_el = (Element) body_list.get(0);
			result_list = body_el.getChildren("Option");
			
			Element body_el1 = (Element) result_list.get(0);
			result_list1 = body_el1.getChildren("Combination");

			Element body_el2 = (Element) result_list1.get(0);
			result_list2 = body_el2.getChildren("ItemList");
			
			Element body_el3 = (Element) result_list2.get(0);
			result_list3 = body_el3.getChildren("Item");

			Element body_el4 = null;
			int cnt = 0;
			for (int h = 0; h < result_list3.size(); h++) {

				body_el4 = (Element) result_list3.get(h);

				StringBuffer unitCnt = new StringBuffer();

				unitCnt.append(" select count(*) cnt \n");
				unitCnt.append(" from  \n");
				unitCnt.append(" ktc_unit_interlock \n");
				unitCnt.append(" where coven_id = 27346 and unit_id = ? \n");

				// pstmt.clearParameters();
				pstmt = conn.prepareStatement(unitCnt.toString());
				pstmt.setLong(1, Long.parseLong(body_el4.getChildText("SellerManagerCode")));

				// pstmt.setString(1, body_el3.getChildText("Value")); //
				// h_order_no
				rs = pstmt.executeQuery();

				while (rs.next()) {
					cnt = rs.getInt("cnt");
				}

				System.out.println("cnt:" + cnt);
				if (rs != null)
					try {
						rs.close();
					} catch (Exception e) {
					}
				if (pstmt != null)
					try {
						pstmt.close();
					} catch (Exception e) {
					}

				if (cnt == 0) {

					StringBuffer setCovenID = new StringBuffer();
					setCovenID
							.append(" insert into ktc_unit_interlock(coven_id , co_unit_id , item_id , unit_id ,insert_date,update_date)  \n");
					setCovenID
							.append(" values (27346,?,?,?,sysdate,sysdate)  \n");

					//System.out.println("setCovenIDinsert::"+ setCovenID.toString());

					pstmt = conn.prepareStatement(setCovenID.toString());

					int insert_cnt = 0;

					try {
						// pstmt.clearParameters();

						// �Ķ���ͼ���

						pstmt.setString(1, body_el4.getChildText("Id")); // �����ɼ�id
						pstmt.setString(2, n_item_id); // ��ǰid
						pstmt.setString(3,
								body_el4.getChildText("SellerManagerCode")); // unit_id


						pstmt.executeUpdate();						
						System.out.println("insert end");

						if (rs != null)
							try {
								rs.close();
							} catch (Exception e) {
							}
						if (pstmt != null)
							try {
								pstmt.close();
							} catch (Exception e) {
							}
					} catch (Exception e) {
						response_type = "FALSE";
						e.printStackTrace();
						conn.rollback();
					}
				} else {
					StringBuffer setCovenID = new StringBuffer();
					setCovenID.append(" update ktc_unit_interlock set  \n");
					setCovenID
							.append(" co_unit_id = ? ,item_id = ? ,update_date = sysdate  \n");
					setCovenID
							.append(" where coven_id = 27346 and unit_id = ? \n");


					pstmt = conn.prepareStatement(setCovenID.toString());

					int insert_cnt = 0;

					try {
						// pstmt.clearParameters();

						// �Ķ���ͼ���

						pstmt.setString(1, body_el4.getChildText("Id")); // �����ɼ�id
						pstmt.setString(2, n_item_id); // ��ǰid
						pstmt.setString(3,body_el4.getChildText("SellerManagerCode")); // unit_id
			

						pstmt.executeUpdate();
						System.out.println("end update111");
						if (rs != null)
							try {
								rs.close();
							} catch (Exception e) {
							}
						if (pstmt != null)
							try {
								pstmt.close();
							} catch (Exception e) {
							}
					} catch (Exception e) {
						response_type = "FALSE";
						e.printStackTrace();
						conn.rollback();
					}
				}

			}

			ResponseType = body_el.getChildText("ResponseType");
			ProductId = body_el.getChildText("ProductId");

		} catch (Exception e) {
			System.out.println("run3() : " + e.getMessage());
			response_type = "FALSE";
		} finally {
		}

		return response_type;
	}

	private String run2(String imgUrl) { // �̹������

		String response_type = "FALSE";
		NaverShopnSendItem NaverShopnSendItem = new NaverShopnSendItem();
		try {
			SimpleCryptLib SimpleCryptLib = new SimpleCryptLib();
			Security.addProvider(new BouncyCastleProvider());

			String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43";
			String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";

			String serviceName = "ImageService"; // ���񽺸�
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
			// String orderID = "200087036";

			// timestamp create
			timestamp = SimpleCryptLib.getTimestamp();
			System.out.println("timestamp:" + timestamp);
			System.out.println("ImageService");

			// generateSign
			data = timestamp + serviceName + operationName;
			signature = SimpleCryptLib.generateSign(data, secretKey);

			// generateKey
			encryptKey = SimpleCryptLib.generateKey(timestamp, secretKey);

			// encrypt
			encryptedData = SimpleCryptLib.encrypt(encryptKey,
					password.getBytes("UTF-8"));

			// decrypt
			decryptedData = new String(SimpleCryptLib.decrypt(encryptKey,
					encryptedData), "UTF-8");

			// sha256
			hashedData = SimpleCryptLib.sha256(password);

			String imglist = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:shop=\"http://shopn.platform.nhncorp.com/\">"
					+ "<soap:Header/>"
					+ "<soap:Body>"
					+ "<shop:UploadImageRequest>"
					+ "<!--Optional:-->"
					+ "<shop:RequestID>njoyny2</shop:RequestID>"
					+ "<shop:AccessCredentials>"
					+ "<shop:AccessLicense>"
					+ accessLicense
					+ "</shop:AccessLicense>"
					+ "<shop:Timestamp>"
					+ timestamp
					+ "</shop:Timestamp>"
					+ "<shop:Signature>"
					+ signature
					+ "</shop:Signature>"
					+ "</shop:AccessCredentials>"
					+ "<shop:Version>2.0</shop:Version>"
					+ "<SellerId>njoyny2</SellerId>"
					+ "<ImageURLList>"
					+ "<!--Zero or more repetitions:-->"
					+ "<shop:URL>"
					+ imgUrl
					+ "</shop:URL>"
					+ "</ImageURLList>"
					+ "</shop:UploadImageRequest>"
					+ "</soap:Body>"
					+ "</soap:Envelope>";

			// Create socket
			String hostname = "ec.api.naver.com";
			// String hostname = "api.naver.com";
			int port = 80;
			InetAddress addr = InetAddress.getByName(hostname);
			Socket sock = new Socket(addr, port);

			// Send header
			String path = "/ShopN/ImageService";
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
					sock.getOutputStream(), "UTF-8"));
			// You can use "UTF8" for compatibility with the Microsoft virtual
			// machine.
			wr.write("POST " + path + " HTTP/1.0 \r\n");
			wr.write("Host: ec.api.naver.com \r\n");
			// wr.write("Host: api.naver.com \r\n");
			// wr.write("Content-Length: " + xmldata.length() + "\r\n");
			wr.write("Content-Length: " + imglist.length() + "\r\n");
			wr.write("Content-Type: text/xml; charset=\"UTF-8\"\r\n");
			wr.write("SOAPAction: \"http://ec.api.naver.com/ShopN/ImageService\" \r\n");
			// wr.write("SOAPAction: \"http://api.naver.com/Checkout/MallService2\" \r\n");
			wr.write("\r\n");

			// Send data
			// wr.write(xmldata);
			wr.write(imglist);
			wr.flush();
			// InputStream test = new
			// InputStream(sock.getInputStream(),"UTF-8");

			// Response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					sock.getInputStream(), "UTF-8"));
			String line = "";
			String line_total = "";
			String tmp = "";
			String tmp2 = "";
			String newxml = "";

			while ((line = rd.readLine()) != null) {
				if (line.startsWith("<?xml")) {
					line = line.replaceAll("&#xd;", " ");
					line_total = line_total + line;
					//System.out.println(line);
				}

			}
			rd.close();
			wr.close();

			line_total = line_total.replaceAll("n:", "");
			//System.out.println(line_total);

			line_total = line_total.replaceAll("n:", "");
			// System.out.println(sf.toString().trim());

			// xml�Ľ�2
			InputStream in = new ByteArrayInputStream(
					line_total.getBytes("UTF-8"));

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

			// conn.setAutoCommit(false);

			long UNIT_ID = 0;
			long cnt = 0;
			long interface_seq = 0;
			long DEL_QTY = 0;
			long ITEM_ID = 0;
			String ITEM_NAME = null;

			Element envel_el = (Element) envel_list.get(0);
			body_list = envel_el.getChildren();

			Element body_el = (Element) body_list.get(0);
			result_list = body_el.getChildren("ImageList");

			for (int h = 0; h < result_list.size(); h++) {

				Element body_el1 = (Element) result_list.get(h);

				result_list1 = body_el1.getChildren("Image");

				for (int i = 0; i < result_list1.size(); i++) {

					Element body_el2 = (Element) result_list1.get(i);

					response_type = body_el2.getChildText("URL");

				}
			}

		} catch (Exception e) {
			System.out.println("run2() : " + e.getMessage());
			response_type = "FALSE";
		} finally {
		}

		return response_type;
	}

	public String run(String InputItem) {
		StringBuffer itemlist = new StringBuffer(); // ��ǰ��ȸ����

		String response_type = "FALSE";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		CallableStatement cStmt = null;

		try {

			StringBuffer selectItem = new StringBuffer();

			selectItem.append(" select  \n");
			selectItem.append("  a.ITEM_ID               , \n");
			selectItem.append("  (select b.co_item_id from ktc_item_interlock b where coven_id = 27346 and b.item_id = a.item_id)     co_item_id      , \n");
			//selectItem.append("  (select CASE WHEN  sum(decode(use_yn,'Y',vir_stock_qty,0)) < 0 then 0 else sum(decode(use_yn,'Y',vir_stock_qty,0)) end  from ktc_unit where item_id = a.item_id 	) qty  	   , \n");
			selectItem.append("  (select  sum(CASE WHEN  vir_stock_qty <= 0 then decode(use_yn,'Y',1,0) else decode(use_yn,'Y',vir_stock_qty,0) end)  from ktc_unit where item_id = a.item_id 	) qty  	   , \n");
			selectItem.append("  a.ITEM_GB              	, \n");
			selectItem.append("  a.PRE_ITEM_NAME      		, \n");
			selectItem.append("  a.MODEL                 , \n");
			selectItem.append("  a.STD_CTG_ID            , \n");
			selectItem.append("  a.START_DATETIME      	, \n");
			selectItem.append("  a.END_DATETIME        	, \n");
			selectItem.append("  decode( (select sum(decode(use_yn,'Y',vir_stock_qty,0))  from ktc_unit where item_id = a.item_id 	)  , 0, 'SUSP' , decode(a.ITEM_STAT_CODE,120103,'SALE', 'SUSP'))   item_stat_code  	, \n");// SALE(�Ǹ���)// SUSP(�Ǹ�����)// QSTK(ǰ��))
			selectItem.append("  (select b.co_ctg_id from ktc_ctg_interlock b where b.coven_id = 27346 and b.sctg_id in (select c.sctg_id from ktc_ctgitem c where c.item_id = a.item_id) and rownum  = 1)     co_ctg_id  	, \n"); // ���̹�����ī�װ�
																																																									// ���̵�
			selectItem.append("  decode((select count(DC_SALE_PRICE) from KTC_SITEITEM where ITEM_ID = a.ITEM_ID and SITE_ID = 10 and sysdate between START_DATETIME and END_DATETIME and sysdate between DC_START_DATETIME and DC_END_DATETIME and rownum = 1), 0, (select SALE_PRICE from KTC_SITEITEM where ITEM_ID = a.ITEM_ID  and SITE_ID = 10 and sysdate between START_DATETIME and END_DATETIME and rownum = 1),decode((select DC_SALE_PRICE from KTC_SITEITEM where ITEM_ID = a.ITEM_ID  and SITE_ID = 10 and sysdate between START_DATETIME and END_DATETIME and rownum = 1),0,(select SALE_PRICE from KTC_SITEITEM where ITEM_ID = a.ITEM_ID  and SITE_ID = 10 and sysdate between START_DATETIME and END_DATETIME and rownum = 1),(select DC_SALE_PRICE from KTC_SITEITEM where ITEM_ID = a.ITEM_ID  and SITE_ID = 10 and sysdate between START_DATETIME and END_DATETIME and rownum = 1))) as SALE_PRICE, \n");
			selectItem.append("  a.MIN_ORDER_QTY       	, \n");
			selectItem.append("  a.EMP_ID                , \n");
			selectItem.append("  a.IMPORT_VEN_NAME     	, \n");
			selectItem.append("  a.PACKER_CODE          	, \n");
			selectItem.append("  a.PACK_MATR_CODE       	, \n");
			selectItem.append("  a.STOCK_DIS_YN         	, \n");
			selectItem.append("  a.VEN_ID                , \n");
			selectItem.append("  a.TAX_GB_CODE          	, \n");
			selectItem.append("  a.ORDER_UNIT_CODE      	, \n");
			selectItem.append("  a.CON_CARD_YN          	, \n");
			selectItem.append("  a.CON_CARD_PRICE      	, \n");
			selectItem.append("  a.ACCOUNT_METHOD_CODE  	, \n");
			selectItem.append("  a.DELY_AREA_CODE       	, \n");
			selectItem.append("  a.LEADTIME_CODE        	, \n");
			selectItem.append("  decode(a.LOW_PRICE_YN,'Y',3,1)  LOW_PRICE_YN        	, \n");
			selectItem.append("  a.LOW_PRICE_STD        	, \n");
			selectItem.append("  a.LOW_PRICE_DELY_FEE   	, \n");
			selectItem.append("  a.BUY_TYPE_CODE       	, \n");
			selectItem.append("  a.DELI_TYPE_CODE       	, \n");
			selectItem.append("  a.WHCENTER_CODE       	, \n");
			selectItem.append("  a.GIFT_PACK_YN         	, \n");
			selectItem.append("  a.GIFT_PACK_FEE        	, \n");
			selectItem.append("  a.BRAND_ID             	, \n");
			selectItem.append("  a.QUICK_YN             	, \n");
			selectItem.append("  a.NOINTEREST_YN        	, \n");
			selectItem.append("  a.NEW_YN               	, \n");
			selectItem.append("  a.RECOMM_YN           	, \n");
			selectItem.append("  a.BEST_YN              	, \n");
			selectItem.append("  a.GIFT_SET_ID          	, \n");
			selectItem.append("  a.GIFT_OFFER_CNT_CODE  	, \n");
			selectItem.append("  a.GIFT_ACC_CNT         	, \n");
			selectItem.append("  a.GIFT_LIMIT_CNT       	, \n");
			selectItem.append("  a.GIFT_START_DATETIME  	, \n");
			selectItem.append("  a.GIFT_END_DATETIME    	, \n");
			selectItem.append("  a.HEADCOPY             	, \n");
			selectItem.append("  a.ITEM_DESC                , \n");
			selectItem.append("  a.ETC                  	, \n");
			selectItem.append("  a.IMG_SET_ID           	, \n");
			selectItem.append("  a.GG_PRICE_METH_CODE   	, \n");
			selectItem.append("  a.GG_SLIDE_YN          	, \n");
			selectItem.append("  a.GG_BUY_PRICE         	, \n");
			selectItem.append("  a.GG_SALE_PRICE        	, \n");
			selectItem.append("  a.GG_SLIDE_PRICE       	, \n");
			selectItem.append("  a.GG_SLIDE_QTY         	, \n");
			selectItem.append("  a.GG_SALE_QTY          	, \n");
			selectItem.append("  a.GG_NET_COMMISSION   	, \n");
			selectItem.append("  a.GG_COMMISSION_CODE  	, \n");
			selectItem.append("  a.INSERT_DATE          	, \n");
			selectItem.append("  a.INSERT_ID           	, \n");
			selectItem.append("  a.MODIFY_DATE          	, \n");
			selectItem.append("  a.MODIFY_ID            	, \n");
			selectItem.append("  (select code_val from ktc_code where code_id = a.ORIGIN_NAME )    ORIGIN_NAME      	, \n");
			selectItem.append("  a.BT_YN                	, \n");
			selectItem.append("  a.ITEM_NAME            	, \n");
			selectItem.append("  a.MAKING_VEN_CODE     	, \n");
			selectItem.append("  a.IMGX                 	, \n");
			selectItem.append("  a.IMGL1                	, \n");
			selectItem.append("  a.IMGL2                	, \n");
			selectItem.append("  a.IMGM1                	, \n");
			selectItem.append("  a.IMGM2                	, \n");
			selectItem.append("  a.IMGS1                	, \n");
			selectItem.append("  a.IMGS2                	, \n");
			selectItem.append("  a.ITMID                	, \n");
			selectItem.append("  a.STORE_ID            	, \n");
			selectItem.append("  a.REG_METHOD_GB        	, \n");
			selectItem.append("  a.REG_VEN_ID           	, \n");
			selectItem.append("  a.VOD_PATH            	, \n");
			selectItem.append("  a.RTN_YN               	, \n");
			selectItem.append("  a.MALL_NAME           	, \n");
			selectItem.append("  a.MALL_URL             	, \n");
			selectItem.append("  a.HUSOISU_YN           	, \n");
			selectItem.append("  a.PENALTY_YN           	, \n");
			selectItem.append("  a.SEQ_NUM              	, \n");
			selectItem.append("  a.CASH_DISCOUNT_YN     	, \n");
			selectItem.append("  a.TWELVE_H_YN          	, \n");
			selectItem.append("  a.FIX_FEE_YN           	, \n");
			selectItem.append("  a.BUYER_DELY_FEE_YN    	, \n");
			selectItem.append("  a.SETYN                	, \n");
			selectItem.append("  a.VIAKTC               	, \n");
			selectItem.append("  a.PREBUYING            	, \n");
			selectItem.append("  a.ANOTHER_MARKET_YN    	, \n");
			selectItem.append("  a.SPECIAL_DC_FORBID    	, \n");
			selectItem.append("  a.CHINA_COMMERCE_YN    	, \n");
			selectItem.append("  a.MALL_URL2            	, \n");
			selectItem.append("  a.COUPON_MEMO          	, \n");
			selectItem.append("  a.FASHIONPLUS_YN       	, \n");
			selectItem.append("  a.INTERPARK_YN         	, \n");
			selectItem.append("  a.BUYER_DELY_FEE_PRICE 	, \n");
			selectItem.append("  a.SINGLE_PLANNING      	, \n");
			selectItem.append("  a.CURRENCY             	, \n");
			selectItem.append("  a.OUTSIDE_ID           	, \n");
			selectItem.append("  a.SINGLE_ITEM_YN       	, \n");
			selectItem.append("  a.STYLE_E2A            	, \n");
			selectItem.append("  a.CODYITEM_1           	, \n");
			selectItem.append("  a.CODYITEM_2           	, \n");
			selectItem.append("  a.CODYITEM_3           	, \n");
			selectItem.append("  a.CODYITEM_4           	, \n");
			selectItem.append("  a.REJECT_DESC          	, \n");
			selectItem.append("  a.SHIPPLAN_DATE        	, \n");
			selectItem.append("  a.RESERVSHIP_YN        	, \n");
			selectItem.append("  a.ELEC_SAFE         ,   	 \n");
			
			selectItem.append("  b.kind        	, \n");
			selectItem.append("  b.itemid        	, \n");
			selectItem.append("  b.string1        ,   	 \n");		
			selectItem.append("  b.string2        ,   	 \n");		
			selectItem.append("  b.string3        ,   	 \n");		
			selectItem.append("  b.string4        ,  	 \n");		
			selectItem.append("  b.string5        ,   	 \n");		
			selectItem.append("  b.string6         ,  	 \n");		
			selectItem.append("  b.string7          , 	 \n");		
			selectItem.append("  b.string8        ,   	 \n");		
			selectItem.append("  b.string9        ,   	 \n");					
			selectItem.append("  b.string10         ,  	 \n");		
			selectItem.append("  b.string11         ,  	 \n");		
			selectItem.append("  b.string12         ,  	 \n");		
			selectItem.append("  b.string13         ,  	 \n");		
			selectItem.append("  b.string14         ,  	 \n");		
			selectItem.append("  b.string15           	 \n");		
			
			selectItem.append(" from ktc_item a ,mirus_detailitem b \n");
			selectItem.append(" where a.item_id > 5000000  \n");
			selectItem.append(" and a.item_id = b.itemid \n");	
			
			selectItem.append(" and a.ven_id in (select ven_id from ktc_vendor where van_status = 120846 ) \n"); // ��ü����
																												// �߰�
																												// 20120109
																												// ����ǥ
			//selectItem.append(" and a.std_ctg_id in ( select ctg_id from ktc_stdctg where prnt_ctg_id=5994 )  \n"); // �����ϰ��
			selectItem.append(" and a.item_id = ? \n");

			System.out.println("selectItem:"+selectItem.toString());
			/* 20120723 ����ǥ ���� */

			/* ��ǰ��� ���� */

			try {


				if (rs != null)
					try {
						rs.close();
					} catch (Exception e) {
					}
				if (pstmt != null)
					try {
						pstmt.close();
					} catch (Exception e) {
					}

				// NaverShopnSendItem NaverShopnSendItem= new
				// NaverShopnSendItem();

				pstmt = conn.prepareStatement(selectItem.toString());
				pstmt.setString(1, InputItem);
				System.out.println("InputItem:"+InputItem);
				rs = pstmt.executeQuery();
				System.out.println("rs:"+rs.getRow());
				while (rs.next()) {
					System.out.println("selectItem in:"+selectItem.toString());					

					SimpleCryptLib SimpleCryptLib = new SimpleCryptLib();
					Security.addProvider(new BouncyCastleProvider());

					String accessLicense = "0100010000f3814c41974dc3e7f98d1cd213fa8b84c396872ff6c1abb57f0d2516f31cfb43";
					String secretKey = "AQABAADKObge3IgWtlgfbo1TaLqHKpjfyGNKYuZbfOZB8m+WJA==";

					String serviceName = "ProductService"; // ���񽺸�
					String id = "eugink";
					String password = "asdf0101";
					String timestamp = null;
					String signature = null;
					String data = null;

					byte[] encryptKey = null;

					String encryptedData = null;
					String decryptedData = null;
					String hashedData = null;

					String operationName = "ManageProduct";
					String ResponseType = "";

					// String orderID = "200087036";

					// timestamp create
					timestamp = SimpleCryptLib.getTimestamp();
					System.out.println("timestamp:" + timestamp);
					System.out.println("ManageProduct");
					// generateSign
					data = timestamp + serviceName + operationName;
					signature = SimpleCryptLib.generateSign(data, secretKey);

					// generateKey
					encryptKey = SimpleCryptLib.generateKey(timestamp,
							secretKey);

					// encrypt
					encryptedData = SimpleCryptLib.encrypt(encryptKey,
							password.getBytes("UTF-8"));

					// decrypt
					decryptedData = new String(SimpleCryptLib.decrypt(
							encryptKey, encryptedData), "UTF-8");

					// sha256
					hashedData = SimpleCryptLib.sha256(password);

					NaverShopnSendItem NaverShopnSendItem = new NaverShopnSendItem();

					String ProductId = "";
					String item_id = "";
					String n_item_id = "";

					item_id = rs.getString("co_item_id");
					n_item_id = rs.getString("item_id");
					
					
					
					
					/*
					 * CLOB������ �޾ƿ���
					 */
					
					// 1.StringBuffer, char[], Reader�� �������ش�. 
					StringBuffer  stringbuffer  = new StringBuffer(); 
					char[]         charbuffer   = new char[1024];
					Reader         reader        = null;   //�̳ѿ��� CLOB�� ������� ��������.
					// 2. ResultSet�� �ִ� CLOB �����͸� Reader�� �ִ´�.
					reader = rs.getCharacterStream("ITEM_DESC");
					int read = 0;
					// 3.  StringBuffer���� append ���ش�. 1024�� ©��..
					while ((read = reader.read(charbuffer, 0, 1024)) != -1){
					      stringbuffer.append(charbuffer, 0, read);
					} 
					// 4. ������ StringBuffer�� append ��  ���� �־��ش�.
					String rpl_cont = stringbuffer.toString();
					// 5. StringBuffer�� �ʱ�ȭ �Ѵ�. (����� ���ϸ�, ���� �����Ϳ� ���� �����Ͱ� �پ ���´�.)
					stringbuffer.delete(0, stringbuffer.capacity());

					
					

					itemlist.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n ");
					itemlist.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:shop=\"http://shopn.platform.nhncorp.com/\">");
					itemlist.append("   <soap:Header/>");
					itemlist.append("   <soap:Body>");
					itemlist.append("      <shop:ManageProductRequest>");
					itemlist.append("         <shop:RequestID>njoyny2</shop:RequestID>");
					itemlist.append("         <shop:AccessCredentials>");
					itemlist.append("            <shop:AccessLicense>" + accessLicense + "</shop:AccessLicense>");
					itemlist.append("            <shop:Timestamp>" + timestamp + "</shop:Timestamp>");
					itemlist.append("            <shop:Signature>" + signature + "</shop:Signature>");
					itemlist.append("         </shop:AccessCredentials>");
					itemlist.append("         <shop:Version>2.0</shop:Version>");
					itemlist.append("         <SellerId>njoyny2</SellerId>");
					itemlist.append("         <Product>");
					if (item_id != null) {
						itemlist.append("            <shop:ProductId>"
								+ item_id + "</shop:ProductId>"); // item_id
																	// ��ǰ�ڵ�
																	// �űԵ�Ͻ�:null
																	// ������:��ǰid(���̹�)
					}
					itemlist.append("            <shop:StatusType>"+rs.getString("item_stat_code")+"</shop:StatusType>"); // SALE
																							// (�Ǹ���)
																							// SUSP(�Ǹ�����)
																							// QSTK(ǰ��))
					itemlist.append("            <shop:SaleType>NEW</shop:SaleType>"); // ��ǰ�Ǹ�����
																						// �ڵ�
																						// ���Է½ýŻ�ǰ����
																						// ����
																						// NEW:�Ż�
					//�űԻ�ǰ�ϰ�쿡�� ī�װ����� ���� 20130329��������û
					if (item_id == null) {
						itemlist.append("            <shop:CategoryId>"
								+ rs.getString("co_ctg_id") + "</shop:CategoryId>"); // ī�װ�����
																				
					}
					
					itemlist.append("            <shop:LayoutType>BASIC</shop:LayoutType>"); // BASIC
																								// :
																								// ��������
																								// IMAGE:�̹���
																								// ������
					itemlist.append("            <shop:Name><![CDATA["
							+ NaverShopnSendItem
									.removeXssString(NaverShopnSendItem
											.parsingSpecialforXml(rs
													.getString("item_name")))
							+ "]]></shop:Name>"); // ��ǰ��
					itemlist.append("            <shop:SellerManagementCode>"
							+ rs.getString("item_id")
							+ "</shop:SellerManagementCode>"); // �Ǹ��ڻ�ǰ�ڵ�
					itemlist.append("            <shop:Model>");
					itemlist.append("               <shop:ManufacturerName><![CDATA["
							+ rs.getString("MAKING_VEN_CODE")
							+ "]]></shop:ManufacturerName>"); // ������
					// itemlist.append("               <shop:BrandName><![CDATA["+
					// rs.getString("brand_id") +"]]></shop:BrandName>" );
					// //�귣���
					itemlist.append("            </shop:Model>");
					itemlist.append("            <shop:OriginArea>"); // ����������
																		// GetOriginAreaList
					itemlist.append("               <shop:Code>03</shop:Code>"); // �󼼼���
																					// ǥ��
					itemlist.append("            </shop:OriginArea>");
					itemlist.append("            <shop:TaxType>TAX</shop:TaxType>"); // �ΰ���
					itemlist.append("            <shop:MinorPurchasable>Y</shop:MinorPurchasable>"); // �̼��ⱸ��
																										// */
					itemlist.append("            <shop:Image>");
					itemlist.append("               <shop:Representative>"); // ��ǥ�̹���
																				// 450*450
					itemlist.append("                  <shop:URL><![CDATA["
							+ NaverShopnSendItem.run2(rs.getString("IMGX"))
							+ "]]></shop:URL>");

					// itemlist.append("                  <shop:URL><![CDATA[http://beta.shop1.phinf.naver.net/20120829_211/nmp_1346214765097FUx6u_JPEG/45862043648053663_117747847.jpg]]></shop:URL>"
					// );
					itemlist.append("               </shop:Representative>");
					itemlist.append("            </shop:Image>");

					// String DetailContent =
					// NaverShopnSendItem.parsingSpecialforXml("�Ǹ�ó/������ :"+rs.getString("MAKING_VEN_CODE")+"/"+
					// rs.getString("ORIGIN_NAME")+"<br>"+
					// rs.getString("ITEM_DESC")) ;
					itemlist.append("            <shop:DetailContent><![CDATA["
							+ "�Ǹ�ó/������ :"
											+ rs.getString("MAKING_VEN_CODE")
											+ "/" + rs.getString("ORIGIN_NAME")
											+ "<br>"
											+ rpl_cont
							+ "]]></shop:DetailContent>"); // ��ǰ������
					// itemlist.append("            <shop:DetailContent><![CDATA["+"�Ǹ�ó/������ :"+rs.getString("MAKING_VEN_CODE")+"/"+
					// rs.getString("ORIGIN_NAME")+ ""
					// +"]]></shop:DetailContent>" ); //��ǰ������
					itemlist.append("            <shop:AfterServiceTelephoneNumber><![CDATA["
							+ "02-1577-3212"
							+ "]]></shop:AfterServiceTelephoneNumber>"); // AS��ȭ��ȣ(�ʼ�)
					itemlist.append("            <shop:AfterServiceGuideContent><![CDATA["
							+ "��ȯ �� ��ǰ���Ǵ� �����̴��� �����ͷ� �����ּ���."
							+ "]]></shop:AfterServiceGuideContent>"); // A/S�ȳ�
					itemlist.append("            <shop:PurchaseReviewExposure>Y</shop:PurchaseReviewExposure>"); // ��������⿩��
					itemlist.append("            <shop:KnowledgeShoppingProductRegistration>Y</shop:KnowledgeShoppingProductRegistration>"); // ���ļ��ε��
					itemlist.append("            <shop:SalePrice>"
							+ rs.getString("SALE_PRICE") + "</shop:SalePrice>"); // �ǸŰ�
					itemlist.append("            <shop:StockQuantity>"+ rs.getString("qty") + "</shop:StockQuantity>");
					
					
					itemlist.append("            <shop:Delivery>");
					itemlist.append("            <shop:Type>1</shop:Type>");
					itemlist.append("            <shop:BundleGroupAvailable>N</shop:BundleGroupAvailable>");
					itemlist.append("            <shop:PayType>2</shop:PayType>");  //��ۺ������ڵ�  1 ���� 3 ����
					itemlist.append("            <shop:FeeType>"+ rs.getString("low_price_yn") + "</shop:FeeType>");  //��ۺ������ڵ�  1 ���� 3 ����
					itemlist.append("            <shop:BaseFee>"+ rs.getString("LOW_PRICE_DELY_FEE") + "</shop:BaseFee>");  //��ۺ�
					itemlist.append("            <shop:ReturnDeliveryCompanyPriority>0</shop:ReturnDeliveryCompanyPriority>");  //�������
					itemlist.append("            <shop:ReturnFee>2500</shop:ReturnFee>");  // ��ǰ��ۺ�
					itemlist.append("            <shop:ExchangeFee>2500</shop:ExchangeFee>");  //��ȯ��ۺ�
					itemlist.append("            </shop:Delivery>");

					
					
					/*
					 * ��ǰ������� �߰� start 20121112
					 */
					itemlist.append("            <shop:ProductSummary>");  //��ǰ������� 20121112

					if("3".equals(rs.getString("kind"))||"4".equals(rs.getString("kind"))){
						
						itemlist.append(" <shop:Shoes> ");  //�Ź� ShoesSummaryType
						itemlist.append(" <shop:Material>�Ѱ�:" + rs.getString("string1") +"/�Ȱ�:"+ rs.getString("string2") +"</shop:Material> "); //��ǰ ����
						itemlist.append(" <shop:Color>" + rs.getString("string3") + "</shop:Color> "); //����
						itemlist.append(" <shop:Size>" + rs.getString("string4") + "</shop:Size> "); //�߱���
						itemlist.append(" <shop:Height>" + rs.getString("string5") + "</shop:Height> "); //������
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string6") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Caution>" + rs.getString("string8") + "</shop:Caution> "); //��޽����ǻ���
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string9") + "</shop:WarrantyPolicy> "); //ǰ����������
						itemlist.append(" <shop:AfterServiceDirector>" + "1577-1533" + "</shop:AfterServiceDirector> "); //a/så����					
						itemlist.append(" </shop:Shoes> ");  //�Ź�
						
					}else if("1".equals(rs.getString("kind"))||"2".equals(rs.getString("kind"))){
						
						itemlist.append(" <shop:Wear> "); //�Ƿ� WearSummaryType
						itemlist.append(" <shop:Material>" + rs.getString("string1") + "</shop:Material> "); //��ǰ����
						itemlist.append(" <shop:Color>" + rs.getString("string2") + "</shop:Color> "); //����
						itemlist.append(" <shop:Size>" + rs.getString("string3") + "</shop:Size> "); //ġ��
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string4") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Caution>" + rs.getString("string6") + "</shop:Caution> "); //��Ź����� ��޽����ǻ���
						//itemlist.append(" <shop:PackDate> string </shop:PackDate> "); //�������
						itemlist.append(" <shop:PackDateText>" + rs.getString("string7") + "</shop:PackDateText> "); //������� �����Է� ���Է½� �ʼ�
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string8") + "</shop:WarrantyPolicy> "); //ǰ����������
						itemlist.append(" <shop:AfterServiceDirector>" + "1577-1533" + "</shop:AfterServiceDirector> ");	//a/så����					
						itemlist.append(" </shop:Wear>"); //�Ƿ�
						
					}else if("5".equals(rs.getString("kind"))||"6".equals(rs.getString("kind"))){	
						
						itemlist.append(" <shop:Bag> ");  //���� BagSummaryType
						itemlist.append(" <shop:Type>" + rs.getString("string1") + "</shop:Tye> "); //����
						itemlist.append(" <shop:Material>" + rs.getString("string2") + "</shop:Material> "); //����
						itemlist.append(" <shop:Color>" + rs.getString("string3") + "</shop:Color> "); //����
						itemlist.append(" <shop:Size>" + rs.getString("string4") + "</shop:Size> "); //ũ��
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string5") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Caution>" + rs.getString("string7") + "</shop:Caution> "); //��޽����ǻ���
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string8") + "</shop:WarrantyPolicy> "); //ǰ����������
						itemlist.append(" <shop:AfterServiceDirector>" + "1577-1533" + "</shop:AfterServiceDirector> ");	//a/så����					
						itemlist.append(" </shop:Wear>");  //����
						
					}else if("7".equals(rs.getString("kind"))||"8".equals(rs.getString("kind"))){	
						
						itemlist.append(" <shop:FashionItems> ");  //�м���ȭ FashionItemsSummaryType
						itemlist.append(" <shop:Type>" + rs.getString("string1") + "</shop:Tye> "); //����
						itemlist.append(" <shop:Material>" + rs.getString("string2") + "</shop:Material> "); //����
						itemlist.append(" <shop:Size>" + rs.getString("string3") + "</shop:Size> "); //ġ��
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string4") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Caution>" + rs.getString("string6") + "</shop:Caution> "); //��޽� ���ǻ���
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string7") + "</shop:WarrantyPolicy> "); //ǰ����������
						itemlist.append(" <shop:AfterServiceDirector>" + "1577-1533" + "</shop:AfterServiceDirector> ");	 //a/så����					
						itemlist.append(" </shop:FashionItems>");  //�м���ȭ
						
					}else if("13".equals(rs.getString("kind"))||"14".equals(rs.getString("kind"))){	
						
						itemlist.append(" <shop:HomeAppliances> ");  //������ǰ(������) HomeAppliancesSummaryType
						itemlist.append(" <shop:ItemName>" + rs.getString("string1") + "</shop:ItemName> ");  //ǰ��
						itemlist.append(" <shop:ModelName>" + rs.getString("string1") + "</shop:ModelName> "); //�𵨸�
						//itemlist.append(" <shop:Certified> string </shop:Certified> "); //�����ǰ �������� ������
						itemlist.append(" <shop:RatedVoltage>" + rs.getString("string3") + "</shop:RatedVoltage> "); //��������
						itemlist.append(" <shop:PowerConsumption>" + rs.getString("string3") + "</shop:PowerConsumption> "); //�Һ�����
						itemlist.append(" <shop:EnergyEfficiencyRating>" + rs.getString("string3") + "</shop:EnergyEfficiencyRating> "); //������ȿ�����
						itemlist.append(" <shop:ReleaseDate>" + rs.getString("string4") + "</shop:ReleaseDate> "); //���ϸ��� ��ó�� 'yyyy-mm'
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string5") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Size>" + rs.getString("string7") + "</shop:Size> "); //ũ��
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string8") + "</shop:WarrantyPolicy> "); //ǰ����������
						itemlist.append(" <shop:AfterServiceDirector>" + "1577-1533" + "</shop:AfterServiceDirector> ");	 //a/så����					
						itemlist.append(" </shop:HomeAppliances>");  //������ǰ(������)
						
					}else if("17".equals(rs.getString("kind"))||"18".equals(rs.getString("kind"))){	
						
						itemlist.append(" <shop:MedicalAppliances> "); //�Ƿ��� MedicalAppliancesSummaryType
						itemlist.append(" <shop:ItemName>" + rs.getString("string1") + "</shop:ItemName> "); //ǰ��
						itemlist.append(" <shop:ModelName>" + rs.getString("string1") + "</shop:ModelName> "); //�𵨸�
						itemlist.append(" <shop:LicenceNo>" + rs.getString("string2") + "</shop:LicenceNo> "); //�Ƿ�������㰡 �Ű��ȣ
						//itemlist.append(" <shop:AdvertisingCertified> string </shop:AdvertisingCertified> "); //�����������������
						//itemlist.append(" <shop:Certified> string </shop:Certified> "); //kc����������
						itemlist.append(" <shop:RatedVoltage>" + rs.getString("string4") + "</shop:RatedVoltage> "); //��������
						itemlist.append(" <shop:PowerConsumption>" + rs.getString("string4") + "</shop:PowerConsumption> "); //�Һ�����
						itemlist.append(" <shop:ReleaseDate>" + rs.getString("string5") + "</shop:ReleaseDate> "); //��ó�� 'yyyy-mm'
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string6") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Purpose>" + rs.getString("string8") + "</shop:Purpose> "); //������
						itemlist.append(" <shop:Usage><![CDATA[" + rs.getString("string8") + "]]></shop:Usage> "); //�����
						itemlist.append(" <shop:Caution>" + rs.getString("string9") + "</shop:Caution> "); //��޽����ǻ���
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string10") + "</shop:WarrantyPolicy> ");  //ǰ����������
						itemlist.append(" <shop:AfterServiceDirector>" + "1577-1533" + "</shop:AfterServiceDirector> ");	//a/så����					
						itemlist.append(" </shop:MedicalAppliances>"); //�Ƿ���
						
					}else if("11".equals(rs.getString("kind"))||"12".equals(rs.getString("kind"))){	
						
						itemlist.append(" <shop:Cosmetic>"); //ȭ��ǰ CosmeticSummaryType
						itemlist.append(" <shop:Capacity>" + rs.getString("string1") + "</shop:Capacity> ");   //�뷮�Ǵ� �߷�
						itemlist.append(" <shop:Specification>" + rs.getString("string2") + "</shop:Specification> "); //��ǰ�ֿ� ���
						//itemlist.append(" <shop:ExpirationDate> string </shop:ExpirationDate> "); //�����ѶǴ� ������ ���Ⱓ
						itemlist.append(" <shop:ExpirationDateText>" + rs.getString("string3") + "</shop:ExpirationDateText> "); //�����ѶǴ� ������ ���Ⱓ(�����Է�)
						itemlist.append(" <shop:Usage><![CDATA[" + rs.getString("string4") + "]]></shop:Usage> "); //�����
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string5") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Distributor>" + rs.getString("string5") + "</shop:Distributor> "); //�����Ǹž���
						itemlist.append(" <shop:MainIngredient>" + rs.getString("string7") + "</shop:MainIngredient> "); //�ֿ伺��
						//itemlist.append(" <shop:Certified> string </shop:Certified> "); //��ǰ�Ǿ�ǰ����û �ɻ������� Y,N
						itemlist.append(" <shop:Caution>" + rs.getString("string9") + "</shop:Caution> "); //���ǻ���
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string10") + "</shop:WarrantyPolicy> "); //ǰ����������
						itemlist.append(" <shop:CustomerServicePhoneNumber>" + "1577-1533" + "</shop:CustomerServicePhoneNumber> ");  //�Һ��ڻ�������ȭ��ȣ						
						itemlist.append(" </shop:Cosmetic>"); //ȭ��ǰ
						
					}else if("9".equals(rs.getString("kind"))||"10".equals(rs.getString("kind"))){	
						
						itemlist.append(" <shop:Jewellery> "); //��� JewellerySummaryType
						itemlist.append(" <shop:Material>" + rs.getString("string1") + "</shop:Material> "); //����
						itemlist.append(" <shop:Purity>" + rs.getString("string1") + "</shop:Purity> "); //����
						itemlist.append(" <shop:BandMaterial>" + rs.getString("string1") + "</shop:BandMaterial> "); //�������
						itemlist.append(" <shop:Weight>" + rs.getString("string2") + "</shop:Weight> "); //�߷�
						itemlist.append(" <shop:Manufacturer>" + rs.getString("string3") + "</shop:Manufacturer> "); //������
						itemlist.append(" <shop:Producer>" + rs.getString("string4") + "</shop:Producer> "); //������
						itemlist.append(" <shop:Size>" + rs.getString("string5") + "</shop:Size> "); //ġ��
						itemlist.append(" <shop:Caution>" + rs.getString("string6") + "</shop:Caution> "); //��������ǻ���
						itemlist.append(" <shop:Specification>" + rs.getString("string7") + "</shop:Specification> "); //�ֿ����
						//itemlist.append(" <shop:ProvideWarranty>" + rs.getString("string8") + "</shop:ProvideWarranty> "); //�������������� Y,N
						itemlist.append(" <shop:ProvideWarranty>N</shop:ProvideWarranty> "); //�������������� Y,N
						itemlist.append(" <shop:WarrantyPolicy>" + rs.getString("string9") + "</shop:WarrantyPolicy> "); //ǰ������ ����
						itemlist.append(" <shop:AfterServiceDirector>" + "1577-1533" + "</shop:AfterServiceDirector> ");	//a/så����					
						itemlist.append(" </shop:Jewellery>"); //���
						
					}else if("15".equals(rs.getString("kind"))||"16".equals(rs.getString("kind"))){	
						
						itemlist.append(" <shop:DietFood> ");		//�ǰ�������ǰ	 DietFoodSummaryType
						itemlist.append(" <shop:FoodType>" + rs.getString("string1") + "</shop:FoodType> "); //��ǰ������
						itemlist.append(" <shop:Producer>" + rs.getString("string2") + "</shop:Producer> "); //������
						itemlist.append(" <shop:Location>" + rs.getString("string2") + "</shop:Location> "); //������
						//itemlist.append(" <shop:PackDate> string </shop:PackDate> "); //�������
						itemlist.append(" <shop:PackDateText>" + rs.getString("string3") + "</shop:PackDateText> "); //�������(�����Է�)
						//itemlist.append(" <shop:ExpirationDate> string </shop:ExpirationDate> "); //������� �Ǵ� ǰ����������
						itemlist.append(" <shop:ExpirationDateText>" + rs.getString("string3") + "</shop:ExpirationDateText> "); //������� �Ǵ� ǰ����������(�����Է�)
						itemlist.append(" <shop:Weight>" + rs.getString("string4") + "</shop:Weight> "); //����������뷮
						itemlist.append(" <shop:Amount>" + rs.getString("string4") + "</shop:Amount> "); //��������� ����
						itemlist.append(" <shop:Ingredients>" + rs.getString("string5") + "</shop:Ingredients> "); //������ �� �Է�
						itemlist.append(" <shop:NutritionFacts>" + rs.getString("string6") + "</shop:NutritionFacts> "); //��������
						itemlist.append(" <shop:Specification>" + rs.getString("string7") + "</shop:Specification> "); //�������
						itemlist.append(" <shop:CautionAndSideEffect>" + rs.getString("string8") + "</shop:CautionAndSideEffect> "); //���뷮,������ �� ����� ���ǻ���
						itemlist.append(" <shop:NonMedicinalUsesMessage>" + rs.getString("string9") + "</shop:NonMedicinalUsesMessage> "); //�������� �� ġ�Ḧ ���� �Ǿ�ǰ�� �ƴ϶�� �����ǹ���
						itemlist.append(" <shop:GeneticallyModified>" + rs.getString("string10") + "</shop:GeneticallyModified> "); //������ ������ ��ǰ�� �ش��ϴ� ����� ǥ�� y,n
						//itemlist.append(" <shop:AdvertisingCertified> string </shop:AdvertisingCertified> "); //ǥ�ñ��� ���� ������ ����
						itemlist.append(" <shop:ImportDeclarationCheck>" + rs.getString("string12") + "</shop:ImportDeclarationCheck> "); //���Խ�ǰ�� �ش��ϴ� ��� �Ű� ������ y,n
						itemlist.append(" <shop:CustomerServicePhoneNumber>" + "1577-1533" + "</shop:CustomerServicePhoneNumber> "); //�Һ��ڻ����� ��ȭ��ȣ
						itemlist.append(" </shop:DietFood>	");		//�ǰ�������ǰ	
						
					}
					
					/*
					 * ��ǰ������� �߰� end 20121112
					 */					
					
					itemlist.append("            </shop:ProductSummary>");					
					
					
					
					
					
					
					
					itemlist.append("		</Product>");
					itemlist.append("		</shop:ManageProductRequest>");
					itemlist.append("		</soap:Body>");
					itemlist.append("		</soap:Envelope>");



					System.out.println("itemlist.toString():"+itemlist.toString());

/************************************/
					int pre_cnt = 0;
					
					StringBuffer unitCnt1 = new StringBuffer();
					
					unitCnt1.append(" select count(*) cnt \n");
					unitCnt1.append(" from ( \n");
					unitCnt1.append("  select nvl(b.coven_id,0) coven_id,a.unit_name,nvl(b.co_unit_id,0) co_unit_id,a.unit_id,a.vir_stock_qty,decode(a.use_yn,'Y','Y','N') use_yn  \n");
					unitCnt1.append(" from ktc_unit a,ktc_unit_interlock b \n");
					unitCnt1.append(" where a.unit_id = b.unit_id(+)  \n");
					unitCnt1.append(" and a.unit_name is not null  \n");
					unitCnt1.append(" and a.item_id = ? \n");
					unitCnt1.append(" )where  coven_id in (27346)    \n");


					try {
						PreparedStatement pstmt3 = null;
						ResultSet rs3 = null;


						pstmt3 = conn.prepareStatement(unitCnt1.toString());
						pstmt3.setString(1, n_item_id); // h_order_no
						rs3 = pstmt3.executeQuery();
						

						while (rs3.next()) {
							pre_cnt = rs3.getInt("cnt");
						}


						/*if (pre_cnt > 0) {
							NaverShopnSendItem.run4(item_id, n_item_id, pre_cnt); // �ɼǵ��

						}*/


						if (rs3 != null)
							try {
								rs3.close();
							} catch (Exception e) {
							}
						if (pstmt3 != null)
							try {
								pstmt3.close();
							} catch (Exception e) {
							}
					} catch (Exception e) {
						System.out.println("�ɼǵ��() : " + e.getMessage());
					} finally {
					}					
					
					/**********************************/
					
					
					
					
					if (item_id != null && pre_cnt > 0) { //���ϵ� ��ǰ�ϰ�� ��ǰ�������� �ɼǼ����� ���� �Ѵ�.
						try {
							StringBuffer unitCnt = new StringBuffer();
		
							unitCnt.append(" select count(*) cnt \n");
							unitCnt.append(" from ( \n");
							unitCnt.append("  select decode(b.coven_id,27346,b.coven_id,0) coven_id,a.unit_name,decode(b.coven_id,27346,b.co_unit_id,0) co_unit_id,a.unit_id,a.vir_stock_qty,decode(a.use_yn,'Y','Y','N') use_yn   \n");
							unitCnt.append(" from ktc_unit a,ktc_unit_interlock b \n");
							unitCnt.append(" where a.unit_id = b.unit_id(+)  \n");
							unitCnt.append(" and a.unit_name is not null  \n");
							unitCnt.append(" and a.item_id = ? \n");
							unitCnt.append(" )where  coven_id in (27346,0)    \n");
		
							System.out.println("�ɼ� �� ���� ���!!!!!!!!!!!!");
							try {
								PreparedStatement pstmt3 = null;
								ResultSet rs3 = null;
		
		
								pstmt3 = conn.prepareStatement(unitCnt.toString());
								pstmt3.setString(1, n_item_id); // h_order_no
								rs3 = pstmt3.executeQuery();
								int cnt = 0;
		
								while (rs3.next()) {
									cnt = rs3.getInt("cnt");
								}
		
		
								if (cnt > 0) {
									NaverShopnSendItem.run4(item_id, n_item_id, cnt); // �ɼǵ��
		
								}
		
		
								if (rs3 != null)
									try {
										rs3.close();
									} catch (Exception e) {
									}
								if (pstmt3 != null)
									try {
										pstmt3.close();
									} catch (Exception e) {
									}
							} catch (Exception e) {
								System.out.println("�ɼǵ��() : " + e.getMessage());
							} finally {
							}
						} catch (Exception e) {
							System.out.println("�ɼǵ��() : " + e.getMessage());
						} finally {
						}
					}
					
					
					String line_total = "";
					URL url = new URL("http://ec.api.naver.com/ShopN/ProductService");
					HttpURLConnection con = (HttpURLConnection) url
							.openConnection();
					con.setDoOutput(true);
					con.setRequestMethod("POST");
					con.setDoOutput(true);
					// �����κ��� �޼����� ���� �� �ֵ��� �Ѵ�. �⺻���� true�̴�.
					con.setDoInput(true);
					con.setUseCaches(false);
					con.setDefaultUseCaches(false);
					// Header ������ ����
					con.addRequestProperty("Content-Type",
							"text/xml;charset=UTF-8");

					// BODY ������ ����
					OutputStreamWriter wr = new OutputStreamWriter(
							con.getOutputStream(), "UTF-8");
					wr.write(itemlist.toString());
					wr.flush();

					// ���ϵ� ��� �б�
					String inputLine = null;
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(con.getInputStream(), "UTF-8"));

					while ((inputLine = rd.readLine()) != null) {
						line_total = line_total + inputLine;
						System.out.println(inputLine);
					}

					rd.close();
					wr.close();


					line_total = line_total.replaceAll("n:", "");
					//System.out.println(line_total);

					// xml�Ľ�2
					InputStream in = new ByteArrayInputStream(
							line_total.getBytes("UTF-8"));

					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(in);

					Element element = document.getRootElement();
					List envel_list = element.getChildren();
					//System.out.println("envel_list:" + envel_list);

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

					PreparedStatement pstmt2 = null;
					ResultSet rs2 = null;
					CallableStatement cStmt2 = null;

					Element envel_el = (Element) envel_list.get(0);
					body_list = envel_el.getChildren();

					Element body_el = (Element) body_list.get(0);
					result_list = body_el.getChildren("ManageProductResponse");

					ResponseType = body_el.getChildText("ResponseType");
					ProductId = body_el.getChildText("ProductId");
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
					System.out.println("ResponseType:"+ResponseType);
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
 
					if (ResponseType.equals("SUCCESS")) {

						if (item_id == null) {

							StringBuffer setCovenID = new StringBuffer();
							setCovenID.append(" insert into ktc_item_interlock(coven_id , co_item_id , item_id ,insert_date,update_date)  \n");
							setCovenID.append(" values (27346,?,?,sysdate,sysdate)  \n");


							pstmt2 = conn.prepareStatement(setCovenID
									.toString());

							int insert_cnt = 0;

							try {
								// pstmt2.clearParameters();

								// �Ķ���ͼ���
								pstmt2.setString(1, ProductId); //����id
								pstmt2.setString(2, n_item_id); // h_order_no
								System.out.println("ProductId:" + ProductId);
								System.out.println("n_item_id:" + n_item_id);

								pstmt2.executeUpdate();
								System.out.println("end insert");
								if (rs2 != null)
									try {
										rs2.close();
									} catch (Exception e) {
									}
								if (pstmt2 != null)
									try {
										pstmt2.close();
									} catch (Exception e) {
									}
							} catch (Exception e) {
								response_type = "FALSE";
								e.printStackTrace();
								conn.rollback();
							}
						} else {


							StringBuffer setCovenID = new StringBuffer();
							setCovenID
									.append(" update ktc_item_interlock set   \n");
							setCovenID.append(" update_date = sysdate  \n");
							setCovenID
									.append(" where coven_id = 27346 and item_id = ?  \n");

							int insert_cnt = 0;
							pstmt2 = conn.prepareStatement(setCovenID
									.toString());

							try {
								// pstmt2.clearParameters();

								// �Ķ���ͼ���

								pstmt2.setString(1, n_item_id); // h_order_no
								pstmt2.executeUpdate();
								System.out.println("end update22");
								if (rs2 != null)
									try {
										rs2.close();
									} catch (Exception e) {
									}
								if (pstmt2 != null)
									try {
										pstmt2.close();
									} catch (Exception e) {
									}
							} catch (Exception e) {
								System.out.println("�ɼǵ��error() : " + e.getMessage());
								response_type = "FALSE";
								e.printStackTrace();
								conn.rollback();
							}
						}

					}

					if (rs2 != null)
						try {
							rs2.close();
						} catch (Exception e) {
						}
					if (pstmt2 != null)
						try {
							pstmt2.close();
						} catch (Exception e) {
						}

					System.out.println("pre_cnt:"+pre_cnt);
					if (item_id==null||pre_cnt == 0) { //����item_id�� ��������  ����
						StringBuffer unitCnt = new StringBuffer();
						System.out.println("�ɼ�ó�����!!!!");
						unitCnt.append(" select count(*) cnt \n");
						unitCnt.append(" from ( \n");
						unitCnt.append("  select decode(b.coven_id,27346,b.coven_id,0) coven_id,a.unit_name,decode(b.coven_id,27346,b.co_unit_id,0) co_unit_id,a.unit_id,a.vir_stock_qty,decode(a.use_yn,'Y','Y','N') use_yn   \n");
						unitCnt.append(" from ktc_unit a,ktc_unit_interlock b \n");
						unitCnt.append(" where a.unit_id = b.unit_id(+)  \n");
						unitCnt.append(" and a.unit_name is not null  \n");
						unitCnt.append(" and a.item_id = ? \n");
						unitCnt.append(" )where  coven_id in (27346,0)    \n");
	
						System.out.println("n_item_id:" + n_item_id);
						try {
							PreparedStatement pstmt3 = null;
							ResultSet rs3 = null;
	
	
							pstmt3 = conn.prepareStatement(unitCnt.toString());
							pstmt3.setString(1, n_item_id); // h_order_no
							rs3 = pstmt3.executeQuery();
							int cnt = 0;
	
							while (rs3.next()) {
								cnt = rs3.getInt("cnt");
							}
	
							
							if (cnt > 0) {
								if (pre_cnt == 0) {
									System.out.println("ProductId:"+item_id);
									System.out.println("n_item_id:"+n_item_id);
									System.out.println("cnt:"+cnt);									
									NaverShopnSendItem.run4(ProductId, n_item_id, cnt); // �ɼǵ��insert
								}else{
								
									NaverShopnSendItem.run4(ProductId, n_item_id, cnt); // �ɼǵ��update
									System.out.println("�ɼǵ��update end");
								}
							}
	
	
							if (rs3 != null)
								try {
									rs3.close();
								} catch (Exception e) {
								}
							if (pstmt3 != null)
								try {
									pstmt3.close();
								} catch (Exception e) {
								}
						} catch (Exception e) {
							System.out.println("�ɼǵ��() : " + e.getMessage());
						} finally {
						}
					}

				}
				//System.out.println("itemlist:" + itemlist.toString());
				if (rs != null) {
					try {
						rs.close();
					} catch (Exception e) {
						response_type = "FALSE";
					}
				}
				if (pstmt != null) {
					try {
						pstmt.close();
					} catch (Exception e) {
						response_type = "FALSE";
					}
				}

			} catch (Exception e) {
				System.out.println("run_1() : " + e.getMessage());
			} finally {
			}

			/* ��ǰ��� �� */

		} catch (Exception e) {
			System.out.println("run_2() : " + e.getMessage());
			response_type = "FALSE";
		} finally {
		}

		return response_type;
	}

	public String[] SetItemID() {
		StringBuffer itemlist = new StringBuffer(); // ��ǰ��ȸ����

		String[] response_type = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		CallableStatement cStmt = null;

		try {

			StringBuffer selectItem = new StringBuffer();

			selectItem.append(" select  \n");
			selectItem.append("  a.ITEM_ID                \n");
			selectItem.append(" from ktc_item a, ktc_ctgitem b  \n");
			selectItem.append(" where a.item_id = b.item_id \n");
			selectItem.append(" and a.item_id > 5000000  \n");
			selectItem.append(" and a.ven_id in (select ven_id from ktc_vendor where van_status = 120846 ) \n"); // ��ü����
			selectItem.append(" and (a.insert_date > sysdate - 1/12 or a.modify_date > sysdate - 1/12)  \n"); // 2�ð� �ȿ� ������ ������
			//and b.LCTG_ID not in (82)  �ؿܻ�ǰ �����߰� �ʿ�
			selectItem.append(" and instr(a.item_name, '����')=0   \n"); // ������ �ƴҰ��
			selectItem.append(" and (buyer_dely_fee_yn ='N' or 	buyer_dely_fee_yn is null)  \n"); // ������ �ƴҰ��ǥ
			selectItem.append(" and (a.std_ctg_id in ( select ctg_id from ktc_stdctg where prnt_ctg_id=5994 )  \n"); // �����ϰ��
			selectItem.append(" or ( b.lctg_id in (32,42,43,31,83,44) and a.ven_id in (select code_val from ktc_code where major_id = 1317596) )        )  \n"); // �ؿ��ϰ�� ���������ؿܾ�ü
			selectItem.append(" and a.std_ctg_id in ( select ctg_id from ktc_stdctg where prnt_ctg_id=5994 )   \n"); //�����ϰ��
			selectItem.append(" and a.item_id in (select itemid from mirus_detailitem)  \n"); //��ǰ��� ������			
			//selectItem.append(" and a.item_stat_code = 120103  \n"); //��ǰ���� �ӽ�
			selectItem.append(" and a.item_id not in (select code_val from ktc_code where major_id = 1317522 ) \n"); //NOT�ؿܹ�ۻ�ǰ 
			//selectItem.append(" and a.ven_id not in (select code_val from ktc_code where major_id = 1317480 ) \n");  //NOT�ؿܹ�۾�ü
			selectItem.append(" and a.ven_id not in (select code_val from ktc_code where major_id = 1317463) \n");	 //NOT�ؿܹ�۾�ü		
			selectItem.append(" and a.ven_id not in (select code_val from ktc_code where major_id = 1317560) \n"); //11���� ������ü	
			selectItem.append(" and a.insert_id != 19502 \n");  //���俥������	
			//selectItem.append(" and a.item_id in (7931180 ) \n");  //�ϴ� ��ü �����Ҷ��� �Ǹ����� ��ǰ���� ����	
		
			//selectItem.append(" and a.item_id in (7931180    )  \n");  //���俥������		
			selectItem.append(" group by a.item_id  \n");  	


			StringBuffer selectCnt = new StringBuffer();
			selectCnt.append(" select count(item_id) count from ( \n");
			selectCnt.append(" select  \n");
			selectCnt.append("  a.ITEM_ID                \n");
			selectCnt.append(" from ktc_item a, ktc_ctgitem b  \n");
			selectCnt.append(" where a.item_id = b.item_id \n");			
			selectCnt.append(" and a.item_id > 5000000  \n");
			selectCnt.append(" and a.ven_id in (select ven_id from ktc_vendor where van_status = 120846 ) \n"); // ��ü����																				// ����ǥ
			selectCnt.append(" and (a.std_ctg_id in ( select ctg_id from ktc_stdctg where prnt_ctg_id=5994 )  \n"); // �����ϰ��
			selectCnt.append(" or ( b.lctg_id in (32,42,43,31,83,44) and a.ven_id in (select code_val from ktc_code where major_id = 1317596) )        )  \n"); // �ؿ��ϰ�� ���������ؿܾ�ü
			selectCnt.append(" and a.std_ctg_id in ( select ctg_id from ktc_stdctg where prnt_ctg_id=5994 )  \n"); //����ī�װ���
			selectCnt.append(" and a.item_id in (select itemid from mirus_detailitem)  \n"); //��ǰ��� ������
			//selectCnt.append(" and a.item_stat_code = 120103  \n"); //��ǰ���� �ӽ�

			selectCnt.append(" and (a.insert_date > sysdate - 1/12 or a.modify_date > sysdate - 1/12)  \n"); // 2�ð� �ȿ� ������ ������
			//and b.LCTG_ID not in (82)  �ؿܻ�ǰ
			selectCnt.append(" and instr(a.item_name, '����')=0   \n"); // ������ �ƴҰ��
			selectCnt.append(" and (buyer_dely_fee_yn ='N' or 	buyer_dely_fee_yn is null)  \n"); // ������ �ƴҰ��
			selectCnt.append(" and a.item_id not in (select code_val from ktc_code where major_id = 1317522 ) \n"); //NOT�ؿܹ�ۻ�ǰ 
			//selectCnt.append(" and a.ven_id not in (select code_val from ktc_code where major_id = 1317480 ) \n"); //NOT�ؿܹ�۾�ü
			selectCnt.append(" and a.ven_id not in (select code_val from ktc_code where major_id = 1317463) \n");	 //NOT�ؿܹ�۾�ü			
			selectCnt.append(" and a.ven_id not in (select code_val from ktc_code where major_id = 1317560) \n"); //11���� ������ü	
			selectCnt.append(" and a.insert_id != 19502 \n");  //���俥������	
			//selectCnt.append(" and a.item_id in (7931180 ) \n");  //�ϴ� ��ü �����Ҷ��� �Ǹ����� ��ǰ���� ����				
			//selectCnt.append(" and a.item_id in (select item_id from ktc_item_interlock where coven_id = 27346) \n");  //�ϴ� ��ü �����Ҷ��� �Ǹ����� ��ǰ���� ����	
			
		 
			//selectCnt.append(" and a.item_id in (5986278   )  \n");  //���俥������	
			selectCnt.append(" group by a.item_id  \n");  			
			selectCnt.append(" )  \n");
			System.out.println("selectItem:" + selectItem);
			/* 20120723 ����ǥ ���� */
			System.out.println("selectCnt:" + selectCnt);
			/* ���� ��ǰ��üũ */
			pstmt = conn.prepareStatement(selectCnt.toString());

			rs = pstmt.executeQuery();
			int count = 0;
			int cnt = 0;

			while (rs.next()) {
				cnt = rs.getInt("count"); // ����üũ
			}

			/* ���� ��ǰ��üũ */

			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (Exception e) {
				}

			/* ���� ��ǰ��ȣ üũ */
			pstmt = conn.prepareStatement(selectItem.toString());

			rs = pstmt.executeQuery();

			response_type = new String[cnt];
			while (rs.next()) {

				System.out.println("cnt:" + cnt);
				response_type[count] = (String) rs.getString("item_id");
				count += 1; // ������


			}
			
			/* ���� ��ǰ��ȣ üũ */
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (Exception e) {
				}

		} catch (Exception e) {
			System.out.println("run_3() : " + e.getMessage());

		} finally {
		}

		return response_type;
	}

	public static void main(String args[]) throws Exception {

		NaverShopnSendItem _CJILR = new NaverShopnSendItem();
		_CJILR.init(); // DB����
		String[] productItemId = _CJILR.SetItemID();
		for (int i = 0; i < productItemId.length; i++) {
			_CJILR.run(productItemId[i]);
		}
		_CJILR.distroy();

	}

}
