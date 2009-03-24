/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.2 $
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

public class PairNode{

    private int value;
    private int priority;
    private PairNode leftChild;
    private PairNode nextSibling;
    private PairNode prev;

    /**
     * Construct the PairNode.
     * @param value the value stored in the node.
     */
    PairNode(int value, int priority){
        this.value = value;
        this.priority = priority;
    }
    public int getValue(){
		return value;
	}
	public int getPriority(){
		return priority;
	}
	public void setPriority(int priority){
		this.priority = priority;
	}
	public PairNode getPrev(){
		return prev;
	}
	public PairNode getNextSibling(){
		return nextSibling;
	}
	public PairNode getLeftChild(){
		return leftChild;
	}
	public void setPrev(PairNode prev){
		this.prev = prev;
	}
	public void setNextSibling(PairNode nextSibling){
		this.nextSibling = nextSibling;
	}
	public void setLeftChild(PairNode leftChild){
		this.leftChild = leftChild;
	}
}
