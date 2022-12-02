package com.didi.cloud.fastdump.common.utils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.didi.cloud.fastdump.common.exception.FastDumpTransportException;
import com.google.common.collect.Maps;

public class BaseHttpUtil {
    protected static final Logger   LOGGER        = LoggerFactory.getLogger(ConvertUtil.class);
    public static final String      UTF8          = "UTF-8";
    public static final String      GBK           = "GBK";
    public static final String      GB2312        = "GB2312";

    private static final String     COOKIE_POLICY = "http.protocol.cookie-policy";
    private static final String     COMPATIBILITY = "compatibility";
    private static final String     EXCEPTION_1   = "UTF-8 is not surportted";
    private static final String     EXCEPTION_2   = "error post data to ";
    private static final String     RESPONSE      = "response=";

    private static final HttpClient HTTP_CLIENT;

    private BaseHttpUtil() {
    }

    public static String post(String url, Map<String, Object> params) throws FastDumpTransportException {
        return post(url, params, null, null, null);
    }

    public static String postEncode(String url, Map<String, Object> params, String reqEncode,
                                    String resEncode) throws FastDumpTransportException {
        return post(url, params, null, reqEncode, resEncode);
    }

    public static Map<String, String> buildHeader() {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    public static String post(String url, Map<String, Object> params, Map<String, String> headers, String reqEncode,
                              String resEncode) throws FastDumpTransportException {
        HttpPost post = new HttpPost(url);
        if (StringUtils.isBlank(reqEncode)) {
            reqEncode = UTF8;
        }

        if (StringUtils.isBlank(resEncode)) {
            resEncode = UTF8;
        }

        List<BasicNameValuePair> httpParams;
        Iterator var7;
        if (params != null && !params.isEmpty()) {
            httpParams = new ArrayList<>(params.size());
            var7 = params.entrySet().iterator();

            while (true) {
                Map.Entry e;
                while (var7.hasNext()) {
                    e = (Map.Entry) var7.next();
                    String k = (String) e.getKey();
                    Object v = e.getValue();
                    if (v == null) {
                        httpParams.add(new BasicNameValuePair(k, null));
                    } else if (!v.getClass().isArray()) {
                        httpParams.add(new BasicNameValuePair(k, v.toString()));
                    } else {
                        int len = Array.getLength(v);

                        for (int i = 0; i < len; ++i) {
                            Object element = Array.get(v, i);
                            if (element != null) {
                                httpParams.add(new BasicNameValuePair(k, element.toString()));
                            } else {
                                httpParams.add(new BasicNameValuePair(k, null));
                            }
                        }
                    }
                }

                if (headers != null) {
                    var7 = headers.entrySet().iterator();

                    while (var7.hasNext()) {
                        e = (Map.Entry) var7.next();
                        post.addHeader((String) e.getKey(), (String) e.getValue());
                    }
                }

                try {
                    post.setEntity(new UrlEncodedFormEntity(httpParams, reqEncode));
                    post.getParams().setParameter(COOKIE_POLICY, COMPATIBILITY);
                    break;
                } catch (UnsupportedEncodingException var20) {
                    throw new FastDumpTransportException(EXCEPTION_1, var20);
                }
            }
        }

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, resEncode);
        } catch (Exception var18) {
            throw new FastDumpTransportException(EXCEPTION_2 + url, var18);
        } finally {
            post.releaseConnection();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("class=BaseHttpUtil||method=post||mesg={}", RESPONSE + response);
        }

