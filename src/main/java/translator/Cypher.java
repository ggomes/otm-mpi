package translator;

import metagraph.MyMetaGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cypher {

    // mpi messaging
    public Map<Long,Integer> rank2index;
    public int[] neighbor_disp;
    public int[] neighbor_length;

    public List<AbstractMessageItem> items = new ArrayList<>();

    public Cypher(MyMetaGraph my_metagraph){
        int num_neighbors = my_metagraph.neighbors.size();
        neighbor_disp = new int[num_neighbors];
        neighbor_length = new int[num_neighbors];

        rank2index = new HashMap<>();
        for(int index=0;index<my_metagraph.neighbors.size();index++)
            rank2index.put(my_metagraph.neighbors.get(index).rank,index);
    }

    public void add_item(long neighbor_rank,AbstractMessageItem item) {

        // mpi messaging
        int index = rank2index.get(neighbor_rank);
        neighbor_length[index]++;

        items.add(item);
    }

    public void compute_displacements(){
        int num_neighbors = neighbor_length.length;
        neighbor_disp = new int[num_neighbors];
        for(int i=0 ; i<num_neighbors-1 ; i++)
            neighbor_disp[i+1] = neighbor_disp[i] + neighbor_length[i];
    }

    @Override
    public String toString() {
        String str = "";
        for(AbstractMessageItem item : items)
            str += String.format("%s\n",item);
        return str;
    }
}
