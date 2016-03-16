package org.rex.db.configuration;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.rex.db.datasource.DataSourceManager;
import org.rex.db.dialect.Dialect;
import org.rex.db.dialect.DialectManager;
import org.rex.db.dynamic.javassist.BeanConvertorManager;
import org.rex.db.exception.DBException;
import org.rex.db.exception.DBRuntimeException;
import org.rex.db.exception.ExceptionResourceFactory;
import org.rex.db.listener.DBListener;
import org.rex.db.listener.ListenerManager;
import org.rex.db.logger.Logger;
import org.rex.db.logger.LoggerFactory;
import org.rex.db.transaction.Definition;
import org.rex.db.util.ReflectUtil;
import org.rex.db.util.ResourceUtil;

public class Configuration {
	
	//-----------------------------singlon instance
	private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
	
	private static final String DEFAULT_CONFIG_PATH = "rexdb.xml";
	
	/**
	 * 以单例模式运行
	 */
	private static volatile Configuration instance;
	
	//-----------------------------configuration
	/**
	 * 配置变量
	 */
	private volatile Properties variables;
	
	//--------settings
	/**
	 * 语言：zh-cn、en
	 */
	private volatile String lang;
	
	/**
	 * 关闭所有输出日志
	 */
	private volatile boolean nolog = false;
	
	/**
	 * 在执行SQL前执行基本校验，防止错误的SQL被发送到数据库
	 */
	private volatile boolean validateSql = true;
	
	/**
	 * 是否检查执行过程中的各种警告，如连接警告、Statement警告、结果集警告
	 */
	private volatile boolean checkWarnings = false;
	
	/**
	 * 执行查询的默认超时时间
	 */
	private volatile int queryTimeout = -1;
	
	/**
	 * 事物默认超时时间
	 */
	private volatile int transactionTimeout = -1;
	
	/**
	 * 事物中的操作执行失败时，是否自动回滚
	 */
	private volatile boolean autoRollback = false;
	
	/**
	 * 事物默认隔离级别
	 */
	private volatile String transactionIsolation;
	
	/**
	 * 启用反射缓存
	 */
	private volatile boolean reflectCache = true;
	
	/**
	 * 使用动态类替代反射调用
	 */
	private volatile boolean dynamicClass = true;
	
	/**
	 * 自动调整日期/时间类型，以适应
	 */
	private volatile boolean dateAdjust = true;
	
	//--------managers
	/**
	 * 数据源
	 */
	private final DataSourceManager dataSourceManager;
	
	/**
	 * 监听
	 */
	private final ListenerManager listenerManager;
	
	/**
	 * 方言
	 */
	private final DialectManager dialectManager;
	
	static{
		try {
			LOGGER.info("loading default configuration {0}.", DEFAULT_CONFIG_PATH);
			loadDefaultConfiguration();
			LOGGER.info("default configuration {0} loaded.", DEFAULT_CONFIG_PATH);
		} catch (DBException e) {
			LOGGER.warn("could not load default configuration {0} from classpath, rexdb is not initialized, cause {1}", DEFAULT_CONFIG_PATH, e.getMessage());
		}
	}
	
	//--构造函数
	public Configuration(){
		dataSourceManager = new DataSourceManager();
		listenerManager = new ListenerManager();
		dialectManager = new DialectManager();
	}
	
	/**
	 * 加载默认配置
	 */
	public synchronized static void loadDefaultConfiguration() throws DBException{
		if(instance != null)
			throw new DBException("DB-C10052", DEFAULT_CONFIG_PATH);
		
		InputStream inputStream = ResourceUtil.getResourceAsStream(DEFAULT_CONFIG_PATH);
		if(inputStream == null){
			LOGGER.warn("could not find configuration {0} in classpath.", DEFAULT_CONFIG_PATH);
		}else
			instance = new XMLConfigurationLoader().load(inputStream);
	}
	
	/**
	 * 从classpath中加载指定配置
	 * @param path classpath中的文件路径
	 * @throws DBException 加载失败时抛出异常
	 */
	public synchronized static void loadConfigurationFromClasspath(String path) throws DBException{
		if(instance != null)
			throw new DBException("DB-F0007", path);
		
		LOGGER.info("loading configuration {0} from classpath.", path);
		instance = new XMLConfigurationLoader().loadFromClasspath(path);
		LOGGER.info("configuration {0} loaded.", path);
	}
	
