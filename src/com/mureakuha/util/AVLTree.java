package com.mureakuha.util;

/**
 * This class implements "counted" AVL-tree maps (self-balancing BSTs).
 * All relevant operations run in logarithmic time with respect to the tree
 * size. Also, this implementation makes it possible to access individual
 * entries by their in-order index; also in logarithmic time.
 *
 * @author coderodde
 * @version I
 */
public class AVLTree<K extends Comparable<? super K>, V> {

  private Entry<K, V> root;
  private int size;

  public static class Entry<K, V> {
    protected K key;
    protected V value;
    protected int count; // size of the left subtree. See entryAt().

    protected Entry<K, V> parent;
    protected Entry<K, V> left;
    protected Entry<K, V> right;

    protected int h;

    public Entry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public V setValue(V v) {
      V old = value;
      value = v;
      return old;
    }

    public Entry<K, V> next() {
      Entry<K, V> e = this;

      if (e.right != null) {
        e = e.right;

        while (e.left != null) {
          e = e.left;
        }

        return e;
      }

      while (e.parent != null && e.parent.right == e) {
        e = e.parent;
      }

      if (e.parent == null) {
        return null;
      }

      return e.parent;
    }

    public Entry<K, V> min() {
      Entry<K, V> e = this;

      while (e.left != null) {
        e = e.left;
      }

      return e;
    }

    public String toString() {
      return "[" + key + " -> " + value + "]";
    }
  }

  /**
   * Associates <tt>key</tt> with <tt>value</tt>. If <tt>key</tt> is already
   * present in the tree, its old value is overwritten by <tt>value</tt>.
   * Runs in logarithmic time.
   *
   * @param key the key.
   * @param value the value.
   * @return the old value for <tt>key</tt>, or <tt>null</tt>, if no such.
   */
  public V put(K key, V value) {
    if (key == null) {
      throw new NullPointerException("'key' is null.");
    }

    Entry<K, V> e = new Entry<K, V>(key, value);

    if (root == null) {
      root = e;
      size = 1;
      return null;
    }

    Entry<K, V> x = root;
    Entry<K, V> p = null;
    int cmp;

    while (x != null) {
      p = x;

      if ((cmp = e.key.compareTo(x.key)) < 0) {
        x = x.left;
      } else if (cmp > 0) {
        x = x.right;
      } else {
        V old = x.value;
        x.value = value;
        return old;
      }
    }

    e.parent = p;

    if (e.key.compareTo(p.key) < 0) {
      p.left = e;
      p.count = 1;
    } else {
      p.right = e;
    }

    Entry<K, V> tmp = p.parent;
    Entry<K, V> tmpLo = p;

    // Update the counters.
    while (tmp != null) {
      if (tmp.left == tmpLo) {
        tmp.count++;
      }

      tmpLo = tmp;
      tmp = tmp.parent;
    }

    size++;

    while (p != null) {
      if (h(p.left) == h(p.right) + 2) {
        Entry<K, V> pp = p.parent;
        Entry<K, V> subroot =
                (h(p.left.left) >= h(p.left.right) ?
                rightRotate(p) :
                leftRightRotate(p));

        if (pp == null) {
          root = subroot;
        } else if (pp.left == p) {
          pp.left = subroot;
        } else {
          pp.right = subroot;
        }

        if (pp != null) {
          pp.h = Math.max(h(pp.left), h(pp.right)) + 1;
        }

        return null;
      } else if (h(p.left) + 2 == h(p.right)) {
        Entry<K, V> pp = p.parent;
        Entry<K, V> subroot =
                (h(p.right.right) >= h(p.right.left) ?
                leftRotate(p) :
                rightLeftRotate(p));

        if (pp == null) {
          root = subroot;
        } else if (pp.left == p) {
          pp.left = subroot;
        } else {
          pp.right = subroot;
        }

        if (pp != null) {
          pp.h = Math.max(h(pp.left), h(pp.right)) + 1;
        }

        return null;
      }

      p.h = Math.max(h(p.left), h(p.right)) + 1;
      p = p.parent;
    }

    return null;
  }

  /**
   * Gets the value associated with <tt>key</tt>. Runs in logarithmic time.
   *
   * @param key the key.
   * @return the value associated with <tt>key</tt>.
   */
  public V get(K key) {
    Entry<K, V> e = root;
    int cmp;

    while (e != null) {
      if ((cmp = key.compareTo(e.key)) < 0) {
        e = e.left;
      } else if (cmp > 0) {
        e = e.right;
      } else {
        return e.value;
      }
    }

    return null;
  }

