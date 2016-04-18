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
package org.rex.db.logger.log4j;

import org.apache.commons.logging.LogFactory;
import org.rex.db.logger.Logger;
import org.rex.db.logger.LoggerFactory;

public class Log4jLoggerFactory extends LoggerFactory {

    @Override
    protected Logger getLoggerImpl(Class<?> cls) {
        return new Log4jLogger(LogFactory.getLog(cls));
    }
    
    @Override
    protected Logger getLoggerImpl(String name) {
        return new Log4jLogger(LogFactory.getLog(name));
    }

}
