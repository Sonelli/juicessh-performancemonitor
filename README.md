JuiceSSH Plugin: Performance Monitor
===========================

A JuiceSSH plugin for monitoring linux servers using the JuiceSSH Plugin SDK.

![screenshot](http://i.imgur.com/uDSJRIr.png?1)

This plugin will connect to your choice of JuiceSSH connection and execute commands to monitor the performance of the server.

For details of how the various stats are discovered check out the following classes:

[CPU checks](Plugin/src/main/java/com/sonelli/juicessh/performancemonitor/controllers/CpuUsageController.java)

[RAM checks](Plugin/src/main/java/com/sonelli/juicessh/performancemonitor/controllers/FreeRamController.java)

[Load Avg checks](Plugin/src/main/java/com/sonelli/juicessh/performancemonitor/controllers/LoadAverageController.java)

[Network checks](Plugin/src/main/java/com/sonelli/juicessh/performancemonitor/controllers/NetworkUsageController.java)

[Disk checks](Plugin/src/main/java/com/sonelli/juicessh/performancemonitor/controllers/DiskUsageController.java)


For details of the JuiceSSH Plugin SDK, it's capabilities, security information and how to get started with writing a Plugin please check our [FAQ](http://juicessh.com/faq)

Feel free to fork/extend/contribute to this plugin!