  /**
   * Gets the entry with ith smallest key. Runs in logarithmic time.
   *
   * @param i the index of the node, by in-order.
   * @return an entry if i is within range, or <tt>null</tt> otherwise.
   */
  public Entry<K, V> entryAt(int i) {
    if (i < 0 || i >= size) {
      return null;
    }

    int save = i;
    Entry<K, V> e = root;

    for (;;) {
      if (i < e.count) {
        e = e.left;
      } else if (i > e.count) {
        i -= e.count + 1;
        e = e.right;
      } else {
        return e;
      }
    }
  }

  /**
   * Removes a mapping with key <tt>key</tt> from this tree. Runs in logarithmic
   * time.
   *
   * @param key the key of a node.
   * @return the value associated with <tt>key</tt> or null, if there was no
   * entries in the tree with key <tt>key</tt>.
   */
  public V remove(K key) {
    Entry<K, V> e = root;
    int cmp;

    while (e != null) {
      if ((cmp = e.key.compareTo(key)) > 0) {
        e = e.left;
      } else if (cmp < 0) {
        e = e.right;
      } else {
        size--;
        V old = e.value;
        e = removeImpl(e);

        Entry<K, V> p = e.parent;

        while (p != null) {

          Entry<K, V> subroot;
          Entry<K, V> pp = p.parent;
          boolean left = (pp == null || pp.left == p);

          if (h(p.left) == h(p.right) + 2) {
            if (h(p.left.left) < h(p.left.right)) {
              subroot = leftRightRotate(p);
            } else {
              subroot = rightRotate(p);
            }
          } else if (h(p.left) + 2 == h(p.right)) {
            if (h(p.right.right) < h(p.right.left)) {
              subroot = rightLeftRotate(p); //?
            } else {
              subroot = leftRotate(p);
            }
          } else {
            p.h = Math.max(h(p.left), h(p.right)) + 1;
            p = p.parent;
            continue;
          }

          if (p == root) {
            root = subroot;
            return old;
          }

          if (left) {
            pp.left = subroot;
          } else {
            pp.right = subroot;
          }

          p = pp;
        }

        return old;
      }
    }

    return null;
  }

  public int size() {
    return size;
  }

  /**
   * Checks all of the AVL-tree invariants.
   *
   * @return <tt>true</tt> if this is a valid AVL-tree,
   * <tt>false</tt> otherwise.
   */
  public boolean isHealthy() {
    return !hasCycles()
            && heightFieldsOK()
            && isBalanced()
            && isWellIndexed();
  }

  public boolean hasCycles() {
    return hasCycles(root, new java.util.HashSet<Entry<K, V>>());
  }

  public boolean heightFieldsOK() {
    return checkHeight(root) == root.h;
  }

  public boolean isBalanced() {
    return isBalanced(root);
  }

  public boolean isWellIndexed() {
    return root.count == countLeft(root.left);
  }

  /**
   * Removes a node from the tree without balancing the tree.
   *
   * @param e the node to remove.
   * @return the actual node removed.
   */
  private Entry<K, V> removeImpl(Entry<K, V> e) {
    if (e.left == null && e.right == null) {
      // The case where the removed node has no children.
      Entry<K, V> p = e.parent;

      if (p == null) {
        root = null;
        return e;
      }

      if (e == p.left) {
        p.left = null;
        p.count = 0;
      } else {
        p.right = null;
      }

      Entry<K, V> pp = p.parent;

      while (pp != null) {
        if (pp.left == p) {
          pp.count--;
        }

        p = pp;
        pp = pp.parent;
      }

      return e;
    }

    if (e.left == null || e.right == null) {
      // Case: only one child.
      Entry<K, V> child = e.left != null ? e.left : e.right;
      Entry<K, V> p = e.parent;
      child.parent = p;

      if (p == null) {
        root = child;
        return e;
      }

      if (e == p.left) {
        p.left = child;
      } else {
        p.right = child;
      }

      while (p != null) {
        if (p.left == child) {
          p.count--;
        }

        child = p;
        p = p.parent;
      }

      return e;
    }

    // Case: two children.
    Entry<K, V> successor = e.right.min();
    e.key = successor.key;
    e.value = successor.value;
    Entry<K, V> child = successor.right;
    Entry<K, V> p = successor.parent;

    if (p.left == successor) {
      p.left = child;
    } else {
      p.right = child;
    }

    if (child != null) {
      child.parent = p;
    }

    Entry<K, V> pLo = child;

    while (p != null) {
      if (p.left == pLo) {
        p.count--;
      }

      pLo = p;
      p = p.parent;
    }

    return successor;
  }

