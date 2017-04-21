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
	
	public synchronized void  addData(T data)
	{
		Node<T> n = new Node<T>();
		n.data = data;
		
		head = addNode(head, n);
		size++;
	}
	
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
	
	public synchronized void removeData(T data)
	{
		head = removeNode(head, data);
		size--;
	}
	
	public synchronized T removeByIndex(int idx)
	{
		Node<T> get = new Node<T>();
		head = removeByIndex(head, get, idx);
		
		if(get.data == null)
			return null;
		size--;
		return get.data;
	}
	
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
		String s = "{";
		Node<T> head = this.head;
		while(head != null)
		{
			s += head.data + " , ";
			head = head.next;
		}
		
		return s + "}";
	}
	
}
