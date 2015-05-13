Using this line currently for packaging up app for Mac OS. 

Edit required library list in -srcfiles option as they change:

/Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/bin/javafxpackager 
-deploy -native -outdir packages -outfile Nicotania -srcdir dist 
-srcfiles Nicotania_0.1.jar:lib/controlsfx-8.20.8.jar:lib/sqlite-jdbc-3.8.7.jar:lib/commons-math3-3.4.1.jar:lib/extfx-0.3.jar:lib/guava-19.0.0.jar 
-appclass nicotania_0.pkg1.Nicotania_01 -name "Nicotania" -title "Nicotania"