  /**
   * Returns the height of an argument node or -1, if <tt>e</tt> is null.
   *
   * @param e the node to measure.
   * @return the height of <tt>e</tt> or -1, if <tt>e</tt> is null.
   */
  private int h(Entry<K, V> e) {
    return e != null ? e.h : -1;
  }

  /**
   * The left rotation of a tree node.
   *
   * @param e the disbalanced node.
   * @return the new root of a balanced subtree.
   */
  private Entry<K, V> leftRotate(Entry<K, V> e) {
    Entry<K, V> ee = e.right;
    ee.parent = e.parent;
    e.parent = ee;
    e.right = ee.left;
    ee.left = e;

    if (e.right != null) {
      e.right.parent = e;
    }

    e.h =  Math.max(h(e.left), h(e.right)) + 1;
    ee.h = Math.max(h(ee.left), h(ee.right)) + 1;

    ee.count += e.count + 1;
    return ee;
  }

  /**
   * The right rotation of a tree node.
   *
   * @param e the disbalanced node.
   * @return the new root of a balanced subtree.
   */
  private Entry<K, V> rightRotate(Entry<K, V> e) {
    Entry<K, V> ee = e.left;
    ee.parent = e.parent;
    e.parent = ee;
    e.left = ee.right;
    ee.right = e;

    if (e.left != null) {
      e.left.parent = e;
    }

    e.h =  Math.max(h(e.left), h(e.right)) + 1;
    ee.h = Math.max(h(ee.left), h(ee.right)) + 1;

    e.count -= ee.count + 1;
    return ee;
  }

  /**
   * The left/right rotation of a tree node.
   *
   * @param e the disbalanced node.
   * @return the new root of a balanced subtree.
   */
  private Entry<K, V> leftRightRotate(Entry<K, V> e) {
    Entry<K, V> ee = e.left;
    e.left = leftRotate(ee);
    return rightRotate(e);
  }

  /**
   * The right/left rotation of a tree node.
   *
   * @param e the disbalanced node.
   * @return the new root of a balanced subtree.
   */
  private Entry<K, V> rightLeftRotate(Entry<K, V> e) {
    Entry<K, V> ee = e.right;
    e.right = rightRotate(ee);
    return leftRotate(e);
  }

  private boolean hasCycles(Entry<K, V> e, java.util.HashSet<Entry<K, V>> set) {
    if (e == null) {
      return false;
    }

    if (set.contains(e)) {
      return true;
    }

    set.add(e);

    if (hasCycles(e.left, set)) {
      return true;
    }

    if (hasCycles(e.right, set)) {
      return true;
    }

    set.remove(e);
    return false;
  }

  private int checkHeight(Entry<K, V> e) {
    if (e == null) {
      return -1;
    }

    int l = checkHeight(e.left);

    if (l == Integer.MIN_VALUE) {
      return l;
    }

    int r = checkHeight(e.right);

    if (r == Integer.MIN_VALUE) {
      return r;
    }

    int h = Math.max(l, r) + 1;

    if (h != e.h) {
      return Integer.MIN_VALUE;
    } else {
      return h;
    }
  }

  private boolean isBalanced(Entry<K, V> e) {
    if (e == null) {
      return true;
    }

    if (Math.abs(h(e.left) - h(e.right)) > 1) {
      return false;
    }

    if (isBalanced(e.left) == false) {
      return false;
    }

    if (isBalanced(e.right) == false) {
      return false;
    }

    return true;
  }

  private int countLeft(Entry<K, V> e) {
    if (e == null) {
      return 0;
    }

    int l;
    int r;

    if ((l = countLeft(e.left)) != e.count) {
      return Integer.MIN_VALUE;
    }

    if ((r = countLeft(e.right)) == Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }

    return l + r + 1;
  }

  /**
   * @param args the command line arguments.
   */
  public static void main(String[] args) {
    int N = 600000;
    long seed = 323L;

    if (args.length > 0) {
      N = Integer.parseInt(args[0]);
    }

    if (args.length > 1) {
      seed = Long.parseLong(args[1]);
    }

    profile1(N, seed);
//    profile2();
  }

