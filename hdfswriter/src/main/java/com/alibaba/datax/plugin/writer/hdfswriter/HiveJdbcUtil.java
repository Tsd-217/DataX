package com.alibaba.datax.plugin.writer.hdfswriter;/*
 * @author Tsd
 * @date 2021/8/23 3:18 下午
 * 概要：
 *
 */

import ch.qos.logback.core.db.dialect.DBUtil;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.common.util.RetryUtil;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.Callable;

public class HiveJdbcUtil {

    private HiveJdbcUtil(){}

    private static synchronized Connection connect(String url, String user, String pass) {
        Properties prop = new Properties();
        prop.put("user", user);
        prop.put("password", pass);
        return connect(url, prop);
    }

    private static Connection connect(String url, Properties prop) {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            DriverManager.setLoginTimeout(Constant.TIMEOUT_SECONDS);
            return DriverManager.getConnection(url, prop);
        } catch (Exception e) {
            throw DataXException.asDataXException(HdfsWriterErrorCode.CONN_DB_ERROR," 具体错误信息为："+e);
        }
    }


    public static Connection getConnection(final String url, final String user, final String pass) {
        try {
            return RetryUtil.executeWithRetry(new Callable<Connection>() {
                @Override
                public Connection call() throws Exception {
                    return HiveJdbcUtil.connect(url, user,
                            pass);

                }
            }, 9, 1000L, true);
        } catch (Exception e) {
            throw DataXException.asDataXException(
                    HdfsWriterErrorCode.CONN_DB_ERROR,
                    String.format("数据库连接失败. 因为根据您配置的连接信息:%s获取数据库连接失败. 请检查您的配置并作出修改.", url), e);
        }

    }
    public static void closeDBResources(Statement stmt, Connection conn) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException unused) {
            }
        }

        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException unused) {
            }
        }
    }

}


