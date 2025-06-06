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

package org.obridge.mappers;


import org.obridge.model.data.Procedure;
import org.obridge.model.data.ProcedureArgument;
import org.obridge.model.data.TypeAttribute;
import org.obridge.model.generator.Pojo;
import org.obridge.model.generator.PojoField;
import org.obridge.util.StringHelper;

import java.util.ArrayList;
import java.util.List;

public final class PojoMapper {

    private PojoMapper() {
    }

    public static Pojo typeToPojo(boolean lombok, String typeName, List<TypeAttribute> typeAttributes) {
        Pojo p = new Pojo();
        p.setClassName(StringHelper.toCamelCase(typeName));
        p.setFields(new ArrayList<>());

        for (TypeAttribute t : typeAttributes) {
            PojoField field = new PojoField();
            field.setFieldName(StringHelper.toCamelCaseSmallBegin(t.getAttrName()));
            field.setFieldType(t.getJavaDataType());
            field.setReadonly(false);
            p.getFields().add(field);
        }

        p.setImports(new ArrayList<>());

        p.getImports().add("java.sql.Timestamp");
        p.getImports().add("java.sql.Date");
        p.getImports().add("java.util.List");
        p.getImports().add("java.math.BigDecimal");
        p.getImports().add("jakarta.annotation.Generated");

        setLombokRelatedData(p, lombok);

        return p;
    }

    public static Pojo procedureToPojo(boolean lombok, Procedure procedure) {
        Pojo p = new Pojo();
        p.setClassName(procedure.getStoredProcedureClassName());
        p.setFields(new ArrayList<>());

        for (ProcedureArgument t : procedure.getArgumentList()) {
            PojoField field = new PojoField();
            field.setFieldName(t.getJavaPropertyName());
            field.setFieldType(t.getJavaDataType());
            p.getFields().add(field);
        }

        p.setImports(new ArrayList<>());

        p.getImports().add("java.sql.Timestamp");
        p.getImports().add("java.sql.Date");
        p.getImports().add("java.util.List");
        p.getImports().add("java.math.BigDecimal");
        p.getImports().add("java.sql.ResultSet");
        p.getImports().add("jakarta.annotation.Generated");

        setLombokRelatedData(p, lombok);

        return p;

    }

    private static void setLombokRelatedData(Pojo p, boolean lombok){
        p.setLombok(lombok);
        p.setGetterSetterMethods(!lombok);

        if(lombok) {
            p.getImports().add("lombok.Getter");
            p.getImports().add("lombok.Setter");
        }
    }
}