  /**
   * The benchmark.
   *
   * @param N the maximum amount of mappings in AVLTree and TreeMap.
   * @param seed the seed for the PRNG.
   */
  static void profile1(int N, long seed) {
    System.out.println("--- profile1(): performance measurement against "
                       + "java.util.TreeMap");

    if (N < 1000) {
      N = 1000;
    }

    java.util.Random r = new java.util.Random(seed);
    java.util.TreeMap<Integer, Integer> tm =
            new java.util.TreeMap<Integer, Integer>();
    AVLTree<Integer, Integer> m = new AVLTree<Integer, Integer>();
    int[] arr = new int[N];

    for (int i = 0; i < N; i++) {
      arr[i] = r.nextInt();
    }

    long ta = System.currentTimeMillis();

    for (int i : arr) {
      tm.put(i, i);
    }

    long tb = System.currentTimeMillis();

    System.out.println("TreeMap.put() in " + (tb - ta) + " ms.");

    ta = System.currentTimeMillis();

    for (int i : arr) {
      m.put(i, i);
    }

    tb = System.currentTimeMillis();

    System.out.println("AVLTree.put() in " + (tb - ta) + " ms.");
    System.out.println("-");
    System.out.println("AVLTree.isHealthy(): " + m.isHealthy());
    System.out.println("-");

    ta = System.currentTimeMillis();

    for (int j = 0; j < 3; j++) {
      for (int i : arr) {
        if (tm.get(i) != i) {
          throw new IllegalStateException("TreeMap.get() failed.");
        }
      }
    }

    tb = System.currentTimeMillis();

    System.out.println("TreeMap.get() in " + (tb - ta) + " ms.");

    ta = System.currentTimeMillis();

    for (int j = 0; j < 3; ++j) {
      for (int i : arr) {
        if (m.get(i) != i) {
          throw new IllegalStateException("AVLTree.get() failed.");
        }
      }
    }

    tb = System.currentTimeMillis();

    System.out.println("AVLTree.get() in " + (tb - ta) + " ms.");
    System.out.println();

    System.out.println("AVLTree.size(): " + m.size());
    System.out.println("TreeMap.size(): " + tm.size());
    System.out.println();

    System.out.println("Delete 100 keys...");

    for (int i = 0; i < 100; i++) {
      tm.remove(arr[i]);
      m.remove(arr[i]);
    }

    System.out.println("Delete 100 keys done.");
    System.out.println();

    System.out.println("AVLTree.size(): " + m.size());
    System.out.println("TreeMap.size(): " + tm.size());
    System.out.println();


    ta = System.currentTimeMillis();

    for (int j = 0; j < 3; ++j) {
      AVLTree.Entry<Integer, Integer> prev = null;

      for (int i = -2; i < arr.length; i++) {
        AVLTree.Entry<Integer, Integer> e = m.entryAt(i);

        if (i < 0 || i >= m.size()) {
          if (e != null) {
            throw new IllegalStateException("'e' should be null.");
          }
        } else {
          if (prev != null && prev.getKey() > e.getKey()) {
            throw new IllegalStateException("Unsorted tree.");
          }

          prev = e;
        }
      }
    }

    tb = System.currentTimeMillis();

    System.out.println("AVLTree.entryAt() in " + (tb - ta) + " ms.");

    System.out.println("-");
    System.out.println("AVLTree.isHealthy(): " + m.isHealthy());
    System.out.println("-");

    ta = System.currentTimeMillis();

    for (int i : arr) {
      tm.remove(i);
    }

    tb = System.currentTimeMillis();

    System.out.println("TreeMap.remove() in " + (tb - ta) + " ms.");

    ta = System.currentTimeMillis();

    for (int i : arr) {
      m.remove(i);
    }

    tb = System.currentTimeMillis();

    System.out.println("AVLTree.remove() in " + (tb - ta) + " ms.");
    System.out.println("Both empty now: " + (tm.isEmpty() && m.size() == 0));
    System.out.println();
  }

  /**
   * Small demo.
   */
  static void profile2() {
    System.out.println("--- profile2(): demo of AVLTree");

    AVLTree<Integer, Integer> m = new AVLTree<Integer, Integer>();

    for (int i = 0; i < 10; i++) {
      m.put(i, i);
    }

    assert m.size() == 10;
    assert m.isHealthy();

    for (int i = -5; i < 15; i++) {
      AVLTree.Entry<Integer, Integer> e = m.entryAt(i);
      System.out.printf("i = %2d: m.entryAt(i): %s\n", i, e);

      if (e != null) {
        assert i == e.getKey() && i == e.getValue();
      }
    }

    System.out.println();

    m.put(5, 55);

    assert m.size() == 10;

    m.remove(5);

    assert m.size() == 9;

    for (int i = -5; i < 15; i++) {
      AVLTree.Entry<Integer, Integer> e = m.entryAt(i);
      System.out.printf("i = %2d: m.entryAt(i): %s\n", i, e);
    }

    assert m.isHealthy();
  }
}
