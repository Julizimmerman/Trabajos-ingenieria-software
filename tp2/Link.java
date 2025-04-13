package anillo;

import java.util.Stack;

public abstract class Link {
    // atributos:
    public Object value ;
    public Link next ;
    public Link previous;

    // constructor:
    public Link(Object value) {
        this.value = value;
    }

    // metodos:
    public abstract Object current() ;
    public abstract Link next();
    public abstract Link add(Object cargo) ;
    public abstract Link remove(Stack<Link> counter) ;
    public abstract Link nextDecision(Link fallback);
}


class NeutralLink extends Link {
    // constructor:
    public NeutralLink() {
        super(null);
    }

    // metodos:
    public Object current(){
        throw new RuntimeException("This ring is empty, so there is no current node");
    }
    public Link next(){
        throw new RuntimeException("This ring is empty, so it has no next node");
    }
    public Link add(Object cargo) {
        NormalLink NewLink = new NormalLink(cargo) ;
        NewLink.next = NewLink ;
        NewLink.previous = NewLink ;
        return NewLink ;
    }
    public Link remove(Stack<Link> counter){
        return this ;
    }
    public Link nextDecision(Link fallback) {
        return this; // si quedó sólo el neutral, se retorna a sí mismo
    }


}


class NormalLink extends Link {
    // constructor:
    public NormalLink(Object value) {
        super(value);
    }

    // metodos:
    public Object current(){
        return this.value ;
    }
    public Link next() {
        return this.next;
    }
    public Link add(Object cargo) {
        NormalLink NewLink = new NormalLink(cargo) ;
        NewLink.previous = this.previous ;
        NewLink.next = this ;
        this.previous.next = NewLink ;
        this.previous = NewLink ;
        return NewLink ;
    }
    public Link remove2(Stack<Link> counter) {
        this.next.previous = this.previous ;
        this.previous.next = this.next ;
        return this.next ;
    }

    public Link remove(Stack<Link> counter) {
        this.next.previous = this.previous;
        this.previous.next = this.next;

        return counter.peek().nextDecision(this.next);
    }

    public Link nextDecision(Link fallback) {
        return fallback; // si aún hay nodos, se retorna el siguiente
    }

}
