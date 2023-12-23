package core;

/**
 * A simple pair class
 * @param <A> First type
 * @param <B> Second type
 * @author LLT
 */
public class Pair<A, B> {

  protected final A a;
  protected final B b;

  private Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<A, B>(a, b);
  }

  public A first() {
    return a;
  }

  public B second() {
    return b;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Pair other = (Pair) obj;
    if (a == null) {
      if (other.a != null) return false;
    } else if (!a.equals(other.a)) return false;
    if (b == null) {
        return other.b == null;
    } else
        return b.equals(other.b);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((a == null) ? 0 : a.hashCode());
    result = prime * result + ((b == null) ? 0 : b.hashCode());
    return result;
  }

  public String toString() {
    return "(" + a + ", " + b + ")";
  }
}
