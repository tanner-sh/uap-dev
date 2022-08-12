package com.tanner.prop.entity;

import com.tanner.devconfig.util.Encode;

public class PropInfo {

  private boolean enableHotDeploy;

  private DomainInfo domain;

  private boolean isEncode;

  private InternalServiceArray[] internalServiceArray;

  private String TransactionManagerProxyClass = "nc.bs.mw.tran.IerpTransactionManagerProxy";

  private String UserTransactionClass = "nc.bs.mw.tran.IerpUserTransaction";

  private String TransactionManagerClass = "nc.bs.mw.tran.IerpTransactionManager";

  private String SqlDebugSetClass = "nc.bs.mw.sql.UFSqlObject";

  private String XADataSourceClass = "nc.bs.mw.ejbsql.IerpXADataSource";

  private DataSourceMeta[] dataSource;

  private String fdbPath;

  private String tokenSeed;

  private String priviledgedToken;

  public PropInfo() {
  }

  public PropInfo(String name, DomainInfo domain, DataSourceMeta[] metas, boolean isEncode,
      InternalServiceArray[] internalService, boolean hotDeploy) {
    this.domain = domain;
    this.isEncode = isEncode;
    this.dataSource = metas;
    this.internalServiceArray = internalService;
    this.enableHotDeploy = hotDeploy;
  }

  public InternalServiceArray[] getInternalService() {
    return this.internalServiceArray;
  }

  public boolean isEncode() {
    return this.isEncode;
  }

  public void setEncode(boolean isEncode) {
    this.isEncode = isEncode;
  }

  public DataSourceMeta[] getDataSource() {
    DataSourceMeta[] metas = new DataSourceMeta[this.dataSource.length];
    for (int i = 0; i < metas.length; i++) {
      try {
        metas[i] = (DataSourceMeta) this.dataSource[i].clone();
        if (isEncode()) {
          metas[i].setPassword(new Encode().decode(metas[i].getPassword()));
        }
      } catch (CloneNotSupportedException e) {
      }
    }
    return metas;
  }

  public void setDataSource(DataSourceMeta[] dataSource) {
    DataSourceMeta[] metas = new DataSourceMeta[dataSource.length];
    for (int i = 0; i < metas.length; i++) {
      try {
        metas[i] = (DataSourceMeta) dataSource[i].clone();
        if (isEncode()) {
          metas[i].setPassword(new Encode().encode(metas[i].getPassword()));
        }
      } catch (CloneNotSupportedException e) {
      }
    }
    this.dataSource = metas;
  }

  public String getXADataSourceClass() {
    return this.XADataSourceClass;
  }

  public void setXADataSourceClass(String dataSourceClass) {
    this.XADataSourceClass = dataSourceClass;
  }

  public boolean isEnableHotDeploy() {
    return this.enableHotDeploy;
  }

  public void setEnableHotDeploy(boolean enableHotDeploy) {
    this.enableHotDeploy = enableHotDeploy;
  }

  public DomainInfo getDomain() {
    return this.domain;
  }

  public void setDomain(DomainInfo domain) {
    this.domain = domain;
  }

  public InternalServiceArray[] getInternalServiceArray() {
    return this.internalServiceArray;
  }

  public void setInternalServiceArray(InternalServiceArray[] internalService) {
    this.internalServiceArray = internalService;
  }

  public String getSqlDebugSetClass() {
    return this.SqlDebugSetClass;
  }

  public void setSqlDebugSetClass(String sqlDebugSetClass) {
    this.SqlDebugSetClass = sqlDebugSetClass;
  }

  public String getTransactionManagerClass() {
    return this.TransactionManagerClass;
  }

  public void setTransactionManagerClass(String transactionManagerClass) {
    this.TransactionManagerClass = transactionManagerClass;
  }

  public String getTransactionManagerProxyClass() {
    return this.TransactionManagerProxyClass;
  }

  public void setTransactionManagerProxyClass(String transactionManagerProxyClass) {
    this.TransactionManagerProxyClass = transactionManagerProxyClass;
  }

  public String getUserTransactionClass() {
    return this.UserTransactionClass;
  }

  public void setUserTransactionClass(String userTransactionClass) {
    this.UserTransactionClass = userTransactionClass;
  }

  public String getFdbPath() {
    return this.fdbPath;
  }

  public void setFdbPath(String fdbPath) {
    this.fdbPath = fdbPath;
  }

  public String getTokenSeed() {
    return this.tokenSeed;
  }

  public void setTokenSeed(String tokenSeed) {
    this.tokenSeed = tokenSeed;
  }

  public String getPriviledgedToken() {
    return this.priviledgedToken;
  }

  public void setPriviledgedToken(String priviledgedToken) {
    this.priviledgedToken = priviledgedToken;
  }
}
