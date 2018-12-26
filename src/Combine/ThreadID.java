/*
 * ThreadID.java
 *
 * Created on October 29, 2005, 2:04 PM
 *
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

package Combine;

public class ThreadID {
  // The next thread ID to be assigned.
  private static volatile int nextID = 0;
  // my thread-local ID
  private static LocalID threadID = new LocalID();
  public static int get() {
    return threadID.get();
  }
  public static void reset() {
      nextID = 0;
  }
  static class LocalID extends ThreadLocal<Integer> {    
    protected synchronized Integer initialValue() {
      return nextID++;
    }
  }
}