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
package org.rex.db.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Base Context.
 * 
 * @version 1.0, 2016-02-01
 * @since Rexdb-1.0
 */
public class BaseContext {

	private String contextId;

	private Date createTime;

	public BaseContext() {
		contextId = UUID.randomUUID().toString();
		this.createTime = Calendar.getInstance().getTime();
	}

	public String getContextId() {
		return contextId;
	}

	public Date getCreateTime() {
		return createTime;
	}

}
