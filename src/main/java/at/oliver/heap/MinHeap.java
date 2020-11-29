package at.oliver.heap;

import java.util.Arrays;

/**
 * MinHeap
 *
 * @param <E>
 */
@SuppressWarnings("unchecked")
public class MinHeap<E extends Comparable<E> & IndexInHeap> {
    private final Object[] items;
    private int size;

    public MinHeap(int capacity) {
        this.items = new Object[capacity];
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    public void add(E item) {
        this.items[this.size] = item;
        item.setHeapIndex(this.size);

        this.sortUpItem(item);
        this.size++;
    }

    /**
     * Removes the smallest item in the {@code MinHeap} and returns it.
     *
     * @return smallest item of the Heap.
     */
    public E removeFirst() {
        E firstItem = (E) this.items[0];

        // put last item on the first position and sort down
        this.items[0] = this.items[--this.size];
        ((E) this.items[0]).setHeapIndex(0);
        this.sortDownItem((E) this.items[0]);

        return firstItem;
    }

    @SuppressWarnings("unused")  // for testing
    public boolean contains(E item) {
        return item == items[item.getHeapIndex()];
    }

    public void updateItem(E item) {  // if compareTo value of an item has changed
        this.sortUpItem(item);  // new value will always be less than previous -> sort up
    }

    private void sortUpItem(E childItem) {
        while(true) {
            E parentItem = (E) this.items[(childItem.getHeapIndex() - 1) / 2];  // index of the parent
            if(childItem.compareTo(parentItem) < 0)  // value of child is smaller than value of parent
            {
                this.swap(childItem, parentItem);  //swapping child and parent
            }
            else {
                return;
            }
        }
    }

    private void sortDownItem(E parentItem) {
        while(true) {
            int indexLeftChild = parentItem.getHeapIndex() * 2 + 1;
            int indexRightChild = parentItem.getHeapIndex() * 2 + 2;
            int swapIndex;

            if(indexLeftChild < this.size) {  // left child exists
                swapIndex = indexLeftChild;

                if(indexRightChild < this.size) {  // right child exists
                    // testing if value of the right child is smaller than the value of the left child
                    if(((E) this.items[indexLeftChild]).compareTo(((E) this.items[indexRightChild])) > 0) {
                        swapIndex = indexRightChild;
                    }
                }
                if(parentItem.compareTo(((E) this.items[swapIndex])) > 0)  // value of parent is higher than value of child
                {
                    this.swap(parentItem, ((E) this.items[swapIndex]));
                }
                else {
                    return;
                }
            }
            else  // doesn't have children
            {
                return;
            }
        }
    }

    private void swap(E itemA, E itemB) {
        this.items[itemA.getHeapIndex()] = itemB;
        this.items[itemB.getHeapIndex()] = itemA;

        int indexA = itemA.getHeapIndex();
        itemA.setHeapIndex(itemB.getHeapIndex());
        itemB.setHeapIndex(indexA);
    }

    @Override
    public String toString() {
        return "MinHeap{" + "size=" + this.size + ", " + "this.items=" + Arrays.toString(items) + '}';
    }
}
