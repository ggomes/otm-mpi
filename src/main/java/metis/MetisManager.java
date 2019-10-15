package metis;

import utils.OTMUtils;
import otm.ScenarioWrapper;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MetisManager {

    public int num_partitions;
    public String metis_input;
    public String nodemap;

    public MetisManager(String metis_prefix, int num_partitions){
        this.num_partitions = num_partitions;
        metis_input = metis_prefix + "_metis";
        nodemap = metis_prefix + "_metis_nodemap.txt";
    }

    ////////////////////////////////////////
    // public
    ////////////////////////////////////////

    // runner for a runner.Scenario
    public void run(ScenarioWrapper base_scenario) throws Exception {
        write_to_metis(base_scenario);
        execute();
    }

    public Map<Integer,Long> read_nodemap(){

        Map<Integer,Long> index2node = new HashMap<>();
        File file = new File(nodemap);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                String [] list = text.split(",");
                index2node.put(Integer.parseInt(list[1]),Long.parseLong(list[0]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        return index2node;
    }

    public List<Integer> read_metis_output(){

        List<Integer> list = new ArrayList<>();
        File file = new File(String.format("%s.part.%d",metis_input,num_partitions));
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text;

            while ((text = reader.readLine()) != null) {
                list.add(Integer.parseInt(text));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        return list;
    }

    ////////////////////////////////////////
    // private helpers
    ////////////////////////////////////////

    private void write_to_metis(ScenarioWrapper scenario){

        // replace node ids with indices
        Map<Long,Integer> node_id2index = new HashMap<>();
        int i=0;
        for(jaxb.Node node : scenario.get_nodes()){
            node_id2index.put(node.getId(),i+1);
            i++;
        }

        // create set of undirected edges
        Set<UEdge> edges = new HashSet<>();
        for(jaxb.Link link : scenario.get_links()){
            int start_index = node_id2index.get(link.getStartNodeId());
            int end_index = node_id2index.get(link.getEndNodeId());
            edges.add(new UEdge(start_index,end_index));
        }

        Set<UEdge> loopy_edges = edges.stream().filter(edge->edge.u==edge.v).collect(Collectors.toSet());
        int num_nodes = node_id2index.size();
        int num_edges = edges.size();
        int num_loopy_edges = loopy_edges.size();

        // print
        System.out.println("The undirected graph has:");
        System.out.println(String.format("\t%d nodes",num_nodes));
        System.out.println(String.format("\t%d edges",num_edges));
        System.out.println(String.format("\t%d looping edges",num_loopy_edges));

        // write to metis format
        Set<UEdge> used_edges = new HashSet<>();
        try {
            PrintWriter writer = new PrintWriter(metis_input, "UTF-8");
            writer.println( num_nodes + " " + (num_edges-num_loopy_edges) );
            for(jaxb.Node node : scenario.get_nodes()){
                Integer node_index = node_id2index.get(node.getId());
                Set<UEdge> myedges = edges.stream()
                        .filter(edge->edge.u==node_index || edge.v==node_index)
                        .collect(Collectors.toSet());
                used_edges.addAll(myedges);
                Set<Integer> neigbors = myedges.stream()
                        .flatMap(edge->edge.get_nodes().stream())
                        .collect(Collectors.toSet());
                neigbors.remove(node_index);
                writer.println(OTMUtils.format_delim(neigbors.toArray()," "));
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // write node to index map
        try {
            PrintWriter writer = new PrintWriter(nodemap, "UTF-8");
            for(Map.Entry<Long,Integer> e : node_id2index.entrySet())
                writer.println(e.getKey()+","+e.getValue());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private void execute() throws Exception {
        System.out.println("******* Metis start *******");
        Process p = Runtime.getRuntime().exec(String.format("gpmetis %s %d",metis_input,num_partitions));
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s = br.readLine()) != null)
            System.out.println("line: " + s);
        p.waitFor();
        p.destroy();
        System.out.println("******* Metis end *******");
    }

    ////////////////////////////////////////
    // classes
    ////////////////////////////////////////

    private class UEdge {
        public int u;
        public int v;
        public UEdge(int a,int b){
            this.u = Math.min(a,b) ;
            this.v = Math.max(a,b);
        }

        public Set<Integer> get_nodes(){
            Set<Integer> nodes = new HashSet<>();
            nodes.add(u);
            nodes.add(v);
            return nodes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UEdge uEdge = (UEdge) o;
            return u == uEdge.u &&
                    v == uEdge.v;
        }

        @Override
        public int hashCode() {
            return Objects.hash(u, v);
        }
    }

}
