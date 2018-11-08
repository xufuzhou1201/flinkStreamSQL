/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 

package com.dtstack.flink.sql.launcher;

import avro.shaded.com.google.common.collect.Lists;
import com.dtstack.flink.sql.Main;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.PackagedProgram;
import java.io.File;
import java.net.URL;
import java.util.List;
import com.dtstack.flink.sql.ClusterMode;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.table.shaded.org.apache.commons.lang.StringUtils;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.table.shaded.org.apache.commons.lang.BooleanUtils;
import org.apache.flink.configuration.Configuration;
/**
 * Date: 2017/2/20
 * Company: www.dtstack.com
 * @author xuchao
 */

public class LauncherMain {
    private static final String CORE_JAR = "core.jar";

    private static String SP = File.separator;


    private static String getLocalCoreJarPath(String localSqlRootJar){
        return localSqlRootJar + SP + CORE_JAR;
    }

    public static void main(String[] args) throws Exception {
        LauncherOptionParser optionParser = new LauncherOptionParser(args);
        LauncherOptions launcherOptions = optionParser.getLauncherOptions();
        String mode = launcherOptions.getMode();
        List<String> argList = optionParser.getProgramExeArgList();
        if(mode.equals(ClusterMode.local.name())) {
            String[] localArgs = argList.toArray(new String[argList.size()]);
            Main.main(localArgs);
        } else {
            String pluginRoot = launcherOptions.getLocalSqlPluginPath();
            File jarFile = new File(getLocalCoreJarPath(pluginRoot));
            String[] remoteArgs = argList.toArray(new String[argList.size()]);
            List list = Lists.newArrayList();
            //list.add(new URL("file://"+launcherOptions.getRemoteSqlPluginPath()+"/kafka09source/kafka09-source.jar"));
            list.add(new URL("file://"+launcherOptions.getRemoteSqlPluginPath()+"/kafka10source/kafka10-source.jar"));
            //list.add(new URL("file://"+launcherOptions.getRemoteSqlPluginPath()+"/kafka11source/kafka11-source.jar"));
            list.add(new URL("file://"+launcherOptions.getRemoteSqlPluginPath()+"/mysqlsink/mysql-sink.jar"));
            list.add(new URL("file://"+launcherOptions.getRemoteSqlPluginPath()+"/mysqlallside/mysql-all-side.jar"));
            list.add(new URL("file://"+launcherOptions.getRemoteSqlPluginPath()+"/mysqlasyncside/mysql-async-side.jar"));
            PackagedProgram program = new PackagedProgram(jarFile, list, remoteArgs);
            if(StringUtils.isNotBlank(launcherOptions.getSavePointPath())){
                program.setSavepointRestoreSettings(SavepointRestoreSettings.forPath(launcherOptions.getSavePointPath(), BooleanUtils.toBoolean(launcherOptions.getAllowNonRestoredState())));
            }

            final JobGraph jobGraph = PackagedProgramUtils.createJobGraph(program, new Configuration(), 1);
            ClusterClientFactory.startJob(launcherOptions,jobGraph);

            System.exit(0);
        }
    }
}
