package util.dstruct;

public class QueueTest1
{
	public static void main(String...args)
	{
		Queue<Character> q = new Queue<Character>();

		for(int i = 0 ; i < 26*2 ; i+=2)
		{
			q.addData((char) ('A' + i%26 + (i>25? 1:0)));
			System.out.println(q);
		}
		
		
		System.out.println();
		System.out.print(q.getSize() + ": ");
		System.out.println(q);
		
		q.removeByIndex(3);
		
		System.out.print(q.getSize() + ": ");
		System.out.println(q);
		
		
		q.removeData();
		
		System.out.print(q.getSize() + ": ");
		System.out.println(q);
		
		q.removeData('H');
		
		System.out.print(q.getSize() + ": ");
		System.out.println(q);
		
		q.addData('X');
		
		System.out.print(q.getSize() + ": ");
		System.out.println(q);
	}
}
