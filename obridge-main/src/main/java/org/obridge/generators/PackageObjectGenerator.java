/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Ferenc Karsany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.obridge.generators;

import lombok.extern.log4j.Log4j2;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.obridge.context.Logging;
import org.obridge.context.OBridgeConfiguration;
import org.obridge.dao.ProcedureDao;
import org.obridge.model.data.OraclePackage;
import org.obridge.util.CodeFormatter;
import org.obridge.util.DataSourceProvider;
import org.obridge.util.MustacheRunner;
import org.obridge.util.OBridgeException;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by fkarsany on 2015.01.28..
 */
@Log4j2
public final class PackageObjectGenerator {

    private PackageObjectGenerator() {
    }

    public static void generate(OBridgeConfiguration c) {
        try {

            log.info("Package Object Generator");

            String packageName             = c.getRootPackageName() + "." + c.getPackages().getPackageObjects();
            String contextPackage          = c.getRootPackageName() + "." + c.getPackages().getProcedureContextObjects();
            String converterPackage        = c.getRootPackageName() + "." + c.getPackages().getConverterObjects();
            String objectPackage           = c.getRootPackageName() + "." + c.getPackages().getEntityObjects();
            String outputDir               = c.getSourceRoot() + "/" + packageName.replace(".", "/") + "/";
            String loggingClassInitializer = "";
            String loggingAnnotationInit   = "";
            String loggingMethod           = "";

            Logging logging = c.getLogging();
            if (logging != null) {
                String initializer = logging.getInitializer();
                String annotationBasedInitializer = logging.getAnnotationBasedInitializer();

                if (initializer != null && !initializer.isEmpty() ) {
                    loggingClassInitializer = initializer;
                }

                if (StringUtils.isBlank(loggingClassInitializer)  && StringUtils.isNotBlank(annotationBasedInitializer)) {
                    loggingAnnotationInit = annotationBasedInitializer;
                }

                if (StringUtils.isNotBlank(logging.getMethod())
                        && (StringUtils.isNotBlank(loggingClassInitializer) || StringUtils.isNotBlank(loggingAnnotationInit))) {
                    loggingMethod = logging.getMethod();
                }
            }

            List<OraclePackage> allPackages = new ProcedureDao(DataSourceProvider.getDataSource(c)).getAllPackages(c.getDbObjects());

            for (OraclePackage oraclePackage : allPackages) {
                oraclePackage.setJavaPackageName(packageName);
                oraclePackage.setContextPackage(contextPackage);
                oraclePackage.setConverterPackage(converterPackage);
                oraclePackage.setObjectPackage(objectPackage);
                oraclePackage.setExtraImportClasses(c.getPackageExtraClassImports());

                if (StringUtils.isNotBlank(loggingMethod)) {
                    if(StringUtils.isNotBlank(loggingClassInitializer)) {
                        oraclePackage.setLoggingInitializer(String.format(loggingClassInitializer, oraclePackage.getJavaClassName()));
                    } else if(StringUtils.isNotBlank(loggingAnnotationInit)) {
                        oraclePackage.setAnnotationBasedLoggingInitializer(loggingAnnotationInit);
                    }

                    oraclePackage.setLoggingMethod(loggingMethod);
                }

                generatePackageObject(outputDir, oraclePackage);
            }

            generateStoredProcedureCallExceptionClass(packageName, outputDir);

        } catch (PropertyVetoException | IOException e) {
            throw new OBridgeException(e);
        }
    }

    private static void generatePackageObject(String outputDir, OraclePackage oraclePackage) throws IOException {
        String javaSource = MustacheRunner.build("package.mustache", oraclePackage);
        String pathname   = outputDir + oraclePackage.getJavaClassName() + ".java";
        FileUtils.writeStringToFile(new File(pathname), CodeFormatter.format(javaSource), "utf-8");
        log.info(" ... " + oraclePackage.getJavaClassName());
    }

    private static void generateStoredProcedureCallExceptionClass(String packageName, String outputDir) throws IOException {
        OraclePackage op = new OraclePackage();
        op.setJavaPackageName(packageName);
        String javaSource = MustacheRunner.build("StoredProcedureCallException.java.mustache", op);
        String pathname   = outputDir + "StoredProcedureCallException.java";
        FileUtils.writeStringToFile(new File(pathname), CodeFormatter.format(javaSource), "utf-8");
        log.info(" ... StoredProcedureCallException");
    }
}
