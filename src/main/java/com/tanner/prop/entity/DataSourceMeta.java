package com.tanner.prop.entity;

import java.io.Serializable;

public class DataSourceMeta implements Serializable, Cloneable {

  private static final long serialVersionUID = 1774004595340815036L;
  private String dataSourceName = "design";
  private String oidMark = "ZZ";
  private String databaseUrl = "jdbc:sqlserver://127.0.0.1:1433;database=nc50";
  private String user = "sa";
  private String password = "sa";
  private String driverClassName = "component.microsoft.sqlserver.jdbc.SQLServerDriver";
  private String databaseType = "SQLSERVER2008";
  private int maxCon = 5;
  private int minCon = 5;
  private String dataSourceClassName = "nc.bs.mw.ejb.xares.IerpDataSource";
  private String xaDataSourceClassName = "nc.bs.mw.ejb.xares.IerpXADataSource";
  private int conIncrement = 0;
  private int conInUse = 0;
  private int conIdle = 0;
  private boolean isBase;

  public DataSourceMeta() {
  }

  public DataSourceMeta(String adataSourceName, String aoidMark, String databaseUrl, String user,
      String password, String driverClassName, String databaseType, String driverType, int maxCon,
      int minCon) {
    this.dataSourceName = adataSourceName;
    this.oidMark = aoidMark;
    this.databaseUrl = databaseUrl;
    this.user = user;
    this.password = password;
    this.driverClassName = driverClassName;
    this.databaseType = databaseType;
    this.maxCon = maxCon;
    this.minCon = minCon;
  }

  public String toString() {
    return getDataSourceName();
  }

  public int getConIdle() {
    return this.conIdle;
  }

  public void setConIdle(int conIdle) {
    this.conIdle = conIdle;
  }

  public int getConIncrement() {
    return this.conIncrement;
  }

  public void setConIncrement(int conIncrement) {
    this.conIncrement = conIncrement;
  }

  public int getConInUse() {
    return this.conInUse;
  }

  public void setConInUse(int conInUse) {
    this.conInUse = conInUse;
  }

  public String getDataSourceName() {
    return this.dataSourceName;
  }

  public void setDataSourceName(String dataSourceName) {
    this.dataSourceName = dataSourceName;
  }

  public String getDatabaseUrl() {
    return this.databaseUrl;
  }

  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public String getUser() {
    return this.user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String pwd) {
    this.password = pwd;
  }

  public String getDriverClassName() {
    return this.driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getDatabaseType() {
    return this.databaseType;
  }

  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
  }

  public int getMaxCon() {
    return this.maxCon;
  }

  public void setMaxCon(int maxCon) {
    this.maxCon = maxCon;
  }

  public int getMinCon() {
    return this.minCon;
  }

  public void setMinCon(int minCon) {
    this.minCon = minCon;
  }

  public String getDataSourceClassName() {
    return this.dataSourceClassName;
  }

  public void setDataSourceClassName(String dataSourceClassName) {
    this.dataSourceClassName = dataSourceClassName;
  }

  public String getOidMark() {
    return this.oidMark;
  }

  public void setOidMark(String oidMark) {
    this.oidMark = oidMark;
  }

  public String getXaDataSourceClassName() {
    return this.xaDataSourceClassName;
  }

  public void setXaDataSourceClassName(String xaDataSourceClassName) {
    this.xaDataSourceClassName = xaDataSourceClassName;
  }

  public boolean isDesign() {
    return this.dataSourceName.equals("design");
  }

  public boolean isBase() {
    return this.isBase;
  }

  public void setBase(boolean isBase) {
    this.isBase = isBase;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
