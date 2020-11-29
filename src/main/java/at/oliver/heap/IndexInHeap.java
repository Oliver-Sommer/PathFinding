package at.oliver.heap;

/**
 * Position, which the implementing object has in a {@code MinHeap}.
 */
public interface IndexInHeap {
    int getHeapIndex();

    void setHeapIndex(int heapIndex);
}
