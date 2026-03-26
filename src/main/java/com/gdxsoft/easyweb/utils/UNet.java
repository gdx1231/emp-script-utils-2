package com.gdxsoft.easyweb.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.msnet.MStr;

/**
 * 访问网络的工具类（使用JDK内置HttpURLConnection）
 * 
 * @author admin
 */
public class UNet {
	private static Logger LOGGER = LoggerFactory.getLogger(UNet.class);
	public static String AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36";
	public static int C_TIME_OUT = 500000;
	public static int R_TIME_OUT = 500000;
	
	private String _LastUrl;
	private boolean _IsShowLog = false;
	private HashMap<String, String> _Headers = new HashMap<>();
	private HashMap<String, String> _Cookies = new HashMap<>();
	private Map<String, String> _ResponseHeaders = new HashMap<>();
	private String userAgent;
	private int _LastStatusCode;
	private String _LastErr;
	private String _LastResult;
	private byte[] _LastBuf;
	private String _Encode = "UTF-8"; // 字符集
	private int timeout = 0;
	private boolean ignoreInvalidCookieWarn = false;
	private int redirectInc = 0;
	private int _LimitRedirectInc = 7;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public UNet() {
	}

	public UNet(String cookie, String charsetName) {
		this._Encode = charsetName;
		this.setCookie(cookie);
	}

	public String getCookies() {
		MStr s = new MStr();
		for (Entry<String, String> entry : this._Cookies.entrySet()) {
			if (s.length() > 0) {
				s.a("; ");
			}
			s.a(entry.getKey());
			s.a("=");
			s.a(entry.getValue());
		}
		return s.toString();
	}

	private void initCookies(String cookie) {
		if (cookie == null || cookie.trim().isEmpty()) {
			return;
		}
		String[] cks = cookie.split(";");
		for (String ck : cks) {
			String[] parts = ck.split("=", 2);
			if (parts.length == 2) {
				this.addCookie(parts[0].trim(), parts[1].trim());
			}
		}
	}

	private void addCookie(String name, String val) {
		name = name.trim();
		this._Cookies.put(name, val);
	}

	public void addHeader(String key, String v) {
		this._Headers.put(key, v);
	}

