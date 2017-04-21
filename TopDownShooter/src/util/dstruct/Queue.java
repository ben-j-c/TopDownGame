package util.dstruct;


/**
 * 
 * @author Ben
 *
 * @param <T> The type of the data being stored in the queue
 */
public class Queue <T extends Comparable<? super T>>
{
	private volatile int size;
	private Node<T> head = null;
	
	private static class Node <T extends Comparable<? super T>> implements Comparable<Node<T>>
	{
		Node<T> next = null;
		T data = null;
		
		@Override
		public int compareTo(Node<T> n)
		{
			return this.data.compareTo(n.data);
		}
	}
	
	/**
	 * Add the data into the queue in a natural order.
	 * Uses .compareTo() to check order.
	 * @param data the data you want to add to the queue
	 */
	public synchronized void  addData(T data)
	{
		Node<T> n = new Node<T>();
		n.data = data;
		
		head = addNode(head, n);
		size++;
	}
	/**
	 * Get and remove the data at the front of the queue.
	 * @return the data that was at the head of the queue
	 */
	public synchronized T removeData()
	{
		if(head == null)
		{
			return null;
		}
		T returner = head.data;
		head = head.next;
		size--;
		return returner;
	}
	/**
	 * Remove the first occurrence of data in the list.
	 * Checks data equivalence with .equals()
	 * @param data
	 */
	public synchronized void removeData(T data)
	{
		head = removeNode(head, data);
		size--;
	}
	
	/**
	 * Remove a specific index from the queue.
	 * @param idx the index of the element to remove
	 * @return
	 */
	public synchronized T removeByIndex(int idx)
	{
		Node<T> get = new Node<T>();
		head = removeByIndex(head, get, idx);
		
		if(get.data == null)
			return null;
		size--;
		return get.data;
	}
	/**
	 * Gets the first occurrence of data in the queue.
	 * Checks data equivalence with .equals()
	 * @param data
	 * @return
	 */
	public synchronized T getData(T data)
	{
		Node<T> n = getNodeWData(head, data);
		if(n == null)
			return null;
		return n.data;
	}
	
	public int getSize()
	{
		return size;
	}
	
	/**
	 * Recursively go down the list and find where this node fits in.
	 * @param head recursive head
	 * @param n node to add
	 * @return recursive new head
	 */
	private Node<T> addNode(Node<T> head, Node<T> n)
	{
		if(head == null)
		{
			return n;
		}
		else if(head.compareTo(n) <= 0)
		{
			head.next = addNode(head.next, n);
			return head;
		}
		else
		{
			n.next = head;
			return n;
		}
	}
	/**
	 * Recursively go down the list decrementing idx, until you reach zero, or the end of the list.  Then return the appropriate data.
	 * @param head
	 * @param get
	 * @param idx
	 * @return
	 */
	private Node<T> removeByIndex(Node<T> head, Node<T> get, int idx)
	{
		if(head == null)
		{
			return null;
		}
		else if(idx == 0)
		{
			get.data = head.data;
			return head.next;
		}
		else
		{
			head.next = removeByIndex(head.next, get, idx - 1);
			return head;
		}
	}
	
	/**
	 * recursively go down the list looking for data and remove it by returning the next node.
	 * @param head the recursive head
	 * @param data the data to remove
	 * @return recursive next node
	 */
	private Node<T> removeNode(Node<T> head, T data)
	{
		if(head == null)
			return null;
		
		if(head.data.equals(data))
		{
			return head.next;
		}
		head.next = removeNode(head.next, data);
		return head;
	}
	/**
	 * Get the first node that when you compare the data, they are .equals() to each other.
	 * @param head
	 * @param data
	 * @return
	 */
	private Node<T> getNodeWData(Node<T> head, T data)
	{
		if(head == null)
			return null;
		
		if(head.data.equals(data))
		{
			return head;
		}
		
		return getNodeWData(head.next, data);
	}
	
	public String toString()
	{
		StringBuilder br = new StringBuilder(50);
		br.append("{ ");
		Node<T> head = this.head;
		while(head != null)
		{
			br.append(head.data);
			br.append(", ");
			head = head.next;
		}
		br.deleteCharAt(br.length() - 2);
		br.append('}');
		return br.toString();
	}
	
}
