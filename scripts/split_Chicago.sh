#cd $OTMMPIHOME/src/main/java
#javac -d $OTMMPIHOME/out_javac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* metis/*.java metagraph/*.java translator/*.java xmlsplitter/*.java

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/505
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/505/ $OTMMPIHOME/config/Chicago.xml 505

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/506
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/506/ $OTMMPIHOME/config/Chicago.xml 506

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/507
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/507/ $OTMMPIHOME/config/Chicago.xml 507

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/508
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/508/ $OTMMPIHOME/config/Chicago.xml 508

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/509
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/509/ $OTMMPIHOME/config/Chicago.xml 509

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/525
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/525/ $OTMMPIHOME/config/Chicago.xml 525

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/500
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/500/ $OTMMPIHOME/config/Chicago.xml 500

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/520
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/520/ $OTMMPIHOME/config/Chicago.xml 520

cd $OTMMPIHOME/out_javac
mkdir $OTMMPIHOME/test/Chicago/518
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/518/ $OTMMPIHOME/config/Chicago.xml 518


#mkdir $OTMMPIHOME/test/Chicago/1024
#java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/1024/ $OTMMPIHOME/config/Chicago.xml 1024