	public void addHeaders(Map<String, String> headers) {
		if (headers == null) {
			return;
		}
		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			if ("content-length".equalsIgnoreCase(key) || 
			    "origin".equalsIgnoreCase(key) || 
			    "host".equalsIgnoreCase(key) || 
			    "connection".equalsIgnoreCase(key)) {
				continue;
			}
			this.addHeader(key, entry.getValue());
		}
	}

	public void clearHeaders() {
		this._Headers.clear();
	}

	public String getUserAgent() {
		return this.userAgent == null ? UNet.AGENT : this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getCookie() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : this._Cookies.entrySet()) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}
		return sb.toString();
	}

	public void setCookie(String cookie) {
		this._Cookies.clear();
		if (cookie != null && !cookie.isEmpty()) {
			initCookies(cookie);
		}
	}

	public int getLastStatusCode() {
		return _LastStatusCode;
	}

	public String getLastErr() {
		return _LastErr;
	}

	public String getLastResult() {
		return _LastResult;
	}

	public byte[] getLastBuf() {
		return _LastBuf;
	}

	public String getLastUrl() {
		return _LastUrl;
	}

	public boolean isShowLog() {
		return _IsShowLog;
	}

	public void setIsShowLog(boolean isShowLog) {
		_IsShowLog = isShowLog;
	}

	public int getLimitRedirectInc() {
		return _LimitRedirectInc;
	}

	public void setLimitRedirectInc(int limitRedirectInc) {
		this._LimitRedirectInc = limitRedirectInc;
	}

	public String getEncode() {
		return _Encode;
	}

	public void setEncode(String encode) {
		_Encode = encode;
	}

	public Map<String, String> getResponseHeaders() {
		return _ResponseHeaders;
	}

	public boolean isIgnoreInvalidCookieWarn() {
		return ignoreInvalidCookieWarn;
	}

	public void setIgnoreInvalidCookieWarn(boolean ignoreInvalidCookieWarn) {
		this.ignoreInvalidCookieWarn = ignoreInvalidCookieWarn;
	}

	private HttpURLConnection createConnection(String url) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		
		// 设置超时
		int connectTimeout = this.timeout > 0 ? this.timeout : C_TIME_OUT;
		int readTimeout = this.timeout > 0 ? this.timeout : R_TIME_OUT;
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		
		// 设置User-Agent
		conn.setRequestProperty("User-Agent", this.getUserAgent());
		
		// 设置Referer
		if (this._LastUrl != null) {
			conn.setRequestProperty("Referer", this._LastUrl);
		}
		
		// 设置自定义Headers
		for (Entry<String, String> entry : this._Headers.entrySet()) {
			conn.setRequestProperty(entry.getKey(), entry.getValue());
		}
		
		// 设置Cookie
		String cookies = this.getCookies();
		if (!cookies.isEmpty()) {
			conn.setRequestProperty("Cookie", cookies);
		}
		
		// 处理HTTPS信任所有证书
		if (conn instanceof HttpsURLConnection) {
			HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, new TrustManager[]{new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() { return null; }
					public void checkClientTrusted(X509Certificate[] certs, String authType) { }
					public void checkServerTrusted(X509Certificate[] certs, String authType) { }
				}}, new java.security.SecureRandom());
				httpsConn.setSSLSocketFactory(sc.getSocketFactory());
				httpsConn.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) { return true; }
				});
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				LOGGER.error("SSL setup failed", e);
			}
		}
		
		return conn;
	}

	private void saveResponseHeaders(HttpURLConnection conn) {
		this._ResponseHeaders.clear();
		Map<String, List<String>> headers = conn.getHeaderFields();
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String headerName = entry.getKey();
			if (headerName == null) continue;
			
			List<String> headerValues = entry.getValue();
			if (headerValues != null && !headerValues.isEmpty()) {
				String headerValue = String.join(", ", headerValues);
				this._ResponseHeaders.put(headerName, headerValue);
				
				// 处理Set-Cookie
				if ("Set-Cookie".equalsIgnoreCase(headerName)) {
					for (String cookieValue : headerValues) {
						parseSetCookieHeader(cookieValue);
					}
				}
			}
		}
	}

	private void parseSetCookieHeader(String setCookieHeader) {
		if (setCookieHeader == null || setCookieHeader.isEmpty()) {
			return;
		}
		String[] cookies = setCookieHeader.split(",");
		for (String cookie : cookies) {
			String[] parts = cookie.split(";", 2);
			if (parts.length > 0) {
				String[] nameValue = parts[0].split("=", 2);
				if (nameValue.length == 2) {
					this.addCookie(nameValue[0].trim(), nameValue[1].trim());
				}
			}
		}
	}

	private String readResponseString(HttpURLConnection conn) throws IOException {
		this._LastStatusCode = conn.getResponseCode();
		
		if (this._LastStatusCode >= 400) {
			LOGGER.error(this._LastStatusCode + " " + this._LastUrl);
		}
		
		InputStream inputStream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
		if (inputStream == null) {
			LOGGER.warn("返回内容为空 " + this._LastUrl);
			return null;
		}
		
		StringBuilder response = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, _Encode))) {
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line).append("\n");
			}
		}
		
		String result = response.toString();
		this._LastResult = result;
		return result;
	}

	private byte[] readResponseBytes(HttpURLConnection conn) throws IOException {
		this._LastStatusCode = conn.getResponseCode();
		
		if (this._LastStatusCode != 200) {
			LOGGER.error("response code: " + this._LastStatusCode);
			return null;
		}
		
		InputStream inputStream = conn.getInputStream();
		if (inputStream == null) {
			LOGGER.warn("返回没有下载内容");
			return null;
		}
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int nRead;
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		byte[] result = buffer.toByteArray();
		this._LastBuf = result;
		return result;
	}

	private String handleRequest(String url, String method, String body, Map<String, String> params) {
		this._LastUrl = url;
		this._LastErr = null;
		this._LastResult = null;
		this._LastBuf = null;
		this.redirectInc = 0;
		
		try {
			HttpURLConnection conn = createConnection(url);
			conn.setRequestMethod(method);
			
			// 设置请求体或参数
			if (body != null) {
				conn.setDoOutput(true);
				try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
					wr.writeBytes(body);
				}
			} else if (params != null && !params.isEmpty()) {
				conn.setDoOutput(true);
				String postData = buildPostData(params);
				try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
					wr.writeBytes(postData);
				}
			}
			
			// 读取响应
			String result = readResponseString(conn);
			saveResponseHeaders(conn);
			
			// 处理重定向
			if ((_LastStatusCode == 302 || _LastStatusCode == 301) && redirectInc < _LimitRedirectInc) {
				redirectInc++;
				String location = conn.getHeaderField("Location");
				if (location != null) {
					return handleRequest(location, "GET", null, null);
				}
			}
			
			return result;
		} catch (IOException e) {
			this._LastErr = e.getMessage();
			LOGGER.error("Request failed", e);
			return null;
		}
	}

	private String buildPostData(Map<String, String> params) {
		StringBuilder postData = new StringBuilder();
		for (Entry<String, String> param : params.entrySet()) {
			if (postData.length() != 0) postData.append('&');
			try {
				postData.append(URLEncoder.encode(param.getKey(), _Encode));
				postData.append('=');
				postData.append(URLEncoder.encode(param.getValue(), _Encode));
			} catch (Exception e) {
				// 使用默认编码
				postData.append(param.getKey());
				postData.append('=');
				postData.append(param.getValue());
			}
		}
		return postData.toString();
	}

	public String doGet(String url) {
		if (this._IsShowLog) {
			LOGGER.info("GET " + url);
		}
		return handleRequest(url, "GET", null, null);
	}

	public String doPost(String url, String body) {
		if (this._IsShowLog) {
			LOGGER.info("POST: " + url);
		}
		return handleRequest(url, "POST", body, null);
	}

	public String doPost(String url, Map<String, String> vals) {
		if (this._IsShowLog) {
			LOGGER.info("POST " + url);
		}
		return handleRequest(url, "POST", null, vals);
	}

	public String doPut(String url, String body) {
		if (this._IsShowLog) {
			LOGGER.info("PUT " + url);
		}
		return handleRequest(url, "PUT", body, null);
	}

	public String doDelete(String url) {
		if (this._IsShowLog) {
			LOGGER.info("DELETE " + url);
		}
		return handleRequest(url, "DELETE", null, null);
	}

	public byte[] downloadData(String url) {
		if (this._IsShowLog) {
			LOGGER.info("DW " + url);
		}
		
		this._LastUrl = url;
		this._LastErr = null;
		this._LastResult = null;
		this._LastBuf = null;
		
		try {
			HttpURLConnection conn = createConnection(url);
			conn.setRequestMethod("GET");
			
			byte[] result = readResponseBytes(conn);
			saveResponseHeaders(conn);
			return result;
		} catch (IOException e) {
			this._LastErr = e.getMessage();
			LOGGER.error("Download failed", e);
			return null;
		}
	}

	// 辅助类用于处理字节数组输出流
	private static class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {
		@Override
		public synchronized byte[] toByteArray() {
			return java.util.Arrays.copyOf(buf, count);
		}
	}
	
	// 兼容旧方法名
	public String postMsg(String u, String body) {
		return this.doPost(u, body);
	}
	
	public String patch(String u, String body) {
		return this.doPatch(u, body);
	}
	
	public String doPatch(String u, String body) {
		if (this._IsShowLog) {
			LOGGER.info("PATCH: " + u);
		}
		return handleRequest(u, "PATCH", body, null);
	}
}