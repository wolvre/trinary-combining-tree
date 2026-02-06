package Combine;
/*
 * TriNode.java
 *
 * Created on December 19, 2018, 16:59 AM
 *
 * Based on "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

/**
 * TriNode declaration for software combining trinary-tree.
 * @author Maurice Herlihy
 * @author wolvre
 */
public class TriNode {
  enum CStatus3{IDLE, FIRST, SECOND, THIRD, RESULT, ROOT};
  boolean locked[] = new boolean[2];   // is node locked?
  int num_nodes, ready;
  CStatus3 cStatus;  // combining status
  int firstValue, secondValue[] = new int[2]; // values to be combined
  int result[] = new int[2];       // result of combining
  TriNode parent;      // reference to parent
  int nid;
  
  /** Creates a root Node */
  public TriNode() {
    cStatus = CStatus3.ROOT;
    locked[0] = false;
    locked[1] = false;
    nid = 0;
    num_nodes = 0;
    ready = 0;
  }
  /** Create a non-root Node */
  public TriNode(TriNode _parent, int _nid) {
    parent = _parent;
    cStatus = CStatus3.IDLE;
    locked[0] = false;
    locked[1] = false;
    nid = _nid;
    num_nodes = 0;
    ready = 0;
  }
  
  synchronized boolean precombine()//(int tid, int stamp) 
          throws InterruptedException {
    while (locked[1] || num_nodes >= 2) // || ready > 0 
        wait();
    switch (cStatus) {
      case IDLE:
        cStatus = CStatus3.FIRST;
        //System.out.printf("%d: Thread %d arrives at Node %d first.\n", stamp++, tid, this.nid);
        return true;
      case FIRST:
        locked[0] = true;
        num_nodes = 1;
        ready = 0;
        cStatus = CStatus3.SECOND;
        //System.out.printf("%d: Thread %d arrives at Node %d second.\n", stamp++, tid, this.nid);
        return false;
      case SECOND:
        num_nodes = 2;
        cStatus = CStatus3.THIRD;
        //System.out.printf("%d: Thread %d arrives at Node %d third.\n", stamp++, tid, this.nid);
        return false;
      case ROOT:
        return false;
      default:
        throw new PanicException("unexpected Node state " + cStatus);
    }
  }
  
  synchronized int combine(int combined) throws InterruptedException {
    while (locked[0] || ready < num_nodes) wait();
    locked[1] = true;
    firstValue = combined;
    switch (cStatus) {
      case FIRST:
        return firstValue;
      case SECOND:
        return firstValue + secondValue[0];
      case THIRD:
        return firstValue + secondValue[0] + secondValue[1];
      default:
        throw new PanicException("unexpected Node state " + cStatus);
    }
  }
  
  synchronized int op(int combined) throws InterruptedException {
    int prior;
    switch (cStatus) {
      case ROOT:
        int oldValue = result[0];
        result[0] += combined;
        return oldValue;
      case SECOND:    
      case THIRD:
        int myIndex = ready;
        secondValue[myIndex] = combined;
        ready ++;
        if (ready >= num_nodes) {
            locked[0] = false;
            notifyAll();
        }   
        while (cStatus != CStatus3.RESULT) wait();
        ready --;
        prior = result[myIndex];
        if (ready <= 0) {
            locked[1] = false;
            cStatus = CStatus3.IDLE;
            num_nodes = 0;
            notifyAll();
        }
        //while (locked[1] || cStatus != CStatus.IDLE || num_nodes > 0 || ready > 0) wait();
        return prior;
      default:
        throw new PanicException("unexpected Node state " + cStatus + " at Node " + this.nid);
    }
  }
  synchronized void distribute(int prior) throws InterruptedException {
    switch (cStatus) {
      case FIRST:
        cStatus = CStatus3.IDLE;
        locked[1] = false;
        break;
      case SECOND:
        result[0] = prior + firstValue;
        cStatus = CStatus3.RESULT;
        break;
      case THIRD:
        result[0] = prior + firstValue;
        result[1] = result[0] + secondValue[0];
        cStatus = CStatus3.RESULT;
        break;
      default:
        throw new PanicException("unexpected Node state " + cStatus);
    }
    notifyAll();
  }
  
}
