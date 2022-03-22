import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class StackImpl {

  public static void main(String[] args){
    //StandardStack<Integer> stack = new StandardStack<>();

    LockFreeStack<Integer> stack = new LockFreeStack<>();
    Random r = new Random();

    for(int i = 0; i < 10000; i++){
      stack.push(r.nextInt());
    }

    List<Thread> threads = new ArrayList<>();

    int push = 2;
    int pop = 2;

    for(int i = 0; i < push; i++){
      Thread t = new Thread(()->{
        while(true){
          stack.push(r.nextInt());
        }
      });
      t.setDaemon(true);
      threads.add(t);
    }

    for(int i = 0; i < pop; i++){
      Thread t = new Thread(()->{
        while(true){
          stack.pop();
        }
      });
      t.setDaemon(true);
      threads.add(t);
    }

    for(Thread t : threads){
      t.start();
    }

    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println(String.format("%,d operations were performed in 10 seconds", stack.getCounter()));
  }

  public static class LockFreeStack<T>{
    private AtomicReference<StackNode<T>> head = new AtomicReference<>();
    private AtomicInteger counter = new AtomicInteger(0);

    public void push(T value){
      StackNode<T> newHead = new StackNode<>(value);
      while(true){
        StackNode<T> currentHead = head.get();
        newHead.next = currentHead;
        if(head.compareAndSet(currentHead, newHead)){
            break;
        }else{
          LockSupport.parkNanos(1);
        }
      }
      counter.incrementAndGet();
    }
    public T pop(){
      StackNode<T> currentHead = head.get();
      while(currentHead != null) {
        StackNode<T> newHead = currentHead.next;
        if (head.compareAndSet(currentHead, newHead)) {
          break;
        } else {
          LockSupport.parkNanos(1);
          currentHead = head.get();
        }
      }
      counter.incrementAndGet();
      return currentHead !=null ? currentHead.value : null;
    }

    public int getCounter(){
      return  counter.get();
    }
  }

  public static class StandardStack<T> {
    private StackNode<T> head;
    private int counter = 0;

    public synchronized void push(T value) {
      StackNode<T> newHead = new StackNode<T>(value);
      newHead.next = head;
      this.head = newHead;
      counter++;
    }

    public synchronized T pop(){
      if(head == null){
        counter++;
        return null;
      }
      T value = head.value;
      head = head.next;
      counter++;
      return value;
    }

    public int getCounter(){
      return counter;
    }
  }
  private static class StackNode<T> {
    public T value;
    public StackNode<T> next;

    public StackNode(T value){
      this.value = value;
      this.next = next;
    }
  }
}
