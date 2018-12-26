package Combine;
/*
 * Combine.java
 *
 * Created on October 29, 2005, 8:57 AM
 */
import java.util.Stack;
/**
 *
 * @author mph
 */
public class TriTree {
  volatile int stamp = 0;
  final static int THREADS = 12;
  final static int TRIES = 1024 * 1024;
  static boolean[] test = new boolean[THREADS * TRIES];
    
  TriNode[] leaf;
  
  /** Creates a new instance of Combine */
  public TriTree(int size) { //size=27
    TriNode[] nodes = new TriNode[size/2];
    nodes[0] = new TriNode();
    for (int i = 1; i < nodes.length; i++) {
      nodes[i] = new TriNode(nodes[(i-1)/3], i);
    }
    leaf = new TriNode[(size + 1)/3];
    for (int i = 0; i < leaf.length; i++) {
      leaf[i] = nodes[nodes.length - i - 1];
    }
  }

  /** binary tree  
  public TriTree(int size) { 
    TriNode[] nodes = new TriNode[size - 1];
    nodes[0] = new TriNode();
    for (int i = 1; i < nodes.length; i++) {
      nodes[i] = new TriNode(nodes[(i-1)/2], i);
    }
    leaf = new TriNode[(size + 1)/2];
    for (int i = 0; i < leaf.length; i++) {
      leaf[i] = nodes[nodes.length - i - 1];
    }
  }*/
  
  public int getAndIncrement() throws InterruptedException {
    Stack<TriNode> stack = new Stack<TriNode>();
    int tid = ThreadID.get();
    TriNode myLeaf = leaf[tid % 9];//[tid / 3];
    TriNode node = myLeaf;
    // phase one
    while (node.precombine()) {
      //System.out.printf("%d: Thread %d precombining done at Node %d.\n", stamp++, tid, node.nid);
      node = node.parent;
    }
    TriNode stop = node;
    //System.out.printf("%d: Thread %d stops precombining at Node %d.\n", stamp++, tid, stop.nid);
    // phase two
    node = myLeaf;
    int combined = 1;
    while (node != stop) {
      combined = node.combine(combined);
      // System.out.printf("%d: Thread %d combining done at Node %d.\n", stamp++, tid, node.nid);
      stack.push(node);
      node = node.parent;
    }
    // phase 3  
    int prior = stop.op(combined);
    if (test[prior]) {
        System.out.printf("ERROR duplicate value %d by Thread %d, Node %d\n", prior, tid, stop.nid);
        return prior;
    }
    else
        test[prior] = true;
    //System.out.printf("%d: Thread %d operation done at Node %d.\n", stamp++, tid, stop.nid);

    // phase 4
    while (!stack.empty()) {
      node = stack.pop();
      node.distribute(prior);
      //System.out.printf("%d: Thread %d distribution done at Node %d.\n", stamp++, tid, node.nid);
    }
    //System.out.printf("%d: Thread %d returns %d.\n", stamp++, tid, prior);
    return prior;
  }
  
}
