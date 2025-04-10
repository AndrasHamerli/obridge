OBridge
=======

[![PayPal donate button](http://img.shields.io/paypal/donate.png?color=yellow)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=QQAFXN8GH5YFN&lc=GB&item_name=OBridge&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted "Help OBridge development using Paypal")  


OBridge provides a simple Java source code generator for calling Oracle PL/SQL package procedures.

Supported input, output parameters and return values are:
  * BINARY_INTEGER
  * BOOLEAN
  * CHAR
  * CLOB
  * BLOB
  * DATE
  * NCHAR
  * NUMBER
  * NVARCHAR2
  * OBJECT - Oracle Object Type
  * PLS_INTEGER
  * TABLE - Table of Oracle Object Type
  * TIMESTAMP
  * VARCHAR2
  * RAW
  
The following types cannot be implemented, because JDBC driver does not supports them:
  * Types declared in source code
  * %ROWTYPE parameters

Usage
-----

Download the latest version from [releases](https://github.com/karsany/obridge/releases).

After downloading, create an XML configuration file:

```xml
<configuration>
	<jdbcUrl>jdbc:oracle:thin:@localhost:1521:xe</jdbcUrl> <!-- jdbc connection string for obridge -->
    <username>scott</username> <!-- username for the connection -->
    <password>tiger</password> <!-- password for the connection -->
	<sourceOwner>SCOTT</sourceOwner> <!-- owner of the database objects -->
	<sourceRoot>.</sourceRoot> <!-- where to generate sources - related to this configuration file -->
	<rootPackageName>hu.obridge.test</rootPackageName> <!-- root Java package, generator builds the directory structure -->
    <useSchemaName>true</useSchemaName> <!-- If true, type converters will have a SCHEMA_NAME constant 
                                         and a connection.setSchema(SCHEMA_NAME); will be placed before each createStuct call  -->
    <useLombokAccessors>true</useLombokAccessors> <!-- POJOs will  be be annotated with lombok @Getter and @Setter instead of getter and setter methods -->
    
	<packages>
		<entityObjects>objects</entityObjects> <!-- object types are going to this package -->
		<converterObjects>converters</converterObjects> <!-- converter util classes are going to this package -->
		<procedureContextObjects>context</procedureContextObjects> <!-- procedure parameter entities are going to this package -->
		<packageObjects>packages</packageObjects> <!-- procedure calling utility classes are going to this package -->
	</packages>

    <packageExtraClassImports> <!-- Additional imports for the generated package classes -->
        <packageExtraClassImport>lombok.extern.slf4j.Slf4j</packageExtraClassImport>
    </packageExtraClassImports>

    <logging> <!-- Logging configuration. -->
        <!-- initializer takes precedence over annotationBasedInitializer. Add %s placeholder for classname! -->
        <initializer>private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(%s.class);</initializer> 
        <!-- Use full class name or add to packageExtraClassImports --> 
        <annotationBasedInitializer>@Slf4j</annotationBasedInitializer>
        <method>log.trace</method>
    </logging>

    <dbObjects> <!-- Optional. If present, then the listed objects will be generated only! -->
        <dbObject><owner>scott</owner><name>ty_dummy</name></dbObject>
    </dbObjects>
</configuration>
```
Run the generator:

	java -jar obridge.jar -c <obridge-config.xml>
		
OBridge connects to the specified database and generates the required classes.

Calling a PL/SQL procedure
--------------------------

For example you have the following PL/SQL code:

```sql
Create Or Replace Package simple_procedures is
  Function simple_func(a In Varchar2,
					   b In Out Varchar2,
					   c Out Varchar2) Return Number;
End simple_procedures;
```
Generated source:

```java
public class SimpleProcedures {
	public static SimpleProceduresSimpleFunc simpleFunc(String a, String b,  Connection connection) throws SQLException { ... }
}
```
You can call the SimpleProcedures.simpleFunc method:

```java
SimpleProceduresSimpleFunc ret = SimpleProcedures.simpleFunc("hello", "world", conn); // conn is the database connection
```
Return object will hold the input and output parameters, converted to Java types.
```java
public class SimpleProceduresSimpleFunc {

	private BigDecimal functionReturn;
	private String a;
	private String b;
	private String c;
	
	// getters, setters

}
```
