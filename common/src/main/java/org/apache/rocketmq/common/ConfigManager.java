/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common;

import java.io.IOException;
import org.apache.rocketmq.common.constant.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象的配置管理（主要涉及读写配置文件）
 */
public abstract class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    public abstract String encode();

    /**
     *加载执行配置文件内容，并通过{@link #decode(String)}方法，注入到子类
     * @return
     */
    public boolean load() {
        String fileName = null;
        try {
            fileName = this.configFilePath();
            String jsonString = MixAll.file2String(fileName);

            if (null == jsonString || jsonString.length() == 0) {
                return this.loadBak();
            } else {
                this.decode(jsonString);
                log.info("load {} OK", fileName);
                return true;
            }
        } catch (Exception e) {
            log.error("load [{}] failed, and try to load backup file", fileName, e);
            return this.loadBak();
        }
    }

    /**
     * 具体的配置文件路径，由子类具体实现
     * @return
     */
    public abstract String configFilePath();

    /**
     * 尝试加载之前的备份文件（在文件持久化的时候，会先备份历史数据，可查看{@link #persist()}）
     * @return
     */
    private boolean loadBak() {
        String fileName = null;
        try {
            fileName = this.configFilePath();
            String jsonString = MixAll.file2String(fileName + ".bak");
            if (jsonString != null && jsonString.length() > 0) {
                this.decode(jsonString);
                log.info("load [{}] OK", fileName);
                return true;
            }
        } catch (Exception e) {
            log.error("load [{}] Failed", fileName, e);
            return false;
        }

        return true;
    }

    /**
     * 解析指定文件中的内容，自实现
     * @param jsonString 指定文件中的内容
     */
    public abstract void decode(final String jsonString);

    /**
     *同步，持久化配置项
     */
    public synchronized void persist() {
        String jsonString = this.encode(true);
        if (jsonString != null) {
            String fileName = this.configFilePath();
            try {
                MixAll.string2File(jsonString, fileName);
            } catch (IOException e) {
                log.error("persist file [{}] exception", fileName, e);
            }
        }
    }

    /**
     * 使用者把需要持久化的配置型，做自定义的处理，自实现
     * @param prettyFormat 是否格式化
     * @return
     */
    public abstract String encode(final boolean prettyFormat);
}
