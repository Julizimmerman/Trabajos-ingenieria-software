package anillo;
import java.util.Stack;

public class Ring {
    public Link curr ;
    public Stack<Link> counter = new Stack<>();

    // constructor:
    public Ring() {
        this.curr = new NeutralLink() ;
        counter.push(curr) ;
    }

    public Ring next() {
        curr = curr.next();
        return this;
    }

    public Object current() {
        return curr.current() ;
    }

    public Ring add(Object cargo) {
        curr = curr.add(cargo) ;
        counter.push(curr) ;
        return this ;
    }

    public Ring remove() {
        counter.pop() ;
        curr = curr.remove(counter);
        return this;
    }
}
