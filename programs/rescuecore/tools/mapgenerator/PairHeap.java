/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.3 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore.tools.mapgenerator;

public class PairHeap{

	private int size;
    private PairNode root;

    /**
     * Construct the pairing heap.
     */
    public PairHeap(){
        size = 0;
        root = null;
    }

    /**
     * Insert into the priority queue.
     * @param val The value to insert
     * @param priority The priority to give the value
     */
    public PairNode insert(int val, int priority){
        PairNode newNode = new PairNode(val,priority);
        size++;
        if(root == null)
            root = newNode;
        else
            root = compareAndLink(root, newNode);
        return newNode;
    }


    /**
     * Insert into the priority queue, and return a PairNode
     * that can be used by decreaseKey.
     * Duplicates are allowed.
     * @param val the value to insert.
     * @param priority The priority to give the value
     * @return the node containing the newly inserted item.
     */
    public PairNode addItem(int val, int priority){
        PairNode newNode = insert(val,priority);
        return newNode;
    }

    /**
     * Find the highest priority value in the priority queue.
     * @return the highest priority value.
     */
    public int findMin(){
        if(isEmpty())
            return -1;
        return root.getValue();
    }

    /**
     * Remove the smallest item from the priority queue.
     * @exception Underflow if the priority queue is empty.
     */
    public int deleteMin(){
        int val = findMin( );
        if(root.getLeftChild() == null)
            root = null;
        else
            root = combineSiblings(root.getLeftChild());

        size--;
        return val;
    }

    /**
     * Change the value of the item stored in the pairing heap.
     * @param p any node returned by addItem.
     * @param newPriority - must decrease
     */
    public void decreaseKey(PairNode p, int newPriority ){
        if(p.getPriority() > newPriority)
            return;
        p.setPriority(newPriority);
        if( p != root ){
            if(p.getNextSibling() != null )
                p.getNextSibling().setPrev(p.getPrev());
            if(p.getPrev().getLeftChild() == p )
                p.getPrev().setLeftChild(p.getNextSibling());
            else
                p.getPrev().setNextSibling(p.getNextSibling());

            p.setNextSibling(null);
            root = compareAndLink(root, p);
        }
    }

    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty( ){
        return size == 0;
    }

    /**
     * Make the priority queue logically empty.
     */
    public void makeEmpty( ){
        size = 0;
        root = null;
    }

    /**
     * Internal method that is the basic operation to maintain order.
     * Links first and second together to satisfy heap order.
     * @param first root of tree 1, which may not be null.
     *    first.nextSibling MUST be null on entry.
     * @param second root of tree 2, which may be null.
     * @return result of the tree merge.
     */
    private PairNode compareAndLink(PairNode first, PairNode second){
        if(second == null)
            return first;

        if(second.getPriority() < first.getPriority()){
            // Attach first as leftmost child of second
            second.setPrev(first.getPrev());
            first.setPrev(second);
            first.setNextSibling(second.getLeftChild());
            if(first.getNextSibling() != null)
                first.getNextSibling().setPrev(first);
            second.setLeftChild(first);
            return second;
        }
        else{
            // Attach second as leftmost child of first
            second.setPrev(first);
            first.setNextSibling(second.getNextSibling());
            if(first.getNextSibling() != null)
                first.getNextSibling().setPrev(first);
            second.setNextSibling(first.getLeftChild());
            if(second.getNextSibling() != null)
                second.getNextSibling().setPrev(second);
            first.setLeftChild(second);
            return first;
        }
    }


    private PairNode[] doubleIfFull( PairNode[] array, int index ){
        if( index == array.length ){
            PairNode[] oldArray = array;

            array = new PairNode[index * 2];
            for( int i = 0; i < index; i++ )
                array[i] = oldArray[i];
        }
        return array;
    }

        // The tree array for combineSiblings
    private PairNode[] treeArray = new PairNode[5];

    /**
     * Internal method that implements two-pass merging.
     * @param firstSibling the root of the conglomerate;
     *     assumed not null.
     */
    private PairNode combineSiblings(PairNode firstSibling){
        if(firstSibling.getNextSibling() == null)
            return firstSibling;

        int numSiblings = 0;
        for(;firstSibling != null; numSiblings++){
            treeArray = doubleIfFull(treeArray, numSiblings);
            treeArray[numSiblings] = firstSibling;
            firstSibling.getPrev().setNextSibling(null);  // break links
            firstSibling = firstSibling.getNextSibling();
        }
        treeArray = doubleIfFull(treeArray, numSiblings);
        treeArray[numSiblings] = null;
        int i = 0;
        for(;i + 1 < numSiblings; i += 2)
            treeArray[i] = compareAndLink(treeArray[i], treeArray[i+1]);

        int j = i-2;
        if(j == numSiblings-3)
            treeArray[j] = compareAndLink(treeArray[j], treeArray[j+2]);
        for(;j >= 2; j-=2)
             treeArray[j-2] = compareAndLink(treeArray[j-2], treeArray[j]);

        return treeArray[0];
    }

        // Test program
    public static void main(String [ ] args){
        PairHeap h = new PairHeap( );
        int numItems = 40000;
        int i = 37;
            for( i = 37; i != 0; i = ( i + 37 ) % numItems )
               h.insert(i,i);
            for( i = numItems-1; i >0; i--)
                if(h.deleteMin() != i )
                    System.out.println( "Oops! " + i );
/*
            for( i = 37; i != 0; i = ( i + 37 ) % numItems )
                h.insert(i,i);
            for( i = 1; i <= numItems; i++ )
                if(h.deleteMin() != i )
                    System.out.println( "Oops! " + i + " " );
*/
    }
}
