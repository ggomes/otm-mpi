package tests;

import api.API;
import org.junit.Test;
import runner.OTM;

import static org.junit.Assert.fail;

public class XMLSplitterTest {

    @Test
    public void split_and_run() {

        float sim_dt = 2f;
        float duration = 1000f;
        int num_partitions = 4;
        String config_file = "config/50_nodes.xml";
        String prefix = "test/50";

        try {

            // split the config file
            String [] args = {prefix,config_file,String.format("%d",num_partitions)};
            xmlsplitter.XMLSplitter.main(args);

            // try to run each one
            for(int i=0;i<num_partitions;i++){
                String cfg_file = String.format("%s_cfg_%d.xml",prefix,i);
                API api = OTM.load(cfg_file,sim_dt,true,"ctm");
                api.run(0f, duration);
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        System.out.println("done");

    }

}
