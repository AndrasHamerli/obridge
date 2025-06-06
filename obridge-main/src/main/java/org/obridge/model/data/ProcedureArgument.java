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

package org.obridge.model.data;

import oracle.jdbc.OracleType;
import org.obridge.mappers.builders.ParameterGetSetRegisterBuilder;
import org.obridge.util.StringHelper;
import org.obridge.util.TypeMapper;

/**
 * User: fkarsany Date: 2013.11.18.
 */
public class ProcedureArgument {

    public static final String TABLE_DATATYPE_NAME = "TABLE";
    private String argumentName;
    private String dataType;
    private String typeName;
    private boolean inParam;
    private boolean outParam;
    private String origTypeName;
    private int sequenceNumber;

    public ProcedureArgument(String argumentName, String dataType, String typeName, boolean inParam, boolean outParam, String origTypeName) {
        this.argumentName = argumentName;
        this.dataType = dataType;
        this.typeName = typeName;
        this.inParam = inParam;
        this.outParam = outParam;
        this.origTypeName = origTypeName;
    }


    public boolean isInParam() {
        return inParam;
    }


    public boolean isOutParam() {
        return outParam;
    }


    public String getArgumentName() {
        return argumentName;
    }


    public String getDataType() {
        return dataType;
    }


    public String getJavaPropertyName() {

        String r;

        if (this.argumentName == null) {
            return "functionReturn";
        } else if (this.argumentName.startsWith("P_")) {
            r = this.argumentName.substring(2);
        } else {
            r = this.argumentName;
        }

        if (Character.isDigit(r.charAt(0))) {
            r = "P_" + r;
        }

        return StringHelper.unJavaKeyword(StringHelper.toCamelCaseSmallBegin(r));

    }

    public String getJavaPropertyNameBig() {
        String javaPropName = this.getJavaPropertyName();
        return javaPropName.substring(0, 1)
                           .toUpperCase() + javaPropName.substring(1);
    }

    public String getJavaTypeName() {
        if (isList()) {
            String mappedType = new TypeMapper().getJavaType(typeName, 1);
            if ("Object".equals(mappedType)) {
                return "List<" + StringHelper.toCamelCase(typeName) + ">";
            } else {
                return "List<" + mappedType + ">";
            }
        } else {
            return StringHelper.toCamelCase(typeName);
        }
    }

    public boolean isList() {
        return TABLE_DATATYPE_NAME.equals(dataType);
    }

    public boolean isPrimitiveList() {
        return isList() && !("Object".equals(new TypeMapper().getJavaType(typeName, 1)));
    }

    public String getUnderlyingTypeName() {
        if (isPrimitiveList()) {
            return new TypeMapper().getJavaType(typeName, 1);
        } else {
            return StringHelper.toCamelCase(typeName);
        }
    }

    public String getJavaDataType() {
        if (this.typeName != null) {
            return getJavaTypeName();
        } else {
            return new TypeMapper().getJavaType(this.dataType, 1);
        }
    }

    public String getJDBCType() {
        return new TypeMapper().getJDBCType(dataType);
    }


    public boolean isJDBCTypeBoolean() {
        return "BOOLEAN".equals(getJDBCType());
    }

    public String getParamSet(int sequenceNumber) {
        return new ParameterGetSetRegisterBuilder(this).setParameter(sequenceNumber);
    }

    public String getRegOutput(int sequenceNumber) {
        if ("OBJECT".equals(dataType) || TABLE_DATATYPE_NAME.equals(dataType)) {
            return String.format("this.declareOutParameter(\"%s\", Types.%s, \"%s\");", argumentName, getJDBCType(), origTypeName);
        } else {
            return String.format("this.declareOutParameter(%d, %s); // %s", sequenceNumber, ("Types." + getJDBCType()).replace("Types.CURSOR", "-10")
                                                                                                                      .replace("Types.BOOLEAN", "Types.INTEGER"), argumentName);
        }
    }

    public String getParamDeclaration() {
        boolean function = argumentName == null || argumentName.isEmpty();

        StringBuilder sb = new StringBuilder();

        if (function) {
            sb.append("this.setFunction(true);\n");
        }

        sb.append("this.declareParameter(new ");

        if (isInParam()) {
            if (isOutParam()) {
                sb.append("SqlInOutParameter");
            } else {
                sb.append("SqlParameter");
            }
        } else {
            sb.append("SqlOutParameter");
        }

        sb.append("(\"");

        if (function) {
            sb.append("FUNCTION_RESULT");
        } else {
            sb.append(getArgumentName());
        }

        sb.append("\", ")
          .append(("Types." + getJDBCType()).replace("Types.CURSOR", "-10")
                                            .replace("Types.BOOLEAN", String.valueOf(OracleType.PLSQL_BOOLEAN.getVendorTypeNumber())));

        if (origTypeName != null && !origTypeName.isEmpty()) {
            sb.append(", \"")
              .append(origTypeName)
              .append("\"");
        }

        sb.append("));");

        return sb.toString();
    }

    public String getParamGet() {
        String callGet = "call.get(\"" + (argumentName == null || argumentName.isEmpty() ? "FUNCTION_RESULT" : argumentName) + "\")";

        System.out.println(callGet);
        System.out.println(this.toString());

        if ("OBJECT".equals(dataType)) {
            return String.format("ctx.set%s(%sConverter.getObject((Struct)%s));", getJavaPropertyNameBig(), getJavaDataType(), callGet);
        } else if (TABLE_DATATYPE_NAME.equals(dataType)) {
            if (isPrimitiveList()) {
                return String.format("ctx.set%s(PrimitiveTypeConverter.asList((Array) %s, %s.class));", getJavaPropertyNameBig(),callGet, getUnderlyingTypeName());
            }
            return String.format("ctx.set%s(%sConverter.getObjectList((Array)%s));", getJavaPropertyNameBig(), getUnderlyingTypeName(), callGet);
        } else if ("Integer".equals(getJavaDataType())) {
            return String.format("ctx.set%s(%s);", getJavaPropertyNameBig(), callGet);
        } else if ("ResultSet".equals(getJavaDataType())) {
            return String.format("ctx.set%s(null); // TODO (ResultSet)%s", getJavaPropertyNameBig(), callGet);
        } else if (TypeMapper.JAVA_BYTEARRAY.equals(getJavaDataType())) {
            return String.format("ctx.set%s(null); // TODO (byte[])%s", getJavaPropertyNameBig(), callGet);
        } else {
            return String.format("ctx.set%s((%s)%s);", getJavaPropertyNameBig(), getJavaDataType(), callGet);
        }
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getOrigTypeName() {
        return origTypeName;
    }


    public boolean isOutputBooleanArgument() {
        return getArgumentName() != null && isJDBCTypeBoolean() && isOutParam();
    }


    @Override
    public String toString() {
        return "ProcedureArgument{" +
                "argumentName='" + argumentName + '\'' +
                ", dataType='" + dataType + '\'' +
                ", typeName='" + typeName + '\'' +
                ", inParam=" + inParam +
                ", outParam=" + outParam +
                ", origTypeName='" + origTypeName + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                '}';
    }
}