	/**
	 * 从文件系统中加载指定配置
	 * @param path 文件系统中的路径
	 * @throws DBException 加载失败时抛出异常
	 */
	public synchronized static void loadConfigurationFromFileSystem(String path) throws DBException{
		if(instance != null)
			throw new DBException("DB-F0007", path);
		
		LOGGER.info("loading configuration {0} from file system.", path);
		instance = new XMLConfigurationLoader().loadFromFileSystem(path);
		LOGGER.info("configuration {0} loaded.", path);
	}

	/**
	 * 获取当前配置
	 */
	public static Configuration getCurrentConfiguration() throws DBRuntimeException{
		if(instance == null){
			try {
				loadDefaultConfiguration();
			} catch (DBException e) {
				throw new DBRuntimeException(e);
			}
		}
		
		if(instance == null)
			throw new DBRuntimeException("DB-F0008", DEFAULT_CONFIG_PATH);
			
		return instance;
	}
	
	
	//---------------------------
	public void applySettings(){
		//语言版本
		if(lang != null){
			ExceptionResourceFactory.getInstance().setLang(lang);
		}
		
		//反射缓存
		if(!reflectCache){
			ReflectUtil.setCacheEnabled(false);
		}
		
		//日志
		if(nolog){
			LoggerFactory.setNolog(true);
		}

	}
	
	//---------------------------
	public void addVariables(Properties variables) {
		if(this.variables == null) 
			this.variables = variables;
		else
			this.variables.putAll(variables);
	}

	public Properties getVariables() {
		return variables;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
	public boolean isNolog() {
		return nolog;
	}

	public void setNolog(boolean nolog) {
		this.nolog = nolog;
	}
	
	public boolean isValidateSql() {
		return validateSql;
	}

	public void setValidateSql(boolean validateSql) {
		LOGGER.info("sql validate has switched to {0}.", validateSql);
		this.validateSql = validateSql;
	}

	public boolean isCheckWarnings() {
		return checkWarnings;
	}

	public void setCheckWarnings(boolean checkWarnings) {
		LOGGER.info("check warnings has switched to {0}.", validateSql);
		this.checkWarnings = checkWarnings;
	}
	
	public int getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public int getTransactionTimeout() {
		return transactionTimeout;
	}

	public void setTransactionTimeout(int transactionTimeout) {
		this.transactionTimeout = transactionTimeout;
	}

	public boolean isAutoRollback() {
		return autoRollback;
	}

	public void setAutoRollback(boolean autoRollback) {
		this.autoRollback = autoRollback;
	}

	public String getTransactionIsolation() {
		return transactionIsolation;
	}

	public void setTransactionIsolation(String transactionIsolation) {
		this.transactionIsolation = Definition.ISOLATION_CONSTANT_PREFIX + '_' +transactionIsolation;
	}
	
	public boolean isReflectCache() {
		return reflectCache;
	}

	public void setReflectCache(boolean reflectCache) {
		this.reflectCache = reflectCache;
	}
	
	public boolean isDynamicClass() {
		return dynamicClass;
	}

	public void setDynamicClass(boolean dynamicClass) {
		if(dynamicClass){//test dynamic
			try{
				BeanConvertorManager.getConvertor(this.getClass());
				this.dynamicClass = true;
			}catch(Throwable e){
				LOGGER.warn("dynamic class setting is true, but could not pass the validation test, now automatically switch to false, {0}", e, e.getMessage());
			}
		}
	}

	public boolean isDateAdjust() {
		return dateAdjust;
	}

	public void setDateAdjust(boolean dateAdjust) {
		this.dateAdjust = dateAdjust;
	}

	//-----------
	public void setDefaultDataSource(DataSource dataSource){
		dataSourceManager.setDefault(dataSource);
	}
	
	public void setDataSource(String id, DataSource dataSource){
		dataSourceManager.add(id, dataSource);
	}

	public DataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}
	
	public void addListener(DBListener listener){
		listenerManager.registe(listener);
	}

	public ListenerManager getListenerManager() {
		return listenerManager;
	}
	
	public void addDialect(DataSource dataSource, Dialect dialect){
		dialectManager.setDialect(dataSource, dialect);;
	}

	public DialectManager getDialectManager() {
		return dialectManager;
	}

	public String toString() {
		return "Configuration [variables=" + variables + ", lang=" + lang + ", dataSourceManager=" + dataSourceManager + ", listenerManager="
				+ listenerManager + ", dialectManager=" + dialectManager + "]";
	}
	
}
