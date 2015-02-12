OBridge
=======

OBridge provides a simple Java source code generator for calling Oracle PL/SQL package procedures.

Supported input, output parameters and return values are:
  * BINARY_INTEGER
  * BOOLEAN
  * CHAR
  * CLOB
  * DATE
  * NCHAR
  * NUMBER
  * NVARCHAR2
  * OBJECT - Oracle Object Type
  * PLS_INTEGER
  * TABLE - Table of Oracle Object Type
  * TIMESTAMP
  * VARCHAR2
  
The following types will be supported:
  * Types declared in source code
  * %ROWTYPE parameters
  
Generated code compiles with Java 1.6 and has no dependencies. Generated code compiles without ojdbc dependency.

Usage
-----

Download the latest version from [releases](https://github.com/karsany/obridge/releases).

After downloading, create an XML configuration file:

    <configuration>
        <jdbcUrl>jdbc:oracle:thin:scott/tiger@localhost:1521:xe</jdbcUrl> <!-- jdbc connection string for obridge -->
        <sourceRoot>.</sourceRoot> <!-- where to generate sources - related to this configuration file -->
        <rootPackageName>hu.obridge.test</rootPackageName> <!-- root Java package, generator builds the directory structure -->
        <packages>
            <entityObjects>objects</entityObjects> <!-- object types are going to this package -->
            <converterObjects>converters</converterObjects> <!-- converter util classes are going to this package -->
            <procedureContextObjects>context</procedureContextObjects> <!-- procedure parameter entities are going to this package -->
            <packageObjects>packages</packageObjects> <!-- procedure calling utility classes are going to this package -->
        </packages>
    </configuration>
		
Run the generator:

	java -jar obridge.jar -c <obridge-config.xml>
		
OBridge connects to the specified database and generates the required classes.

Calling a PL/SQL procedure
--------------------------

For example you have the following PL/SQL code:

	Create Or Replace Package simple_procedures is
	  Function simple_func(a In Varchar2,
						   b In Out Varchar2,
						   c Out Varchar2) Return Number;
	End simple_procedures;
		
Generated source:

	public class SimpleProcedures {
	    public static SimpleProceduresSimpleFunc simpleFunc(String a, String b,  Connection connection) throws SQLException { ... }
	}
	
You can call the SimpleProcedures.simpleFunc method:

    SimpleProceduresSimpleFunc ret = SimpleProcedures.simpleFunc("hello", "world", conn); // conn is the database connection

Return object will hold the input and output parameters, converted to Java types.

    public class SimpleProceduresSimpleFunc {
    
        private BigDecimal functionReturn;
        private String a;
        private String b;
        private String c;
        
        // getters, setters
    
    }
