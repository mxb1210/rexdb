/**
 * Copyright 2016 the Rex-Soft Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rex.db.datasource.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.rex.db.exception.DBException;
import org.rex.db.util.ReflectUtil;

/**
 * Simple Connection Proxy
 * 
 * @version 1.0, 2016-01-29
 * @since Rexdb-1.0
 */
public class SimpleConnectionProxy implements InvocationHandler {

	private ArrayList<Statement> openedStatements;
	private SimpleConnectionPool connectionPool;
	private volatile boolean closed;
	private volatile boolean forceClosed;

	protected Connection delegate;

	private long creationTime;
	private long lastAccess;

	private Method[] overrideMethods;

	private ConnectionProxy proxy;

	private static final Set<String> SQL_ERRORS = new HashSet<String>(){
		private static final long serialVersionUID = 1L;
		{
			add("57P01"); // ADMIN SHUTDOWN
			add("57P02"); // CRASH SHUTDOWN
			add("57P03"); // CANNOT CONNECT NOW
			add("57P02"); // CRASH SHUTDOWN
			add("01002"); // SQL92 disconnect error
		}
	};

	public SimpleConnectionProxy() {
		openedStatements = new ArrayList<Statement>(64);
		creationTime = lastAccess = System.currentTimeMillis();
		overrideMethods = SimpleConnectionProxy.class.getMethods();
	}

	/**
	 * binds connection
	 */
	public ConnectionProxy bind(Connection conn) {
		this.delegate = conn;
		proxy = (ConnectionProxy) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { ConnectionProxy.class }, this);
		return proxy;
	}

	/**
	 * dynamic proxy
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		for (int i = 0; i < overrideMethods.length; i++) {
			if (methodEqueals(overrideMethods[i], method)) {
				return overrideMethods[i].invoke(this, args);
			}
		}
		return method.invoke(delegate, args);
	}

	/**
	 * Compares the given method and parameters
	 */
	private boolean methodEqueals(Method m1, Method m2) {
		if (m1.getName() == m2.getName()) {
			if (!m1.getReturnType().equals(m2.getReturnType()))
				return false;
			Class<?>[] params1 = m1.getParameterTypes();
			Class<?>[] params2 = m2.getParameterTypes();
			if (params1.length == params2.length) {
				for (int i = 0; i < params1.length; i++) {
					if (params1[i] != params2[i])
						return false;
				}
				return true;
			}
		}
		return false;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public long getLastAccess() {
		return lastAccess;
	}

	public void markLastAccess() {
		this.lastAccess = System.currentTimeMillis();
	}

	public void unclose() {
		closed = false;
	}

	public void setConnectionPool(SimpleConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	protected void checkClosed() throws SQLException {
		if (closed) {
			throw new SQLException("Connection is closed");
		}
	}

	public SQLException checkException(SQLException sqle) {
		String sqlState = sqle.getSQLState();
		forceClosed |= (sqlState != null && (sqlState.startsWith("08") || SQL_ERRORS.contains(sqlState)));
		return sqle;
	}

	public boolean isForceClosed() {
		return forceClosed;
	}

	// ----------------implements
	public void close() throws SQLException {
		if (!closed) {
			closed = true;
			try {
				for (int i = openedStatements.size() - 1; i >= 0; i--) {
					openedStatements.get(i).close();
				}
			} catch (SQLException e) {
				throw checkException(e);
			} finally {
				openedStatements.clear();
				connectionPool.releaseConnection(proxy);
			}
		}
	}

	public final void closeConnection() throws SQLException {
		delegate.close();
	}

	public boolean isClosed() throws SQLException {
		return closed;
	}

	public Statement createStatement() throws SQLException {
		checkClosed();
		try {
			Statement statement = delegate.createStatement();
			openedStatements.add(statement);

			return statement;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		checkClosed();
		try {
			Statement statement = delegate.createStatement(resultSetType, resultSetConcurrency);
			openedStatements.add(statement);

			return statement;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkClosed();
		try {
			Statement statement = delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
			openedStatements.add(statement);

			return statement;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		checkClosed();
		try {
			CallableStatement statement = delegate.prepareCall(sql);
			openedStatements.add(statement);

			return statement;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		checkClosed();
		try {
			CallableStatement statement = delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
			openedStatements.add(statement);

			return statement;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkClosed();
		try {
			CallableStatement statementProxy = delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
			openedStatements.add(statementProxy);

			return statementProxy;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		checkClosed();
		try {
			PreparedStatement statementProxy = delegate.prepareStatement(sql);
			openedStatements.add(statementProxy);

			return statementProxy;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		checkClosed();
		try {
			PreparedStatement statementProxy = delegate.prepareStatement(sql, autoGeneratedKeys);
			openedStatements.add(statementProxy);

			return statementProxy;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		checkClosed();
		try {
			PreparedStatement statementProxy = delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
			openedStatements.add(statementProxy);

			return statementProxy;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		checkClosed();
		try {
			PreparedStatement statementProxy = delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
			openedStatements.add(statementProxy);

			return statementProxy;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		checkClosed();
		try {
			PreparedStatement statementProxy = delegate.prepareStatement(sql, columnIndexes);
			openedStatements.add(statementProxy);

			return statementProxy;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		checkClosed();
		try {
			PreparedStatement statementProxy = delegate.prepareStatement(sql, columnNames);
			openedStatements.add(statementProxy);

			return statementProxy;
		} catch (SQLException e) {
			throw checkException(e);
		}
	}

	public boolean isValid(int timeout) throws SQLException {
		if (closed)
			return false;

		try {
			return (Boolean)ReflectUtil.invokeMethod(delegate, "isValid", new Class[]{int.class}, new Object[]{timeout});
		} catch (DBException e) {
			return false;
		}
	}

}
