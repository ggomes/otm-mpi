cd $OTMMPIHOME/out_javac

mkdir $OTMMPIHOME/test/Chicago/1013
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/1013/ $OTMMPIHOME/config/Chicago.xml 1013

mkdir $OTMMPIHOME/test/Chicago/1017
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/Chicago/1017/ $OTMMPIHOME/config/Chicago.xml 1017