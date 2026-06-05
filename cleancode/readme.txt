windows 
compile class : javac -cp ".;D:\evan\yen\hardening_tomcat9\apache-tomcat-9.0.118\lib\servlet-api.jar" .\AppCleanTemp.java .\AppStartupListener.java
create jar  : jar cf housekeeping.jar com
buka isi jar :  jar tf housekeeping.jar


listener di letakan di : D:\evan\yen\hardening_tomcat9\apache-tomcat-9.0.118\conf\web.xml
<listener>
	<listener-class>com.housekeeping.AppStartupListener</listener-class>
</listener>