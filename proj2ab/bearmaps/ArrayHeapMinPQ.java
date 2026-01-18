package bearmaps;

import java.util.*;

public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T> {

    private class PriorityNode<T> implements Comparable<PriorityNode> {
        T item;
        double priority;

        PriorityNode(T e, double p) {
            this.item = e;
            this.priority = p;
        }

        T getItem() {
            return item;
        }

        double getPriority() {
            return priority;
        }

        void setPriority(double priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(PriorityNode other) {
            if (other == null) {
                return -1;
            }
            return Double.compare(this.getPriority(), other.getPriority());
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (o == null || o.getClass() != this.getClass()) {
                return false;
            } else {
                return ((PriorityNode) o).getItem().equals(getItem());
            }
        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }

    }

    private List<PriorityNode> minHeap;
    private int size;
    private Map<PriorityNode, Integer> itemMap;

    public ArrayHeapMinPQ() {
        this.minHeap = new ArrayList<>();
        this.itemMap = new HashMap<>();
        PriorityNode sentinel = new PriorityNode(null, -1);
        this.minHeap.add(sentinel);
        this. size = 0;
    }

    /**
     * Adds item to last position then promote as highly as possible
     * @param item
     * @param priority
     */
    public void add(T item, double priority){
        if (this.contains(item)) {
            throw new IllegalArgumentException("This item is already in the queue!");
        }
        PriorityNode newNode = new PriorityNode(item,priority);
        size++;
        minHeap.add(newNode);
        itemMap.put(newNode, size);
        swim(size);

    }


    /** Returns true if the PQ contains the given item. */
    public boolean contains(T item) {
        PriorityNode search = new PriorityNode(item, 1);
        return itemMap.containsKey(search);
    }

    /** Returns item in the root node */
    public T getSmallest() {
        if (size == 0) {
            throw new NoSuchElementException("The heap is empty!");
        }
        return (T) minHeap.get(1).getItem();
    }

    /** Removes and returns the minimum item. Throws NoSuchElementException if the PQ is empty. */
    public T removeSmallest() {
        if (size == 0) {
            throw new NoSuchElementException("The heap is empty!");
        }
        T smallest = (T) minHeap.get(1).getItem();
        PriorityNode smallestNode = new PriorityNode(smallest, 0);

        if (size == 1) {
            minHeap.remove(1);
            size = 0;
            return smallest;
        }

        swap(1, size);
        minHeap.remove(size);
        sink(1);
        size--;
        itemMap.remove(smallestNode);
        return smallest;
    }

    /** Returns the number of items in the PQ. */
    public int size() {
        return size;
    }

    /** Changes the priority of the given item. Throws NoSuchElementException if the item
     * doesn't exist. */
    public void changePriority(T item, double priority) {
        if (!contains(item)) {
            throw new NoSuchElementException("This item is not in the heap!");
        }

        PriorityNode changedNode = new PriorityNode(item,priority);
        int currIndex = itemMap.get(changedNode);
        minHeap.get(currIndex).setPriority(priority);

        // Swim up if parent is bigger than current, otherwise sink
        if (changedNode.compareTo(parent(currIndex)) < 0) {
            swim(currIndex);
        } else {
            sink(currIndex);
        }
    }

    /** Helper method that swaps two elements in the heap
     */
    void swap(int from, int to) {
        PriorityNode fromNode = minHeap.get(from);
        PriorityNode toNode = minHeap.get(to);
        minHeap.set(to, fromNode);
        minHeap.set(from, toNode);

        // Changing indexes in the mapping hashmap
        itemMap.put(fromNode, to);
        itemMap.put(toNode, from);
    }

    /** Helper method that sinks an element until heap order had been restored
     */
    void sink(int currIndex) {
        PriorityNode curr = minHeap.get(currIndex);
        // ADD CONDITION FOR NULL CHILDREN

        int cmpL; int cmpR; int cmpChildren;
        boolean nullL = minHeap.size() <= currIndex*2;
        boolean nullR = minHeap.size() <= currIndex*2 +1;

        if (nullL) {
            cmpL = -1;
        } else {
            cmpL = curr.compareTo(left(currIndex));
        }

        if (nullR) {
            cmpR = -1;
        } else {
            cmpR = curr.compareTo(right(currIndex));
        }

        // Return if both children are equal or greater (or null)
        if (cmpL != 1 && cmpR != 1) {
            return;
        }

        if (nullL && cmpR == 1) {
            swap(currIndex, currIndex*2+1);
            return;
        } else if (nullR && cmpL == 1) {
            swap(currIndex, currIndex*2);
            return;
        }

        cmpChildren = left(currIndex).compareTo(right(currIndex));

        if (cmpChildren >= 0) {
            swap(currIndex, currIndex*2+1); // Swap with the right if the two children are equal or right is smallest
            sink(currIndex*2+1);
        } else {
            swap(currIndex, currIndex*2); // Swap with the left if left is smallest
            sink(currIndex*2);
        }

    }

    /** Helper method that swims an element up until heap order had been restored
     */
    void swim (int currIndex) {
        PriorityNode curr = minHeap.get(currIndex);
        int cmpParent = curr.compareTo(parent(currIndex));
        if (cmpParent >= 0) {
            return;
        }

        swap(currIndex, currIndex/2); // Swap curr and its parent
        swim(currIndex/2);
    }

    PriorityNode parent (int k) {
        return minHeap.get(k/2);
    }

    PriorityNode left(int k) {
        return minHeap.get(k*2);
    }

    PriorityNode right (int k) {
        return minHeap.get(k*2+1);
    }

}
