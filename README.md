##Ejemplo de accceso a hbase usando Phoenix con identificación de usuario

El problema que se está intentando resolver es trasldadar la indentidad a la capa de phoenix en un cluster que no tienen activado Kerberos.

 * Las pruebas realizadas usando el PQS no han funcionado correctamente, en teoría con el prámetro doAs debería funcionar pero no funciona
 * Se ha optado por realizar la conexión usando el driver directo, no el de PSQ. Este driver se conecta a zookeeper en vez de hacer uso del PQS
 
 
### JDBC contra zooekeeper
 
Preparamos la conexión conociendo
 * Zookeeper qurorum
 * Hbase zookeeper base path
 
```java
final String ZK_PORT = "2181";
final String ZK_HOST = "odin003.bigdata.zylk.net";
final String ZK_PATH = "/hbase-unsecure";
//jdbc:phoenix:m1.hdp.local,m2.hdp.local,d1.hdp.local:2181:/hbase-unsecure
final String url = "jdbc:phoenix:" + ZK_HOST +":"+ ZK_PORT+":"+ZK_PATH;
```
 
Una vez tenemos preparada la conexión la ejecutamos usando el objeto UserGroupInformation que nos permite obtener la identidad remota de un usuarios y ejectura la conexión como si fueras dicho usuario. Al hacerlo así las politicas de ranger que se tengan aplicadas a las tablas de HBase tienen efecto, así como el modelo de auditoría.

```java
UserGroupInformation user2Ugi = UserGroupInformation.createRemoteUser("hbase", null);
user2Ugi.doAs(new PrivilegedExceptionAction<Void>() {
			@Override
			public Void run() throws Exception {
				try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
					conn.setAutoCommit(true);
					try (ResultSet rs = stmt.executeQuery("select STATE,CITY,POPULATION from US_POPULATION")) {
						while (rs.next()) {
							result.add(rs.getString("STATE"));
						}
					} 
				} 
				return null;
			}
		});
```  

 
 
