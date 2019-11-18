package tests;

import api.OTM;

public class XMLSplitterTest {

    public static void main(String[] notused) throws Exception {

        float duration = 1000f;
        int num_partitions = 2;
        String config_file = "config/50_nodes.xml";
        String prefix = "test/50_x";

        // split the config file
        String [] args = {prefix,config_file,String.format("%d",num_partitions)};
        xmlsplitter.XMLSplitter.main(args);

        // try to run each one
        for(int i=0;i<num_partitions;i++){
            String cfg_file = String.format("%s_cfg_%d.xml",prefix,i);
            OTM otm = new OTM();
            otm.load(cfg_file,true,false);
            System.out.println("Running partioned file " + i);
            otm.run(0f, duration);
        }

        System.out.println("done");

    }

}
