package Combine;
/*
 * Node.java
 *
 * Created on October 29, 2005, 8:59 AM
 *
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

/**
 * Node declaration for software combining tree.
 * @author Maurice Herlihy
 */
public class Node {
  enum CStatus{IDLE, FIRST, SECOND, RESULT, ROOT};
  boolean locked;   // is node locked?
  CStatus cStatus;  // combining status
  int firstValue, secondValue; // values to be combined
  int result;       // result of combining
  Node parent;      // reference to parent
  
  /** Creates a root Node */
  public Node() {
    cStatus = CStatus.ROOT;
    locked = false;
  }
  /** Create a non-root Node */
  public Node(Node _parent) {
    parent = _parent;
    cStatus = CStatus.IDLE;
    locked = false;
  }
  
  synchronized boolean precombine() throws InterruptedException {
    while (locked) wait();
    switch (cStatus) {
      case IDLE:
        cStatus = CStatus.FIRST;
        return true;
      case FIRST:
        locked = true;
        cStatus = CStatus.SECOND;
        return false;
      case ROOT:
        return false;
      default:
        throw new PanicException("unexpected Node state " + cStatus);
    }
  }
  
  synchronized int combine(int combined) throws InterruptedException {
    while (locked) wait();
    locked = true;
    firstValue = combined;
    switch (cStatus) {
      case FIRST:
        return firstValue;
      case SECOND:
        return firstValue + secondValue;
      default:
        throw new PanicException("unexpected Node state " + cStatus);
    }
  }
  
  synchronized int op(int combined) throws InterruptedException {
    switch (cStatus) {
      case ROOT:
        int oldValue = result;
        result += combined;
        return oldValue;
      case SECOND:
        secondValue = combined;
        locked = false;
        notifyAll();
        while (cStatus != CStatus.RESULT) wait();
        locked = false;
        notifyAll();
        cStatus = CStatus.IDLE;
        return result;
      default:
        throw new PanicException("unexpected Node state");
    }
  }
  synchronized void distribute(int prior) throws InterruptedException {
    switch (cStatus) {
      case FIRST:
        cStatus = CStatus.IDLE;
        locked = false;
        break;
      case SECOND:
        result = prior + firstValue;
        cStatus = CStatus.RESULT;
        break;
      default:
        throw new PanicException("unexpected Node state");
    }
    notifyAll();
  }
  
}