        return response;
    }

    public static String postForString(String url, String content,
                                       Map<String, String> headers) throws FastDumpTransportException {
        HttpPost post = new HttpPost(url);

        Iterator<Map.Entry<String, String>> var4;
        if (StringUtils.isNotBlank(content)) {
            if (headers != null) {
                var4 = headers.entrySet().iterator();

                while (var4.hasNext()) {
                    Map.Entry<String, String> e = var4.next();
                    post.addHeader(e.getKey(), e.getValue());
                }
            }

            try {
                BasicHttpEntity requestBody = new BasicHttpEntity();
                requestBody.setContent(new ByteArrayInputStream(content.getBytes(UTF8)));
                requestBody.setContentLength(content.getBytes(UTF8).length);
                post.setEntity(requestBody);
                post.getParams().setParameter(COOKIE_POLICY, COMPATIBILITY);
            } catch (UnsupportedEncodingException var12) {
                throw new FastDumpTransportException(EXCEPTION_1, var12);
            }
        }

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, UTF8);
            EntityUtils.consume(entity);
        } catch (Exception var10) {
            throw new FastDumpTransportException(EXCEPTION_2 + url, var10);
        } finally {
            post.releaseConnection();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("class=BaseHttpUtil||method=postForString||msg={}", RESPONSE + response);
        }

        return response;
    }

    public static String deleteForString(String url, String content,
                                         Map<String, String> headers) throws FastDumpTransportException {
        HttpDelete post = new HttpDelete(url);

        Iterator<Map.Entry<String, String>> var4;
        if (StringUtils.isNotBlank(content)) {
            if (headers != null) {
                var4 = headers.entrySet().iterator();

                while (var4.hasNext()) {
                    Map.Entry<String, String> e = var4.next();
                    post.addHeader(e.getKey(), e.getValue());
                }
            }

            try {
                BasicHttpEntity requestBody = new BasicHttpEntity();
                requestBody.setContent(new ByteArrayInputStream(content.getBytes(UTF8)));
                requestBody.setContentLength(content.getBytes(UTF8).length);
                post.getParams().setParameter(COOKIE_POLICY, COMPATIBILITY);
            } catch (UnsupportedEncodingException var12) {
                throw new FastDumpTransportException(EXCEPTION_1, var12);
            }
        }

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, UTF8);
            EntityUtils.consume(entity);
        } catch (Exception var10) {
            throw new FastDumpTransportException(EXCEPTION_2 + url, var10);
        } finally {
            post.releaseConnection();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("class=BaseHttpUtil||method=deleteForString||msg={}", RESPONSE + response);
        }

        return response;
    }

    public static String putForString(String url, String content,
                                      Map<String, String> headers) throws FastDumpTransportException {
        HttpPut post = new HttpPut(url);

        Iterator<Map.Entry<String, String>> var4;
        if (StringUtils.isNotBlank(content)) {
            if (headers != null) {
                var4 = headers.entrySet().iterator();

                while (var4.hasNext()) {
                    Map.Entry<String, String> e = var4.next();
                    post.addHeader(e.getKey(), e.getValue());
                }
            }

            try {
                BasicHttpEntity requestBody = new BasicHttpEntity();
                requestBody.setContent(new ByteArrayInputStream(content.getBytes(UTF8)));
                requestBody.setContentLength((long) content.getBytes(UTF8).length);
                post.setEntity(requestBody);
                post.getParams().setParameter(COOKIE_POLICY, COMPATIBILITY);
            } catch (UnsupportedEncodingException var12) {
                throw new FastDumpTransportException(EXCEPTION_1, var12);
            }
        }

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, UTF8);
            EntityUtils.consume(entity);
        } catch (Exception var10) {
            throw new FastDumpTransportException(EXCEPTION_2 + url, var10);
        } finally {
            post.releaseConnection();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("class=BaseHttpUtil||method=putForString||msg={}", RESPONSE + response);
        }

        return response;
    }

    public static String get(String url, Map<String, String> params) throws FastDumpTransportException {
        Iterator<Map.Entry<String, String>> var3;
        if (params != null) {
            StringBuilder builder = (new StringBuilder(url)).append('?');
            var3 = params.entrySet().iterator();

            while (var3.hasNext()) {
                Map.Entry<String, String> e = var3.next();
                builder.append(e.getKey()).append('=').append(e.getValue()).append('&');
            }

            url = builder.toString();
        }

        HttpGet get = new HttpGet(url);

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(get).getEntity();
            response = EntityUtils.toString(entity, UTF8);
        } catch (Exception var8) {
            throw new FastDumpTransportException(EXCEPTION_2 + url, var8);
        } finally {
            get.releaseConnection();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("class=BaseHttpUtil||method=get||msg={}", RESPONSE + response);
        }

        return response;
    }

    public static String get(String url, Map<String, String> params,
                             Map<String, String> headers) throws FastDumpTransportException {
        if (params != null) {
            StringBuilder builder = new StringBuilder(url).append('?');
            for (Map.Entry<String, String> e : params.entrySet()) {
                builder.append(e.getKey()).append('=').append(e.getValue()).append('&');
            }
            url = builder.toString();
        }

        HttpGet get = new HttpGet(url);
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                get.addHeader(e.getKey(), e.getValue());
            }
        }

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(get).getEntity();
            response = EntityUtils.toString(entity, UTF8);
        } catch (Exception e) {
            throw new FastDumpTransportException(EXCEPTION_2 + url, e);
        } finally {
            get.releaseConnection();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("class=BaseHttpUtil||method=get||msg={}", RESPONSE + response);
        }
        return response;
    }

    public static Header buildHttpHeader(String esUser, String passWord) {
        // 构建认证信息的header
        Header header;
        try {
            header = new BasicHeader("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", esUser, passWord).getBytes(UTF8)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return header;
    }

    static {
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);
        HTTP_CLIENT = new DefaultHttpClient(connectionManager);
        HTTP_CLIENT.getParams().setParameter("http.connection.timeout", 20000);
        HTTP_CLIENT.getParams().setParameter("http.socket.timeout", 20000);
    }
}